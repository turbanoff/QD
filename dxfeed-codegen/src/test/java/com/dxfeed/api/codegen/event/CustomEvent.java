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
package com.dxfeed.api.codegen.event;

import java.math.BigInteger;

import com.devexperts.io.Marshalled;
import com.dxfeed.annotation.*;
import com.dxfeed.event.EventType;

/**
 * Testing full custom event with marshalled field and wrapped int value
 */
@EventTypeMapping
public class CustomEvent implements EventType<String> {
	private String symbol;
	private BigInteger bigValue;
	private WrappedInt wrappedValue;
	private transient Marshalled<?> attachment;

	public CustomEvent() {
	}

	public CustomEvent(String symbol) {
		this.symbol = symbol;
	}

	// should not be automatically mapped
	@Override
	public String getEventSymbol() {
		return symbol;
	}

	@Override
	public void setEventSymbol(String eventSymbol) {
		this.symbol = eventSymbol;
	}

	// should be automatically mapped to Marshalled
	public BigInteger getBigValue() {
		return bigValue;
	}

	public void setBigValue(BigInteger bigValue) {
		this.bigValue = bigValue;
	}

	@EventFieldMapping(type = EventFieldType.INT)
	public WrappedInt getWrappedValue() {
		return wrappedValue;
	}

	public void setWrappedValue(WrappedInt wrappedValue) {
		this.wrappedValue = wrappedValue;
	}

	@EventFieldMapping(type = EventFieldType.TRANSIENT)
	public Object getAttachment() {
		return attachment == null ? null : attachment.getObject();
	}

	public void setAttachment(Object attachment) {
		this.attachment = Marshalled.forObject(attachment);
	}

	Marshalled<?> getMarshalledAttachment() {
		return attachment;
	}

	void setMarshalledAttachment(Marshalled<?> attachment) {
		this.attachment = attachment;
	}
}
