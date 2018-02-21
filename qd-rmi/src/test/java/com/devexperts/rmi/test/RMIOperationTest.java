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
package com.devexperts.rmi.test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import com.devexperts.rmi.RMIOperation;
import com.dxfeed.promise.Promise;
import junit.framework.TestCase;

public class RMIOperationTest extends TestCase {

	interface Bar {
		float foo(int n, String str, List<Integer> lists, Thread... threads) throws IOException;
	}

	public void testOperationSignatures() {
		Method method;
		@SuppressWarnings("rawtypes")
		Class[] paramTypes = {int.class, String.class, List.class, Thread[].class};
		try {
			method = Bar.class.getMethod("foo", paramTypes);
		} catch (NoSuchMethodException e) {
			fail();
			return;
		}
		String serviceName = "com.devexperts.rmi.test.RMIOperationTest$Bar";
		List<RMIOperation<?>> ops = new ArrayList<>();
		ops.add(RMIOperation.valueOf(Bar.class, method));
		ops.add(RMIOperation.valueOf(serviceName, method));
		ops.add(RMIOperation.valueOf(serviceName, float.class, "foo", paramTypes));
		String signature = serviceName + "#foo(int,java.lang.String,java.util.List,[Ljava.lang.Thread;):float";

		for (RMIOperation<?> op : ops) {
			assertEquals(op.getSignature(), signature);
		}
	}

	interface Foo {
		Promise<Integer> bar(int a, int b);
	}

	public void testOperationWithPromise() throws NoSuchMethodException {
		Method method;
		try {
			method = Foo.class.getMethod("bar", int.class, int.class);
		} catch (NoSuchMethodException e) {
			fail();
			return;
		}
		RMIOperation<?> op = RMIOperation.valueOf(Foo.class.getName(), method);
		String signature =  Foo.class.getName() + "#bar(int,int):java.lang.Integer";
		assertEquals(op.getSignature(), signature);
	}

	interface Hack {
		Promise<Collection<String>> hack(Set<Integer> set);
	}

	public void testRawTypes() {
		Method method;
		try {
			method = Hack.class.getMethod("hack", Set.class);
		} catch (NoSuchMethodException e) {
			fail();
			return;
		}
		RMIOperation<?> op = RMIOperation.valueOf(Hack.class.getName(), method);
		String signature =  Hack.class.getName() + "#hack(java.util.Set):java.util.Collection";
		assertEquals(op.getSignature(), signature);
	}
}
