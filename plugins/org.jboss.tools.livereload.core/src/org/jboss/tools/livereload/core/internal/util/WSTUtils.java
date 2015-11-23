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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jst.server.tomcat.core.internal.TomcatServerBehaviour;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.tools.livereload.core.internal.server.jetty.LiveReloadProxyServer;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadLaunchConfiguration;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadServerBehaviour;
import org.jboss.tools.livereload.core.internal.util.TimeoutUtils.TaskMonitor;

/**
 * @author xcoulon
 * 
 */
@SuppressWarnings("restriction")
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
	public static final String TOMCAT_60_SERVER_TYPE = "org.eclipse.jst.server.tomcat.60";
	public static final String TOMCAT_70_SERVER_TYPE = "org.eclipse.jst.server.tomcat.70";

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
			if (isLiveReloadServer(server)) {
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
			if (isLiveReloadServer(server)) {
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
				if (isLiveReloadServer(server)) {
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
		Logger.debug("Could not identify server from client location " + browserLocation);
		return null;
	}

	/**
	 * Returns the Web Port for the given {@link IServer}, <code>8080</code> if the server is of an unknown type or <code>-1</code> if
	 * the server is not started or if the server type is unknown.
	 * 
	 * @param server
	 * @return
	 */
	public static int getWebPort(final IServer server) {
		// ignore not-started servers and unknown server types
		if(server.getServerState() != IServer.STATE_STARTED || server.getServerType() == null) {
			return -1;
		}
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
		if(TOMCAT_70_SERVER_TYPE.equals(serverType) || TOMCAT_60_SERVER_TYPE.equals(serverType)) {
			final TomcatServerBehaviour tomcatServer = (TomcatServerBehaviour) server.getAdapter(ServerBehaviourDelegate.class);
			for(ServerPort port: tomcatServer.getTomcatServer().getServerPorts()) {
				if("HTTP".equals(port.getProtocol())) {
					return port.getPort();
				}
			}
		}
		// default assumption for unknown specific server type...
		Logger.debug("Assuming that server '" + server.getName() + "' is running on port 8080. LiveReload may not work as expected if it is not the case.");
		return 8080;
	}

	/**
	 * @return true if the given server type is LiveReload, false otherwise (null server, wrong or unknown server type)
	 * 
	 * @param server the server to check
	 */
	public static boolean isLiveReloadServer(final IServer server) {
		return server != null && server.getServerType() != null && LIVERELOAD_SERVER_TYPE.equals(server.getServerType().getId());
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
	public static IServer createLiveReloadServer(final int websocketPort, 
			final boolean injectScript, final boolean allowRemoteConnections) throws CoreException {
		return createLiveReloadServer("LiveReload Server at localhost", "localhost", websocketPort, injectScript, allowRemoteConnections);
	}
	
	/**
	 * Create a new LiveReload Server.
	 * 
	 * @param serverName the server name (or Id)
	 * @param websocketPort
	 *            the web socket port
	 * @param enableProxy
	 * @param injectScript
	 * @param allowRemoteConnections
	 * @return the server
	 * @throws CoreException
	 */
	public static IServer createLiveReloadServer(final String serverName, final String hostname, final int websocketPort, 
			final boolean injectScript, final boolean allowRemoteConnections) throws CoreException {
		IRuntimeType rt = ServerCore.findRuntimeType(LIVERELOAD_RUNTIME_TYPE);
		IRuntimeWorkingCopy rwc = rt.createRuntime(null, null);
		IRuntime runtime = rwc.save(true, null);
		IServerType st = ServerCore.findServerType(LIVERELOAD_SERVER_TYPE);
		IServerWorkingCopy swc = (IServerWorkingCopy) st.createServer(serverName, null, null);
		swc.setServerConfiguration(null);
		swc.setName(serverName);
		swc.setHost(hostname);
		swc.setRuntime(runtime);
		swc.setAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, websocketPort);
		swc.setAttribute(LiveReloadLaunchConfiguration.ENABLE_PROXY_SERVER, true);
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
	 * Creates a {@link Job} that will start or restart the given {@link IServer} within the given interval of time. 
	 * @param server
	 * @param timeout
	 * @param unit
	 * @return a Job not scheduled yet.
	 * @throws CoreException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static Job startOrRestartServer(final IServer server, final int timeout, final TimeUnit unit)
			throws TimeoutException, InterruptedException, ExecutionException {
		if (server == null) {
			return null;
		}
		final boolean needsRestart = server.getServerState() == IServer.STATE_STOPPING || server.getServerState() == IServer.STATE_STARTING || server.getServerState() == IServer.STATE_STARTED;
		final String jobMessage = (needsRestart ? "Restarting " : "Starting ") + server.getName() + "...";
		return new Job(jobMessage) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final Long limitTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, unit);
					if (server.getServerState() == IServer.STATE_STARTING || server.getServerState() == IServer.STATE_STARTED) {
						server.stop(true);
					}
					if(server.getServerState() == IServer.STATE_STARTING || server.getServerState() == IServer.STATE_STARTED || server.getServerState() == IServer.STATE_STOPPING) {
						while (server.getServerState() != IServer.STATE_STOPPED && System.currentTimeMillis() < limitTime
								&& !monitor.isCanceled()) {
							TimeUnit.MILLISECONDS.sleep(500);
						}
					}
					if(monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					// the timeout should not be higher that Integer.MAX_VALUE seconds
					final int ticks = (int)TimeUnit.SECONDS.convert(timeout, unit); 
					final SubMonitor subMonitor = SubMonitor.convert(monitor, ticks);
					server.start(ILaunchManager.RUN_MODE, subMonitor);
					// using a ServerListener to catch start progression (faster than blocking until timetout in case of server startup failure)
					while (server.getServerState() != IServer.STATE_STARTED && System.currentTimeMillis() < limitTime
							&& !monitor.isCanceled()) {
						TimeUnit.MILLISECONDS.sleep(500);
					}
					subMonitor.done();
					if (server.getServerState() != IServer.STATE_STARTED) {
						if (needsRestart) {
							final String errorMessage = "Failed to restart " + server.getName() + " before timeout";
							Logger.error(errorMessage);
						} else {
							final String errorMessage = "Failed to start " + server.getName() + " before timeout"; 
							Logger.error(errorMessage);
						}
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				} catch (Exception e) {
					if (needsRestart) {
						final String errorMessage = "Failed to restart " + server.getName() + " before timeout";
						Logger.error(errorMessage);
					} else {
						final String errorMessage = "Failed to start " + server.getName() + " before timeout"; 
						Logger.error(errorMessage);
					}
					return Status.CANCEL_STATUS;
				}
			}
		};
	}

	public static void stop(final IServer server, final long duration, final TimeUnit unit) throws TimeoutException {
		if (server.canStop().isOK()) {
			final ServerListener listener = new ServerListener(server);
			try {
				server.stop(true);
				TimeoutUtils.timeout(new TaskMonitor() {
					@Override
					public boolean isComplete() {
						return listener.serverStopped;
					}
				}, duration, unit);
			} finally {
				listener.dispose();
			}
		}
	}
	
	public static void restart(final IServer server, final long duration, final TimeUnit unit) {
		final ServerListener listener = new ServerListener(server);
		try {
			server.restart(ILaunchManager.RUN_MODE, new NullProgressMonitor());
			TimeoutUtils.timeout(new TaskMonitor() {
				@Override
				public boolean isComplete() {
					return listener.serverStopped && listener.serverStarted;
				}
			}, duration, unit);
		} finally {
			listener.dispose();
		}
	}
	
	static class ServerListener implements IServerListener {

		private final IServer server;
		public boolean serverStopped = false;
		public boolean serverStarted = false;
		public boolean serverStarting = false;
		public boolean serverStopping = false;
		
		public ServerListener(IServer server) {
			this.server = server;
			server.addServerListener(this);
		}

		public void dispose() {
			server.removeServerListener(this);
		}
		
		@Override
		public void serverChanged(ServerEvent event) {
			if(event.getServer().getServerState() == IServer.STATE_STOPPED) {
				Logger.debug("Server stopped");
				serverStopped = true;
			} else if(event.getServer().getServerState() == IServer.STATE_STOPPING) {
				Logger.debug("Server stopping");
				serverStopping = true;
			} else if(event.getServer().getServerState() == IServer.STATE_STARTING) {
				Logger.debug("Server starting");
				serverStarting = true;
			} else if(event.getServer().getServerState() == IServer.STATE_STARTED) {
				Logger.debug("Server started");
				serverStarted = true;
			}
		}

		/**
		 * Reset all states
		 */
		public void reset() {
			this.serverStopped = false;
			this.serverStarted = false;
			this.serverStarting = false;
			this.serverStopping = false;			
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((server == null) ? 0 : server.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ServerListener other = (ServerListener) obj;
			if (server == null) {
				if (other.server != null) {
					return false;
				}
			} else if (!server.getName().equals(other.server)) {
				return false;
			}
			return true;
		}
	}

}
