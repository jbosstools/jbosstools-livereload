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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.tools.livereload.internal.server.jetty.LiveReloadServer;
import org.jboss.tools.livereload.internal.service.EventService;
import org.jboss.tools.livereload.internal.service.ServerLifeCycleListener;
import org.jboss.tools.livereload.internal.service.ServerResourcePublishedListener;
import org.jboss.tools.livereload.internal.service.WorkspaceResourceChangedListener;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.WSTUtils;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadServerBehaviour extends ServerBehaviourDelegate {

	/** The LiveReloadServer that embeds a jetty server. */
	private LiveReloadServer liveReloadServer;
	private WorkspaceResourceChangedListener listener;

	@Override
	protected void initialize(IProgressMonitor monitor) {
		super.initialize(monitor);
		final IServerWorkingCopy swc = getServer().createWorkingCopy();
		try {
			// configure the websocket port if it hasn't been done before (could be initialized during tests)
			if (swc.getAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, -1) == -1) {
				swc.setAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT,
						LiveReloadLaunchConfiguration.DEFAULT_WEBSOCKET_PORT);
				swc.saveAll(true, null);
			}
		} catch (CoreException e) {
			Logger.error("Failed to save the new LiveReload server configuration", e);
		}

	}

	/**
	 * Starts the {@link LiveReloadServer} which is responsable for the embedded
	 * web/websocket/proxy server configuration and lifecycle
	 * 
	 * @param serverBehaviour
	 * @throws InterruptedException 
	 */
	public void startServer() {
		// set the server status to "Starting"
		setServerStarting();
		// now, let's init and start the embedded jetty server from the
		// server attributes
		final IServer server = getServer();
		final int websocketPort = server.getAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, -1);
		this.liveReloadServer = new LiveReloadServer(websocketPort);
		liveReloadServer.start();
		// set the server status to "Started"
		setServerStarted();
		// listen to file changes in the workspace
		addWorkspaceResourceChangeListener();
		// listen to existing servers publish
		addServerPublishListeners();
		// listen to server lifecycles
		addServerLifeCycleListener();

		// FIXME: add listener for server events
		// ServerCore#addServerLifecycleListener(IServerLifecycleListener)
	}
	
	

	/**
	 * listen to all publish events for all existing servers, except the LiveReload one(s).
	 */
	private void addServerPublishListeners() {
		for (IServer server : ServerCore.getServers()) {
			if (server.getServerType().getId().equals(WSTUtils.LIVERELOAD_SERVER_TYPE)) {
				continue;
			}
			Logger.info("New Server Publish Listener added for existing server: " + server.getName());
			server.addPublishListener(new ServerResourcePublishedListener());
		}

	}

	/**
	 * listen to all lifecycle events from servers, ie, when a server is started and stopped 
	 */
	private void addServerLifeCycleListener() {
		ServerCore.addServerLifecycleListener(new ServerLifeCycleListener());
	}

	/**
	 * listen to file changes in the workspace
	 */
	private void addWorkspaceResourceChangeListener() {
		listener = new WorkspaceResourceChangedListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_BUILD);
	}

	@Override
	public void stop(boolean force) {
		setServerStopping();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		EventService.getInstance().resetSubscribers();
		if (this.liveReloadServer != null) {
			try {
				liveReloadServer.stop();
			} catch (Exception e) {
				Logger.error("Failed to stop LiveReload server", e);
			}
		}
		setServerStopped();
	}

	@Override
	public IStatus canStart(String launchMode) {
		return Status.OK_STATUS;
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
	public IStatus canPublish() {
		return Status.CANCEL_STATUS;
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

	@Override
	public void dispose() {
		super.dispose();
	}

}
