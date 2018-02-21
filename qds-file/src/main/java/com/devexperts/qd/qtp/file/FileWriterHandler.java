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
package com.devexperts.qd.qtp.file;

import com.devexperts.logging.Logging;
import com.devexperts.qd.DataScheme;
import com.devexperts.qd.ng.RecordBuffer;
import com.devexperts.qd.ng.RecordMode;
import com.devexperts.qd.qtp.*;
import com.devexperts.qd.stats.QDStats;

public class FileWriterHandler extends AbstractConnectionHandler<TapeConnector> {
	private static final Logging log = Logging.getLogging(FileWriterHandler.class);

	private final FileWriterImpl writer;
	private final MessageAdapter adapter;

	private final State state = new State();

	public FileWriterHandler(TapeConnector connector) {
		super(connector);
		MessageAdapter.Factory factory = MessageConnectors.retrieveMessageAdapterFactory(connector.getFactory());
		adapter = factory.createAdapter(connector.getStats().getOrCreate(QDStats.SType.CONNECTIONS));
		writer = new FileWriterImpl(connector.getAddress(), adapter.getScheme(), connector);
	}

	public void init() {
		adapter.setMessageListener(provider -> state.messagesAvailable());
		adapter.start();
		// note: open by itself cannot fail. The actual files are open when we start writing there.
		writer.open();
		subscribe();
	}

	private void subscribe() {
		RecordBuffer buf = RecordBuffer.getInstance(RecordMode.SUBSCRIPTION);
		DataScheme scheme = adapter.getScheme();
		for (int i = 0; i < scheme.getRecordCount(); i++) {
			buf.add(scheme.getRecord(i), scheme.getCodec().getWildcardCipher(), null);
		}
		adapter.processStreamAddSubscription(buf);
		buf.release();
	}

	@Override
	protected void doWork() throws InterruptedException {
		while (true) {
			if (isClosed())
				return; // bail out if closed
			state.awaitAvailable();
			if (isClosed())
				return; // bail out if closed
			state.messagesAreAvailable = false;
			adapter.retrieveMessages(writer); // retrieve all messages
			state.processed();
		}
	}

	void awaitProcessed() throws InterruptedException {
		state.awaitProcessed();
	}

	@Override
	protected void closeImpl(Throwable reason) {
		writer.close();
		try {
			adapter.close();
		} catch (Throwable t) {
			log.error("Failed to close adapter", t);
		}
		if (reason == null || reason instanceof RuntimeException || reason instanceof Error) {
			// QTP worker thread had already logged any unchecked exceptions
			log.info("Writing stopped");
		} else {
			log.error("Writing stopped", reason);
		}
	}

	private static class State {
		volatile boolean messagesAreAvailable = true;
		volatile boolean processed = false;

		void messagesAvailable() {
			if (messagesAreAvailable)
				return;
			notifyAvailableSync();
		}

		private synchronized void notifyAvailableSync() {
			if (!messagesAreAvailable) {
				messagesAreAvailable = true;
				processed = false;
				notifyAll();
			}
		}

		synchronized void processed() {
			if (!messagesAreAvailable) {
				processed = true;
				notifyAll();
			}
		}

		synchronized void awaitAvailable() throws InterruptedException {
			while (!messagesAreAvailable)
				wait();
		}

		synchronized void awaitProcessed() throws InterruptedException {
			while (!processed)
				wait();
		}
	}
}
