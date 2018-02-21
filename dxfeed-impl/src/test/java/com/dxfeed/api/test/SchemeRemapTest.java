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
package com.dxfeed.api.test;

import java.util.*;
import java.util.concurrent.*;

import com.devexperts.qd.*;
import com.devexperts.qd.kit.*;
import com.devexperts.qd.ng.*;
import com.devexperts.qd.qtp.*;
import com.devexperts.qd.stats.QDStats;
import com.devexperts.qd.util.Decimal;
import com.devexperts.test.ThreadCleanCheck;
import com.dxfeed.api.DXEndpoint;
import com.dxfeed.api.DXFeedSubscription;
import com.dxfeed.event.market.*;
import junit.framework.TestCase;

/**
 * Tests compatibility with a scheme with missing records and records with different ids.
 */
public class SchemeRemapTest extends TestCase {
	private static final char EX = 'X';
	private static final String SYMBOL = "TEST";
	private static final String SYMBOL_EX = SYMBOL + "&" + EX;
	private static final int CIPHER = PentaCodec.INSTANCE.encode(SYMBOL);
	private static final DataRecord TRADE_RECORD = new DefaultRecord(0, "Trade&" + EX, false,
		new DataIntField[] {
			new DecimalField(0, "Trade&X.Last.Price", SerialFieldType.DECIMAL),
			new DecimalField(1, "Trade&X.Last.Size", SerialFieldType.DECIMAL)
		}, new DataObjField[0]);
	private static final DataScheme SOURCE_SCHEME = new DefaultScheme(PentaCodec.INSTANCE, TRADE_RECORD);

	private static final int PORT = (100 + new Random().nextInt(300)) * 100 + 74;

	private DXEndpoint endpoint;
	private List<MessageConnector> connectors = Collections.emptyList();

	@Override
	protected void setUp() throws Exception {
		ThreadCleanCheck.before();
		endpoint = DXEndpoint.create();
	}

	@Override
	protected void tearDown() throws Exception {
		endpoint.close();
		MessageConnectors.stopMessageConnectors(connectors);
		ThreadCleanCheck.after();
	}

	public void testSchemeRemap() throws InterruptedException {
		runDataProvider();
		runDataConsumer();
	}

	private void runDataProvider() {
		QDTicker ticker = QDFactory.getDefaultFactory().createTicker(SOURCE_SCHEME);
		final QDDistributor distributor = ticker.distributorBuilder().build();
		distributor.getAddedRecordProvider().setRecordListener(provider -> {
			RecordBuffer sub = RecordBuffer.getInstance(RecordMode.SUBSCRIPTION);
			provider.retrieve(sub);
			RecordCursor cursor;
			while ((cursor = sub.next()) != null) {
				if (cursor.getDecodedSymbol().equals(SYMBOL)) {
					RecordBuffer buf = RecordBuffer.getInstance();
					RecordCursor a = buf.add(TRADE_RECORD, CIPHER, null);
					a.setInt(0, Decimal.compose(95.47)); // price
					a.setInt(1, Decimal.compose(123)); // size
					distributor.process(buf);
					buf.release();
				}
			}
			sub.release();
		});
		connectors = MessageConnectors.createMessageConnectors(new AgentAdapter.Factory(ticker), ":" + PORT, QDStats.VOID);
		MessageConnectors.startMessageConnectors(connectors);
	}

	private void runDataConsumer() throws InterruptedException {
		endpoint.connect("localhost:" + PORT);
		final BlockingQueue<MarketEvent> queue = new ArrayBlockingQueue<>(10);
		// subscribe to quote & trade
		DXFeedSubscription<MarketEvent> sub = endpoint.getFeed().createSubscription(Quote.class, Trade.class);
		sub.addEventListener(queue::addAll);
		sub.addSymbols(SYMBOL_EX);
		MarketEvent event = queue.poll(5, TimeUnit.SECONDS);
		assertTrue(event instanceof Trade);
		Trade trade = (Trade)event;
		assertEquals(SYMBOL_EX, trade.getEventSymbol());
		assertEquals(95.47, trade.getPrice());
		assertEquals(123, trade.getSize());
		assertEquals(0, queue.size());
	}
}




