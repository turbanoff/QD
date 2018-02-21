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
package com.devexperts.qd.qtp.blob;

import java.io.EOFException;
import java.io.IOException;

import com.devexperts.io.BufferedInput;
import com.devexperts.qd.DataRecord;
import com.devexperts.qd.QDLog;
import com.devexperts.qd.ng.RecordBuffer;
import com.devexperts.qd.qtp.*;

/**
 * Parses QTP messages in blob format from byte stream.
 * Blob format contains {@link MessageType#HISTORY_DATA} messages
 * (by default, changeable with {@link #readAs(MessageType) readAs} method)
 * for a single record and symbol that are specified in constructor.
 * The input for this parser must be configured with {@link #setInput(BufferedInput)} method
 * immediately after construction.
 *
 * @see AbstractQTPParser
 */
public class BlobQTPParser extends AbstractQTPParser {

	private final DataRecord record;
	private final int cipher;
	private final String symbol;

	/**
	 * Constructs parser with a specified record and symbol.
	 */
	public BlobQTPParser(DataRecord record, String symbol) {
		super(record.getScheme());
		this.record = record;
		this.cipher = record.getScheme().getCodec().encode(symbol);
		this.symbol = symbol;
	}

	@Override
	protected void parseImpl(BufferedInput in, MessageConsumer consumer) {
		try {
			RecordBuffer buf = nextRecordsMessage(consumer, MessageType.HISTORY_DATA);
			// Parsing loop
			long position = in.totalPosition();
			while (in.hasAvailable()) {
				try {
					buf.add(record, cipher, symbol).readDataFrom(in);
				} catch (EOFException e) {
					break; // record is not complete
				}
			}
			stats.updateIOReadRecordBytes(record.getId(), in.totalPosition() - position);
			stats.updateIOReadDataRecord();
		} catch (IOException e) {
			QDLog.log.error("Cannot parse data blob", e);
			consumer.handleCorruptedStream();			
		}
	}
}
