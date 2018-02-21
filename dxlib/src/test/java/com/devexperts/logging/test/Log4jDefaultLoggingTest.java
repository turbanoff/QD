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
package com.devexperts.logging.test;

import com.devexperts.logging.Logging;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class Log4jDefaultLoggingTest {

	@Test
	public void testDevexpertsLoggingDefaultLevelIsDebug() {
		System.getProperties().setProperty(Logging.LOG_CLASS_NAME, "com.devexperts.logging.Log4jLogging");
		Logging log = Logging.getLogging(Log4jDefaultLoggingTest.class);
		assertTrue(log.debugEnabled());
	}
}