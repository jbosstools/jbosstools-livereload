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

package org.jboss.tools.livereload.ui.internal.command;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadServerBehaviour;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.jboss.tools.livereload.ui.internal.util.Logger;

/**
 * @author xcoulon
 * 
 */
public class OpenInWebBrowserWithLiveReloadLauncher implements IEditorLauncher {

	/**
	 * Opens the given file using the file:// protocol
	 */
	public void open(final IPath file) {
		try {
			final IServer liveReloadServer = WSTUtils.findOrCreateLiveReloadServer(false, false);
			final LiveReloadServerBehaviour liveReloadServerBehaviour = (LiveReloadServerBehaviour) WSTUtils
					.findServerBehaviour(liveReloadServer);
			if (liveReloadServerBehaviour.isRemoteConnectionsAllowed()
					&& !OpenInWebBrowserViaLiveReloadUtils.promptRemoteConnections(liveReloadServerBehaviour)) {
				return;
			}
			if (liveReloadServerBehaviour.isProxyEnabled() && !liveReloadServerBehaviour.isScriptInjectionEnabled()
					&& !OpenInWebBrowserViaLiveReloadUtils.promptForScriptInjection(liveReloadServerBehaviour)) {
				return;
			}
			
			OpenInWebBrowserViaLiveReloadUtils.openInBrowserAfterStartup(file, liveReloadServer, 30, TimeUnit.SECONDS);
		} catch (Exception e) {
			Logger.error("Failed to open file in Web Browser with LiveReload support", e);
		}
	}
}
