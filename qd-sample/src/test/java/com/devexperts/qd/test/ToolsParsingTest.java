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

import java.util.*;

import com.devexperts.qd.DataScheme;
import com.devexperts.qd.tools.Tools;
import junit.framework.TestCase;

public class ToolsParsingTest extends TestCase {
	private static final DataScheme SCHEME = new TestDataScheme(20131101);
	
	public void testSymbols() {
		assertSameSet(Arrays.asList("*"), Arrays.asList(Tools.parseSymbols("all", SCHEME)));
		assertSameSet(Arrays.asList("IBM"), Arrays.asList(Tools.parseSymbols("IBM", SCHEME)));
		assertSameSet(Arrays.asList("IBM", "MSFT"), Arrays.asList(Tools.parseSymbols("IBM,MSFT", SCHEME)));
		assertSameSet(Arrays.asList("IBM", "MSFT", "GOOG"), Arrays.asList(Tools.parseSymbols("IBM,MSFT,GOOG", SCHEME)));
		assertSameSet(Arrays.asList("IBM,MSFT"), Arrays.asList(Tools.parseSymbols("IBM\\,MSFT", SCHEME)));
		assertSameSet(Arrays.asList("IBM,MSFT", "GOOG"), Arrays.asList(Tools.parseSymbols("IBM\\,MSFT,GOOG", SCHEME)));
		assertSameSet(Arrays.asList("IBM", "MSFT,GOOG"), Arrays.asList(Tools.parseSymbols("IBM,MSFT\\,GOOG", SCHEME)));
		assertSameSet(Arrays.asList("IBM,MSFT,GOOG"), Arrays.asList(Tools.parseSymbols("IBM\\,MSFT\\,GOOG", SCHEME)));

		assertSameSet(Arrays.asList(","), Arrays.asList(Tools.parseSymbols("\\,", SCHEME)));
		assertSameSet(Arrays.asList("="), Arrays.asList(Tools.parseSymbols("=", SCHEME)));

		assertSameSet(Arrays.asList("IBM{p=1,q=2}"), Arrays.asList(Tools.parseSymbols("IBM{p=1,q=2}", SCHEME)));
	}

	private void assertSameSet(List<String> expected, List<String> received) {
		assertEquals(new HashSet<String>(expected), new HashSet<String>(received));
	}
}
