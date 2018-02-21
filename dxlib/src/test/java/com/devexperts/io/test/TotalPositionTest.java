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
package com.devexperts.io.test;

import java.io.IOException;

import com.devexperts.io.*;
import junit.framework.TestCase;

public class TotalPositionTest extends TestCase {
	private static final int CNT = 30000;
	private static final int REP = 5;

	public void testByteArrayOutputTotalPosition() throws IOException {
		ByteArrayOutput out = new ByteArrayOutput();
		for (int i = 0; i < REP; i++) {
			checkOutputTotalPosition(out, 0);
			byte[] buffer = out.getBuffer();
			ByteArrayInput in = new ByteArrayInput(buffer);
			checkInputTotalPosition(in);
			out.clear(); // must reset totalPosition
		}
	}

	public void testChunkedOutputTotalPosition() throws IOException {
		ChunkedOutput out = new ChunkedOutput();
		for (int i = 0; i < REP; i++) {
			checkOutputTotalPosition(out, 0);
			ChunkList chunks = out.getOutput(this);
			ChunkedInput in = new ChunkedInput();
			in.addAllToInput(chunks, this);
			checkInputTotalPosition(in);
			out.clear(); // must reset totalPosition
		}
	}

	public void testChunkedOutputTotalPositionKeep() throws IOException {
		ChunkedOutput out = new ChunkedOutput();
		for (int i = 0; i < REP; i++) {
			checkOutputTotalPosition(out, i * CNT);
			long wasTotalPosition = out.totalPosition();
			ChunkList chunks = out.getOutput(this);
			assertEquals(wasTotalPosition, out.totalPosition());
			ChunkedInput in = new ChunkedInput();
			in.addAllToInput(chunks, this);
			checkInputTotalPosition(in);
			//out.clear(); // no clear here!!!
		}
	}

	public void testChunkedOutputFromChunkListWrapped() throws IOException {
		ChunkedOutput out = new ChunkedOutput();
		for (int i = 0; i < REP; i++) {
			long wasTotalPosition = out.totalPosition();
			ChunkList chunks = ChunkPool.DEFAULT.getChunkList(this);
			int nBytes = 42;
			Chunk chunk = Chunk.wrap(new byte[nBytes], this);
			chunks.add(chunk, this);
			out.writeAllFromChunkList(chunks, this);
			assertEquals(wasTotalPosition + nBytes, out.totalPosition());
		}
	}

	public void testChunkedOutputFromChunkListPooled() throws IOException {
		ChunkedOutput out = new ChunkedOutput();
		for (int i = 0; i < REP; i++) {
			long wasTotalPosition = out.totalPosition();
			ChunkList chunks = ChunkPool.DEFAULT.getChunkList(this);
			int nBytes = 42;
			Chunk chunk = ChunkPool.DEFAULT.getChunk(this);
			chunk.setRange(3, nBytes, this);
			chunks.add(chunk, this);
			out.writeAllFromChunkList(chunks, this);
			assertEquals(wasTotalPosition + nBytes, out.totalPosition());
		}
	}

	private void checkOutputTotalPosition(BufferedOutput out, long basePosition) throws IOException {
		for (int i = 0; i < CNT; i++) {
			assertEquals(basePosition + i, out.totalPosition());
			out.write(i & 0xff);
		}
		assertEquals(basePosition + CNT, out.totalPosition());
	}

	private void checkInputTotalPosition(BufferedInput in) throws IOException {
		for (int i = 0; i < CNT; i++) {
			assertEquals(i, in.totalPosition());
			assertEquals(i & 0xff, in.read());
		}
		assertEquals(CNT, in.totalPosition());
	}
}
