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
package com.devexperts.rmi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.GuardedBy;

import com.devexperts.logging.Logging;
import com.devexperts.rmi.task.*;
import com.devexperts.util.IndexedSet;

abstract class AbstractServiceDescriptorsProcessor implements RMIServiceDescriptorsListener, Runnable {
	private final Logging log = Logging.getLogging(getClass());

	private final Executor executor;

	@GuardedBy("this")
	private final IndexedSet<RMIServiceId, RMIServiceDescriptor> descriptors =
		IndexedSet.create(RMIServiceDescriptor.INDEXER_BY_SERVICE_ID);
	private final AtomicInteger scheduled = new AtomicInteger(0);

	protected AbstractServiceDescriptorsProcessor(Executor executor) {
		this.executor = executor;
	}

	@Override
	public void descriptorsUpdated(List<RMIServiceDescriptor> descriptors) {
		synchronized (this) {
			this.descriptors.addAll(descriptors);
		}
		// do it outside of lock
		boolean schedule = scheduled.getAndIncrement() == 0;
		if (RMIEndpointImpl.RMI_TRACE_LOG)
			log.trace("Update descriptors by " + this + ", descriptors=" + descriptors + ", schedule=" + schedule);
		if (schedule)
			executor.execute(this);
	}

	@Override
	public void run() {
		int oldScheduled;
		do {
			oldScheduled = scheduled.get();
			List<RMIServiceDescriptor> descriptors = takeDescriptors();
			if (RMIEndpointImpl.RMI_TRACE_LOG)
				log.trace("Process descriptors by " + this + ", descriptors=" + descriptors);
			process(descriptors);
		} while (!scheduled.compareAndSet(oldScheduled, 0));
	}

	protected abstract void process(List<RMIServiceDescriptor> descriptors);

	private synchronized List<RMIServiceDescriptor> takeDescriptors() {
		List<RMIServiceDescriptor> result = new ArrayList<>(this.descriptors);
		this.descriptors.clear();
		return result;
	}
}
