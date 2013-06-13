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

import static org.jboss.tools.livereload.ui.internal.command.OpenInWebBrowserViaLiveReloadUtils.retrieveServerModuleFromSelectedElement;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.IServerModule;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadServerBehaviour;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.jboss.tools.livereload.ui.internal.util.Logger;

/**
 * Command to open the Web Browser at the location computed from the selected
 * {@link IServerModule} via the associated LiveReload Proxy if it exists.
 * 
 * @author xcoulon
 * 
 */
public class OpenInWebBrowserViaLiveReloadProxyCommandHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// first, check if there's a LiveReload created and started.
		final IServerModule appModule = retrieveServerModuleFromSelectedElement(HandlerUtil.getCurrentSelection(event));
		CHECK: if (appModule != null) {
			try {
				final IServer liveReloadServer = WSTUtils.findOrCreateLiveReloadServer(true, false);
				final LiveReloadServerBehaviour liveReloadServerBehaviour = (LiveReloadServerBehaviour) WSTUtils
						.findServerBehaviour(liveReloadServer);
				if (liveReloadServerBehaviour.isRemoteConnectionsAllowed()
						&& !OpenInWebBrowserViaLiveReloadUtils.promptRemoteConnections(liveReloadServerBehaviour)) {
					break CHECK;
				}
				if (liveReloadServerBehaviour.isProxyEnabled() && !liveReloadServerBehaviour.isScriptInjectionEnabled()
						&& !OpenInWebBrowserViaLiveReloadUtils.promptForScriptInjection(liveReloadServerBehaviour)) {
					break CHECK;
				}
				OpenInWebBrowserViaLiveReloadUtils.openInBrowserAfterStartup(appModule, liveReloadServer, 30, TimeUnit.SECONDS);
			} catch (Exception e) {
				Logger.error("Failed to open Web Browser via LiveReload command for selected module " + appModule, e);
			}
		}
		return null;
	}

}
