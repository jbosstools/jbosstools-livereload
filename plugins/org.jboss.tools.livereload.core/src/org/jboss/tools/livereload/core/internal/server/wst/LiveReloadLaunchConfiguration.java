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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.jboss.tools.livereload.core.internal.JBossLiveReloadCoreActivator;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadLaunchConfiguration implements ILaunchConfigurationDelegate {

	private static final String SERVER_ID = "server-id"; //$NON-NLS-1$

	public static final String LIVERELOAD_MODE = "livereload";

	public static final String WEBSOCKET_PORT = JBossLiveReloadCoreActivator.PLUGIN_ID + ".websocket_port";

	public static final String ENABLE_PROXY_SERVER = JBossLiveReloadCoreActivator.PLUGIN_ID + ".enable_proxy_server";

	public static final String ALLOW_REMOTE_CONNECTIONS = JBossLiveReloadCoreActivator.PLUGIN_ID + ".allow_remote_connections";

	public static final String ENABLE_SCRIPT_INJECTION = JBossLiveReloadCoreActivator.PLUGIN_ID + ".enable_script_injection";

	public static final int DEFAULT_WEBSOCKET_PORT = 35729;


	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// Quoting Rob Stryker: "First thing's first, this never should be registered in the debug manager"
		DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
		final String serverId = configuration.getAttribute(SERVER_ID, (String) null);
		LiveReloadServerBehaviour serverBehaviour = (LiveReloadServerBehaviour) WSTUtils.findServerBehaviour(serverId);
		if (serverBehaviour == null) {
			// can't carry on if ServerBehaviour is not found
			return;
		}
		serverBehaviour.startServer();
	}

}
