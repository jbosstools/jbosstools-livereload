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

package org.jboss.tools.livereload.internal.io;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.internal.command.ServerUtils;
import org.jboss.tools.livereload.internal.configuration.ILiveReloadWebServerConfiguration;
import org.jboss.tools.livereload.internal.util.Logger;

/**
 * The LiveReload Server that implements the livereload protocol (based on
 * websockets) and optionnaly provides a proxy server to inject
 * <code>&lt;SCRIPT&gt;</code> in the returned HTML pages.
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadWebServer {

	private LiveReloadCommandBroadcaster broadcaster;

	private LiveReloadWebServerRunnable liveReloadWebServerRunnable;

	/**
	 * Constructor
	 * 
	 * @param config
	 *            the LiveReload configuration to use.
	 * @throws UnknownHostException
	 */
	public LiveReloadWebServer(final IServer server, final ILiveReloadWebServerConfiguration config) throws UnknownHostException {
		if (config.isUseProxyServer()) {
			this.broadcaster = new LiveReloadCommandBroadcaster(config.getProxyServerPort());
			this.liveReloadWebServerRunnable = new LiveReloadWebServerRunnable(config.getProxyServerPort(),
					config.getWebsocketServerPort(), broadcaster);
		} else {
			this.broadcaster = new LiveReloadCommandBroadcaster(ServerUtils.getPort(server));
			this.liveReloadWebServerRunnable = new LiveReloadWebServerRunnable(config.getWebsocketServerPort(),
					broadcaster);
		}
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

	/**
	 * Receives notification when some resource with the given path changed.
	 * 
	 * @param path
	 *            the path of the resource that changed.
	 */
	public void notifyResourceChange(final String path) {
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

		private final LiveReloadCommandBroadcaster liveReloadCommandBroadcaster;

		/**
		 * Constructor to use when script injection proxy should be enabled
		 * 
		 * @param proxyConnectorPort
		 * @param websocketConnectorPort
		 * @param liveReloadCommandBroadcaster
		 * @throws UnknownHostException
		 */
		public LiveReloadWebServerRunnable(final int proxyConnectorPort, final int websocketConnectorPort,
				final LiveReloadCommandBroadcaster liveReloadCommandBroadcaster) throws UnknownHostException {
			this.server = new Server();
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
				final LiveReloadCommandBroadcaster liveReloadCommandBroadcaster) throws UnknownHostException {
			this.server = new Server();
			this.enableProxy = false;
			this.proxyConnectorPort = -1;
			this.websocketConnectorPort = websocketConnectorPort;
			this.liveReloadCommandBroadcaster = liveReloadCommandBroadcaster;
		}

		@Override
		public void run() {
			try {
				// connectors
				final String hostAddress = InetAddress.getLocalHost().getHostAddress();
				// TODO include SSL Connector
				final SelectChannelConnector websocketConnector = new SelectChannelConnector();
				websocketConnector.setPort(websocketConnectorPort);
				websocketConnector.setMaxIdleTime(0);
				server.addConnector(websocketConnector);
				if (enableProxy) {
					final SelectChannelConnector proxyConnector = new SelectChannelConnector();
					proxyConnector.setPort(proxyConnectorPort);
					proxyConnector.setMaxIdleTime(0);
					server.addConnector(proxyConnector);

					final HandlerCollection handlers = new HandlerCollection();
					server.setHandler(handlers);

					final ServletContextHandler liveReloadContext = new ServletContextHandler(handlers, "/",
							ServletContextHandler.NO_SESSIONS);

					// Livereload specific content
					liveReloadContext.addServlet(new ServletHolder(new LiveReloadWebSocketServlet(
							liveReloadCommandBroadcaster)), "/livereload");
					liveReloadContext.addFilter(new FilterHolder(new LiveReloadScriptFileFilter()),
							"/livereload/livereload.js", null);

					// Handling all applications behind the proxy
					liveReloadContext.addServlet(new ServletHolder(LiveReloadProxyServlet.class), "/");
					liveReloadContext.addFilter(new FilterHolder(new LiveReloadScriptInjectionFilter(hostAddress,
							this.websocketConnectorPort)), "/*", null);
				}
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
