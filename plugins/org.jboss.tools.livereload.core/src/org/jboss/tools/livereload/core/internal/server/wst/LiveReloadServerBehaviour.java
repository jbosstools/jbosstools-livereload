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

package org.jboss.tools.livereload.core.internal.server.wst;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.SocketUtil;
import org.jboss.tools.livereload.core.internal.JBossLiveReloadCoreActivator;
import org.jboss.tools.livereload.core.internal.server.jetty.JettyServerRunner;
import org.jboss.tools.livereload.core.internal.server.jetty.LiveReloadProxyServer;
import org.jboss.tools.livereload.core.internal.server.jetty.LiveReloadServer;
import org.jboss.tools.livereload.core.internal.service.EventService;
import org.jboss.tools.livereload.core.internal.service.LiveReloadClientRefreshFilter;
import org.jboss.tools.livereload.core.internal.service.LiveReloadClientRefreshedEvent;
import org.jboss.tools.livereload.core.internal.service.LiveReloadClientRefreshingEvent;
import org.jboss.tools.livereload.core.internal.service.ServerLifeCycleListener;
import org.jboss.tools.livereload.core.internal.service.ServerStartedAndStoppedFilter;
import org.jboss.tools.livereload.core.internal.service.ServerStartedEvent;
import org.jboss.tools.livereload.core.internal.service.ServerStoppedEvent;
import org.jboss.tools.livereload.core.internal.service.Subscriber;
import org.jboss.tools.livereload.core.internal.service.WorkspaceResourceChangedListener;
import org.jboss.tools.livereload.core.internal.util.Logger;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadServerBehaviour extends ServerBehaviourDelegate implements Subscriber {

	public static final String PROXY_PORTS = "proxy_ports";
	/** The LiveReload Server that runs a jetty server. */
	private LiveReloadServer liveReloadServer;
	/** The LiveReload Proxies that runs on separate jetty servers. */
	private final Map<IServer, LiveReloadProxyServer> proxyServers = new HashMap<IServer, LiveReloadProxyServer>();
	private final Map<IServer, JettyServerRunner> proxyRunners = new HashMap<IServer, JettyServerRunner>();

	private WorkspaceResourceChangedListener resourceChangeListener;
	private ServerLifeCycleListener serverLifeCycleListener;
	private JettyServerRunner liveReloadServerRunnable;
	private int websocketPort;

	@Override
	protected void initialize(IProgressMonitor monitor) {
		super.initialize(monitor);
		// register for Server started and Server stopped events
		EventService.getInstance().subscribe(this, new ServerStartedAndStoppedFilter());
		EventService.getInstance().subscribe(this, new LiveReloadClientRefreshFilter());
	}

	/**
	 * Needs to properly stop everything when the LiveReload {@link IServer} is
	 * deleted.
	 */
	@Override
	public void dispose() {
		super.dispose();
		try {
			if (liveReloadServer != null) {
				liveReloadServer.stop();
				for (Iterator<Entry<IServer, JettyServerRunner>> iterator = proxyRunners.entrySet().iterator(); iterator
						.hasNext();) {
					Entry<IServer, JettyServerRunner> entry = iterator.next();
					JettyServerRunner runner = entry.getValue();
					JettyServerRunner.stop(runner);
					iterator.remove();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public IStatus canPublish() {
		return Status.OK_STATUS;
	}

	@Override
	public IStatus canStart(String launchMode) {
		return Status.OK_STATUS;
	}

	/**
	 * Starts the {@link LiveReloadServer} which is responsable for the embedded
	 * web/websocket/proxy server configuration and lifecycle
	 * 
	 * @param serverBehaviour
	 * @throws CoreException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public void startServer() throws CoreException {
		try {
			if(SocketUtil.isPortInUse(websocketPort)) {
				throw new CoreException(new Status(IStatus.ERROR, JBossLiveReloadCoreActivator.PLUGIN_ID, "Cannot start LiveReload server because port " + websocketPort + " is already in use."));
			}
			// set the server status to "Starting"
			setServerStarting();
			// now, let's init and start the embedded jetty server from the
			// server attributes
			final IServer server = getServer();
			// retrieve the websocket port, use the default value if it was missing
			this.websocketPort = server.getAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, LiveReloadLaunchConfiguration.DEFAULT_WEBSOCKET_PORT);
			// fix the new default behaviour: proxy is now always enabled
			if(!isProxyEnabled()) {
				setProxyEnabled(true);
			}
			final boolean allowRemoteConnections = isRemoteConnectionsAllowed();
			final boolean enableScriptInjection = isScriptInjectionEnabled();
			this.liveReloadServer = new LiveReloadServer(server.getName(), server.getHost(), this.websocketPort, allowRemoteConnections,
					enableScriptInjection);
			this.liveReloadServerRunnable = JettyServerRunner.start(liveReloadServer);
			if(!this.liveReloadServerRunnable.isSuccessfullyStarted()) { 
				setServerStopped();
			} else {
				// listen to file changes in the workspace
				addWorkspaceResourceChangeListener();
				// listen to server lifecycles
				addServerLifeCycleListener();
				// set the server status to "Started"
				setServerStarted();
			}
		} catch (TimeoutException e) {
			setServerStopped();
			throw new CoreException(new Status(IStatus.ERROR, JBossLiveReloadCoreActivator.PLUGIN_ID, e.getMessage(), e));
		}
	}

	/**
	 * @param server
	 * @return
	 */
	public boolean isScriptInjectionEnabled() {
		return getServer().getAttribute(LiveReloadLaunchConfiguration.ENABLE_SCRIPT_INJECTION, false);
	}
	
	/**
	 * Method to configure if script injection is allowed or not
	 * @param allowScriptInjection true to allow, false otherwise.
	 * @throws CoreException 
	 */
	public void setScriptInjectionAllowed(boolean allowScriptInjection) throws CoreException {
		configureServerAttribute(LiveReloadLaunchConfiguration.ENABLE_SCRIPT_INJECTION, allowScriptInjection);
	}

	/**
	 * @param server
	 * @return
	 */
	public boolean isRemoteConnectionsAllowed() {
		return getServer().getAttribute(LiveReloadLaunchConfiguration.ALLOW_REMOTE_CONNECTIONS, false);
	}
	
	/**
	 * Method to configure if remote connections are allowed or not
	 * @param allowRemoteConnections true to allow, false otherwise.
	 * @throws CoreException 
	 */
	public void setRemoteConnectionsAllowed(boolean allowRemoteConnections) throws CoreException {
		configureServerAttribute(LiveReloadLaunchConfiguration.ALLOW_REMOTE_CONNECTIONS, allowRemoteConnections);
	}

	/**
	 * @param server
	 * @return
	 */
	public boolean isProxyEnabled() {
		return getServer().getAttribute(LiveReloadLaunchConfiguration.ENABLE_PROXY_SERVER, false);
	}

	/**
	 * Method to configure if proxy is enabled or not
	 * @param enableProxy true to allow, false otherwise.
	 * @throws CoreException 
	 */
	public void setProxyEnabled(boolean enableProxy) throws CoreException {
		configureServerAttribute(LiveReloadLaunchConfiguration.ENABLE_PROXY_SERVER, enableProxy);
	}

	/**
	 * Sets the attribute value in the underlying {@link IServer}
	 * @param attributeName
	 * @param attributeValue
	 * @throws CoreException
	 */
	private void configureServerAttribute(final String attributeName, boolean attributeValue) throws CoreException {
		final IServerWorkingCopy workingCopy = getServer().createWorkingCopy();
		workingCopy.setAttribute(attributeName, attributeValue);
		//setServerRestartState(true);
		workingCopy.save(true, new NullProgressMonitor());
	}

	@Override
	public IStatus canStop() {
		if (getServer().getServerState() == IServer.STATE_STARTING
				|| getServer().getServerState() == IServer.STATE_STARTED) {
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}

	@Override
	public void stop(boolean force) {
		setServerStopping();
		JettyServerRunner.stop(liveReloadServerRunnable);
		removeWorkspaceResourceChangeListener();
		// listen to server lifecycles
		removeServerLifeCycleListener();
		setServerStopped();
	}

	@Override
	public String getId() {
		return LiveReloadServer.class.getSimpleName();
	}

	/**
	 * @return the liveReloadServer
	 */
	public LiveReloadServer getLiveReloadServer() {
		return liveReloadServer;
	}

	/**
	 * listen to all lifecycle events from servers, ie, when a server is started
	 * and stopped
	 */
	private void addServerLifeCycleListener() {
		this.serverLifeCycleListener = new ServerLifeCycleListener(getServer());
		ServerCore.addServerLifecycleListener(serverLifeCycleListener);
	}

	/**
	 * @return the serverLifeCycleListener
	 */
	public ServerLifeCycleListener getServerLifeCycleListener() {
		return serverLifeCycleListener;
	}

	/**
	 * removes the current listener to all lifecycle events from servers, ie,
	 * when a server is started and stopped
	 */
	private void removeServerLifeCycleListener() {
		if(serverLifeCycleListener != null) {
			ServerCore.removeServerLifecycleListener(serverLifeCycleListener);
			serverLifeCycleListener.stop();
		}
	}

	/**
	 * listen to file changes in the workspace
	 */
	private void addWorkspaceResourceChangeListener() {
		this.resourceChangeListener = new WorkspaceResourceChangedListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this.resourceChangeListener,
				IResourceChangeEvent.POST_BUILD);
	}

	/**
	 * removes current listener to file changes in the workspace
	 */
	private void removeWorkspaceResourceChangeListener() {
		if(resourceChangeListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		}
	}

	@Override
	public void inform(EventObject event) {
		if (!isProxyEnabled()) {
			return;
		}
		if (event instanceof ServerStartedEvent) {
			startProxy(((ServerStartedEvent) event).getServer());
		} else if (event instanceof ServerStoppedEvent) {
			stopProxy(((ServerStoppedEvent) event).getServer());
		} else if (event instanceof LiveReloadClientRefreshingEvent) {
			setServerPublishState(IServer.PUBLISH_STATE_INCREMENTAL);
		} else if (event instanceof LiveReloadClientRefreshedEvent) {
			setServerPublishState(IServer.PUBLISH_STATE_NONE);
		}

	}

	private void startProxy(final IServer startedServer) {
		// re-start exiting proxy id already exist
		if (proxyServers.containsKey(startedServer)) {
			final LiveReloadProxyServer proxyServer = proxyServers.get(startedServer);
			try {
				proxyServer.start();
			} catch (Exception e) {
				Logger.error("Failed to start LiveReload Proxy on port " + ((NetworkConnector)proxyServer.getConnectors()[0]).getPort()
						+ " for server " + startedServer.getName(), e);
			}
		}
		// create a new proxy
		else {
			final int targetPort = WSTUtils.getWebPort(startedServer);
			if (targetPort != -1) {
				try {
					// now, let's init and start the embedded jetty proxy server
					// from the
					// server attributes
					final boolean allowRemoteConnections = isRemoteConnectionsAllowed();
					final boolean enableScriptInjection = isScriptInjectionEnabled();
					final String proxyHost = getProxyHost();
					final int proxyPort = getProxyPort(startedServer);
					final LiveReloadProxyServer proxyServer = new LiveReloadProxyServer(proxyHost, proxyPort, startedServer.getHost(), targetPort,
							websocketPort, allowRemoteConnections, enableScriptInjection);
					proxyServers.put(startedServer, proxyServer);
					final JettyServerRunner proxyRunner = JettyServerRunner.start(proxyServer);
					proxyRunners.put(startedServer, proxyRunner);
				} catch (Exception e) {
					Logger.error("Failed to create or start LiveReload proxy for server " + startedServer.getName(), e);
				}

			}
		}
	}
	
	/**
	 * @return the hostname configured for this LiveReload server
	 */
	private String getProxyHost() {
		return getServer().getHost();
	}

	/**
	 * Checks if this started server has already a configured proxy and returns
	 * the associated port if it is still free, or associate a new port if the
	 * previous port is not avaiable anymore, or create a new port if none
	 * existed before.
	 * 
	 * @param startedServer
	 * @return the proxy port
	 * @throws CoreException
	 */
	private int getProxyPort(final IServer startedServer) throws CoreException {
		final IServer liveReloadServer = getServer();
		@SuppressWarnings("unchecked")
		final Map<String, Integer> proxyPorts = liveReloadServer.getAttribute(PROXY_PORTS,
				new HashMap<String, Integer>());
		final String startedServerId = startedServer.getId();
		if (proxyPorts.containsKey(startedServerId)) {
			final int port = proxyPorts.get(startedServerId);
			if (!SocketUtil.isPortInUse(port)) {
				return port;
			}
		}
		final IServerWorkingCopy swc = getServer().createWorkingCopy();
		final int port = SocketUtil.findUnusedPort(50000, 60000);
		proxyPorts.put(startedServerId, port);
		swc.setAttribute(PROXY_PORTS, proxyPorts);
		swc.saveAll(true, null);
		return port;
	}

	private void stopProxy(final IServer stoppedServer) {
		proxyServers.remove(stoppedServer);
		final JettyServerRunner proxyRunner = proxyRunners.remove(stoppedServer);
		if (proxyRunner != null) {
			JettyServerRunner.stop(proxyRunner);
		}
	}

	public Map<IServer, LiveReloadProxyServer> getProxyServers() {
		return proxyServers;
	}

	public void setServerStarting() {
		setServerState(IServer.STATE_STARTING);
	}

	public void setServerStarted() {
		setServerState(IServer.STATE_STARTED);
	}

	public void setServerStopping() {
		setServerState(IServer.STATE_STOPPING);
	}

	public void setServerStopped() {
		setServerState(IServer.STATE_STOPPED);
	}


}
