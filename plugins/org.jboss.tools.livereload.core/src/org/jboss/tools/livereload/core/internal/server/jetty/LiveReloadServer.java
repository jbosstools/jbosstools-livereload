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

import java.util.EventObject;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
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
public class LiveReloadServer extends Server implements Subscriber {

	private static final String MIN_WEB_SOCKET_PROTOCOL_VERSION = "minVersion";

	private static final String MIN_WEB_SOCKET_PROTOCOL_VERSION_VALUE = "-1";

	private ServerConnector websocketConnector;

	private final int websocketPort;

	private final String hostname;

	private int connectedClients = 0;

	/**
	 * Constructor
	 * @param name the server name (appears in the Servers Views)
	 * @param websocketPort the websocket port
	 * @param allowRemoteConnections flag to allow remote connections
	 * @param enableScriptInjection flag to enable script injection
	 */
	public LiveReloadServer(final String name, final String hostname, final int websocketPort, 
			final boolean allowRemoteConnections, final boolean enableScriptInjection) {
		super();
		this.websocketPort = websocketPort;
		this.hostname = hostname;
		configure(name, hostname, websocketPort, allowRemoteConnections, enableScriptInjection);
	}

	/**
	 * Configure the Jetty Server with the given parameters
	 * @param name the server name (same as the Server Adapter)
	 * @param websocketPort the websockets port
	 * @param allowRemoteConnections should allow remote connections
	 * @param enableScriptInjection should inject livereload.js script in returned HTML pages
	 */
	private void configure(final String name, final String hostname, final int websocketPort, 
			final boolean allowRemoteConnections, final boolean enableScriptInjection) {
		setAttribute(JettyServerRunner.NAME, name);
		websocketConnector = new ServerConnector(this);
		websocketConnector.setReuseAddress(true);
		if (!allowRemoteConnections) {
			websocketConnector.setHost(hostname);
		}
		websocketConnector.setPort(websocketPort);
		addConnector(websocketConnector);
		final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		final ServletHolder liveReloadServletHolder = new ServletHolder("ws-events", new LiveReloadWebSocketServlet());
		// Fix for BrowserSim (Safari) due to check in WebSocketFactory
		liveReloadServletHolder
				.setInitParameter(MIN_WEB_SOCKET_PROTOCOL_VERSION, MIN_WEB_SOCKET_PROTOCOL_VERSION_VALUE);
		context.addServlet(liveReloadServletHolder, "/livereload");
		context.addServlet(new ServletHolder(new LiveReloadScriptFileServlet()), "/livereload.js");
		final ServletHolder servlet = new ServletHolder(new WorkspaceFileServlet());
		servlet.setInitParameter(WorkspaceFileServlet.BASE_PATH, "/_workspace");
		context.addServlet(servlet, servlet.getInitParameter(WorkspaceFileServlet.BASE_PATH) + "/*");
		final LiveReloadScriptInjectionMiddleManServlet middleManServlet = new LiveReloadScriptInjectionMiddleManServlet(
				websocketConnector.getHost(), websocketPort, "/", websocketConnector.getHost(), websocketPort,
				servlet.getInitParameter(WorkspaceFileServlet.BASE_PATH), websocketPort, enableScriptInjection);
		context.addServlet(new ServletHolder(middleManServlet), "/");
		setHandler(context);
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

	public String getHost() {
		return hostname;
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
