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

import java.util.EnumMap;

import com.devexperts.qd.*;
import com.devexperts.qd.impl.AbstractCollectorBuilder;
import com.devexperts.qd.impl.stripe.StripedFactory;
import com.devexperts.util.SystemProperties;

public class LoggedQDFactory extends QDFactory {
	private static final String LOGGER_PROPERTY = "com.devexperts.qd.logger";

	public static QDFactory getInstance() {
		QDFactory factory = StripedFactory.getInstance();
		try {
			if (SystemProperties.getProperty(LOGGER_PROPERTY, null) != null)
				factory = new LoggedQDFactory(new Logger(QDLog.getInstance(), ""), factory);
		} catch (SecurityException e) {
			// ignore
		}
		return factory;
	}

	private final Logger log;
	private final QDFactory delegate;

	private final EnumMap<QDContract, Counter> counter = new EnumMap<>(QDContract.class);

	public LoggedQDFactory(Logger log, QDFactory delegate) {
		this.log = log;
		this.delegate = delegate;
		for (QDContract contract : QDContract.values()) {
			counter.put(contract, new Counter());
		}
	}

	@Override
	public QDCollector.Builder<?> collectorBuilder(QDContract contract) {
		return new AbstractCollectorBuilder(contract) {
			@Override
			public QDCollector build() {
				int n = counter.get(contract).next();
				log.debug("create" + contract + "(" + getScheme() + ", " + getStats() + ") = #" + n);
				switch (contract) {
				case TICKER:
					return new LoggedTicker(log.child("ticker" + n), (QDTicker)buildViaDelegate(), this);
				case STREAM:
					return new LoggedStream(log.child("stream" + n), (QDStream)buildViaDelegate(), this);
				case HISTORY:
					return new LoggedHistory(log.child("history" + n), (QDHistory)buildViaDelegate(), this);
				default:
					throw new IllegalArgumentException();
				}
			}

			private QDCollector buildViaDelegate() {
				return delegate.collectorBuilder(getContract()).copyFrom(this).build();
			}
		};
	}

	@Override
	public QDAgent.Builder createVoidAgentBuilder(QDContract contract, DataScheme scheme) {
		return delegate.createVoidAgentBuilder(contract, scheme);
	}
}
