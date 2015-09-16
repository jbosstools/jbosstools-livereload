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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
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

	/** The underlying connector. */
	private ServerConnector connector;

	/** network settings. */
	private final String proxyHost, targetHost;
	private final int proxyPort, targetPort;

	/**
	 * Constructor
	 * 
	 * @param proxyHost
	 * @param proxyPort
 	 * @param targetHost
	 * @param targetPort
	 * @param liveReloadPort
	 * @param allowRemoteConnections
	 * @param enableScriptInjection
	 * @throws UnknownHostException
	 */
	public LiveReloadProxyServer(final String proxyHost, final int proxyPort, final String targetHost, final int targetPort, final int liveReloadPort, final boolean allowRemoteConnections,
			final boolean enableScriptInjection) {
		super();
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.targetHost = targetHost;
		this.targetPort = targetPort;
		configure(proxyHost, proxyPort, targetHost, targetPort, liveReloadPort, allowRemoteConnections, enableScriptInjection);
	}

	private void configure(final String proxyHost, final int proxyPort, final String targetHost, final int targetPort, final int liveReloadPort, final boolean allowRemoteConnections,
			final boolean enableScriptInjection) {
		setAttribute(JettyServerRunner.NAME, "LiveReload-Proxy-Server-" + proxyPort + ":" + targetPort);
		final ServerConnector connector = new ServerConnector(this);
		// restrict access to clients on the same host
		if (!allowRemoteConnections) {
			connector.setHost(proxyHost);
		} 
		// allow remote connections
		else {
			connector.setHost(null);
		}
		connector.setPort(proxyPort);
		connector.setReuseAddress(true);
		//connector.setMaxIdleTime(0);
		addConnector(connector);

		final ServletContextHandler context = new ServletContextHandler();
		//context.setConnectorNames(new String[] { connector.getName() });
		final ServletHolder proxyServletHolder = new ServletHolder(new ApplicationsProxyServlet(proxyHost, proxyPort, targetHost, targetPort));
		//proxyServletHolder.setAsyncSupported(true);
		proxyServletHolder.setInitParameter("maxThreads", "256"); //$NON-NLS-1$ //$NON-NLS-2$
		context.addServlet(proxyServletHolder, "/*");
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
		return connector.getConnectedEndPoints().size();
	}
	
	/**
	 * @return the proxyHost
	 */
	public String getProxyHost() {
		return proxyHost;
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

	/**
	 * @return the targetHost
	 */
	public String getTargetHost() {
		return targetHost;
	}
	
	@Override
	public String toString() {
		return "Proxy Server (" + proxyPort + " -> " + targetPort + ")";
	}

}
