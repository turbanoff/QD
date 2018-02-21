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
@XmlJavaTypeAdapter(type=char.class, value=XmlCharAdapter.class)
@XmlSchema(namespace = XmlNamespace.EVENT, elementFormDefault = XmlNsForm.QUALIFIED)
package com.dxfeed.event.market;

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.dxfeed.impl.XmlCharAdapter;
import com.dxfeed.impl.XmlNamespace;
