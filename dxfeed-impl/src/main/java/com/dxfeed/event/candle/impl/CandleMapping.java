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
package com.dxfeed.event.candle.impl;

import com.devexperts.qd.DataRecord;
import com.devexperts.qd.ng.RecordCursor;
import com.devexperts.qd.util.Decimal;
import com.devexperts.qd.util.MappingUtil;
import com.devexperts.util.TimeUtil;

public class CandleMapping extends CandleEventMapping {
// BEGIN: CODE AUTOMATICALLY GENERATED: DO NOT MODIFY. IT IS REGENERATED BY com.dxfeed.api.codegen.ImplCodeGen
	private final int iTime;
	private final int iSequence;
	private final int iCount;
	private final boolean vCountIsDecimal;
	private final int iOpen;
	private final int iHigh;
	private final int iLow;
	private final int iClose;
	private final int iVolume;
	private final int iVWAP;
	private final int iBidVolume;
	private final int iAskVolume;
	private final int iOpenInterest;
	private final int iImpVolatility;

	public CandleMapping(DataRecord record) {
		super(record);
		iTime = MappingUtil.findIntField(record, "Time", true);
		iSequence = MappingUtil.findIntField(record, "Sequence", true);
		iCount = MappingUtil.findIntField(record, "Count", false);
		vCountIsDecimal = MappingUtil.isDecimalField(record, iCount);
		iOpen = MappingUtil.findIntField(record, "Open", true);
		iHigh = MappingUtil.findIntField(record, "High", true);
		iLow = MappingUtil.findIntField(record, "Low", true);
		iClose = MappingUtil.findIntField(record, "Close", true);
		iVolume = MappingUtil.findIntField(record, "Volume", false);
		iVWAP = MappingUtil.findIntField(record, "VWAP", false);
		iBidVolume = MappingUtil.findIntField(record, "Bid.Volume", false);
		iAskVolume = MappingUtil.findIntField(record, "Ask.Volume", false);
		iOpenInterest = MappingUtil.findIntField(record, "OpenInterest", false);
		iImpVolatility = MappingUtil.findIntField(record, "ImpVolatility", false);
	}

	public long getTimeMillis(RecordCursor cursor) {
		return getInt(cursor, iTime) * 1000L;
	}

	public void setTimeMillis(RecordCursor cursor, long time) {
		setInt(cursor, iTime, TimeUtil.getSecondsFromTime(time));
	}

	public int getTimeSeconds(RecordCursor cursor) {
		return getInt(cursor, iTime);
	}

	public void setTimeSeconds(RecordCursor cursor, int time) {
		setInt(cursor, iTime, time);
	}

	public int getSequence(RecordCursor cursor) {
		return getInt(cursor, iSequence);
	}

	public void setSequence(RecordCursor cursor, int sequence) {
		setInt(cursor, iSequence, sequence);
	}

	public long getCount(RecordCursor cursor) {
		if (iCount < 0)
			return 0;
		if (vCountIsDecimal) {
			return (long)Decimal.toDouble(getInt(cursor, iCount));
		} else {
			return getInt(cursor, iCount);
		}
	}

	public void setCount(RecordCursor cursor, long count) {
		if (iCount < 0)
			return;
		if (vCountIsDecimal) {
			setInt(cursor, iCount, Decimal.composeDecimal(count, 0));
		} else {
			setInt(cursor, iCount, (int)count);
		}
	}

	public double getCountDouble(RecordCursor cursor) {
		if (iCount < 0)
			return Double.NaN;
		if (vCountIsDecimal) {
			return Decimal.toDouble(getInt(cursor, iCount));
		} else {
			return getInt(cursor, iCount);
		}
	}

	public void setCountDouble(RecordCursor cursor, double count) {
		if (iCount < 0)
			return;
		if (vCountIsDecimal) {
			setInt(cursor, iCount, Decimal.compose(count));
		} else {
			setInt(cursor, iCount, (int)count);
		}
	}

	public int getCountDecimal(RecordCursor cursor) {
		if (iCount < 0)
			return 0;
		if (vCountIsDecimal) {
			return getInt(cursor, iCount);
		} else {
			return Decimal.composeDecimal(getInt(cursor, iCount), 0);
		}
	}

	public void setCountDecimal(RecordCursor cursor, int count) {
		if (iCount < 0)
			return;
		if (vCountIsDecimal) {
			setInt(cursor, iCount, count);
		} else {
			setInt(cursor, iCount, (int)Decimal.toDouble(count));
		}
	}

	public double getOpen(RecordCursor cursor) {
		return Decimal.toDouble(getInt(cursor, iOpen));
	}

	public void setOpen(RecordCursor cursor, double open) {
		setInt(cursor, iOpen, Decimal.compose(open));
	}

	public int getOpenDecimal(RecordCursor cursor) {
		return getInt(cursor, iOpen);
	}

	public void setOpenDecimal(RecordCursor cursor, int open) {
		setInt(cursor, iOpen, open);
	}

	public double getHigh(RecordCursor cursor) {
		return Decimal.toDouble(getInt(cursor, iHigh));
	}

	public void setHigh(RecordCursor cursor, double high) {
		setInt(cursor, iHigh, Decimal.compose(high));
	}

	public int getHighDecimal(RecordCursor cursor) {
		return getInt(cursor, iHigh);
	}

	public void setHighDecimal(RecordCursor cursor, int high) {
		setInt(cursor, iHigh, high);
	}

	public double getLow(RecordCursor cursor) {
		return Decimal.toDouble(getInt(cursor, iLow));
	}

	public void setLow(RecordCursor cursor, double low) {
		setInt(cursor, iLow, Decimal.compose(low));
	}

	public int getLowDecimal(RecordCursor cursor) {
		return getInt(cursor, iLow);
	}

	public void setLowDecimal(RecordCursor cursor, int low) {
		setInt(cursor, iLow, low);
	}

	public double getClose(RecordCursor cursor) {
		return Decimal.toDouble(getInt(cursor, iClose));
	}

	public void setClose(RecordCursor cursor, double close) {
		setInt(cursor, iClose, Decimal.compose(close));
	}

	public int getCloseDecimal(RecordCursor cursor) {
		return getInt(cursor, iClose);
	}

	public void setCloseDecimal(RecordCursor cursor, int close) {
		setInt(cursor, iClose, close);
	}

	public long getVolume(RecordCursor cursor) {
		if (iVolume < 0)
			return 0;
		return (long)Decimal.toDouble(getInt(cursor, iVolume));
	}

	public void setVolume(RecordCursor cursor, long volume) {
		if (iVolume < 0)
			return;
		setInt(cursor, iVolume, Decimal.composeDecimal(volume, 0));
	}

	public double getVolumeDouble(RecordCursor cursor) {
		if (iVolume < 0)
			return Double.NaN;
		return Decimal.toDouble(getInt(cursor, iVolume));
	}

	public void setVolumeDouble(RecordCursor cursor, double volume) {
		if (iVolume < 0)
			return;
		setInt(cursor, iVolume, Decimal.compose(volume));
	}

	public int getVolumeDecimal(RecordCursor cursor) {
		if (iVolume < 0)
			return 0;
		return getInt(cursor, iVolume);
	}

	public void setVolumeDecimal(RecordCursor cursor, int volume) {
		if (iVolume < 0)
			return;
		setInt(cursor, iVolume, volume);
	}

	public double getVWAP(RecordCursor cursor) {
		if (iVWAP < 0)
			return Double.NaN;
		return Decimal.toDouble(getInt(cursor, iVWAP));
	}

	public void setVWAP(RecordCursor cursor, double _VWAP) {
		if (iVWAP < 0)
			return;
		setInt(cursor, iVWAP, Decimal.compose(_VWAP));
	}

	public int getVWAPDecimal(RecordCursor cursor) {
		if (iVWAP < 0)
			return 0;
		return getInt(cursor, iVWAP);
	}

	public void setVWAPDecimal(RecordCursor cursor, int _VWAP) {
		if (iVWAP < 0)
			return;
		setInt(cursor, iVWAP, _VWAP);
	}

	public long getBidVolume(RecordCursor cursor) {
		if (iBidVolume < 0)
			return 0;
		return (long)Decimal.toDouble(getInt(cursor, iBidVolume));
	}

	public void setBidVolume(RecordCursor cursor, long bidVolume) {
		if (iBidVolume < 0)
			return;
		setInt(cursor, iBidVolume, Decimal.composeDecimal(bidVolume, 0));
	}

	public double getBidVolumeDouble(RecordCursor cursor) {
		if (iBidVolume < 0)
			return Double.NaN;
		return Decimal.toDouble(getInt(cursor, iBidVolume));
	}

	public void setBidVolumeDouble(RecordCursor cursor, double bidVolume) {
		if (iBidVolume < 0)
			return;
		setInt(cursor, iBidVolume, Decimal.compose(bidVolume));
	}

	public int getBidVolumeDecimal(RecordCursor cursor) {
		if (iBidVolume < 0)
			return 0;
		return getInt(cursor, iBidVolume);
	}

	public void setBidVolumeDecimal(RecordCursor cursor, int bidVolume) {
		if (iBidVolume < 0)
			return;
		setInt(cursor, iBidVolume, bidVolume);
	}

	public long getAskVolume(RecordCursor cursor) {
		if (iAskVolume < 0)
			return 0;
		return (long)Decimal.toDouble(getInt(cursor, iAskVolume));
	}

	public void setAskVolume(RecordCursor cursor, long askVolume) {
		if (iAskVolume < 0)
			return;
		setInt(cursor, iAskVolume, Decimal.composeDecimal(askVolume, 0));
	}

	public double getAskVolumeDouble(RecordCursor cursor) {
		if (iAskVolume < 0)
			return Double.NaN;
		return Decimal.toDouble(getInt(cursor, iAskVolume));
	}

	public void setAskVolumeDouble(RecordCursor cursor, double askVolume) {
		if (iAskVolume < 0)
			return;
		setInt(cursor, iAskVolume, Decimal.compose(askVolume));
	}

	public int getAskVolumeDecimal(RecordCursor cursor) {
		if (iAskVolume < 0)
			return 0;
		return getInt(cursor, iAskVolume);
	}

	public void setAskVolumeDecimal(RecordCursor cursor, int askVolume) {
		if (iAskVolume < 0)
			return;
		setInt(cursor, iAskVolume, askVolume);
	}

	public long getOpenInterest(RecordCursor cursor) {
		if (iOpenInterest < 0)
			return 0;
		return (long)Decimal.toDouble(getInt(cursor, iOpenInterest));
	}

	public void setOpenInterest(RecordCursor cursor, long openInterest) {
		if (iOpenInterest < 0)
			return;
		setInt(cursor, iOpenInterest, Decimal.composeDecimal(openInterest, 0));
	}

	public double getOpenInterestDouble(RecordCursor cursor) {
		if (iOpenInterest < 0)
			return Double.NaN;
		return Decimal.toDouble(getInt(cursor, iOpenInterest));
	}

	public void setOpenInterestDouble(RecordCursor cursor, double openInterest) {
		if (iOpenInterest < 0)
			return;
		setInt(cursor, iOpenInterest, Decimal.compose(openInterest));
	}

	public int getOpenInterestDecimal(RecordCursor cursor) {
		if (iOpenInterest < 0)
			return 0;
		return getInt(cursor, iOpenInterest);
	}

	public void setOpenInterestDecimal(RecordCursor cursor, int openInterest) {
		if (iOpenInterest < 0)
			return;
		setInt(cursor, iOpenInterest, openInterest);
	}

	public double getImpVolatility(RecordCursor cursor) {
		if (iImpVolatility < 0)
			return Double.NaN;
		return Decimal.toDouble(getInt(cursor, iImpVolatility));
	}

	public void setImpVolatility(RecordCursor cursor, double impVolatility) {
		if (iImpVolatility < 0)
			return;
		setInt(cursor, iImpVolatility, Decimal.compose(impVolatility));
	}

	public int getImpVolatilityDecimal(RecordCursor cursor) {
		if (iImpVolatility < 0)
			return 0;
		return getInt(cursor, iImpVolatility);
	}

	public void setImpVolatilityDecimal(RecordCursor cursor, int impVolatility) {
		if (iImpVolatility < 0)
			return;
		setInt(cursor, iImpVolatility, impVolatility);
	}
// END: CODE AUTOMATICALLY GENERATED
}
