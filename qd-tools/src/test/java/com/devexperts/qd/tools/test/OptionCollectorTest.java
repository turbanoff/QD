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
package com.devexperts.qd.tools.test;

import com.devexperts.qd.qtp.QDEndpoint;
import com.devexperts.qd.tools.OptionCollector;
import org.junit.Test;

public class OptionCollectorTest {

	@Test
	public void testParser() throws Exception {
		// Simple collectors
		testParser("ticker", true);
		testParser("stream", true);
		testParser("history", true);
		testParser("all", true);
		// Old options format
		testParser("ticker-se", true);
		testParser("stream-nwc", true);
		testParser("history-se", true);
		testParser("all-se", true);
		testParser("all-nwc", true);
		testParser("filtered-stream[23423]", true);
		// New options format
		testParser("all[]", false);
		testParser("all[se]", true);
		testParser("all[ts]", true);
		testParser("all[se,nwc,ts,filtered=100]", true);
		testParser("stream[se]", true);
		testParser("stream[nwc]", true);
		testParser("stream[se,nwc,filtered=345,ts]", true);
		testParser("ticker[se]", true);
		testParser("ticker[ts]", true);
		testParser("ticker[ts,se]", true);
		testParser("history[se]", true);
		testParser("history[ts]", true);
		testParser("history[se,ts]", true);
		testParser("all[se=true]", true);
		// Ignore whitespaces
		testParser("all [      se  ,   nwc     ,ts ,filtered =  100 ]   ", true);
		// Bad expressions
		testParser("ticker[nwc]", false);
		testParser("ticker[filtered=5990]", false);
		testParser("history[nwc]", false);
		testParser("history[filtered=23424]", false);
		testParser("all[se,]", false);
		testParser("all[,]", false);
		testParser("all[filtered]", false);
		testParser("all-se[ts]", false);
		testParser("all-ts", false);
	}

	private void testParser(String testConfiguration, boolean shouldParsed) throws Exception {
		Exception exception = null;
		try {
			OptionCollector optionCollector = new OptionCollector(testConfiguration);
			optionCollector.applyEndpointOption(QDEndpoint.newBuilder());
			QDEndpoint endpoint = optionCollector.createEndpoint("testEndpoint");
			endpoint.close();
		} catch (IllegalArgumentException e) {
			exception = e;
		}
		if (shouldParsed && exception != null)
			throw new AssertionError("No exception expected for \"" + testConfiguration + "\" input", exception);
		if (!shouldParsed && exception == null)
			throw new AssertionError("IllegalArgumentException expected for \"" + testConfiguration + "\" input");
	}

}
