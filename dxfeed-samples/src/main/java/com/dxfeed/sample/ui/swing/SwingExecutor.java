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
package com.dxfeed.sample.ui.swing;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import javax.swing.*;

public class SwingExecutor implements Executor {
	private final ConcurrentLinkedQueue<Runnable> commands = new ConcurrentLinkedQueue<>();
	private final int delay;

	public SwingExecutor(int delay) {
		this.delay = delay;
		SwingUtilities.invokeLater(this::createTimer);
	}

	private void createTimer() {
		Timer timer = new Timer(delay, e -> {
			Runnable command;
			while ((command = commands.poll()) != null)
				command.run();
		});
		timer.start();
	}

	@Override
	public void execute(Runnable command) {
		commands.add(command);
	}
}
