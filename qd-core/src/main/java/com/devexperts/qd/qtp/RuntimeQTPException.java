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
package com.devexperts.qd.qtp;

import java.io.IOException;

/**
 * Wraps checked {@link IOException} that happens in {@link AbstractQTPComposer},
 * {@link AbstractQTPParser} or their descendants.
 */
public class RuntimeQTPException extends RuntimeException {
	/**
	 * Creates new QTP runtime IO exception with a specified cause.
	 */
	public RuntimeQTPException(Throwable cause) {
		super(cause);
	}
}
