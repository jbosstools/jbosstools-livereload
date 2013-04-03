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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.tools.livereload.internal.service.LiveReloadService;
import org.jboss.tools.livereload.internal.util.Logger;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadServerBehaviour extends ServerBehaviourDelegate {

	/** The LiveReloadService that controls the underlying embedded jetty server. */
	private LiveReloadService liveReloadService;

	@Override
	protected void initialize(IProgressMonitor monitor) {
		super.initialize(monitor);
		final IServerWorkingCopy swc = getServer().createWorkingCopy();
		try {
			swc.setAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, LiveReloadLaunchConfiguration.DEFAULT_WEBSOCKET_PORT);
			swc.saveAll(true, null);
		} catch (CoreException e) {
			Logger.error("Failed to save the new LiveReload server configuration", e);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
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
	public IStatus canStart(String launchMode) {
		return Status.OK_STATUS;
	}

	@Override
	public IStatus canStop() {
		if(getServer().getServerState() == IServer.STATE_STARTING || getServer().getServerState() == IServer.STATE_STARTED) {
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}

	@Override
	public IStatus canPublish() {
		return Status.CANCEL_STATUS;
	}

	@Override
	public void stop(boolean force) {
		setServerStopping();
		if(this.liveReloadService != null) {
			try {
				liveReloadService.stopEmbeddedServer();
			} catch (Exception e) {
				Logger.error("Failed to stop LiveReload server", e);
			}
		}
		setServerStopped();
	}

	/**
	 * Binds the LiveReloadService that controls the underlying embedded jetty server.
	 * @param liveReloadService
	 */
	public void setLiveReloadService(final LiveReloadService liveReloadService) {
		this.liveReloadService = liveReloadService;
	}

}
