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

import java.util.List;

import com.devexperts.util.TimeUtil;
import com.dxfeed.api.DXEndpoint;
import com.dxfeed.event.candle.Candle;
import com.dxfeed.event.candle.CandleSymbol;
import com.dxfeed.event.market.Quote;
import com.dxfeed.promise.Promise;
import junit.framework.TestCase;

public class ClosedEndpointTest extends TestCase {
	DXEndpoint endpoint;

	@Override
	protected void setUp() throws Exception {
		endpoint = DXEndpoint.create(DXEndpoint.Role.LOCAL_HUB);
		endpoint.close();
	}

	public void testLastEventPromise() {
		Promise<Quote> promise = endpoint.getFeed().getLastEventPromise(Quote.class, "IBM");
		assertTrue(promise.isCancelled());
	}

	public void test() {
		long now = System.currentTimeMillis();
		Promise<List<Candle>> promise = endpoint.getFeed().getTimeSeriesPromise(Candle.class, CandleSymbol.valueOf("IBM"),
			now - 10 * TimeUtil.DAY, now);
		assertTrue(promise.isCancelled());
	}

}
