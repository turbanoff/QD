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
package com.devexperts.qd.tools;

/**
 * This exception signals to print a detailed help when tool is started without arguments.
 */
class NoArgumentsException extends BadToolParametersException {
	private static final long serialVersionUID = 0;

	NoArgumentsException() {
		super("No arguments");
	}
}
