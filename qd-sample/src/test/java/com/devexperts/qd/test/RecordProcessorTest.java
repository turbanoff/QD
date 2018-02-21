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
package com.devexperts.qd.test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.devexperts.qd.DataRecord;
import com.devexperts.qd.ng.*;
import com.devexperts.qd.util.RecordProcessor;
import com.devexperts.test.ThreadCleanCheck;
import junit.framework.TestCase;

public class RecordProcessorTest extends TestCase {
	private static final int MESSAGES_COUNT = 100;
	private static final int MAX_WAIT_TIME = 10000;

	private static final DataRecord record = new TestDataScheme(20121022L).getRecord(0);

	private final AtomicInteger errorCount = new AtomicInteger();
	private final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
	private volatile int messageNumber;
	private ExecutorService executor;


	@Override
	protected void setUp() throws Exception {
		ThreadCleanCheck.before();
	}

	@Override
	protected void tearDown() throws Exception {
		if (executor != null)
			executor.shutdown();
		ThreadCleanCheck.after();
	}

	private class BufRecordProvider extends AbstractRecordProvider {
		private final ArrayBlockingQueue<RecordBuffer> buffers = new ArrayBlockingQueue<>(10);
		private volatile RecordListener listener;

		public void add(RecordBuffer buf) {
			try {
				buffers.put(buf);
			} catch (InterruptedException e) {
				errorCount.incrementAndGet();
			}
		}

		@Override
		public RecordMode getMode() {
			return RecordMode.DATA;
		}

		@Override
		public boolean retrieve(RecordSink sink) {
			RecordBuffer buffer = buffers.poll();
			if (buffer == null) {
				System.out.println("retrieve() called when no data is available!");
				errorCount.incrementAndGet();
				return false;
			}
			buffer.retrieve(sink);
			return !buffers.isEmpty();
		}

		@Override
		public void setRecordListener(RecordListener listener) {
			this.listener = listener;
		}

		public void dataAvailable() {
			RecordListener listener = this.listener;
			if (listener != null)
				listener.recordsAvailable(this);
		}
	}

	public void testDataProcessor() throws InterruptedException, BrokenBarrierException, TimeoutException {
		BufRecordProvider dataProvider = new BufRecordProvider();

		executor = Executors.newFixedThreadPool(10);
		RecordProcessor dataProcessor = new RecordProcessor(executor) {
			@Override
			public void process(RecordSource source) {
				int lastValue = -1;
				for (RecordCursor cursor; (cursor = source.next()) != null;)
					lastValue = cursor.getInt(0);
				if (lastValue != messageNumber)
					errorCount.incrementAndGet();
				messageNumber++;
				try {
					cyclicBarrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					throw new IllegalStateException(e);
				}
			}
		};

		dataProcessor.startProcessing(dataProvider);

		for (int i = 0; i < MESSAGES_COUNT; i++) {
			RecordBuffer buf = new RecordBuffer();
			RecordCursor cursor = buf.add(record, record.getScheme().getCodec().encode("IBM"), "IBM");
			cursor.setInt(0, i);

			dataProvider.add(buf);
			dataProvider.dataAvailable();
			cyclicBarrier.await(MAX_WAIT_TIME, TimeUnit.MILLISECONDS);
		}

		if (errorCount.get() > 0)
			fail("Errors were encountered during processing");
		dataProcessor.stopProcessing();
	}
}
