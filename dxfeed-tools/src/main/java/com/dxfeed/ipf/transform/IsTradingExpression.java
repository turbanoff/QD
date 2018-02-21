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

import java.io.IOException;

import com.dxfeed.schedule.Schedule;

class IsTradingExpression extends Expression<Boolean> {
	private final Object parameter;

	IsTradingExpression(Compiler compiler) throws IOException {
		super(Boolean.class);
		compiler.skipToken('(');
		parameter = compiler.readExpression();
		compiler.skipToken(')');
		Compiler.getDate(Compiler.newTestContext(), parameter); // Early check of expression constraints (data types)
	}

	@Override
	Boolean evaluate(TransformContext ctx) {
		return Schedule.getInstance(ctx.currentProfile().getTradingHours()).getDayById(Compiler.getDayId(Compiler.getDate(ctx, parameter))).isTrading();
	}
}
