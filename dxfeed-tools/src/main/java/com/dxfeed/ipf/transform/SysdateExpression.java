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
package com.dxfeed.ipf.transform;

import java.util.Date;

class SysdateExpression extends Expression<Date> {
	static final Expression<Date> SYSDATE = new SysdateExpression();

	private SysdateExpression() {
		super(Date.class);
	}

	@Override
	Date evaluate(TransformContext ctx) {
		return ctx.getSysdate();
	}
}
