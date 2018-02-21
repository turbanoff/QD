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
import com.devexperts.qd.ng.RecordBuffer;
import com.devexperts.qd.ng.RecordMode;
import junit.framework.TestCase;

public class HistoryReduceTimeTest extends TestCase {
	private static final long TIME1 = 22222222222L;
	private static final long TIME2 = 22222222200L;

	private final DataScheme SCHEME = new TestDataScheme(1, 432, TestDataScheme.Type.HAS_TIME);
	private final DataRecord RECORD = SCHEME.getRecord(0);
	private final int CIPHER = SCHEME.getCodec().encode("HABA");

	public void testReduceHistoryTime() {
		QDHistory history = QDFactory.getDefaultFactory().createHistory(SCHEME);
		QDDistributor distributor = history.distributorBuilder().build();
		AsserteableListener al = new AsserteableListener();
		AsserteableListener rl = new AsserteableListener();
		distributor.getAddedSubscriptionProvider().setSubscriptionListener(al);
		distributor.getRemovedSubscriptionProvider().setSubscriptionListener(rl);
		// create agent with subscription. make sure subscription gets reported
		QDAgent agent1 = history.agentBuilder().build();
		agent1.setSubscription(getSubscription(TIME1));
		al.assertAvailable();
		rl.assertNotAvailable();
		SubscriptionMap orig_sub1 = new SubscriptionMap(SCHEME, getSubscription(TIME1));
		SubscriptionMap sub1 = new SubscriptionMap(SCHEME, distributor.getAddedRecordProvider());
		assertEquals("sub1", orig_sub1, sub1);
		// create 2nd agent with larget subscription. make sure subscriptin gets reported
		QDAgent agent2 = history.agentBuilder().build();
		agent2.setSubscription(getSubscription(TIME2));
		al.assertAvailable();
		rl.assertNotAvailable();
		SubscriptionMap orig_sub2 = new SubscriptionMap(SCHEME, getSubscription(TIME2));
		SubscriptionMap sub2 = new SubscriptionMap(SCHEME, distributor.getAddedRecordProvider());
		assertEquals("sub2", orig_sub2, sub2);
		// close 2nd agent (with larget subscription). make sure subscription reduction gets reported
		agent2.close();
		al.assertAvailable();
		rl.assertNotAvailable();
		SubscriptionMap sub3 = new SubscriptionMap(SCHEME, distributor.getAddedRecordProvider());
		assertEquals("sub3", orig_sub1, sub3);
	}

	private RecordBuffer getSubscription(long time) {
		RecordBuffer sb = new RecordBuffer(RecordMode.HISTORY_SUBSCRIPTION);
		sb.visitRecord(RECORD, CIPHER, null, time);
		return sb;
	}
}
