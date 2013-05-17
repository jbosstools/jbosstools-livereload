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

package org.jboss.tools.livereload.internal.server.jetty;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.TimeoutUtils;
import org.jboss.tools.livereload.internal.util.TimeoutUtils.TaskMonitor;

/**
 * The LiveReload Server that implements the livereload protocol (based on
 * websockets) and optionnaly provides a proxy server to inject
 * <code>&lt;SCRIPT&gt;</code> in the returned HTML pages.
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadServer {

	private LiveReloadWebServerRunnable liveReloadWebServerRunnable;

	/**
	 * Constructor
	 * 
	 * @param config
	 *            the LiveReload configuration to use.
	 * @throws UnknownHostException
	 */
	public LiveReloadServer(final int websocketPort, final boolean enableProxyServer, final boolean allowRemoteConnections, final boolean enableScriptInjection) {
		final Server server = LiveReloadServerFactory.createServer(websocketPort, enableProxyServer, allowRemoteConnections, enableScriptInjection);
		this.liveReloadWebServerRunnable = new LiveReloadWebServerRunnable(server);
	}

	/**
	 * Starts the server
	 */
	public void start() {
		final Thread serverThread = new Thread(liveReloadWebServerRunnable, "jetty-livereload");
		serverThread.start();
		// wait until server is started
		final TaskMonitor monitor = new TaskMonitor() {
			@Override
			public boolean isComplete() {
				return liveReloadWebServerRunnable.isStarted();
			}
		};
		if (TimeoutUtils.timeout(monitor, 5, TimeUnit.SECONDS)) {
			Logger.error("Failed to start LiveReload Server within expected time");
			// attempt to stop what can be stopped.
			stop();
		}
	}

	public boolean isStarted() {
		return liveReloadWebServerRunnable.isStarted();
	}

	/**
	 * Stops the server
	 */
	public void stop() {
		try {
			liveReloadWebServerRunnable.stop();
		} catch (Exception e) {
			Logger.error("Failed to stop LiveReload Server");
		}
	}

	/**
	 * The Jetty-based server running in a separate thread (to avoid blocking
	 * the UI)
	 * 
	 * @author xcoulon
	 * 
	 */
	class LiveReloadWebServerRunnable implements Runnable {

		private final Server server;

		private boolean isStarted = false;

		/**
		 * Constructor to use when script injection proxy should not be enabled
		 * 
		 * @param websocketConnectorPort
		 * @throws UnknownHostException
		 */
		public LiveReloadWebServerRunnable(final Server server) {
			this.server = server;
		}

		@Override
		public void run() {
			try {
				Logger.debug("Starting LiveReload Websocket Server...");
				server.start();
				isStarted = true;
				server.join();
			} catch (Exception e) {
				Logger.error("Failed to start embedded jetty server to provide support for LiveReload", e);
			}
		}

		public void stop() throws Exception {
			if (server != null) {
				server.stop();
				isStarted = false;
			}
		}

		public boolean isStarted() {
			return isStarted;
		}
	}

}
