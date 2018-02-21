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

import java.io.IOException;

import com.devexperts.io.*;
import junit.framework.TestCase;

/**
 * This is test fixture and test routines for CompactInt class.
 */
public class CompactIntTest extends TestCase {
	public CompactIntTest(String name) {
		super(name);
	}

	private ByteArrayInput in;
	private ByteArrayOutput out;

	public void testInt() {
		for (int i = 0; i >= 0; i += Math.max(1, i / 1000)) {
			helpTestInt(i);
			helpTestInt(-i);
		}
	}

	public void testLong() {
		for (long i = 0; i >= 0; i += Math.max(1, i / 1000)) {
			helpTestLong(i);
			helpTestLong(-i);
		}
	}

	public void helpTestInt(int n) {
		try {
			out.setPosition(0);
			in.setPosition(0);
			out.writeCompactInt(n);
			in.setLimit(out.getPosition());
			int x = IOUtil.readCompactInt(in);
			assertEquals(out.getPosition(), in.getPosition());
			assertEquals(out.getPosition(), IOUtil.getCompactLength(n));
			if (x != n)
				fail("error: [" + n + " = " + Integer.toHexString(n) + "] became [" + x + " = " + Integer.toHexString(x) + "]");
			int p = out.getPosition();
			out.writeCompactInt(n);
			assertEquals(out.getPosition() - p, p);
			for (int j = 0; j < p; j++)
				assertEquals(out.getBuffer()[j], out.getBuffer()[p + j]);
			in.setLimit(out.getPosition());
			x = in.readCompactInt();
			assertEquals(out.getPosition(), in.getPosition());
			if (x != n)
				fail("error: [" + n + " = " + Integer.toHexString(n) + "] became [" + x + " = " + Integer.toHexString(x) + "]");
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	public void helpTestLong(long n) {
		try {
			out.setPosition(0);
			in.setPosition(0);
			IOUtil.writeCompactLong(out, n);
			in.setLimit(out.getPosition());
			long x = IOUtil.readCompactLong(in);
			assertEquals(out.getPosition(), in.getPosition());
			if (x != n)
				fail("error: [" + n + " = " + Long.toHexString(n) + "] became [" + x + " = " + Long.toHexString(x) + "]");
			int p = out.getPosition();
			out.writeCompactLong(n);
			assertEquals(out.getPosition() - p, p);
			for (int j = 0; j < p; j++)
				assertEquals(out.getBuffer()[j], out.getBuffer()[p + j]);
			in.setLimit(out.getPosition());
			x = in.readCompactLong();
			assertEquals(out.getPosition(), in.getPosition());
			if (x != n)
				fail("error: [" + n + " = " + Long.toHexString(n) + "] became [" + x + " = " + Long.toHexString(x) + "]");
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	protected void setUp() {
		out = new ByteArrayOutput(1024);
		in = new ByteArrayInput(out.getBuffer());
	}

	protected void tearDown() {
		in = null;
		out = null;
	}
}
