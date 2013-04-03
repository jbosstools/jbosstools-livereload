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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jetty.server.Server;
import org.jboss.tools.livereload.internal.util.Logger;

/**
 * The LiveReload Server that implements the livereload protocol (based on
 * websockets) and optionnaly provides a proxy server to inject
 * <code>&lt;SCRIPT&gt;</code> in the returned HTML pages.
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadServer implements IWebResourceChangedListener {

	private AbstractCommandBroadcaster broadcaster;

	private LiveReloadWebServerRunnable liveReloadWebServerRunnable;

	/**
	 * Constructor
	 * 
	 * @param config
	 *            the LiveReload configuration to use.
	 * @throws UnknownHostException
	 */
	public LiveReloadServer(final int websocketPort) throws UnknownHostException  {
		this.broadcaster = new ResourceChangedBroadcaster();
		this.liveReloadWebServerRunnable = new LiveReloadWebServerRunnable(websocketPort,
				broadcaster);
	}
	
	/**
	 * Starts the server
	 */
	public void start() {
		Thread serverThread = new Thread(liveReloadWebServerRunnable, "jetty-livereload");
		serverThread.start();
	}

	/**
	 * Stops the server
	 */
	public void stop() throws Exception {
		liveReloadWebServerRunnable.stop();
	}

	/* (non-Javadoc)
	 * @see org.jboss.tools.livereload.internal.server.jetty.IWebResourceChangedListener#notifyResourceChange(java.lang.String)
	 */
	@Override
	public void notifyResourceChange(final IPath path) {
		this.broadcaster.notifyResourceChange(path);
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

		private final boolean enableProxy;

		private final int proxyConnectorPort;

		private final int websocketConnectorPort;

		private final AbstractCommandBroadcaster liveReloadCommandBroadcaster;

		/**
		 * Constructor to use when script injection proxy should be enabled
		 * 
		 * @param proxyConnectorPort
		 * @param websocketConnectorPort
		 * @param liveReloadCommandBroadcaster
		 * @throws UnknownHostException
		 */
		public LiveReloadWebServerRunnable(final int proxyConnectorPort, final int websocketConnectorPort,
				final AbstractCommandBroadcaster liveReloadCommandBroadcaster) throws UnknownHostException {
			this.server = LiveReloadServerFactory.onServer(liveReloadCommandBroadcaster).websocketPort(websocketConnectorPort).proxyPort(proxyConnectorPort).build();
			this.enableProxy = true;
			this.proxyConnectorPort = proxyConnectorPort;
			this.websocketConnectorPort = websocketConnectorPort;
			this.liveReloadCommandBroadcaster = liveReloadCommandBroadcaster;
		}

		/**
		 * Constructor to use when script injection proxy should not be enabled
		 * 
		 * @param proxyConnectorPort
		 * @param websocketConnectorPort
		 * @param liveReloadCommandBroadcaster
		 * @throws UnknownHostException
		 */
		public LiveReloadWebServerRunnable(final int websocketConnectorPort,
				final AbstractCommandBroadcaster liveReloadCommandBroadcaster) throws UnknownHostException {
			this.server = LiveReloadServerFactory.onServer(liveReloadCommandBroadcaster).websocketPort(websocketConnectorPort).build();
			this.enableProxy = false;
			this.proxyConnectorPort = -1;
			this.websocketConnectorPort = websocketConnectorPort;
			this.liveReloadCommandBroadcaster = liveReloadCommandBroadcaster;
		}

		@Override
		public void run() {
			try {
				Logger.debug("Starting LiveReload Websocket Server...");
				server.start();
				server.join();
			} catch (Exception e) {
				Logger.error("Failed to start embedded jetty server to provide support for LiveReload", e);
			}
		}

		public void stop() throws Exception {
			if (server != null) {
				server.stop();
			}
		}
	}

}
