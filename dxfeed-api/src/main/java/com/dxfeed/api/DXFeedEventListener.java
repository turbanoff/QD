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
package com.dxfeed.api;

import java.util.List;

/**
 * The listener interface for receiving events of the specified type {@code E}.
 *
 * @param <E> the type of events.
 */
@FunctionalInterface
public interface DXFeedEventListener<E> {
	/**
	 * Invoked when events of type {@code E} are received.
	 *
	 * @param events the list of received events. 
	 */
	public void eventsReceived(List<E> events);
}
