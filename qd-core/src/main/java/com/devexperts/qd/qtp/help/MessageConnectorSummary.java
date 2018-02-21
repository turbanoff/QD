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
package com.devexperts.qd.qtp.help;

import java.lang.annotation.*;

/**
 * Describes MessageConnector and its java bean properties.
 * This annotation is used to generate help summary about a MessageConnector
 * by Help tool.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MessageConnectorSummary {
	/**
	 * Short description of a MessageConnector.
	 */
	String info();
	/**
	 * Address format.
	 */
	String addressFormat();
}
