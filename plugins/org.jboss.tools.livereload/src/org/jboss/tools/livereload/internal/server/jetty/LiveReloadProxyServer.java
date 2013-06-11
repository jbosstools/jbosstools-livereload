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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * The LiveReload Server that implements the livereload protocol (based on
 * websockets) and optionnaly provides a proxy server to inject
 * <code>&lt;SCRIPT&gt;</code> in the returned HTML pages.
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadProxyServer extends Server {

	private SelectChannelConnector connector;

	private final int proxyPort, targetPort;

	/**
	 * Constructor
	 * 
	 * @param config
	 *            the LiveReload configuration to use.
	 * @throws UnknownHostException
	 */
	public LiveReloadProxyServer(final int proxyPort, final int targetPort, final int liveReloadPort, final boolean allowRemoteConnections,
			final boolean enableScriptInjection) {
		super();
		this.proxyPort = proxyPort;
		this.targetPort = targetPort;
		configure(proxyPort, targetPort, liveReloadPort, allowRemoteConnections, enableScriptInjection);
	}

	private void configure(final int proxyPort, final int targetPort, final int liveReloadPort, final boolean allowRemoteConnections,
			final boolean enableScriptInjection) {
		setAttribute(JettyServerRunner.NAME, "LiveReload-Proxy-Server-" + proxyPort + ":" + targetPort);
		final SelectChannelConnector connector = new SelectChannelConnector();
		if (!allowRemoteConnections) {
			connector.setHost("localhost");
		}
		connector.setPort(proxyPort);
		connector.setMaxIdleTime(0);
		addConnector(connector);

		final ServletContextHandler context = new ServletContextHandler();
		context.setConnectorNames(new String[] { connector.getName() });
		ServletHolder servletHolder = new ServletHolder(new ApplicationsProxyServlet(targetPort));
		context.addServlet(servletHolder, "/*");
		setHandler(context);
		if (enableScriptInjection) {
			context.addFilter(new FilterHolder(new LiveReloadScriptInjectionFilter(liveReloadPort)), "/*", null);
		}
	}

	/**
	 * Returns the number of connections on the websocket connector.
	 * 
	 * @return the number of connections on the websocket connector.
	 */
	public int getNumberOfConnectedClients() {
		return connector.getConnections();
	}
	
	/**
	 * @return the proxyPort
	 */
	public int getProxyPort() {
		return proxyPort;
	}

	/**
	 * @return the targetPort
	 */
	public int getTargetPort() {
		return targetPort;
	}

	@Override
	public String toString() {
		return "Proxy Server (" + proxyPort + " -> " + targetPort + ")";
	}

	public String getProxyHost() {
		return "localhost";
	}

}
