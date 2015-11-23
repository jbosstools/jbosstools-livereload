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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.tools.livereload.core.internal.JBossLiveReloadCoreActivator;

/**
 * The LiveReload Server that implements the livereload protocol (based on
 * websockets) and optionnaly provides a proxy server to inject
 * <code>&lt;SCRIPT&gt;</code> in the returned HTML pages.
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadServerDelegate extends ServerDelegate {

	@Override
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		// configure the websocket port if it hasn't been done before (could
		// be initialized during tests)
		if (getAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, -1) == -1) {
			setAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT,
					LiveReloadLaunchConfiguration.DEFAULT_WEBSOCKET_PORT);
		}
		if (getAttribute(LiveReloadLaunchConfiguration.NOTIFICATION_DELAY, -1) == -1) {
			setAttribute(LiveReloadLaunchConfiguration.NOTIFICATION_DELAY,
					LiveReloadLaunchConfiguration.DEFAULT_NOTIFICATION_DELAY);
		}
		
	}

	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		return new Status(IStatus.OK, JBossLiveReloadCoreActivator.PLUGIN_ID, "foo");
	}

	public IModule[] getChildModules(IModule[] module) {
		return new IModule[0];
	}

	public IModule[] getRootModules(IModule module) throws CoreException {
		return new IModule[] { module };
	}

	@Override
	public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {

	}

	
}
