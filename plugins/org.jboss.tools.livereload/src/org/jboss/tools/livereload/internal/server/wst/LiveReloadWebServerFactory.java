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

package org.jboss.tools.livereload.internal.server.wst;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.internal.server.jetty.ApplicationsProxyServlet;
import org.jboss.tools.livereload.internal.server.jetty.LiveReloadScriptFileFilter;
import org.jboss.tools.livereload.internal.server.jetty.LiveReloadScriptInjectionFilter;
import org.jboss.tools.livereload.internal.server.jetty.LiveReloadWebSocketServlet;

/**
 * Factory to initialize the WebServer with websocket support, optional script
 * injection proxy (for server mode) or resource server (for directory mode).
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadWebServerFactory {

	public static ServerBuilder onServer(final IServer server, final int websocketPort) {
		ServerBuilder builder = new ServerBuilder(server, websocketPort);
		return builder;
	}

	
	public static class ServerBuilder {

		private final IServer server;

		private final int websocketPort;

		private boolean enableProxy = false;

		private int proxyPort = -1;
		
		public ServerBuilder(final IServer server, final int websocketPort) {
			this.server = server;
			this.websocketPort = websocketPort;
		}
		
		public ServerBuilder enableProxy(final boolean enableProxy, final int proxyPort) {
			this.enableProxy = enableProxy;
			this.proxyPort = proxyPort;
			return this;
		}
		
		public Server build() throws UnknownHostException {
			final Server server = new Server();
			// connectors
			final String hostAddress = InetAddress.getLocalHost().getHostAddress();
			// TODO include SSL Connector
			final SelectChannelConnector websocketConnector = new SelectChannelConnector();
			websocketConnector.setPort(websocketPort);
			websocketConnector.setMaxIdleTime(0);
			server.addConnector(websocketConnector);
			if (enableProxy) {
				final SelectChannelConnector proxyConnector = new SelectChannelConnector();
				proxyConnector.setPort(proxyPort);
				proxyConnector.setMaxIdleTime(0);
				server.addConnector(proxyConnector);

				final HandlerCollection handlers = new HandlerCollection();
				server.setHandler(handlers);

				final ServletContextHandler liveReloadContext = new ServletContextHandler(handlers, "/",
						ServletContextHandler.NO_SESSIONS);

				// Livereload specific content
//				liveReloadContext.addServlet(new ServletHolder(new LiveReloadWebSocketServlet(
//						liveReloadCommandBroadcaster)), "/livereload");
				liveReloadContext.addServlet(new ServletHolder(new LiveReloadWebSocketServlet(
						null)), "/livereload");
				liveReloadContext.addFilter(new FilterHolder(new LiveReloadScriptFileFilter()),
						"/livereload/livereload.js", null);

				// Handling all applications behind the proxy
				liveReloadContext.addServlet(new ServletHolder(ApplicationsProxyServlet.class), "/");
				liveReloadContext.addFilter(new FilterHolder(new LiveReloadScriptInjectionFilter(hostAddress,
						this.websocketPort)), "/*", null);
			}
			return server;
		}
		
	}
}
