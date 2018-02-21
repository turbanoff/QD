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
package com.devexperts.qd;

import com.devexperts.qd.ng.RecordListener;

/**
 * The <code>SubscriptionListener</code> is used to receive notifications about
 * subscription availability in the corresponding subscription providers.
 * <p>
 * Generally, the notification is issued only if provider transforms from 'empty' state
 * into 'available' state at some moment after transition. In other cases notification
 * is not issued, though certain exclusions exist. The notification is issued in a state
 * that allows listener to perform potentially costly operations like event scheduling,
 * synchronization, or farther notification.
 *
 * <h3>Legacy interface</h3>
 *
 * <b>FUTURE DEPRECATION NOTE:</b>
 *    New code shall not implement this interface.
 *    Implement {@link RecordListener}.
 *    New code is also discouraged from using this interface unless it is need for interoperability with
 *    legacy code. Various legacy APIs will be gradually migrated to NG interfaces and classes.
 */
public interface SubscriptionListener {
	/**
	 * This listener immediately retrieves and discards subscription from provider.
	 */
	public static final SubscriptionListener VOID = Void.VOID;

	/**
	 * Notifies this listener that subscription is available in the specified
	 * subscription provider.
	 */
	public void subscriptionAvailable(SubscriptionProvider provider);
}
