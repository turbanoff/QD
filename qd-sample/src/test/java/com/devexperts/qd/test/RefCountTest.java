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

import com.devexperts.qd.*;
import com.devexperts.qd.kit.*;
import junit.framework.TestCase;

/**
 * Reference counting unit test that is known to reproduce DR#4.
 */
public class RefCountTest extends TestCase {
	public RefCountTest(String s) {
		super(s);
	}

	private static final SymbolCodec codec = new PentaCodec();
	private static final DataRecord record = new DefaultRecord(0, "Haba", false, null, null);
	private static final DataScheme scheme = new DefaultScheme(codec, new DataRecord[] {record});

	private static SubscriptionIterator sub(int size) {
		SubscriptionBuffer sb = new SubscriptionBuffer();
		while (--size >= 0)
			sb.visitRecord(record, 0, "abcde" + size);
		return sb;
	}

	private static DataIterator data(int size) {
		DataBuffer db = new DataBuffer();
		while (--size >= 0) {
			db.visitRecord(record, 0, "abcde" + size);
			for (int i = 0; i < record.getIntFieldCount(); i++)
				db.visitIntField(record.getIntField(i), 0);
			for (int i = 0; i < record.getObjFieldCount(); i++)
				db.visitObjField(record.getObjField(i), null);
		}
		return db;
	}

	public void testDistributorCloseA() {
		QDTicker ticker = QDFactory.getDefaultFactory().createTicker(scheme);
		ticker.agentBuilder().build().setSubscription(sub(1));
		for (int i = 10; --i >= 0;)
			ticker.distributorBuilder().build().close();
	}

	public void testDistributorCloseB() {
		QDTicker ticker = QDFactory.getDefaultFactory().createTicker(scheme);
		for (int i = 10; --i >= 0;) {
			ticker.distributorBuilder().build().close();
			ticker.agentBuilder().build().setSubscription(sub(1));
		}
	}

	public void testAgentCloseA() {
		QDTicker ticker = QDFactory.getDefaultFactory().createTicker(scheme);
		for (int i = 10; --i >= 0;) {
			QDAgent agent = ticker.agentBuilder().build();
			agent.setSubscription(sub(1));
			agent.close();
		}
	}

	public void testAgentCloseB() {
		QDTicker ticker = QDFactory.getDefaultFactory().createTicker(scheme);
		QDDistributor distributor = ticker.distributorBuilder().build();
		for (int i = 10; --i >= 0;) {
			QDAgent agent = ticker.agentBuilder().build();
			agent.setSubscription(sub(1000));
			agent.close();
		}
	}

	public void testAgentCloseC() {
		QDTicker ticker = QDFactory.getDefaultFactory().createTicker(scheme);
		QDDistributor distributor = ticker.distributorBuilder().build();
		for (int i = 10; --i >= 0;) {
			QDAgent agent = ticker.agentBuilder().build();
			agent.setSubscription(sub(1000));
			distributor.getAddedSubscriptionProvider().retrieveSubscription(new SubscriptionBuffer());
			agent.close();
			distributor.getRemovedSubscriptionProvider().retrieveSubscription(new SubscriptionBuffer());
		}
	}

	public void testTotal() {
		QDTicker ticker = QDFactory.getDefaultFactory().createTicker(scheme);
		QDDistributor distributor = ticker.distributorBuilder().build();
		QDAgent agent = ticker.agentBuilder().build();
		agent.addSubscription(sub(1));
		agent.removeSubscription(sub(1));
		agent.addSubscription(sub(1));
		distributor.processData(data(1));
	}

	public void testMultipleRecords() {
		DataRecord[] records = new DataRecord[10];
		SubscriptionBuffer sb = new SubscriptionBuffer();
		for (int i = records.length; --i >= 0;)
			sb.visitRecord(records[i] = new DefaultRecord(i, Integer.toString(i), false, null, null), 0, "abcde0");
		DataScheme scheme = new DefaultScheme(codec, records);
		QDTicker ticker = QDFactory.getDefaultFactory().createTicker(scheme);
		ticker.agentBuilder().build().setSubscription(sb);
	}
}
