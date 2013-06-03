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

package org.jboss.tools.livereload.test.previewserver;

import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.tools.livereload.internal.server.jetty.JettyServerRunner;
import org.jboss.tools.livereload.internal.util.Logger;

/**
 * @author xcoulon
 * 
 */
public class PreviewServerBehaviour extends ServerBehaviourDelegate {

	public final static String PORT = "port";
	public final static String BASE_LOCATION = "baseLocation";
	/** The LiveReloadServer that embeds a jetty server. */
	private PreviewServer previewServer;
	private JettyServerRunner previewServerRunnable;

	/**
	 * Starts the {@link PreviewServer} which is responsable for the embedded
	 * web/websocket/proxy server configuration and lifecycle
	 * 
	 * @param serverBehaviour
	 * @throws Exception
	 * @throws InterruptedException
	 */
	public void startServer() throws Exception {
		try {
			// set the server status to "Starting"
			setServerStarting();
			// now, let's init and start the embedded jetty server from the
			// server attributes
			final IServer server = getServer();
			final int port = server.getAttribute(PORT, -1);
			final String baseLocation = server.getAttribute(BASE_LOCATION, "");
			this.previewServer = new PreviewServer();
			previewServer.configure(port, baseLocation);
			this.previewServerRunnable = JettyServerRunner.start(previewServer);
			// set the server status to "Started"
			setServerStarted();
		} catch (TimeoutException e) {
			Logger.error("Failed to start Preview server", e);
			setServerStopped();
		}
	}

	@Override
	public void stop(boolean force) {
		setServerStopping();
		JettyServerRunner.stop(previewServerRunnable);
		setServerStopped();
	}

	/**
	 * Stops the server and waits until server is stopped to return
	 */
	public void stop() {

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
