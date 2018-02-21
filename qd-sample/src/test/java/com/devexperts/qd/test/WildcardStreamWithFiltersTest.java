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
import com.devexperts.qd.kit.PatternFilter;
import com.devexperts.qd.ng.RecordBuffer;
import junit.framework.TestCase;

public class WildcardStreamWithFiltersTest extends TestCase {
	public void testDistributorFilter() {
		check(true, false);
	}

	public void testAgentFilter() {
		check(false, true);
	}

	public void testDistributorAndAgentFilter() {
		check(true, true);
	}

	void check(boolean distributorFilter, boolean agentFilter) {
		// create stuff
		DataScheme scheme = new TestDataScheme(20110825);
		DataRecord record = scheme.getRecord(0);
		SymbolCodec codec = scheme.getCodec();
		QDStream stream = QDFactory.getDefaultFactory().createStream(scheme);
		stream.setEnableWildcards(true);
		SubscriptionFilter filter = PatternFilter.valueOf("A*", scheme);
		QDDistributor distributor = stream.distributorBuilder()
			.withFilter(QDFilter.fromFilter(distributorFilter ? filter : null, scheme))
			.build();
		QDAgent agent = stream.agentBuilder()
			.withFilter(QDFilter.fromFilter(agentFilter ? filter : null, scheme))
			.build();

		// subscribe to wildcard
		SubscriptionBuffer sub = new SubscriptionBuffer();
		sub.visitRecord(record, codec.getWildcardCipher(), null);
		agent.addSubscription(sub);

		// distribute 3 records
		RecordBuffer buf = RecordBuffer.getInstance();
		int validCipher = codec.encode("A");
		buf.add(record, validCipher, null);
		buf.add(record, codec.encode("B"), null);
		buf.add(record, validCipher, null);
		distributor.processData(buf);

		// check that only two records with valid symbols are processed
		buf.clear();
		agent.retrieveData(buf);
		assertEquals(2, buf.size());
		assertEquals(validCipher, buf.next().getCipher());
		assertEquals(validCipher, buf.next().getCipher());
	}
}
