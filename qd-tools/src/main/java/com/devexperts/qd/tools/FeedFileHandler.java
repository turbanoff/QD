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

import java.io.*;

import com.devexperts.logging.Logging;
import com.devexperts.qd.QDLog;
import com.devexperts.qd.SubscriptionFilter;
import com.devexperts.qd.qtp.*;
import com.devexperts.qd.stats.QDStats;
import com.devexperts.util.LogUtil;

class FeedFileHandler {
	private final Logging log = QDLog.log;

	private final QDEndpoint endpoint;
	private final SubscriptionFilter filter;
	private final File file;
	private final File backupFile;
	private final File parentFile;

	FeedFileHandler(QDEndpoint endpoint, String file_name, SubscriptionFilter filter) {
		this.endpoint = endpoint;
		this.filter = filter;
		this.file = new File(file_name);
		this.backupFile = new File(file_name + ".bak");
		this.parentFile = file.getParentFile() != null ? file.getParentFile() : new File(".");
	}

	public void readFile() throws IOException {
		MessageAdapter adapter = new DistributorAdapter(endpoint.getTicker(), endpoint.getStream(), endpoint.getHistory(), filter, QDStats.VOID);
		try {
			adapter.start();
			if (file.exists()) {
				readFileInternal(file, adapter);
			} else if (backupFile.exists()) {
				readFileInternal(backupFile, adapter);
			} else
				log.info("Storage file " + LogUtil.hideCredentials(file) + " is not found -- starting with empty storage");
		} finally {
			adapter.close();
		}
	}

	private void readFileInternal(File file, MessageAdapter adapter) throws IOException {
		log.info("Reading storage from file " + LogUtil.hideCredentials(file));
		long time = System.currentTimeMillis();
		try (InputStream in = new FileInputStream(file)) {
			InputStreamParser parser = new InputStreamParser(endpoint.getScheme());
			parser.init(in);
			parser.parse(adapter);
		}
		time = System.currentTimeMillis() - time;
		log.info("Done reading in " + time + " ms");
	}

	public void writeFile() {
		log.info("Writing storage to file " + LogUtil.hideCredentials(backupFile));
		try {
			long time = System.currentTimeMillis();
			parentFile.mkdirs();
			OutputStreamComposer composer;
			try (OutputStream out = new FileOutputStream(backupFile)) {
				composer = new OutputStreamComposer(endpoint.getScheme());
				composer.init(out, filter);
				composer.setOptSet(ProtocolOption.SUPPORTED_SET);
				composer.visitDescribeProtocol(ProtocolDescriptor.newSelfProtocolDescriptor("snapshot"));
				composer.composeEndpoint(endpoint);
			}
			time = System.currentTimeMillis() - time;
			log.info("Written " + composer.getRecordCounter() + " records in " + time + " ms. Renaming to file " + LogUtil.hideCredentials(file));
			file.delete();
			if (!backupFile.renameTo(file))
				log.error("Failed to rename file");
		} catch (IOException | RuntimeException e) {
			log.error("Failed to write", e);
		} finally {
			backupFile.delete();
		}
	}
}
