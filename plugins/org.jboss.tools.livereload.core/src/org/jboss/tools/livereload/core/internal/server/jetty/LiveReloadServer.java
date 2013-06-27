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
import java.util.EventObject;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.tools.livereload.core.internal.service.EventService;
import org.jboss.tools.livereload.core.internal.service.LiveReloadClientConnectedEvent;
import org.jboss.tools.livereload.core.internal.service.LiveReloadClientConnectionFilter;
import org.jboss.tools.livereload.core.internal.service.LiveReloadClientDisconnectedEvent;
import org.jboss.tools.livereload.core.internal.service.Subscriber;

/**
 * The LiveReload Server that implements the livereload protocol (based on
 * websockets) and optionnaly provides a proxy server to inject
 * <code>&lt;SCRIPT&gt;</code> in the returned HTML pages.
 * 
 * @author xcoulon
 * 
 */
@SuppressWarnings("unchecked")
public class LiveReloadServer extends Server implements Subscriber {

	private static final String MIN_WEB_SOCKET_PROTOCOL_VERSION = "minVersion";

	private static final String MIN_WEB_SOCKET_PROTOCOL_VERSION_VALUE = "-1";

	private SelectChannelConnector websocketConnector;

	private final int websocketPort;

	private int connectedClients = 0;

	/**
	 * Constructor
	 * 
	 * @param config
	 *            the LiveReload configuration to use.
	 * @throws UnknownHostException
	 */
	public LiveReloadServer(final String name, final int websocketPort, final boolean enableProxyServer,
			final boolean allowRemoteConnections, final boolean enableScriptInjection) {
		super();
		this.websocketPort = websocketPort;
		configure(name, websocketPort, enableProxyServer, allowRemoteConnections, enableScriptInjection);
	}

	/**
	 * Configure the Jetty Server with the given parameters
	 * @param name the server name (same as the Server Adapter)
	 * @param websocketPort the websockets port
	 * @param enableProxyServer should proxy be enabled 
	 * @param allowRemoteConnections should allow remote connections
	 * @param enableScriptInjection should inject livereload.js script in returned HTML pages
	 */
	private void configure(final String name, final int websocketPort, final boolean enableProxyServer,
			final boolean allowRemoteConnections, final boolean enableScriptInjection) {
		setAttribute(JettyServerRunner.NAME, name);
		websocketConnector = new SelectChannelConnector();
		if (!allowRemoteConnections) {
			websocketConnector.setHost("localhost");
		}
		websocketConnector.setStatsOn(true);
		websocketConnector.setPort(websocketPort);
		websocketConnector.setMaxIdleTime(0);
		addConnector(websocketConnector);
		final HandlerCollection handlers = new HandlerCollection();
		setHandler(handlers);
		final ServletContextHandler context = new ServletContextHandler(handlers, "/",
				ServletContextHandler.NO_SESSIONS);
		context.setConnectorNames(new String[] { websocketConnector.getName() });
		ServletHolder liveReloadServletHolder = new ServletHolder(new LiveReloadWebSocketServlet());
		// Fix for BrowserSim (Safari) due to check in WebSocketFactory
		liveReloadServletHolder
				.setInitParameter(MIN_WEB_SOCKET_PROTOCOL_VERSION, MIN_WEB_SOCKET_PROTOCOL_VERSION_VALUE);
		context.addServlet(liveReloadServletHolder, "/livereload");
		context.addServlet(new ServletHolder(new LiveReloadScriptFileServlet()), "/livereload.js");
		if (enableProxyServer) {
			context.addServlet(new ServletHolder(new WorkspaceFileServlet()), "/*");
			if (enableScriptInjection) {
				context.addFilter(new FilterHolder(new LiveReloadScriptInjectionFilter(websocketPort)), "/*", null);
			}
		}
		EventService.getInstance().subscribe(this, new LiveReloadClientConnectionFilter());
	}

	/**
	 * Returns the number of connections on the websocket connector.
	 * 
	 * @return the number of connections on the websocket connector.
	 */
	public int getNumberOfConnectedClients() {
		return connectedClients;
	}

	public int getPort() {
		return websocketPort;
	}

	@Override
	public String toString() {
		return "LiveReload Server";
	}

	@Override
	public void inform(EventObject event) {
		if(event instanceof LiveReloadClientConnectedEvent) {
			this.connectedClients++;
		} else if(event instanceof LiveReloadClientDisconnectedEvent) {
			this.connectedClients--;
			if(connectedClients < 0) {
				connectedClients = 0;
			}
		}

	}

	@Override
	public String getId() {
		return toString();
	}
}
