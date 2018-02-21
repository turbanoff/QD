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
package com.devexperts.util.test;

import java.util.Random;

import com.devexperts.util.MathUtil;
import junit.framework.TestCase;

public class MathUtilTest extends TestCase {
	private static final long P14 = 100000000000000L;

	public void testRoundDecimal() {
		assertFalse(1.1 - 0.2 == 0.9);
		assertTrue(MathUtil.roundDecimal(1.1 - 0.2) == 0.9);

		checkRound(0);
		checkRound(1);
		checkRound(-1);
		checkRound(Double.NaN);
		checkRound(Double.POSITIVE_INFINITY);
		checkRound(Double.NEGATIVE_INFINITY);
		checkRound(Double.MAX_VALUE);
		checkRound(-Double.MAX_VALUE);
		checkRound(111111111111L);
		checkRound(99999999999999L);
		checkRound(-99999999999999L);
		checkRound(0.99999999999999);
		checkRound(-0.99999999999999);
		checkRound(12345.123456789);
		checkRound(-12345.123456789);

		assertEquals(12345.123456789, MathUtil.roundDecimal(12345.1234567894));
		assertEquals(12345.123456790, MathUtil.roundDecimal(12345.1234567896));

		assertEquals(0.0, MathUtil.roundDecimal(Double.MIN_VALUE));
		assertEquals(-0.0, MathUtil.roundDecimal(-Double.MIN_VALUE));

		Random r = new Random(20090102);

		// check powers of 10 
		for (int power = 0; power <= 14; power++) {
			checkMantissaPower(r, P14, power);
			checkMantissaPower(r, 1, power);
			checkMantissaPower(r, -P14, power);
			checkMantissaPower(r, -1, power);
		}

		// check 999..9 * 10^n
		for (int power = 0; power <= 14; power++) {
			checkMantissaPower(r, P14 - 1, power);
			checkMantissaPower(r, -P14 + 1, power);
		}

		// check randoms
		for (int i = 0; i < 1000; i++) {
			long mantissa = r.nextLong() % P14;
			int power = r.nextInt(15);
			checkMantissaPower(r, mantissa, power);
		}
	}

	private void checkMantissaPower(Random r, long mantissa, int power) {
		long divisor = 1;
		for (int j = 0; j < power; j++) {
			divisor *= 10;
		}
		double v = (double)mantissa / divisor;
		checkRound(v);
		if (mantissa / (P14 / 10) != 0) {
			assertEquals(v, MathUtil.roundDecimal(v + (r.nextInt(9) - 4) * 0.1 / divisor));
		}
	}

	private void checkRound(double x) {
		assertEquals(x, MathUtil.roundDecimal(x));
	}

	public void testIntDivRem() {
		int[] numbers = {Integer.MIN_VALUE, -5, Integer.MAX_VALUE - 10};
		for (int a : numbers)
			for (int b : numbers)
				for (int i = 0; i < 10; i++)
					for (int j = 0; j < 10; j++)
						checkIntDivRem(a + i, b + j);
	}

	public void testLongDivRem() {
		long[] numbers = {Long.MIN_VALUE, Integer.MIN_VALUE - 5L, -5, Integer.MAX_VALUE - 5L, Long.MAX_VALUE - 10};
		for (long a : numbers)
			for (long b : numbers)
				for (long i = 0; i < 10; i++)
					for (long j = 0; j < 10; j++)
						checkLongDivRem(a + i, b + j);
	}

	private void checkIntDivRem(int a, int b) {
		if (b == 0) {
			try {
				MathUtil.div(a, b);
				fail();
			} catch (ArithmeticException e) {}
			try {
				MathUtil.rem(a, b);
				fail();
			} catch (ArithmeticException e) {}
		} else {
			int d = MathUtil.div(a, b);
			int r = MathUtil.rem(a, b);
			// if a == Integer.MIN_VALUE and b == -1 then d == Integer.MIN_VALUE due to overflow and r == 0
			// although mathematically incorrect (must be d == -Integer.MIN_VALUE) it is still true that a == b * d + r due to overflow
			if (a != Integer.MIN_VALUE || b != -1)
				assertTrue((double)a == (double)b * (double)d + (double)r); // check for overflows
			assertTrue(a == b * d + r);
			assertTrue(r >= 0);
			assertTrue(b == Integer.MIN_VALUE || r < Math.abs(b));
		}
	}

	private void checkLongDivRem(long a, long b) {
		if (b == 0) {
			try {
				MathUtil.div(a, b);
				fail();
			} catch (ArithmeticException e) {}
			try {
				MathUtil.rem(a, b);
				fail();
			} catch (ArithmeticException e) {}
		} else {
			long d = MathUtil.div(a, b);
			long r = MathUtil.rem(a, b);
			// if a == Long.MIN_VALUE and b == -1 then d == Long.MIN_VALUE due to overflow and r == 0
			// although mathematically incorrect (must be d == -Long.MIN_VALUE) it is still true that a == b * d + r due to overflow
			if (a != Long.MIN_VALUE || b != -1)
				assertTrue(Math.abs((double)b * (double)d + (double)r - (double)a) <= 5); // check for overflows
			assertTrue(a == b * d + r);
			assertTrue(r >= 0);
			assertTrue(b == Long.MIN_VALUE || r < Math.abs(b));
		}
	}
}
