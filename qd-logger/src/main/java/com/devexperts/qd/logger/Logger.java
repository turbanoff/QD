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
package com.devexperts.qd.logger;

import com.devexperts.qd.DataRecord;
import com.devexperts.qd.QDLog;

public class Logger {
	private final String name;
	private final QDLog log;

	private final String header;

	public Logger(QDLog log, String name) {
		if (log == null)
			throw new NullPointerException("log");
		if (name == null)
			throw new NullPointerException("name");
		this.log = log;
		this.name = name;
		this.header = name.isEmpty() ? "" : "{" + name + "} ";
	}

	static void appendRecord(StringBuilder sb, DataRecord record, int cipher, String symbol) {
		sb.append(record.getScheme().getCodec().decode(cipher, symbol));
		sb.append(':');
		sb.append(record.getName());
	}

	static void appendTime(StringBuilder sb, DataRecord record, int time_hi) {
		if (record.hasTime() && time_hi != 0) {
			sb.append('@');
			sb.append(record.getIntField(0).toString(time_hi));
		}
	}

	void debug(Object msg) {
		log.debug(header + msg);
	}

	Logger child(String s) {
		return new Logger(log, name.isEmpty() ? s : (name + "." + s));
	}

	public String toString() {
		String cn = getClass().getName();
		int i = cn.lastIndexOf(".");
		return cn.substring(i + 1) + "{" + name + "}";
	}
}
