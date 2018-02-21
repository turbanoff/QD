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
package com.devexperts.qd.test;

import com.devexperts.qd.samplecert.SampleCert;
import com.devexperts.rmi.RMIEndpoint;
import com.devexperts.rmi.test.NTU;
import com.devexperts.test.ThreadCleanCheck;
import junit.framework.TestCase;

/**
 * This test ensures that threads do not leak when establishing non-SSL connection to SSL endpoint.
 * See QD-531.
 */
public class SSLMismatchTest extends TestCase {

	private RMIEndpoint server;
	private RMIEndpoint client;

	@Override
	protected void setUp() throws Exception {
		ThreadCleanCheck.before();
	}

	@Override
	public void tearDown() {
		if (server != null)
			server.close();
		if (client != null)
			client.close();
		ThreadCleanCheck.after();
	}

	public void testThreadLeak() throws InterruptedException {
		SampleCert.init();
		server = RMIEndpoint.createEndpoint();
		NTU.connect(server, "ssl+:1234");
		client = RMIEndpoint.createEndpoint();
		NTU.connect(client, "localhost:1234[reconnectDelay=100000]");
		Thread.sleep(300);
		// disconnect and reconnect multiple times
		client.disconnect();
		client.connect("localhost:1234[reconnectDelay=0]");
		// wait more to have multiple connection attempts
		Thread.sleep(3000);
		//waitConnected(client);
	}
}
