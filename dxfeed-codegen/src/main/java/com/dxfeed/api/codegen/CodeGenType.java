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
package com.dxfeed.api.codegen;

import java.util.Collection;

interface CodeGenType {
	CodeGenExecutable getMethod(String name);

	Collection<CodeGenExecutable> getDeclaredExecutables();

	boolean isAssignableTo(Class<?> cls);

	boolean isSameType(Class<?> cls);

	ClassName getClassName();

	CodeGenType getSuperclass();

	boolean isPrimitive();

	Object getUnderlyingType(); // for logging purposes
}
