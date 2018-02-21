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

import java.util.List;
import java.util.Set;

import com.dxfeed.ipf.InstrumentProfileField;

class RetainFieldsStatement extends Statement {
	private final List<InstrumentProfileField> removeStandard;
	private final Set<String> retainCustom;

	RetainFieldsStatement(Compiler compiler, List<InstrumentProfileField> removeStandard, Set<String> retainCustom) {
		super(compiler);
		this.removeStandard = removeStandard;
		this.retainCustom = retainCustom;
	}

	@Override
	ControlFlow execute(TransformContext ctx) {
		boolean modified = false;
		for (int i = 0; i < removeStandard.size(); i++) {
			InstrumentProfileField ipf = removeStandard.get(i);
			if (ipf.isNumericField()) {
				if (ipf.getNumericField(ctx.currentProfile()) != 0) {
					ipf.setNumericField(ctx.copyProfile(), 0);
					modified = true;
				}
			} else {
				if (ipf.getField(ctx.currentProfile()).length() != 0) {
					ipf.setField(ctx.copyProfile(), "");
					modified = true;
				}
			}
		}
		List<String> customFields = ctx.customFieldsForRetainFieldsStatement;
		customFields.clear();
		ctx.currentProfile().addNonEmptyCustomFieldNames(customFields);
		for (int i = 0; i < customFields.size(); i++)
			if (!retainCustom.contains(customFields.get(i))) {
				ctx.copyProfile().setField(customFields.get(i), "");
				modified = true;
			}
		if (modified)
			incModificationCounter(ctx);
		return ControlFlow.NORMAL;
	}
}
