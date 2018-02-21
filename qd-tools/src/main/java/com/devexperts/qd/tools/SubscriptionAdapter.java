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
package com.devexperts.qd.tools;

import com.devexperts.qd.*;
import com.devexperts.qd.qtp.*;
import com.devexperts.qd.stats.QDStats;

/**
 * Specialized {@link AgentAdapter AgentAdapter} that receives subscription from uplink, but does not
 * deliver any data back. Data is delivered to a separately specified {@link ConnectionProcessor}.
 */
class SubscriptionAdapter extends AgentAdapter {

	public static class Factory extends AgentAdapter.Factory {
		private final MessageListener listener;
		private final MessageVisitor dataVisitor;

		public Factory(QDEndpoint endpoint, SubscriptionFilter filter, MessageListener listener, MessageVisitor dataVisitor) {
			super(endpoint, filter);
			this.listener = listener;
			this.dataVisitor = dataVisitor;
		}

		@Override
		public MessageAdapter createAdapter(QDStats stats) {
			return new SubscriptionAdapter(endpoint, ticker, stream, history, getFilter(), stats, listener, dataVisitor);
		}
	}

	private final MessageVisitor dataVisitor;
	private final MessageListener listener;

	SubscriptionAdapter(QDEndpoint endpoint, QDTicker ticker, QDStream stream, QDHistory history,
		SubscriptionFilter filter, QDStats stats, MessageListener listener, MessageVisitor dataVisitor)
	{
		super(endpoint, ticker, stream, history, filter, stats);
		this.dataVisitor = dataVisitor;
		this.listener = listener;
	}

	@Override
	protected QDAgent.Builder createAgentBuilder(QDCollector collector, SubscriptionFilter filter, String keyProperties) {
		return super.createAgentBuilder(collector, filter, keyProperties).withVoidRecordListener(dataVisitor == null);
	}

	@Override
	protected void notifyListener() {
		super.notifyListener();
		if (listener != null)
			listener.messagesAvailable(this);
	}

	@Override
	public boolean retrieveMessages(MessageVisitor visitor) {
		if (visitor == dataVisitor) {
			//noinspection StatementWithEmptyBody
			while (super.retrieveDataMessages(visitor)) { // get data messages to dataVisitor
				// process until everything is processed
			}
			return false;
		}
		return super.retrieveMessages(visitor);
	}

	@Override
	protected boolean retrieveDataMessages(MessageVisitor visitor) {
		return false; // do not retrieve data messages to standard consumers (socket writer thread)
	}

	@Override
	public boolean isProtocolDescriptorCompatible(ProtocolDescriptor desc) {
		for (QDContract contract : getEndpoint().getContracts())
			if (desc.canSend(MessageType.forAddSubscription(contract)))
				return true;
		return false;
	}
}
