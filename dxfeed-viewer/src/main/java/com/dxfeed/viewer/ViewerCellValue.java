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
package com.dxfeed.viewer;

import java.awt.*;
import java.text.*;
import java.util.Locale;
import java.util.TimeZone;

import com.devexperts.util.TimeFormat;

class ViewerCellValue implements Comparable<ViewerCellValue> {
	private static final NumberFormat PRICE_FORMAT = new DecimalFormat(".00########", new DecimalFormatSymbols(Locale.US));
	private static final NumberFormat SIZE_FORMAT = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.US));

	public static final String NA = "N/A";

	private String text;
	private Color color;
	private Color background;
	private int alignment;
	private double value;
	private TimeZone timeZone;

	ViewerCellValue(String text, Color color, Color background, int alignment, TimeZone timeZone) {
		this(text, color, background, alignment, 0.0, timeZone);
	}

	ViewerCellValue(String text, Color color, Color background, int alignment, double value, TimeZone timeZone) {
		this.text = text;
		this.color = color;
		this.background = background;
		this.alignment = alignment;
		this.value = value;
		this.timeZone = timeZone;
	}

	String getText() {
		return text;
	}

	Color getColor() {
		return color;
	}

	Color getBackground() {
		return background;
	}

	int getAlignment() {
		return alignment;
	}

	double getValue() {
		return value;
	}

	public String toString() {
		return text;
	}

	static String formatPrice(double price) {
		if (Double.isNaN(price))
			return NA;
		if (price == 0)
			return "0";
		synchronized (PRICE_FORMAT) {
			return PRICE_FORMAT.format(price);
		}
	}

	static String formatSize(long size) {
		if (size == Long.MIN_VALUE)
			return NA;
		synchronized (SIZE_FORMAT) {
			return SIZE_FORMAT.format(size);
		}
	}

	static String formatExchange(char exchange) {
		return Character.isLetter(exchange) ? Character.toString(exchange) : "";
	}

	static String formatTime(long time, TimeZone tz) {
		return TimeFormat.getInstance(tz).withMillis().format(time);
	}

	@Override
	public int compareTo(ViewerCellValue other) {
		int cmp = Double.compare(this.value, other.value);
		if (cmp == 0) {
			cmp = this.text.compareTo(other.text);
		}
		return cmp;
	}
}
