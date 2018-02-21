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
package com.dxfeed.ipf.live.test;

import java.util.*;

import com.dxfeed.ipf.*;
import com.dxfeed.ipf.live.InstrumentProfileCollector;
import junit.framework.TestCase;

public class InstrumentProfileCollectorTest extends TestCase {
	InstrumentProfileCollector collector = new InstrumentProfileCollector();

	public void testUpdateRemoved() {
		long update0 = collector.getLastUpdateTime();
		assertViewIdentities(); // empty

		// first instrument
		InstrumentProfile i1 = new InstrumentProfile();
		randomInstrument(i1, 20140618);
		Object g1 = new Object();
		collector.updateInstrumentProfiles(Arrays.asList(i1), g1);
		long update1 = collector.getLastUpdateTime();
		assertTrue(update1 > update0);
		assertViewIdentities(i1);

		// second instrument has same symbol but "REMOVED" type
		InstrumentProfile i2 = new InstrumentProfile();
		randomInstrument(i2, 20140618);
		i2.setType(InstrumentProfileType.REMOVED.name());
		Object g2 = new Object();
		collector.updateInstrumentProfiles(Arrays.asList(i2), g2);
		long update2 = collector.getLastUpdateTime();
		assertTrue(update2 > update1);
		assertViewIdentities(); // becomes empty

		// removed again (nothing shall change)
		InstrumentProfile i3 = new InstrumentProfile();
		randomInstrument(i3, 20140618);
		i3.setType(InstrumentProfileType.REMOVED.name());
		Object g3 = new Object();
		collector.updateInstrumentProfiles(Arrays.asList(i3), g2);
		long update3 = collector.getLastUpdateTime();
		assertTrue(update3 == update2);
		assertViewIdentities(); // becomes empty
	}

	public void testRandomInstrumentEqualsNoUpdate() {
		long update0 = collector.getLastUpdateTime();
		assertViewIdentities(); // empty

		// first instrument
		InstrumentProfile i1 = new InstrumentProfile();
		randomInstrument(i1, 20140617);
		Object g1 = new Object();
		collector.updateInstrumentProfiles(Arrays.asList(i1), g1);
		long update1 = collector.getLastUpdateTime();
		assertTrue(update1 > update0);
		assertViewIdentities(i1);

		// second instrument is equal to the first one (but different instance)
		InstrumentProfile i2 = new InstrumentProfile();
		randomInstrument(i2, 20140617);
		Object g2 = new Object();
		collector.updateInstrumentProfiles(Arrays.asList(i2), g2);
		long update2 = collector.getLastUpdateTime();
		assertTrue(update2 == update1);
		assertViewIdentities(i1);

		// try to remove old generation (nothing happens)
		collector.removeGenerations(Collections.singleton(g1));
		long update3 = collector.getLastUpdateTime();
		assertTrue(update3 == update1);
		assertViewIdentities(i1);

		// now test random field updates
		InstrumentProfile iUpdate = new InstrumentProfile(i1);
		long tPrev = update1;
		Object gPrev = g2;
		for (InstrumentProfileField field : InstrumentProfileField.values()) {
			if (field == InstrumentProfileField.SYMBOL)
				continue;
			if (field.isNumericField())
				field.setNumericField(iUpdate, field.getNumericField(iUpdate) + 1);
			else
				field.setField(iUpdate, field.getField(iUpdate) + "*");

			// send new instance with update
			InstrumentProfile iNew = new InstrumentProfile(iUpdate);
			Object gNew = new Object();
			collector.updateInstrumentProfiles(Collections.singletonList(iNew), gNew);
			long tNew = collector.getLastUpdateTime();
			assertTrue(tNew > tPrev);
			assertViewIdentities(iNew);
			tPrev = tNew;

			// try to remove old gen (nothing happens, already updated to new)
			collector.removeGenerations(Collections.singleton(gPrev));
			tNew = collector.getLastUpdateTime();
			assertTrue(tNew == tPrev);
			assertViewIdentities(iNew);
			gPrev = gNew;
		}
	}

	private void randomInstrument(InstrumentProfile ip, long seed) {
		Random r = new Random(seed);
		for (InstrumentProfileField field : InstrumentProfileField.values())
			if (field.isNumericField())
				field.setNumericField(ip, r.nextInt(10000));
			else
				field.setField(ip, String.valueOf(r.nextInt(10000)));
		for (int i = 0; i < 10; i++)
			ip.setField(String.valueOf(r.nextInt(10000)), String.valueOf(r.nextInt(10000)));
	}


	public void testGenerationRemove() {
		assertViewSymbols();

		// first instrument
		InstrumentProfile i1 = new InstrumentProfile();
		i1.setSymbol("INS_1");
		Object g1 = new Object();
		collector.updateInstrumentProfiles(Arrays.asList(i1), g1);
		assertViewSymbols("INS_1");

		// second instrument
		InstrumentProfile i2 = new InstrumentProfile();
		i2.setSymbol("INS_2");
		Object g2 = new Object();
		collector.updateInstrumentProfiles(Arrays.asList(i2), g2);
		assertViewSymbols("INS_1", "INS_2");

		// remove first generation
		collector.removeGenerations(Collections.singleton(g1));
		assertViewSymbols("INS_2");

		// remove second generation
		collector.removeGenerations(Collections.singleton(g2));
		assertViewSymbols();
	}

	private void assertViewSymbols(String... symbols) {
		Set<String> expected = new HashSet<>(Arrays.asList(symbols));
		Set<String> actual = new HashSet<>();
		for (InstrumentProfile instrument : collector.view()) {
			assertTrue("not removed", !instrument.getType().equals(InstrumentProfileType.REMOVED.name()));
			actual.add(instrument.getSymbol());
		}
		assertEquals(expected, actual);
	}

	private void assertViewIdentities(InstrumentProfile... ips) {
		Set<InstrumentProfile> expected = Collections.newSetFromMap(new IdentityHashMap<InstrumentProfile, Boolean>());
		expected.addAll(Arrays.asList(ips));
		Set<InstrumentProfile> actual = Collections.newSetFromMap(new IdentityHashMap<InstrumentProfile, Boolean>());
		for (InstrumentProfile instrument : collector.view()) {
			assertTrue("not removed", !instrument.getType().equals(InstrumentProfileType.REMOVED.name()));
			actual.add(instrument);
		}
		assertEquals(expected, actual);
	}
}
