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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.wst.server.core.util.SocketUtil;
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
	
	private IStatus status = null;

	/**
	 * Starts the server
	 * 
	 * @throws TimeoutException
	 */
	public static JettyServerRunner start(final Server liveReloadServer) throws TimeoutException {
		final JettyServerRunner runner = new JettyServerRunner(liveReloadServer);
		Logger.debug("Starting {} on port {}", liveReloadServer, runner.getPort());
		final Thread serverThread = new Thread(runner, (String) liveReloadServer.getAttribute(JettyServerRunner.NAME));
		// wait until server is started
		final TaskMonitor monitor = new TaskMonitor() {
			@Override
			public boolean isComplete() {
				// task is complete if server started or if the runner status is not OK (ie, an error occurred) 
				final boolean started = runner.isStarted();
				final boolean statusOk = runner.status != null && !runner.status.isOK();
				return started || statusOk;
			}
		};
		serverThread.start();
		
		if (TimeoutUtils.timeout(monitor, 15, TimeUnit.SECONDS)) {
			Logger.error("Failed to start " + liveReloadServer + " within expected time (reason: timeout)");
			// attempt to stop what can be stopped.
			stop(runner);
			throw new TimeoutException("Failed to start " + liveReloadServer + " within expected time (reason: timeout)");
		}
		Logger.debug("Server {} started (success={})", liveReloadServer, runner.status.isOK());
		return runner;
	}

	public static void stop(final JettyServerRunner runner) {
		if (runner != null) {
			try {
				Logger.debug("Stopping {}...", runner.server.getAttribute(NAME));
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
				Logger.debug("{} fully stopped: {}", runner.server.getAttribute(NAME), !SocketUtil.isPortInUse(runner.getPort()));
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
				status = Status.OK_STATUS;
				server.join();
			}
		} catch (final Exception startException) {
			status = Logger.error("Failed to start '" + server.getAttribute(NAME) + "'", startException);
			try {
				server.stop();
			} catch (Exception stopException) {
				Logger.error("Failed to stop server '" + server.getAttribute(NAME) + "' after startup failure", stopException);
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
	boolean isStarted() {
		boolean started = server.isStarted();
		for (Connector connector : server.getConnectors()) {
			started = started && connector.isStarted();
		}
		return started;
	}

	/**
	 * Returns true if the server was successfully started, false otherwise
	 * @return
	 */
	public boolean isSuccessfullyStarted() {
		return isStarted() && (status != null && status.isOK());
	}
	
	/**
	 * Returns true if the underlying Jetty server is stopped.
	 * 
	 * @return
	 */
	boolean isStopped() {
		return server.isStopped();
	}
	
	public int getPort() {
		return ((NetworkConnector)server.getConnectors()[0]).getPort();
	}

}