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

package org.jboss.tools.livereload.internal.command;

import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.internal.server.wst.LiveReloadLaunchConfiguration;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.ProjectUtils;
import org.jboss.tools.livereload.internal.util.WSTUtils;

/**
 * @author xcoulon
 * 
 */
public class OpenInWebBrowserWithLiveReloadLauncher implements IEditorLauncher {

	public OpenInWebBrowserWithLiveReloadLauncher() {
		// do nothing
	}

	public void open(IPath file) {
		final List<IServer> liveReloadServers = WSTUtils.retrieveLiveReloadServers();
		if (liveReloadServers.isEmpty()) {
			Logger.warn("No LiveReload server available");
			return;
		}
		try {
			// arbitrarily pick the first server.
			final IServer liveReloadServer = liveReloadServers.get(0);
			final String host = liveReloadServer.getHost();
			final int port = liveReloadServer.getAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, -1);
			final IProject project = ProjectUtils.findProjectFromAbsolutePath(file);
			final IPath location = new Path("/").append(project.getName()).append(file.makeRelativeTo(project.getLocation()));
			
			URL fileUrl = new URL("http", host, port, location.toString());
			PlatformUI.getWorkbench().getBrowserSupport().createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR,
					"org.eclipse.ui.browser", null, null).openURL(fileUrl);
		} catch (Exception e) {
			Logger.error("Failed to open file in Web Browser with LiveReload support", e);
		}
	}
}
