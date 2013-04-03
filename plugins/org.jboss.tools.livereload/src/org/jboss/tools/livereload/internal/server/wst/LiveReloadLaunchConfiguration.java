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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.internal.LiveReloadActivator;
import org.jboss.tools.livereload.internal.service.LiveReloadService;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.WSTUtils;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadLaunchConfiguration implements ILaunchConfigurationDelegate {

	private static final String SERVER_ID = "server-id"; //$NON-NLS-1$

	public static final String WEBSOCKET_PORT = LiveReloadActivator.PLUGIN_ID + ".websocket_port";

	public static final int DEFAULT_WEBSOCKET_PORT = 35729;

	public static final String PROXY_PORT = LiveReloadActivator.PLUGIN_ID + ".proxy_port";

	public static final String ROOT_FOLDER = LiveReloadActivator.PLUGIN_ID + ".root_folder";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.
	 * eclipse.debug.core.ILaunchConfiguration, java.lang.String,
	 * org.eclipse.debug.core.ILaunch,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		final String serverId = configuration.getAttribute(SERVER_ID, (String) null);
		LiveReloadServerBehaviour serverBehaviour = WSTUtils.findServerBehaviour(serverId);
		if (serverBehaviour == null) {
			// can't carry on if ServerBehaviour is not found
			return;
		}
		// set the server status to "Starting"
		serverBehaviour.setServerStarting();
		try {
			// now, let's init and start the embedded jetty server from the
			// server attributes
			final IServer server = serverBehaviour.getServer();
			final int websocketPort = server.getAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, -1);
			LiveReloadService liveReloadService = new LiveReloadService(websocketPort);
			serverBehaviour.setLiveReloadService(liveReloadService);
			liveReloadService.startEmbeddedServer();
			serverBehaviour.setServerStarted();
		} catch (Exception e) {
			Logger.error("Failed to launch the LiveReload server", e);
			serverBehaviour.setServerStopped();
		}
		// set the server status to "Started"
	}

}
