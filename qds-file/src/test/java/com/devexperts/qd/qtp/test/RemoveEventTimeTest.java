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
package com.devexperts.qd.qtp.test;

import com.devexperts.io.ByteArrayInput;
import com.devexperts.io.ByteArrayOutput;
import com.devexperts.qd.*;
import com.devexperts.qd.kit.*;
import com.devexperts.qd.ng.*;
import com.devexperts.qd.qtp.*;
import com.devexperts.qd.qtp.text.TextQTPComposer;
import com.devexperts.qd.qtp.text.TextQTPParser;
import junit.framework.TestCase;

public class RemoveEventTimeTest extends TestCase {
	private static final DataRecord RECORD = new DefaultRecord(0, "TradeHistory", true, new DataIntField[] {
		new CompactIntField(0, "TradeHistory.Time"),
		new CompactIntField(1, "TradeHistory.Sequence"),
		new CompactIntField(2, "TradeHistory.Price"),
		new CompactIntField(3, "TradeHistory.Size")
	}, new DataObjField[0]);
	private static final DataScheme SCHEME = new DefaultScheme(PentaCodec.INSTANCE, RECORD);
	private static final String SYMBOL1 = "SYMBOL1";
	private static final String SYMBOL2 = "SYMBOL2";

	public void testBinary() {
		BinaryQTPComposer composer = new BinaryQTPComposer(SCHEME, true);
		BinaryQTPParser parser = new BinaryQTPParser(SCHEME);
		composer.setOptSet(ProtocolOption.SUPPORTED_SET);
		check(composer, parser);
	}

	public void testBinaryWithEventTimeFields() {
		BinaryQTPComposer composer = new BinaryQTPComposer(SCHEME, true);
		BinaryQTPParser parser = new BinaryQTPParser(SCHEME);
		composer.setOptSet(ProtocolOption.SUPPORTED_SET);
		composer.setWriteEventTimeSequence(true);
		check(composer, parser);
	}

	public void testText() {
		TextQTPComposer composer = new TextQTPComposer(SCHEME);
		TextQTPParser parser = new TextQTPParser(SCHEME);
		composer.setOptSet(ProtocolOption.SUPPORTED_SET);
		check(composer, parser);
	}

	public void testTextWithTimeFields() {
		TextQTPComposer composer = new TextQTPComposer(SCHEME);
		TextQTPParser parser = new TextQTPParser(SCHEME);
		composer.setOptSet(ProtocolOption.SUPPORTED_SET);
		composer.setWriteEventTimeSequence(true);
		check(composer, parser);
	}

	private void check(AbstractQTPComposer composer, AbstractQTPParser parser) {
		RecordBuffer bufOut = RecordBuffer.getInstance(RecordMode.FLAGGED_DATA);
		addData(bufOut, SYMBOL1, 0, 42, 80); // no flags
		addData(bufOut, SYMBOL1, EventFlag.REMOVE_EVENT.flag(), 42, 80); // flags
		addData(bufOut, SYMBOL2, 0, 142, 180); // not flags
		addData(bufOut, SYMBOL2, EventFlag.REMOVE_EVENT.flag(), 142, 180); // flags

		ByteArrayOutput out = new ByteArrayOutput();
		composer.setOutput(out);
		composer.visitData(bufOut, MessageType.TICKER_DATA);

		RecordBuffer bufIn = RecordBuffer.getInstance(RecordMode.FLAGGED_DATA);
		ByteArrayInput in = new ByteArrayInput(out.toByteArray());
		parser.setInput(in);
		parser.parse(new MessageConsumerAdapter() {
			@Override
			public void processTickerData(DataIterator iterator) {
				bufIn.processData(iterator);
			}
		});

		assertEquals(bufOut.size(), bufIn.size());
		assertData(bufIn, SYMBOL1, 0, 42, 80);
		assertData(bufIn, SYMBOL1, EventFlag.REMOVE_EVENT.flag(), 42, 80);
		assertData(bufIn, SYMBOL2, 0, 142, 180);
		assertData(bufIn, SYMBOL2, EventFlag.REMOVE_EVENT.flag(), 142, 180);
	}

	private void addData(RecordBuffer buf, String symbol, int flags, int time, int sequence) {
		RecordCursor cursor = buf.add(RECORD, 0, symbol);
		cursor.setEventFlags(flags);
		cursor.setInt(0, time);
		cursor.setInt(1, sequence);
	}

	private void assertData(RecordBuffer buf, String symbol, int flags, int time, int sequence) {
		RecordCursor cursor = buf.next();
		assertEquals(0, cursor.getCipher());
		assertEquals(symbol, cursor.getSymbol());
		assertEquals(flags, cursor.getEventFlags());
		assertEquals(time, cursor.getInt(0));
		assertEquals(sequence, cursor.getInt(1));
	}

}
