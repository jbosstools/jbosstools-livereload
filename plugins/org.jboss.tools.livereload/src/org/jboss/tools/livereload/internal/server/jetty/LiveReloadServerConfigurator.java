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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Factory to initialize the WebServer with websocket support, optional script
 * injection proxy (for server mode) or resource server (for directory mode).
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadServerConfigurator {

	private static final String MIN_WEB_SOCKET_PROTOCOL_VERSION = "minVersion";

	private static final String MIN_WEB_SOCKET_PROTOCOL_VERSION_VALUE = "-1";

	public static ServerBuilder initServer(final int websocketPort) {
		return new ServerBuilder(websocketPort);
	}

	public static class ServerBuilder {
		
		private static final String MIN_WEB_SOCKET_PROTOCOL_VERSION = "minVersion";
		
		private static final String MIN_WEB_SOCKET_PROTOCOL_VERSION_VALUE = "-1";

		private int websocketPort;

		public ServerBuilder(final int websocketPort) {
			this.websocketPort = websocketPort;
		}

		public Server build() {
			final Server server = new Server();
			// connectors
			// TODO include SSL Connector
			final SelectChannelConnector websocketConnector = new SelectChannelConnector();
			websocketConnector.setPort(websocketPort);
			websocketConnector.setMaxIdleTime(0);
			server.addConnector(websocketConnector);
			final HandlerCollection handlers = new HandlerCollection();
			server.setHandler(handlers);
			final ServletContextHandler liveReloadContext = new ServletContextHandler(handlers, "/",
					ServletContextHandler.NO_SESSIONS);
			// //liveReloadContext.addFilter(new FilterHolder(new
			// LiveReloadScriptFileFilter()),
			// // "/livereload.js", null);

			ServletHolder liveReloadServletHolder = new ServletHolder(new LiveReloadWebSocketServlet());
			// Fix for BrowserSim (Safari) due to check in WebSocketFactory
			liveReloadServletHolder.setInitParameter(MIN_WEB_SOCKET_PROTOCOL_VERSION,
					MIN_WEB_SOCKET_PROTOCOL_VERSION_VALUE); 
			liveReloadContext.addServlet(liveReloadServletHolder, "/livereload");
			liveReloadContext.addServlet(new ServletHolder(new LiveReloadScriptFileServlet()), "/livereload/livereload.js");
			liveReloadContext.addFilter(new FilterHolder(new LiveReloadScriptInjectionFilter("localhost", websocketPort)), "/*", null);
			liveReloadContext.addServlet(new ServletHolder(new WorkspaceFileServlet()), "/*");
			// if (enableProxy) {
			// final SelectChannelConnector proxyConnector = new
			// SelectChannelConnector();
			// proxyConnector.setPort(proxyPort);
			// proxyConnector.setMaxIdleTime(0);
			// server.addConnector(proxyConnector);
			//
			// // Livereload specific content
			// // liveReloadContext.addServlet(new ServletHolder(new
			// LiveReloadWebSocketServlet(
			// // liveReloadCommandBroadcaster)), "/livereload");
			//
			// // Handling all applications behind the proxy
			// liveReloadContext.addServlet(new
			// ServletHolder(ApplicationsProxyServlet.class), "/");
			// final String hostAddress =
			// InetAddress.getLocalHost().getHostAddress();
			// liveReloadContext.addFilter(new FilterHolder(new
			// LiveReloadScriptInjectionFilter(hostAddress,
			// this.websocketPort)), "/*", null);
			// }
			return server;
		}

	}
}
