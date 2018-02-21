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

import java.util.Random;

import com.devexperts.qd.*;
import com.devexperts.qd.kit.*;
import com.devexperts.qd.sample.SampleScheme;
import junit.framework.TestCase;

public class PatternFilterTest extends TestCase {
	private static final DataScheme SCHEME = SampleScheme.getInstance();
	private static final SymbolCodec CODEC = SCHEME.getCodec();

	public void testPatterns() {
		assertTrue(accepts("", "HABA"));
		assertTrue(accepts("", "true"));
		assertTrue(accepts("", "\u1234\u6789"));
		assertTrue(accepts("", "MSFT"));
		assertTrue(accepts("", "IBM"));
		assertTrue(accepts("", ".OPRA"));

		assertTrue(accepts("*", "HABA"));
		assertTrue(accepts("*", "true"));
		assertTrue(accepts("*", "\u1234\u6789"));
		assertTrue(accepts("*", "MSFT"));
		assertTrue(accepts("*", "IBM"));
		assertTrue(accepts("*", ".OPRA"));

		assertTrue(accepts(".*", ".OPRA"));
		assertTrue(accepts(".*", ".HABA"));
		assertFalse(accepts(".*", "IBM"));
		assertFalse(accepts(".*", "MSFT"));

		assertTrue(accepts(".[A-K]*", ".ABBA"));
		assertTrue(accepts(".[A-K]*", ".K"));
		assertFalse(accepts(".[A-K]*", ".Z"));
		assertFalse(accepts(".[A-K]*", ".L"));
		assertFalse(accepts(".[A-K]*", "."));
		assertFalse(accepts(".[A-K]*", ""));

		assertFalse(accepts(".[^A-K]*", ".ABBA"));
		assertFalse(accepts(".[^A-K]*", ".K"));
		assertTrue(accepts(".[^A-K]*", ".Z"));
		assertTrue(accepts(".[^A-K]*", ".L"));
		assertFalse(accepts(".[^A-K]*", "."));
		assertFalse(accepts(".[^A-K]*", ""));

		assertTrue(accepts2("[/A-Z][DFT][HG]*", "/FH"));
		assertTrue(accepts2("[/A-Z][DFT][HG]*", "ZTG"));
		assertFalse(accepts2("[/A-Z][DFT][HG]*", "ZTI"));
		assertFalse(accepts2("[/A-Z][DFT][HG]*", ".TG"));
		assertFalse(accepts2("[/A-Z][DFT][HG]*", "/EH"));

		assertTrue(accepts2("[A-DH-KQ-U]*", "ABBA"));
		assertTrue(accepts2("[A-DH-KQ-U]*", "BUBA"));
		assertTrue(accepts2("[A-DH-KQ-U]*", "CAKA"));
		assertTrue(accepts2("[A-DH-KQ-U]*", "DADA"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "ECHO"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "FIGA"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "GIGA"));
		assertTrue(accepts2("[A-DH-KQ-U]*", "HOP"));
		assertTrue(accepts2("[A-DH-KQ-U]*", "IX"));
		assertTrue(accepts2("[A-DH-KQ-U]*", "J"));
		assertTrue(accepts2("[A-DH-KQ-U]*", "KOKE"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "LADA"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "MODA"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "NET"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "OK"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "P"));
		assertTrue(accepts2("[A-DH-KQ-U]*", "QUEUE"));
		assertTrue(accepts2("[A-DH-KQ-U]*", "REST"));
		assertTrue(accepts2("[A-DH-KQ-U]*", "TELEPORATATION"));
		assertTrue(accepts2("[A-DH-KQ-U]*", "UNIT"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "VICTORY"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "WWWWWWWWWWWWWw"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "XING"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "YAW"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "ZED"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "~"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "!NOT"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "&MORE"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "#MARK"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "(SOME)"));
		assertFalse(accepts2("[A-DH-KQ-U]*", "\u1234\u6789"));

		assertTrue(accepts("HABA*", "HABA"));
		assertTrue(accepts("HABA*", "HABA*"));
		assertFalse(accepts("HABA*", "HAB*"));
		assertFalse(accepts("HABA*", "HABB"));
		assertFalse(accepts("HABA*", "HBBA"));
		assertFalse(accepts("HABA*", "\u1234"));

		assertFalse(accepts("HABA", ""));
		assertFalse(accepts("HABA", "H"));
		assertFalse(accepts("HABA", "HA"));
		assertFalse(accepts("HABA", "HAB"));
		assertTrue(accepts("HABA", "HABA"));
		assertFalse(accepts("HABA", "HABAH"));
		assertFalse(accepts("HABA", "HABAHA"));
		assertFalse(accepts("HABA", "HABAHAB"));
		assertFalse(accepts("HABA", "HABAHABA"));

		assertTrue(accepts("[^I]*", "HABA"));
		assertTrue(accepts("[^I]*", "true"));
		assertTrue(accepts("[^I]*", "\u1234\u6789"));
		assertTrue(accepts("[^I]*", "MSFT"));
		assertFalse(accepts("[^I]*", "IBM"));
		assertTrue(accepts("[^I]*", ".OPRA"));

		// lowercase letters are reserved only at start:
		assertTrue(accepts("A[A-Z]p*", "ABp"));
		assertFalse(accepts("A[A-Z]p*", "ABP"));

		// colon in the middle is treated as regular char [QD-731], but quoted forms a supported, too
		assertTrue(accepts("UKX:X", "UKX:X"));
		assertTrue(accepts("UKX[:]X", "UKX:X"));
		assertTrue(accepts("UKX\\:X", "UKX:X"));

		// wild-card can be in the beginning [QD-753] Support wildcard pattern in any place of symbol filter
		assertTrue(accepts("*:TR", ":TR"));
		assertTrue(accepts("*:TR", "A:TR"));
		assertTrue(accepts("*:TR", "HABA:TR"));
		assertFalse(accepts("*:TR", "TR"));
		assertFalse(accepts("*:TR", "A:PR"));

		// wild-card can be in the middle [QD-753] Support wildcard pattern in any place of symbol filter
		assertTrue(accepts("A*B", "AB"));
		assertTrue(accepts("A*B", "AAB"));
		assertTrue(accepts("A*B", "ABB"));
		assertTrue(accepts("A*B", "AXXXXB"));
		assertFalse(accepts("A*B", "A"));
		assertFalse(accepts("A*B", "B"));
		assertFalse(accepts("A*B", "BA"));
		assertFalse(accepts("A*B", "BB"));
		assertFalse(accepts("A*B", "AA"));

		// lowercase is ok after wildcard
		assertTrue(accepts("A*b", "Ab"));
	}

	public void testErrors() {
		checkError("AB*D*");
		checkError("*AB*D");
		checkError("AB**D");
		checkError("[*");
		checkError("[HABA*");
		checkError("HABA]*");
		checkError("[A--Z]*");
		checkError("*ABC*");
		checkError("+ABC*");
		checkError("+ABC*");
		checkError("[A[B]]*");
		checkError("[AB-]*");
		checkError("[-AB]*");
		checkError("[^-AB]*");
		checkError("\u1234*");

		// lowercase starts are reserved
		checkError("a*");
		checkError("z*");
		checkError("abba");
		checkError("haba");
	}

	public void testQuotedPattern() {
		assertEquals("Trade", PatternFilter.quote("Trade"));
		assertEquals("Trade[&]N", PatternFilter.quote("Trade&N"));
		assertEquals("[f]eed", PatternFilter.quote("feed"));

		Random r = new Random(20110930);
		for (int i = 0; i < 10000; i++) {
			int len = r.nextInt(10) + 1;
			StringBuilder sb = new StringBuilder(len);
			for (int j = 0; j < len; j++)
				sb.append((char)(' ' + r.nextInt(127 - ' ')));
			String symbol = sb.toString();
			String quotedPattern = PatternFilter.quote(symbol);
			accepts(quotedPattern, symbol);
			// also make sure that quoted pattern can be used as a composite filter with the same effect
			// and denotes a single quoted symbol symbol
			QDFilter filter = CompositeFilters.valueOf(quotedPattern, QDFactory.getDefaultScheme());
			assertEquals(quotedPattern, filter.toString());
			assertEquals(QDFilter.Kind.SYMBOL_SET, filter.getKind());
			assertEquals(1, filter.getSymbolSet().size());
			int cipher = CODEC.encode(symbol);
			assertTrue(filter.getSymbolSet().contains(cipher, symbol));
		}
	}

	private boolean accepts(String pattern, String symbol) {
		int cipher = CODEC.encode(symbol);
		SubscriptionFilter filter = PatternFilter.valueOf(pattern, null);
		return filter == null || filter.acceptRecord(SCHEME.getRecord(0), cipher, cipher == 0 ? symbol : null);
	}

	private boolean accepts2(String pattern, String symbol) {
		assertTrue(pattern.startsWith("["));
		return accepts(pattern, symbol) && !accepts("[^" + pattern.substring(1), symbol);
	}

	private void checkError(String pattern) {
		try {
			PatternFilter.valueOf(pattern, null);
			fail("Invalid pattern passed: " + pattern);
		} catch (FilterSyntaxException e) {
			// ok
		}
	}
}
