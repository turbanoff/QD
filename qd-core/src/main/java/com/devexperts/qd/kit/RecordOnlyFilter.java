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
package com.devexperts.qd.kit;

import com.devexperts.qd.*;
import com.devexperts.qd.spi.QDFilterContext;
import com.devexperts.util.InvalidFormatException;

/**
 * Subscription filter that filters only based on the record name. Any concrete implementation
 * of this class gets a performance optimization from {@link com.devexperts.qd.kit.CompositeFilters}.
 * All filters that extend this class are {@link StableSubscriptionFilter stable} by definition.
 */
public abstract class RecordOnlyFilter extends QDFilter {
	// ------- static factory -------

	/**
	 * Parses a given specification as record-only filter for a given scheme.
	 * Here, instead of ":&lt;record-name&gt;" pattern filter to specify a record filter,
	 * any mention of a "&lt;record-name&gt;" is considered to be a record filter.
	 *
	 * @param spec the filter specification.
	 * @param scheme the scheme.
	 * @return filter.
	 * @throws InvalidFormatException if spec is invalid.
	 */
	public static RecordOnlyFilter valueOf(String spec, DataScheme scheme) {
		if (spec.equals("*") || spec.equals("all"))
			return new Anything(scheme);
		SubscriptionFilter filter = CompositeFilters.getFactory(scheme).createFilter(spec, QDFilterContext.RECORD_ONLY);
		if (filter instanceof RecordOnlyFilter)
			return (RecordOnlyFilter)filter;
		throw new InvalidFormatException("\"" + spec + "\" does not specify a list of records");
	}

	// ------- instance -------

	protected RecordOnlyFilter(DataScheme scheme) {
		super(scheme);
		if (scheme == null)
			throw new NullPointerException("scheme must be specified for record filter");
	}

	@Override
	public final boolean accept(QDContract contract, DataRecord record, int cipher, String symbol) {
		return acceptRecord(record);
	}

	public abstract boolean acceptRecord(DataRecord record);

	@Override
	public final Kind getKind() {
		return Kind.RECORD_ONLY;
	}

	/**
	 * This final implementation always returns {@code true}.
	 */
	@Override
	public final boolean isStable() {
		return true;
	}

	/**
	 * This final implementation always returns {@code false}.
	 */
	@Override
	public final boolean isDynamic() {
		return false;
	}

	/**
	 * This final implementation always returns {@code true}.
	 */
	@Override
	public final boolean isFast() {
		return true;
	}

	/**
	 * This final implementation always returns this.
	 */
	@Override
	public final QDFilter toStableFilter() {
		return this;
	}

	private static class Anything extends RecordOnlyFilter {
		private Anything(DataScheme scheme) {
			super(scheme);
		}

		@Override
		public boolean acceptRecord(DataRecord record) {
			return true;
		}

		@Override
		public String toString() {
			return ":*";
		}
	}
}
