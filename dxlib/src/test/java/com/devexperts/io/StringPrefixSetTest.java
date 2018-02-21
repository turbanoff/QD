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
package com.devexperts.io;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class StringPrefixSetTest extends TestCase {

	StringPrefixSet list;
	//Serialization List tests

	public void testIllegalArgumentException() {
		throwAndCatchException("*.devexperts.rmi.RMIEndpoint");
		throwAndCatchException("*.devexperts.*");
		throwAndCatchException("com.*.rmi.RMIEndpoint");

		throwAndCatchException("*;com.*.rmi.RMIEndpoint");
		throwAndCatchException("*;com.this;*.devexperts.rmi.RMIEndpoint");
		throwAndCatchException("*;com.this;*.devexperts.*");
		throwAndCatchException("*;com.this;com.*.rmi.RMIEndpoint");

		throwAndCatchException("*.devexperts.rmi.RMIEndpoint;*;com.this");
		throwAndCatchException("*.devexperts.*;*;com.this");
		throwAndCatchException("com.*.rmi.RMIEndpoint;*;com.this");

		throwAndCatchException("*", "com.this", "*.devexperts.rmi.RMIEndpoint;*", "com.this");
		throwAndCatchException("*", "com.this", "*.devexperts.*;*", "com.this");
		throwAndCatchException("*", "com.this", "com.*.rmi.RMIEndpoint;*", "com.this");
	}

	private void throwAndCatchException(String... classes) {
		try {
			StringPrefixSet.valueOf(Arrays.asList(classes));
			fail();
		} catch (RuntimeException e) {
			if (!(e instanceof IllegalArgumentException))
				fail();
		}
	}

	public void testAnythingAndNothing() {
		list = StringPrefixSet.valueOf("*");
		assertEquals(list, StringPrefixSet.ANYTHING_SET);
		list = StringPrefixSet.valueOf(Arrays.asList(new String[]{"com.this", "String", "*", "java.lang.Math", "com.devexperts.*"}));
		assertEquals(list, StringPrefixSet.ANYTHING_SET);
		assertTrue(list.accept(String.class.getName()));
		assertTrue(list.accept(List.class.getName()));
		assertTrue(list.accept(Marshalled.class.getName()));


		list = StringPrefixSet.valueOf(Arrays.asList(new String[]{"com.this", ""}));
		assertFalse(list.equals(StringPrefixSet.NOTHING_SET));
		list = StringPrefixSet.valueOf("");
		assertEquals(list, StringPrefixSet.NOTHING_SET);
		assertFalse(list.accept(String.class.getName()));
		assertFalse(list.accept(List.class.getName()));
		assertFalse(list.accept(Marshalled.class.getName()));
	}

	public void testFullNames() {
		list = StringPrefixSet.valueOf(String.class.getName());
		assertTrue(list.accept(String.class.getName()));
		assertFalse(list.accept(List.class.getName()));

		list = StringPrefixSet.valueOf(Marshalled.class.getName());
		assertTrue(list.accept(Marshalled.class.getName()));
		assertFalse(list.accept(String.class.getName()));

		list = StringPrefixSet.valueOf(Arrays.asList(new String[]{String.class.getName(), Marshalled.class.getName(),
			Object.class.getName(), Marshalled[].class.getName()}));
		assertTrue(list.accept(String.class.getName()));
		assertFalse(list.accept(List.class.getName()));
		assertTrue(list.accept(Marshalled.class.getName()));
		assertTrue(list.accept(Object.class.getName()));
	}

	public void testPrefixes() {
		list = StringPrefixSet.valueOf("com.devexperts.io.*");
		assertFalse(list.accept("java.lang.String"));
		assertFalse(list.accept("java.util.List"));
		assertTrue(list.accept("com.devexperts.io.Marshalled"));
		assertTrue(list.accept("com.devexperts.io.IOUtil"));
		assertFalse(list.accept("com.devexperts.util.SerialContextClass"));
		assertFalse(list.accept("com.devexperts.Something"));
		assertTrue(list.accept("com.devexperts.io.pack.Class"));


		list = StringPrefixSet.valueOf("com.devexperts.*");
		assertFalse(list.accept("java.lang.String"));
		assertFalse(list.accept("java.util.List"));
		assertTrue(list.accept("com.devexperts.io.Marshalled"));
		assertTrue(list.accept("com.devexperts.io.IOUtil"));
		assertTrue(list.accept("com.devexperts.util.SerialContextClass"));
		assertTrue(list.accept("com.devexperts.Something"));
		assertFalse(list.accept("com.All"));
		assertTrue(list.accept("com.devexperts.io.pack.Class"));


		list = StringPrefixSet.valueOf(";", "com.devexperts.io.*;com.devexperts.util.*");
		assertFalse(list.accept("java.lang.String"));
		assertFalse(list.accept("java.util.List"));
		assertTrue(list.accept("com.devexperts.io.Marshalled"));
		assertTrue(list.accept("com.devexperts.io.IOUtil"));
		assertTrue(list.accept("com.devexperts.util.SerialContextClass"));
		assertFalse(list.accept("com.devexperts.Something"));
		assertFalse(list.accept("com.devexperts.annotation.Description"));
		assertTrue(list.accept("com.devexperts.io.pack.Class"));
	}


	public void testPrefixesAndFullNames() {
		list = StringPrefixSet.valueOf(";",
			"com.devexperts.io.*;com.devexperts.util.*;java.util.List;java.lang.*;java.util.concurrent.Executor");
		assertTrue(list.accept("java.lang.String"));
		assertTrue(list.accept("java.util.List"));
		assertTrue(list.accept("com.devexperts.io.Marshalled"));
		assertTrue(list.accept("com.devexperts.io.IOUtil"));
		assertTrue(list.accept("com.devexperts.util.SerialContextClass"));
		assertFalse(list.accept("com.devexperts.Something"));
		assertTrue(list.accept("com.devexperts.io.pack.Class"));
		assertTrue(list.accept("java.util.concurrent.Executor"));
		assertFalse(list.accept("java.util.concurrent.ExecutorService"));

	}

}
