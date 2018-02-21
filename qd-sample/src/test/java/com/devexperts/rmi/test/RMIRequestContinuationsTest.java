/*
 * !++
 * QDS - Quick Data Signalling Library
 * !-
 * Copyright (C) 2002 - 2018 Devexperts LLC
 * !-
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * !__
 */
package com.devexperts.rmi.test;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

import com.devexperts.logging.Logging;
import com.devexperts.rmi.*;
import com.devexperts.rmi.message.RMIRequestMessage;
import com.devexperts.rmi.message.RMIRequestType;
import com.devexperts.rmi.task.*;
import com.devexperts.test.ThreadCleanCheck;
import com.devexperts.test.TraceRunnerWithParametersFactory;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(TraceRunnerWithParametersFactory.class)
public class RMIRequestContinuationsTest {
	private static final Logging log = Logging.getLogging(RMIRequestContinuationsTest.class);
	
	private RMIEndpoint server;
	private RMIEndpoint client;

	private final List<Throwable> exceptions = new Vector<>();
	private TestThreadPool executorService;

	private static final int TIMEOUT = 10000;
	private RMIEndpoint trueClient;

	private final Function<Object, RMIClientPort> getTrueClientPort;
	private final ChannelLogic channelLogic;

	@Parameterized.Parameters(name="type={0}")
	public static Iterable<Object[]> parameters() {
		return Arrays.asList(new Object[][]{
			{TestType.REGULAR},
			{TestType.CLIENT_CHANNEL},
			{TestType.SERVER_CHANNEL}
		});
	}


	public RMIRequestContinuationsTest(TestType type) {
		server = RMIEndpoint.newBuilder()
			.withName("Server")
			.withSide(RMIEndpoint.Side.SERVER)
			.build();
		client = RMIEndpoint.newBuilder()
			.withName("Client")
			.withSide(RMIEndpoint.Side.CLIENT)
			.build();
		client.setRequestRunningTimeout(20000); // to make sure tests don't run forever
		this.channelLogic = new ChannelLogic(type, client, server, null);
		switch (type) {
		case REGULAR:
			getTrueClientPort = (subject) -> {
				try {
					channelLogic.initServerPort();
				} catch (InterruptedException e) {
					fail(e.getMessage());
				}
				return trueClient.getClient().getPort(subject);
			};
			break;
		case CLIENT_CHANNEL:
			getTrueClientPort = (subject) -> {
				channelLogic.request = trueClient.getClient().getPort(subject)
					.createRequest(new RMIRequestMessage<>(RMIRequestType.DEFAULT, TestService.OPERATION));
				channelLogic.clientPort = channelLogic.request.getChannel();
				channelLogic.request.send();
				try {
					channelLogic.initServerPort();
				} catch (InterruptedException e) {
					fail(e.getMessage());
				}
				return channelLogic.clientPort;
			};
			break;
		case SERVER_CHANNEL:
		default:
			getTrueClientPort = (subject) -> {
				channelLogic.request = trueClient.getClient().getPort(subject)
					.createRequest(new RMIRequestMessage<>(RMIRequestType.DEFAULT, TestService.OPERATION));
				for (RMIService<?> handler : channelLogic.testService.handlers)
					channelLogic.request.getChannel().addChannelHandler(handler);
				server.getServer().export(channelLogic.testService);
				channelLogic.request.send();
				try {
					channelLogic.initClientPort();
				} catch (InterruptedException e) {
					fail(e.getMessage());
				}
				return channelLogic.clientPort;
			};
			break;
		}
	}

	@Before
	public void setUp() {
		ThreadCleanCheck.before();
		exceptions.clear();
	}

	@After
	public void tearDown() {
		client.close();
		if (trueClient != null)
			trueClient.close();
		server.close();
		if (executorService != null)
			executorService.shutdown();
		ThreadCleanCheck.after();
		assertTrue(exceptions.isEmpty());
	}

	private void connectDefault(int port) throws InterruptedException {
		NTU.connect(server, ":" + NTU.port(port));
		NTU.connect(client, NTU.LOCAL_HOST + ":" + NTU.port(port));
		channelLogic.initPorts();
	}

	protected void setExecutor(int nThreads, String name) {
		this.executorService = new TestThreadPool(nThreads, name, exceptions);
		if (channelLogic.type != TestType.SERVER_CHANNEL)
			server.getServer().setDefaultExecutor(executorService);
		else
			client.getClient().setDefaultExecutor(executorService);
	}

	@Test
	public void testTaskSuspended() throws InterruptedException {
		NTU.exportServices(server.getServer(), new RMIServiceImplementation<>(new SlowCancelServiceImpl(), SlowCancelService.class, SlowCancelService.NAME), channelLogic);
		setExecutor(1, "testTaskSuspended");
		connectDefault(20);
		client.getClient().setRequestRunningTimeout(TIMEOUT);
		client.getClient().setRequestSendingTimeout(TIMEOUT);
		RMIRequest<Double> request;
		ArrayList<RMIRequest<Double>> requests = new ArrayList<>();

		for (int i = 0; i < 3; i++) {
			request = channelLogic.clientPort.createRequest(SlowCancelServiceImpl.getResultSlowly, 10, 20, i, 100L);
			request.send();
			assertTrue(SlowCancelServiceImpl.startResultSlowly.await(10, TimeUnit.SECONDS));
			requests.add(request);
		}

		log.info("Requests sent");
		final RMIRequest<Void> accelerateOperation = channelLogic.clientPort.createRequest(SlowCancelServiceImpl.accelerateOperation, 0);
		final CountDownLatch accelerationCompleted = new CountDownLatch(1);
		accelerateOperation.setListener(request1 -> accelerationCompleted.countDown());
		accelerateOperation.send();


		log.info("----   ---   ---   ----");
		assertTrue(accelerationCompleted.await(10, TimeUnit.SECONDS));
		log.info("accelerateOperation = " + accelerateOperation);

		try {
			assertEquals(requests.get(0).getBlocking(), (Double)30.0);
			assertEquals(requests.get(1).getNonBlocking(), null);
		} catch (RMIException e) {
			fail(e.getType().toString());
		}
		requests.forEach(RMIRequest::cancelOrAbort);
	}

	@Test
	public void testCancelContinuation() throws InterruptedException {
		setExecutor(1, "RMIReqContinuationTest-cancelContinuation-server");
		NTU.exportServices(server.getServer(), new RMIServiceImplementation<>(new SlowCancelServiceImpl(), SlowCancelService.class, SlowCancelService.NAME), channelLogic);
		connectDefault(25);
		client.getClient().setRequestRunningTimeout(TIMEOUT);
		client.getClient().setRequestSendingTimeout(TIMEOUT);
		RMIRequest<Double> request;
		RMIRequest<Void> closeOperation;
		RMIRequest<Double> getTimeSuspend;


		request = channelLogic.clientPort.createRequest(SlowCancelServiceImpl.getResultSlowly, 10, 20, 0, 1000L);
		request.send();
		assertTrue(SlowCancelServiceImpl.startResultSlowly.await(10, TimeUnit.SECONDS));
		log.info("Requests sent");
		Thread.sleep(100);
		closeOperation = channelLogic.clientPort.createRequest(SlowCancelServiceImpl.closeOperation, 0);
		closeOperation.send();
		long startTime = System.currentTimeMillis();
		while (!closeOperation.isCompleted()) {
			Thread.sleep(30);
			if (System.currentTimeMillis() > 10000 + startTime)
				fail();
		}
		log.info("closeOperation " + 0 + " complete");
		try {
			request.getBlocking();
			fail();
		} catch (RMIException e) {
			assertEquals(e.getType(), RMIExceptionType.CANCELLED_DURING_EXECUTION);
		}
		getTimeSuspend = channelLogic.clientPort.createRequest(SlowCancelServiceImpl.getTimeSuspend, 0);
		getTimeSuspend.send();
		try {
			log.info("getTimeSuspend = " + getTimeSuspend.getBlocking());
		} catch (RMIException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testContinuationAndSecurity() throws InterruptedException {
		RMICommonTest.SomeSubject trueSubject = new RMICommonTest.SomeSubject("true");
		NTU.exportServices(server.getServer(), new RMIServiceImplementation<>(new SlowCancelServiceImpl(), SlowCancelService.class, SlowCancelService.NAME), channelLogic);
		NTU.connect(server, ":" + NTU.port(30));
		server.setSecurityController(new RMICommonTest.SomeSecurityController(trueSubject));

		trueClient = RMIEndpoint.createEndpoint(RMIEndpoint.Side.CLIENT);
		NTU.connect(trueClient, NTU.LOCAL_HOST + ":" + NTU.port(30));
		RMIClientPort clientPort = getTrueClientPort.apply(trueSubject);


		RMIRequest<Double> request = clientPort.createRequest(SlowCancelServiceImpl.getResultSlowly, 10, 20, 0, 100L);
		request.send();

		assertTrue(SlowCancelServiceImpl.startResultSlowly.await(10, TimeUnit.SECONDS));
		log.info("Requests sent");
		RMIRequest<Void> accelerateOperation = clientPort.createRequest(SlowCancelServiceImpl.accelerateOperation, 0);
		final CountDownLatch accelerationCompleted = new CountDownLatch(1);
		accelerateOperation.setListener(request1 -> accelerationCompleted.countDown());
		accelerateOperation.send();

		assertTrue(accelerationCompleted.await(10, TimeUnit.SECONDS));

		log.info("accelerateOperation = " + accelerateOperation);

		try {
			assertEquals(request.getBlocking(), (Double)30.0);
		} catch (RMIException e) {
			fail(e.getType().toString());
		}
		trueClient.close();
	}

	@SuppressWarnings("unused")
	private static interface SlowCancelService {
		static final String NAME = "SlowCancelService";
		double getTimeSuspend(int index);
		double getResultSlowly(int a, int b, int index, long timeRelax);
		void accelerateOperation(int index);
		void closeOperation(int index);
	}

	private static class SlowCancelServiceImpl implements SlowCancelService {

		static RMIOperation<Double> getTimeSuspend = RMIOperation.valueOf(NAME, double.class, "getTimeSuspend", int.class);
		static RMIOperation<Double> getResultSlowly = RMIOperation.valueOf(NAME, double.class, "getResultSlowly", int.class, int.class, int.class, long.class);
		static RMIOperation<Void> accelerateOperation = RMIOperation.valueOf(NAME, void.class, "accelerateOperation", int.class);
		static RMIOperation<Void> closeOperation = RMIOperation.valueOf(NAME, void.class, "closeOperation", int.class);
		private static CountDownLatch startResultSlowly;

		private final ArrayList<Long> timersSuspend = new ArrayList<>(3);
		private final List<RMIContinuation<Double>> continuations = new ArrayList<>(3);
		private final List<Callable<Double>> callable = new ArrayList<>(3);
		private final List<RMITask<Double>> tasks = new ArrayList<>(3);

		SlowCancelServiceImpl() {
			startResultSlowly = new CountDownLatch(1);
		}

		@SuppressWarnings("ConstantConditions")
		@Override
		public double getTimeSuspend(int index) {
			Double result;
			synchronized (continuations) {
				if (timersSuspend.get(index) != null)
					result = (double)timersSuspend.get(index);
				else
					result = null;
			}
			return result;
		}

		@Override
		public double getResultSlowly(final int a, final int b, int index, final long timeRelax) {
			startResultSlowly.countDown();
			final RMITask<Double> task = RMITask.current(double.class);
			Callable<Double> continuation = () -> {
				log.info("resume getResultSlowly");
				try {
					Thread.sleep(timeRelax);
				} catch (InterruptedException e) {
					task.completeExceptionally(e);
				}
				return getResult(a, b);
			};
			saveInfo(task, index, continuation);
			return 0;
		}

		@Override
		public void accelerateOperation(int index) {
			RMIContinuation<Double> continuation;
			Callable<Double> callable;
			synchronized (continuations) {
				continuation = continuations.get(index);
				callable = this.callable.get(index);
				if (System.currentTimeMillis() - timersSuspend.get(index) < 10000)
					timersSuspend.add(index, System.currentTimeMillis() - timersSuspend.get(index));
			}
			continuation.resume(callable);
			return;
		}

		@Override
		public void closeOperation(int index) {
			final RMITask<Void> task = RMITask.current(void.class);
			RMITask<Double> taskTemp;
			synchronized (continuations) {
				taskTemp = tasks.get(index);
				taskTemp.cancel();
			}
			task.complete(null);
		}
		private void saveInfo(RMITask<Double> task, int index, Callable<Double> callable) {
			synchronized (continuations) {
				log.info("thread = " + Thread.currentThread());
				continuations.add(index, task.suspend(task1 -> {
					int index1 = (Integer)(task1.getRequestMessage().getParameters().getObject()[2]);
					synchronized (continuations) {
						if (timersSuspend.get(index1) != null) {
							if (System.currentTimeMillis() - timersSuspend.get(index1) < 10000)
								timersSuspend.add(index1, System.currentTimeMillis() - timersSuspend.get(index1));
						} else {
							timersSuspend.add(index1, 0L);
						}
					}
				}));
				this.callable.add(index, callable);
				timersSuspend.add(index, System.currentTimeMillis());
				tasks.add(index, task);
			}
		}

		private double getResult(int a, int b) {
			return a + b;
		}
	}

	//---------------------------------------------

	//only RMIClient
	@Test
	public void testSuspendResume() throws InterruptedException {
		if (channelLogic.isChannel())
			return;
		NTU.exportServices(server.getServer(), new RMIServiceImplementation<>(new BadServiceImpl(), BadService.class, BadService.NAME), channelLogic);
		connectDefault(35);
		setExecutor(3, "RMIReqContinuationTest-suspendResume-server");
		client.getClient().setRequestRunningTimeout(TIMEOUT);
		client.getClient().setRequestSendingTimeout(TIMEOUT);
		RMIRequest<Double> getWaitSquare;
		RMIRequest<Double> accelerate;


		getWaitSquare = channelLogic.clientPort.createRequest(BadServiceImpl.getWaitSquare, 11.0);
		getWaitSquare.send();
		if (!BadServiceImpl.START_GET_SQUARE.await(10, TimeUnit.SECONDS)) {
			fail();
		}
		accelerate = channelLogic.clientPort.createRequest(BadServiceImpl.accelerate, 11.0);
		accelerate.send();

		try {
			assertEquals(accelerate.getBlocking(), (Double)(11.0 * 11));
		} catch (RMIException e) {
			fail(e.getMessage());
		}
		log.info("accelerate complete");
		try {
			assertEquals(getWaitSquare.getBlocking(), (Double)(11.0 * 11));
		} catch (RMIException e) {
			fail(e.getMessage());
		}

	}

	@SuppressWarnings("unused")
	private static interface BadService {
		static final String NAME = "BadService";
		double accelerate(double a);
		double getSquare(double a);
	}

	private static class BadServiceImpl implements BadService {

		private static RMIOperation<Double> accelerate = RMIOperation.valueOf(NAME, double.class, "accelerate", double.class);
		private static RMIOperation<Double> getWaitSquare = RMIOperation.valueOf(NAME, double.class, "getSquare", double.class);
		private static final CountDownLatch START_GET_SQUARE= new CountDownLatch(1);

		private final Object lock = new Object();
		private volatile RMIContinuation<Double> continuation;

		@SuppressWarnings("NakedNotify")
		@Override
		public double accelerate(final double param) {
			final RMITask<Double> task = RMITask.current(double.class);
			log.info("continuation = " + continuation);
			continuation.resume(() -> {
				log.info("resume");
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					task.completeExceptionally(e);
				}
				log.info("FIRST param = " + param);
				return param * param;
			});
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				task.completeExceptionally(e);
			}
			synchronized (lock) {
				lock.notifyAll();
			}
			log.info("SECOND param = " + param);
			log.info("task = " + task);
			return param * param;
		}

		@SuppressWarnings("WaitNotInLoop")
		@Override
		public double getSquare(double a) {
			RMITask<Double> task = RMITask.current(double.class);
			continuation = task.suspend(RMITask::cancel);
			START_GET_SQUARE.countDown();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					task.completeExceptionally(e);
				}
			}
			return 0;
		}
	}

	//-------------------------------------------

}
