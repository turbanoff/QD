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

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import javax.annotation.Nonnull;

import com.devexperts.auth.AuthToken;
import com.devexperts.io.Marshalled;
import com.devexperts.logging.Logging;
import com.devexperts.qd.qtp.QDEndpoint;
import com.devexperts.rmi.*;
import com.devexperts.rmi.impl.RMIEndpointImpl;
import com.devexperts.rmi.samples.DifferentServices;
import com.devexperts.rmi.security.SecurityController;
import com.devexperts.rmi.task.*;
import com.devexperts.test.ThreadCleanCheck;
import com.devexperts.test.TraceRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(TraceRunner.class)
public class RMICommonTest {
	private static final Logging log = Logging.getLogging(RMICommonTest.class);

	private RMIEndpoint server;
	private RMIEndpoint client;
	private RMIClientPort clientPort;

	private RMIEndpoint trueClient;
	private RMIEndpoint falseClient;

	static volatile boolean finish = false;

	@Before
	public void setUp() {
		ThreadCleanCheck.before();
		finish = false;
	}

	@After
	public void tearDown() {
		finish = true;
		if (client != null)
			client.close();
		if (falseClient != null)
			falseClient.close();
		if (trueClient != null)
			trueClient.close();
		if (server != null)
			server.close();
		ThreadCleanCheck.after();
	}

	protected RMIEndpoint server() {
		if (server == null)
			server = RMIEndpoint.createEndpoint();
		return server;
	}

	protected RMIEndpoint client() {
		if (client == null) {
			client = RMIEndpoint.createEndpoint();
			client.setRequestRunningTimeout(20000); // to make sure tests don't run forever
		}
		return client;
	}

	private void initPorts() {
		clientPort = client().getClient().getPort(client.getSecurityController().getSubject());
	}

	private void connectDefault(int port) {
		NTU.connect(server(), ":" + NTU.port(port));
		NTU.connect(client(), NTU.LOCAL_HOST + ":" + NTU.port(port));
		initPorts();
	}

	private void exportServices(RMIServer server, RMIService<?> service) {
		server().getServer().export(service);
	}

	public interface Summator {
		int sum(int a, int b) throws RMIException;

		int getOperationsCount() throws RMIException;
	}

	public static class SummatorImpl implements Summator {
		private int k = 0;

		@Override
		public int sum(int a, int b) {
			k++;
			return a + b;
		}

		@Override
		public int getOperationsCount() {
			return k;
		}
	}

	//only RMIClient
	@Test
	public void testNullSubject() {
		connectDefault(3);
		exportServices(server().getServer(), new RMIServiceImplementation<>(new SummatorImpl(), Summator.class));
		RMIRequest<Integer> sum = client.getClient().getPort(Marshalled.NULL).createRequest(
			RMIOperation.valueOf(Summator.class, int.class, "sum", int.class, int.class), 25, 48);
		sum.send();
		try {
			assertEquals(sum.getBlocking(), Integer.valueOf(73));
			log.info(sum.getBlocking().toString());
		} catch (RMIException e) {
			fail(e.getType().name());
		}
	}

// --------------------------------------------------

	//only for RMIClient
	@Test
	public void testReexporting() throws InterruptedException {
		final CountDownLatch exportLatch = new CountDownLatch(2);
		log.info(" ---- testReexporting ---- ");
		connectDefault(19);
		try {
			ConstNumber num = clientPort.getProxy(ConstNumber.class, "TwoFive");
			client.getClient().getService("*").addServiceDescriptorsListener(descriptors -> exportLatch.countDown());
			RMIService<ConstNumber> two = new RMIServiceImplementation<>(new Two(), ConstNumber.class, "TwoFive");
			RMIService<ConstNumber> five = new RMIServiceImplementation<>(new Five(), ConstNumber.class, "TwoFive");
			exportServices(server.getServer(), two);
			assertEquals(num.getValue(), 2);
			log.info("---");
			exportServices(server.getServer(), five);
			exportLatch.await(10, TimeUnit.SECONDS);
			long result = num.getValue();
			if (result != 2 && result != 5)
				fail();
			assertEquals(num.getValue(), result);
			log.info("---");
			server.getServer().unexport(two);
			Thread.sleep(150);
			assertEquals(num.getValue(), 5);
		} catch (RMIException e) {
			fail(e.getMessage());
		}
	}

	public interface ConstNumber {
		long getValue() throws RMIException;
	}

	public static class Two implements ConstNumber {
		@Override
		public long getValue() {
			return 2;
		}
	}

	public static class Five implements ConstNumber {
		@Override
		public long getValue() {
			return 5;
		}
	}

// --------------------------------------------------

	public interface InfiniteLooper {
		void loop() throws RMIException, InterruptedException;
	}

	public static class ServerDisconnectingInfiniteLooper implements RMICommonTest.InfiniteLooper {
		private final RMIEndpoint server;

		public ServerDisconnectingInfiniteLooper(RMIEndpoint server) {
			this.server = server;
		}

		@Override
		@SuppressWarnings({"InfiniteLoopStatement"})
		public void loop() throws InterruptedException {
			server.disconnect();
			long startLoop = System.currentTimeMillis();
			while (!finish && System.currentTimeMillis() < startLoop + 10000) {
				Thread.sleep(10);
			}
		}
	}


	public static class SimpleInfiniteLooper implements InfiniteLooper {
		@Override
		public void loop() throws RMIException, InterruptedException {
			long startLoop = System.currentTimeMillis();
			while (!finish && System.currentTimeMillis() < startLoop + 10000) {
				Thread.sleep(10);
			}
		}
	}

	//only for RMIClient
	@Test
	public void testRequestRunningTimeout() {
		client().setRequestRunningTimeout(0);
		exportServices(server().getServer(), new RMIServiceImplementation<>(new SimpleInfiniteLooper(), InfiniteLooper.class));
		connectDefault(29);
		InfiniteLooper looper = clientPort.getProxy(InfiniteLooper.class);
		try {
			looper.loop();
		} catch (RMIException e) {
			if (e.getType() != RMIExceptionType.REQUEST_RUNNING_TIMEOUT) {
				fail(e.getMessage());
			} // else ok
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}

// --------------------------------------------------

	static class CountingExecutorService extends AbstractExecutorService implements AutoCloseable {
		private final ExecutorService delegate;
		private int submissionsNumber = 0;

		CountingExecutorService(ExecutorService es) {
			this.delegate = es;
		}

		// :KLUDGE: only this method is used by RMI
		@Nonnull
		@Override
		public Future<?> submit(Runnable task) {
			submissionsNumber++;
			FutureTask<?> fut = new FutureTask<>(task, null);
			delegate.execute(fut);
			return fut;
		}

		@Override
		public void close() {
			shutdown();
		}

		@Override
		public void shutdown() {
			delegate.shutdown();
		}

		@Nonnull
		@Override
		public List<Runnable> shutdownNow() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isShutdown() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isTerminated() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean awaitTermination(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void execute(@Nonnull Runnable command) {
			command.run();
		}

		public int getSubmissionsNumber() {
			return submissionsNumber;
		}
	}

	public interface Ping {
		void ping() throws RMIException;
	}

	public static class SimplePing implements Ping {
		@Override
		public void ping() {
		}
	}

	// only for request-channel-serverPort
	@Test
	public void testSpecificExecutors() throws InterruptedException {
		connectDefault(33);
		// Set custom executor and try
		RMIServiceImplementation<Ping> service;
		Ping proxy;
		try (CountingExecutorService currentExecutor =
			     new CountingExecutorService(Executors.newSingleThreadExecutor(r -> new Thread(r, "RMICommonTest-CountingExecutorService-part-1"))))
		{
			server.getServer().setDefaultExecutor(currentExecutor);
			service = new RMIServiceImplementation<>(new SimplePing(), Ping.class);
			server.getServer().export(service);
			proxy = clientPort.getProxy(Ping.class);
			log.info("---------------- Running part 1 ----------------");
			pingAndCount(proxy, currentExecutor, 5);
		}

		// change executor and make sure processing goes to the new one
		try (CountingExecutorService currentExecutor =
			     new CountingExecutorService(Executors.newSingleThreadExecutor(r -> new Thread(r, "RMICommonTest-CountingExecutorService-part-2"))))
		{
			server.getServer().setDefaultExecutor(currentExecutor);
			log.info("---------------- Running part 2 ----------------");
			pingAndCount(proxy, currentExecutor, 3);
		}

		// Specify explicit executor for the service
		try (CountingExecutorService currentExecutor =
			     new CountingExecutorService(Executors.newSingleThreadExecutor(r -> new Thread(r, "RMICommonTest-CountingExecutorService-part-3"))))
		{
			service.setExecutor(currentExecutor);
			log.info("---------------- Running part 3 ----------------");
			pingAndCount(proxy, currentExecutor, 7);
		}
	}

	private static void pingAndCount(Ping proxy, CountingExecutorService currentExecutor, int n) {
		for (int i = 0; i < n; i++)
			try {
				proxy.ping();
			} catch (RMIException e) {
				fail(e.getMessage());
			}
		if (currentExecutor.getSubmissionsNumber() != n) {
			fail(currentExecutor.getSubmissionsNumber() + " vs " + n);
		}
	}

// --------------------------------------------------

	//only for RMIClient
	@Test
	public void testConnectionAfterSendingRequest() {
		initPorts();
		exportServices(server().getServer(), DifferentServices.CALCULATOR_SERVICE);
		@SuppressWarnings("unchecked")
		RMIRequest<Double> sum = clientPort.createRequest(DifferentServices.CalculatorService.PLUS, 12.1321, 352.561);
		sum.send();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		connectDefault(43);
		try {
			assertEquals(sum.getBlocking(), (Double)(12.1321 + 352.561));
		} catch (RMIException e) {
			fail(e.getMessage());
		}
	}

// --------------------------------------------------

	//only RMIClient
	@Test
	public void testSubject() throws InterruptedException {
		initPorts();
		SomeSubject trueSubject = new SomeSubject("true");
		SomeSubject falseSubject = new SomeSubject("false");
		exportServices(server().getServer(), new RMIServiceImplementation<>(new SummatorImpl(), Summator.class, "summator"));
		server().setSecurityController(new SomeSecurityController(trueSubject));
		trueClient = RMIEndpoint.createEndpoint(RMIEndpoint.Side.CLIENT);
		NTU.connect(server(), ":" + NTU.port(49));
		NTU.connect(trueClient, NTU.LOCAL_HOST + ":" + NTU.port(49));
		trueClient.setSecurityController(new SomeSecurityController(trueSubject));
		Summator summator = trueClient.getClient().getProxy(Summator.class, "summator");

		try {
			assertEquals(summator.sum(256,458), 256 + 458);
		} catch (RMIException e) {
			fail(e.getMessage());
		}
		trueClient.close();

		falseClient = RMIEndpoint.createEndpoint(RMIEndpoint.Side.CLIENT);
		NTU.connect(falseClient, NTU.LOCAL_HOST + ":" + NTU.port(49));
		log.info("_____________________");
		falseClient.setSecurityController(new SomeSecurityController(falseSubject));
		summator = falseClient.getProxy(Summator.class, "summator");
		try {
			summator.sum(256, 458);
			fail();
		} catch (RMIException e) {
			if (e.getType() != RMIExceptionType.SECURITY_VIOLATION);
		}
		falseClient.close();
	}

	public static class SomeSecurityController implements SecurityController {
		private final SomeSubject subject;

		public SomeSecurityController(SomeSubject subject) {
			this.subject = subject;
		}

		@Override
		public Object getSubject() {
			return subject;
		}

		@Override
		public void doAs(Object subject, Runnable action) throws SecurityException {
			if (subject instanceof SomeSubject && ((SomeSubject)subject).getCode().equals(this.subject.getCode()))
				action.run();
			else
				throw new SecurityException();
		}
	}

	public static class SomeSubject implements Serializable {
		private static final long serialVersionUID = -0L;
		private String code;

		public SomeSubject(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}

	// --------------------------------------------------

	private static class BasicSecurityController implements SecurityController {

		@Override
		public Object getSubject() {
			return null;
		}

		@Override
		public void doAs(Object subject, Runnable action) throws SecurityException {
			if(subject instanceof AuthToken) {
				if (((AuthToken)subject).getUser().equals("test") && ((AuthToken)subject).getPassword().equals("demo"))
					action.run();
				else
					throw new SecurityException();
			} else {
				throw new SecurityException();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAuthorization() throws NoSuchMethodException {
		server = RMIEndpoint.createEndpoint(RMIEndpoint.Side.SERVER);
		server.setSecurityController(new BasicSecurityController());
		NTU.connect(server, ":" + NTU.port(51));
		server.getServer().export(new SummatorImpl(), Summator.class);

		QDEndpoint endpointClient = QDEndpoint.newBuilder().withName("QD_CLIENT").build().user("test").password("demo");
		client = new RMIEndpointImpl(RMIEndpoint.Side.CLIENT, endpointClient, null, null);
		NTU.connect(client, NTU.LOCAL_HOST + ":" + NTU.port(51));

		QDEndpoint endpointBadClient = QDEndpoint.newBuilder().withName("QD_BAD_CLIENT").build().user("test").password("test");
		falseClient = new RMIEndpointImpl(RMIEndpoint.Side.CLIENT, endpointBadClient, null, null);
		NTU.connect(falseClient, NTU.LOCAL_HOST + ":" + NTU.port(51));

		// Operation on good client goes through
		RMIOperation<Integer> operation = RMIOperation.valueOf(Summator.class, Summator.class.getMethod("sum", int.class, int.class));
		RMIRequest<Integer> request = client.getClient().createRequest(null, operation, 1, 23);
		request.send();
		try {
			assertEquals((int)request.getBlocking(), 24);
		} catch (RMIException e) {
			fail(e.getMessage());
		}

		// Operation on bad client returns security exception
		request = falseClient.getClient().createRequest(null, operation, 1, 23);
		request.send();
		try {
			request.getBlocking();
			fail();
		} catch (RMIException e) {
			if(e.getType() != RMIExceptionType.SECURITY_VIOLATION)
				fail(e.getMessage());
		}
	}
}
