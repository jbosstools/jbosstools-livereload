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

package org.jboss.tools.livereload.core.internal.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.tools.livereload.core.internal.server.jetty.LiveReloadProxyServer;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadLaunchConfiguration;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadServerBehaviour;

/**
 * @author xcoulon
 * 
 */
public class WSTUtils {

	public static final String LIVERELOAD_RUNTIME_TYPE = "org.jboss.tools.livereload.serverTypeRuntime";
	public static final String LIVERELOAD_SERVER_TYPE = "org.jboss.tools.livereload.serverType";
	public static final String OPENSHIFT_SERVER_TYPE = "org.jboss.tools.openshift.express.openshift.server.type";
	public static final String HTTP_PREVIEW_SERVER_TYPE = "org.eclipse.wst.server.preview.server";
	public static final String HTTP_PREVIEW_PORT = "port";
	public static final String TEST_PREVIEW_SERVER_TYPE = "org.jboss.tools.livereload.test.previewServerType";
	public static final String TEST_PREVIEW_PORT = "port";
	public static final String JBOSSASAS_SERVER_PREFIX = "org.jboss.ide.eclipse.as.";
	public static final String JBOSSAS_SERVER_PORT = "org.jboss.ide.as.serverport";
	public static final String TOMCAT_SERVER_TYPE = "org.apache.tomcat.servertype";
	public static final String TOMCAT_SERVER_PORT = "org.apache.tomcat.serverport";

	/**
	 * Returns the list of the existing LiveReload servers.
	 * 
	 * @return the list of the existing LiveReload servers, or an empty list if
	 *         none exists yet.
	 * 
	 */
	public static List<IServer> findLiveReloadServers() {
		final List<IServer> liveReloadServers = new ArrayList<IServer>();
		for (IServer server : ServerCore.getServers()) {
			if (server.getServerType().getId().equals(LIVERELOAD_SERVER_TYPE)) {
				liveReloadServers.add(server);
			}
		}
		return liveReloadServers;
	}

	/**
	 * Returns the first existing LiveReload server.
	 * 
	 * @return the first existing LiveReload servers, or null if none exists
	 *         yet.
	 */
	public static IServer findLiveReloadServer() {
		for (IServer server : ServerCore.getServers()) {
			if (server.getServerType().getId().equals(LIVERELOAD_SERVER_TYPE)) {
				return server;
			}
		}
		return null;
	}

	/**
	 * Returns the {@link LiveReloadServerBehaviour} associated with the given
	 * {@link IServer}
	 * 
	 * @param server
	 * @return the associated server bahviour
	 */
	public static ServerBehaviourDelegate findServerBehaviour(final String serverId) {
		final IServer server = ServerCore.findServer(serverId);
		if (server != null) {
			return (ServerBehaviourDelegate) server.loadAdapter(ServerBehaviourDelegate.class, null);
		}
		return null;
	}

	/**
	 * Returns the {@link LiveReloadServerBehaviour} associated with the given
	 * {@link IServer} provided it is a LiveReload server
	 * 
	 * @param server
	 * @return the associated server bahviour
	 */
	public static ServerBehaviourDelegate findServerBehaviour(final IServer server) {
		if (server != null) {
			return (ServerBehaviourDelegate) server.loadAdapter(ServerBehaviourDelegate.class, null);
		}
		return null;
	}

	/**
	 * Attempts to retrieve the associated {@link IServer} for a given URL (with
	 * support for http:// and https:// schemes)
	 * 
	 * @param browserLocation
	 *            the browser location
	 * @return the enclosing Server or null if nothing matches.
	 */
	public static IServer extractServer(final String browserLocation) {
		return extractServer(browserLocation, Arrays.asList(ServerCore.getServers()));
	}

	/**
	 * Attempts to retrieve the associated {@link IServer} for a given URL (with
	 * support for http:// and https:// schemes) This method allows us to use a
	 * list of mock servers during the JUnit tests.
	 * 
	 * @param browserLocation
	 *            the browser location
	 * @param servers
	 *            the list of existing servers
	 * @return the enclosing Server or null if nothing matches.
	 */
	public static IServer extractServer(final String browserLocation, final List<IServer> servers) {
		try {
			final URL url = new URL(browserLocation);
			final int port = url.getPort();
			for (IServer server : servers) {
				// removed the host.equals(server.getHost()) comparison
				// FIXME support comparison between IP address and 'localhost' ?
				if (port == getWebPort(server)) {
					return server;
				}
				// special case for LiveReload Server that may run Proxy Servers
				// as well:
				if (server.getServerType().getId().equals(LIVERELOAD_SERVER_TYPE)) {
					@SuppressWarnings("unchecked")
					final Map<String, Integer> proxyPorts = (Map<String, Integer>) server.getAttribute(
							LiveReloadServerBehaviour.PROXY_PORTS, Collections.emptyMap());
					for (Entry<String, Integer> entry : proxyPorts.entrySet()) {
						final Integer serverProxiedPort = entry.getValue();
						if (port == serverProxiedPort) {
							final String serverId = entry.getKey();
							return ServerCore.findServer(serverId);
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			Logger.error("Unable to parse URL '" + browserLocation + "'", e);
		}
		return null;
	}

	/**
	 * Returns the Web Port for the given {@link IServer} or <code>-1</code> if
	 * the given server is not supported(yet).
	 * 
	 * @param server
	 * @return
	 */
	public static int getWebPort(final IServer server) {
		final String serverType = server.getServerType().getId();
		final Object adapter = server.loadAdapter(IJBossServer.class, null);
		// JBoss AS Server
		if (adapter != null && adapter instanceof IJBossServer) {
			return ((IJBossServer)adapter).getJBossWebPort();
		}
		if (serverType.equals(HTTP_PREVIEW_SERVER_TYPE)) {
			return server.getAttribute(HTTP_PREVIEW_PORT, 8080);
		}
		if (serverType.equals(TEST_PREVIEW_SERVER_TYPE)) {
			return server.getAttribute(TEST_PREVIEW_PORT, 8080);
		}
		if (serverType.equals(LIVERELOAD_SERVER_TYPE)) {
			return server.getAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, -1);
		}
		// if (TOMCAT_SERVER_TYPE.equals(serverType)) {
		// return server.getAttribute(TOMCAT_SERVER_PORT, -1);
		// }
		// Logger.warn("Unsupported server type: " +
		// server.getServerType().getName());
		return -1;
	}

	/**
	 * Returns true if the given server type is LiveReload
	 * 
	 * @param server
	 * @return
	 */
	public static boolean isLiveReloadServer(final IServer server) {
		return server != null && server.getServerType().getId().equals(LIVERELOAD_SERVER_TYPE);
	}

	/**
	 * Filters the given list and returns a list containing only server that are
	 * in {@link IServer.#STATE_STARTED} state.
	 * 
	 * @return a list of started server or empty list if none was started.
	 */
	public static List<IServer> filterStartedServers(final List<IServer> servers) {
		final List<IServer> startedServers = new ArrayList<IServer>();
		for (IServer server : servers) {
			if (server.getServerState() == IServer.STATE_STARTED) {
				startedServers.add(server);
			}
		}
		return startedServers;
	}

	/**
	 * Create a new LiveReload Server.
	 * 
	 * @param websocketPort
	 *            the web socket port
	 * @param enableProxy
	 * @param injectScript
	 * @param allowRemoteConnections
	 * @return the server
	 * @throws CoreException
	 */
	public static IServer createLiveReloadServer(final int websocketPort, final boolean enableProxy,
			final boolean injectScript, final boolean allowRemoteConnections) throws CoreException {
		final String serverName = "LiveReload Server at localhost";
		IRuntimeType rt = ServerCore.findRuntimeType(LIVERELOAD_RUNTIME_TYPE);
		IRuntimeWorkingCopy rwc = rt.createRuntime(null, null);
		IRuntime runtime = rwc.save(true, null);
		IServerType st = ServerCore.findServerType(LIVERELOAD_SERVER_TYPE);
		IServerWorkingCopy swc = (IServerWorkingCopy) st.createServer(serverName, null, null);
		swc.setServerConfiguration(null);
		swc.setName(serverName);
		swc.setRuntime(runtime);
		swc.setAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, websocketPort);
		swc.setAttribute(LiveReloadLaunchConfiguration.ENABLE_PROXY_SERVER, enableProxy);
		swc.setAttribute(LiveReloadLaunchConfiguration.ENABLE_SCRIPT_INJECTION, injectScript);
		swc.setAttribute(LiveReloadLaunchConfiguration.ALLOW_REMOTE_CONNECTIONS, allowRemoteConnections);
		return swc.save(true, new NullProgressMonitor());
	}

	/**
	 * finds the {@link LiveReloadProxyServer} for the given {@link IServer}
	 * 
	 * @param appServer
	 *            the server that is proxied
	 * @return the proxy server or null if none exists
	 */
	public static LiveReloadProxyServer findLiveReloadProxyServer(final IServer appServer) {
		for (IServer liveReloadServer : findLiveReloadServers()) {
			if (isServerStarted(liveReloadServer)) {
				LiveReloadServerBehaviour serverBehaviour = (LiveReloadServerBehaviour) liveReloadServer
						.getAdapter(ServerBehaviourDelegate.class);
				if (serverBehaviour != null && serverBehaviour.getProxyServers().containsKey(appServer)) {
					return serverBehaviour.getProxyServers().get(appServer);
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the given {@link IServer} is started.
	 * 
	 * @param appServer
	 *            the server to check
	 * @return true if the given server is started, false oherwise.
	 */
	public static boolean isServerStarted(final IServer appServer) {
		return appServer.getServerState() == IServer.STATE_STARTED;
	}
	
	/**
	 * Starts or restarts the given {@link IServer} within the given interval of time.
	 * @param liveReloadServer
	 * @param timeout
	 * @param unit
	 * @throws CoreException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static void startOrRestartServer(final IServer liveReloadServer, final int timeout, final TimeUnit unit)
			throws TimeoutException, InterruptedException, ExecutionException {
		Future<?> future = Executors.newSingleThreadExecutor().submit(new Runnable() {
			@Override
			public void run() {
				try {
					final Long limitTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, unit);
					if (liveReloadServer.getServerState() == IServer.STATE_STARTED
							&& liveReloadServer.getServerRestartState()) {
						Logger.info("Restarting the server {}", liveReloadServer.getName());
						liveReloadServer.restart(ILaunchManager.RUN_MODE, new NullProgressMonitor());
					} else if (liveReloadServer.getServerState() == IServer.STATE_STOPPED) {
						Logger.info("Starting the server {}", liveReloadServer.getName());
						liveReloadServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
					}
					while (liveReloadServer.getServerState() != IServer.STATE_STARTED && System.currentTimeMillis() < limitTime) {
						TimeUnit.MILLISECONDS.sleep(500);
					}
				} catch (Exception e) {
					Logger.error("Failed (re)start Livereload Server", e);
				}

			}
		});
		future.get(timeout, unit);
	}

}
