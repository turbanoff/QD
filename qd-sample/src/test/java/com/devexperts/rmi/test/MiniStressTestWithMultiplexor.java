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

import java.util.Random;

import com.devexperts.logging.Logging;
import com.devexperts.qd.tools.Tools;
import com.devexperts.rmi.RMIException;
import com.devexperts.rmi.RMIRequest;
import com.devexperts.test.TraceRunner;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(TraceRunner.class)
public class MiniStressTestWithMultiplexor extends MiniStressTest {
	private static final Logging log = Logging.getLogging(MiniStressTestWithMultiplexor.class);

	private Thread toolThread;
	private volatile boolean toolOk = true;

	private final int randomPortDistributor = NTU.PORT_00 + 78; // port randomization
	private final int randomPortAgent = NTU.PORT_00 + 79; // port randomization

	void connect() {
		NTU.connect(first, "(" + NTU.LOCAL_HOST + ":" + randomPortDistributor + ")(" + NTU.LOCAL_HOST + ":" + randomPortAgent + ")");
		NTU.connect(second, "(" + NTU.LOCAL_HOST + ":" + randomPortDistributor + ")(" + NTU.LOCAL_HOST + ":" + randomPortAgent + ")");
	}

	@Override
	public void setUp() {
		super.setUp();
		toolThread = new Thread("Tool-thread") {
			@Override
			public void run() {
				toolOk = Tools.invoke("multiplexor",
                    ":" + randomPortDistributor, ":" + randomPortAgent,
                    "-s", "10", "-R");
			}
		};
		toolThread.start();
	}

	@Override
	public void tearDown() throws Exception {
		toolThread.interrupt();
		toolThread.join();
		assertTrue(toolOk);
		super.tearDown();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void testStress() {
		first.getServer().export(new ExternalService());
		second.getServer().export(new ExternalService());
		connect();
		int n = 400;
		RMIRequest<String>[] requests = new RMIRequest[2 * n];
		Random rnd = new Random();
		for (int i = 0; i < 2 * n; i++) {
			idx.put(i, rnd.nextDouble());
			requests[i] = first.getClient().getPort(null).createRequest(ExternalService.operation, i, rnd.nextInt(10) + 1, idx.get(i));
			idx.put(++i, rnd.nextDouble());
			requests[i] = second.getClient().getPort(null).createRequest(ExternalService.operation, i, rnd.nextInt(10) + 1, idx.get(i));
		}

		for (RMIRequest<?> request : requests) {
			request.getChannel().addChannelHandler(new InternalService());
			request.send();
		}

		String r;
		for (int i = 0; i < requests.length; i++) {
			try {
				r = requests[i].getBlocking();
				assertEquals("i = " + i, idx.get(i), Double.valueOf(r.substring(r.lastIndexOf('t') + 2)));
				if ((i + 1) % 100 == 0)
					log.info( (i + 1) + " requests processed");
			} catch (RMIException e) {
				e.printStackTrace();
				fail("type = " + e.getType());
			}
		}
	}
}
