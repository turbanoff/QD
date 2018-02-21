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
package com.devexperts.qd.qtp.nio;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.*;

import com.devexperts.qd.qtp.ReconnectHelper;
import com.devexperts.qd.qtp.socket.SocketUtil;
import com.devexperts.util.LogUtil;

/**
 * Accepting connections thread.
 */
class NioAcceptor extends NioWorkerThread {

	private final ReconnectHelper reconnectHelper;
	private final ServerSocketChannel serverChannel;

	NioAcceptor(NioCore core) throws IOException {
		super(core, "Acceptor");
		reconnectHelper = new ReconnectHelper(core.connector.getReconnectDelay());
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(true);
	}

	boolean isConnected() {
		return serverChannel.socket().isBound();
	}

	protected void makeIteration() {
		if (!serverChannel.socket().isBound())
			try {
				reconnectHelper.sleepBeforeConnection();
				log.info("Trying to listen at " + LogUtil.hideCredentials(core.address));
				serverChannel.socket().bind(core.bindSocketAddress);
				log.info("Listening at " + LogUtil.hideCredentials(core.address));
			} catch (InterruptedException e) {
				return;
			} catch (IOException e) {
				if (!core.isClosed())
					log.error("Failed to listen at " + LogUtil.hideCredentials(core.address), e);
				return;
			}

		SocketChannel channel;
		try {
			channel = serverChannel.accept();
			log.info("Accepted client socket " + LogUtil.hideCredentials(SocketUtil.getAcceptedSocketAddress(channel.socket())));
		} catch (ClosedChannelException e) {
			core.close();
			return;
		} catch (IOException e) {
			log.error("Failed to accept client socket", e);
			return;
		}

		try {
			channel.configureBlocking(false);
			Socket socket = channel.socket();
			socket.setKeepAlive(true);
			socket.setTcpNoDelay(true);
			socket.setSoTimeout(core.connector.getSocketTimeout());

			core.registerConnection(new NioConnection(core, channel));
		} catch (Throwable t) {
			log.error("Failed to configure accepted client socket", t);
			try {
				channel.close();
			} catch (IOException e) {
				log.error("Failed to close client socket", e);
			}
		}
	}

	void close() {
		try {
			serverChannel.close();
			log.info("Stopped listening at " + LogUtil.hideCredentials(core.address));
		} catch (Throwable t) {
			log.error("Failed to close server socket at " + LogUtil.hideCredentials(core.address), t);
		}
		interrupt();
	}
}
