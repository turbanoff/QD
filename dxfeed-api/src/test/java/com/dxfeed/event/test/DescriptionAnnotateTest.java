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
package com.dxfeed.event.test;

import com.devexperts.annotation.Description;
import com.dxfeed.event.market.Quote;
import junit.framework.TestCase;

/**
 * Tests that events are successfully annotated with {@link Description}.
 */
public class DescriptionAnnotateTest extends TestCase {

	public void testEventsHaveDescriptionAnnotation() {
		Description description = Quote.class.getAnnotation(Description.class);
		assertNotNull(description);
	}
}
