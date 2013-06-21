/******************************************************************************* 
 * Copyright (c) 2008 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Xavier Coulon - Initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.livereload.core.internal.server.jetty;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.jboss.tools.livereload.core.internal.util.Logger;
import org.jboss.tools.livereload.core.internal.util.TimeoutUtils;
import org.jboss.tools.livereload.core.internal.util.TimeoutUtils.TaskMonitor;

/**
 * The Jetty-based server running in a separate thread (to avoid blocking the
 * UI)
 * 
 * @author xcoulon
 * 
 */
public class JettyServerRunner implements Runnable {

	public static final String NAME = "serverName";

	private final Server server;

	/**
	 * Starts the server
	 * 
	 * @throws TimeoutException
	 */
	public static JettyServerRunner start(final Server jettyServer) throws TimeoutException {
		final JettyServerRunner runner = new JettyServerRunner(jettyServer);
		Logger.debug("Starting {} on port {}", jettyServer, runner.getPort());
		final Thread serverThread = new Thread(runner, (String) jettyServer.getAttribute(JettyServerRunner.NAME));
		serverThread.start();
		// wait until server is started
		final TaskMonitor monitor = new TaskMonitor() {
			@Override
			public boolean isComplete() {
				return runner.isStarted();
			}
		};
		if (TimeoutUtils.timeout(monitor, 15, TimeUnit.SECONDS)) {
			Logger.error("Failed to start " + jettyServer + " within expected time (reason: timeout)");
			// attempt to stop what can be stopped.
			stop(runner);
			throw new TimeoutException("Failed to start " + jettyServer + " within expected time (reason: timeout)");
		}
		return runner;
	}

	public static void stop(final JettyServerRunner runner) {
		if (runner != null) {
			try {
				runner.stop();
				final TaskMonitor monitor = new TaskMonitor() {
					@Override
					public boolean isComplete() {
						return runner.isStopped();
					}
				};
				if (TimeoutUtils.timeout(monitor, 5, TimeUnit.SECONDS)) {
					Logger.error("Failed to stop LiveReload Server within expected time (reason: timeout)");
					// attempt to stop what can be stopped.
					throw new TimeoutException(
							"Failed to stop LiveReload Server within expected time (reason: timeout)");
				}
			} catch (Exception e) {
				Logger.error("Failed to stop LiveReload Server", e);
			}
		}
	}

	/**
	 * Constructor to use when script injection proxy should not be enabled
	 * 
	 * @param websocketConnectorPort
	 * @throws UnknownHostException
	 */
	private JettyServerRunner(final Server server) {
		this.server = server;
	}

	@Override
	public void run() {
		try {
			// avoids starting the server if it was already started before
			if (!server.isStarted()) { 
				Logger.debug("Starting {}...", server.getAttribute(NAME));
				server.start();
				server.join();
			}
		} catch (Exception e) {
			Logger.error("Failed to start '" + server.getAttribute(NAME) + "'", e);
			try {
				server.stop();
			} catch (Exception e1) {
				Logger.error("Failed to stop server '" + server.getAttribute(NAME) + "' after startup failure", e);
			}
		}
	}

	private void stop() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

	/**
	 * Returns true if the underlying Jetty server is started.
	 * 
	 * @return
	 */
	public boolean isStarted() {
		boolean started = server.isStarted();
		for (Connector connector : server.getConnectors()) {
			started = started && connector.isStarted();
		}
		return started;
	}

	/**
	 * Returns true if the underlying Jetty server is stopped.
	 * 
	 * @return
	 */
	public boolean isStopped() {
		return server.isStopped();
	}

	public int getPort() {
		return server.getConnectors()[0].getPort();
	}
}