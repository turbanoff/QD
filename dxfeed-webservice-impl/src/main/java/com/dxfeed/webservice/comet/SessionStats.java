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
package com.dxfeed.webservice.comet;

import java.util.Comparator;
import java.util.Objects;

import com.devexperts.util.TimePeriod;

class SessionStats implements Cloneable {

	public String sessionId;
	public volatile int numSessions;
	public volatile long createTime = -1;
	public volatile long lastActiveTime = -1;

	public volatile int maxQueueSize;
	public volatile int queueSize;
	public volatile int subSize;
	public volatile int subTimeSeriesSize;

	public volatile long writeEvents;
	public volatile long write;
	public volatile long writeMeta;
	public volatile long readEvents;
	public volatile long read;
	public volatile long readMeta;

	public static Comparator<SessionStats> getComparator(String column) {
		switch (Objects.requireNonNull(column).trim().toLowerCase()) {
		case "id":
			return Comparator.comparing((SessionStats stats) -> stats.sessionId);
		case "read_mps":
			return Comparator.comparing((SessionStats stats) -> stats.readEvents).reversed();
		case "read":
			return Comparator.comparing((SessionStats stats) -> stats.read + stats.readMeta).reversed();
		case "write_mps":
			return Comparator.comparing((SessionStats stats) -> stats.writeEvents).reversed();
		case "write":
			return Comparator.comparing((SessionStats stats) -> stats.write + stats.writeMeta).reversed();
		case "queue":
			return Comparator.comparing((SessionStats stats) -> stats.queueSize).reversed();
		case "time":
			return Comparator.comparing((SessionStats stats) -> stats.createTime);
		case "inactivity":
			return Comparator.comparing((SessionStats stats) -> stats.lastActiveTime);
		}
		throw new IllegalArgumentException("Unknown sort column: " + column);
	}

	public void regSubscription(int size, boolean timeSeries) {
		if (timeSeries) {
			subTimeSeriesSize = size;
		} else {
			subSize = size;
		}
	}

	public void regQueueSize(int size) {
		queueSize = size;
		if (size > maxQueueSize)
			maxQueueSize = size;
	}

	public void clear() {
		numSessions = 0;
		createTime = lastActiveTime = 0;

		maxQueueSize = queueSize = subSize = subTimeSeriesSize = 0;
		writeEvents = write = writeMeta = 0;
		readEvents = read = readMeta = 0;
	}

	@Override
	public SessionStats clone() {
		SessionStats stats = new SessionStats();

		stats.sessionId = sessionId;
		stats.numSessions = numSessions;
		stats.createTime = createTime;
		stats.lastActiveTime = lastActiveTime;

		stats.maxQueueSize = maxQueueSize;
		stats.queueSize = queueSize;
		stats.subSize = subSize;
		stats.subTimeSeriesSize = subTimeSeriesSize;

		stats.writeEvents = writeEvents;
		stats.write = write;
		stats.writeMeta = writeMeta;
		stats.readEvents = readEvents;
		stats.read = read;
		stats.readMeta = readMeta;
		
		return stats;
	}

	public void accumulate(SessionStats other, boolean up) {
		int maxSize = other.maxQueueSize;
		if (maxSize > maxQueueSize)
			maxQueueSize = maxSize;

		sessionId = other.sessionId;

		int sizeMultiplier = up ? 1 : 0;
		queueSize += other.queueSize * sizeMultiplier;
		subSize += other.subSize * sizeMultiplier;
		subTimeSeriesSize += other.subTimeSeriesSize * sizeMultiplier;
        numSessions += other.numSessions * sizeMultiplier;
		createTime += other.createTime * sizeMultiplier;
		lastActiveTime += other.lastActiveTime * sizeMultiplier;

		int multiplier = up ? 1 : -1;
		writeEvents += other.writeEvents * multiplier;
		write += other.write * multiplier;
		writeMeta += other.writeMeta * multiplier;
		readEvents += other.readEvents * multiplier;
		read += other.read * multiplier;
		readMeta += other.readMeta * multiplier;
	}

	public String getTotalRated(int sessions, double period) {
		return "Sessions: " + numSessions + ";"
			+ " Sub: " + getRated(subSize, sessions) + ";"
			+ " SubTs: " + getRated(subTimeSeriesSize, sessions) + ";"
			+ " Queue: " + getRated(queueSize, sessions) + ", max " + maxQueueSize + ";"
			+ " Write: " + getRated(writeEvents, period) + " mps, "
			+ getRated(write, period) + " pps, meta " + getRated(writeMeta, period) + " pps;"
			+ " Read: " + getRated(readEvents, period) + " mps, "
			+ getRated(read, period) + " pps, meta " + getRated(readMeta, period) + " pps;";
	}

	public String getRated(double period) {
		return "Sub: " + subSize + ";"
			+ " SubTs: " + subTimeSeriesSize + ";"
			+ " Queue: " + queueSize + ", max " + maxQueueSize + ";"
			+ " Write: " + getRated(writeEvents, period) + " mps, "
			+ getRated(write, period) + " pps, meta " + getRated(writeMeta, period) + " pps;"
			+ " Read: " + getRated(readEvents, period) + " mps, "
			+ getRated(read, period) + " pps, meta " + getRated(readMeta, period) + " pps;";
	}

	protected void dumpStats(StringBuilder buff, double period, long currentTime) {
		buff.append(String.format("\n%30s", sessionId));
		if (currentTime != 0) {
			long inactivity = currentTime - lastActiveTime;
			buff.append(" ")
				.append((inactivity > 10000) ? TimePeriod.valueOf(inactivity).toString() : Long.toString(inactivity))
				.append("/")
				.append(TimePeriod.valueOf(currentTime - createTime));
		}
		buff.append(" - ").append(getRated(period));
	}

	public static double getRated(long k, double period) {
		if (k <= 0 || period <= 0)
			return 0;
		double d = k / period;
		return d <= 9.99 ? Math.max(Math.floor(d * 100 + 0.5) / 100, 0.01) :
			d <= 99.9 ? Math.floor(d * 10 + 0.5) / 10 : Math.floor(d + 0.5);
	}
}
