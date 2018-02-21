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
package com.devexperts.rmi.task;

import java.util.List;

/**
 * The listener for notification of {@link RMIObservableServiceDescriptors},
 * that some implementation of service has changed some properties.
 */
public interface RMIServiceDescriptorsListener {
	/**
	 * This method provides notification for {@link RMIObservableServiceDescriptors},
	 * that implementations of service has changed.
	 * <b>This method must not block and it shall avoid synchronization</b>.
	 * It is being invoked under many layers of internal locks (can deadlock otherwise).
	 *
	 * @param descriptors the list of updated service descriptors.
	 */
	void descriptorsUpdated(List<RMIServiceDescriptor> descriptors);
}
