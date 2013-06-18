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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.IServerModule;
import org.jboss.tools.livereload.ui.internal.util.ICallback;
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
		final IServerModule appModule = retrieveServerModuleFromSelectedElement(HandlerUtil.getCurrentSelection(event));
		try {
			OpenInWebBrowserViaLiveReloadUtils.openWithLiveReloadServer(appModule, true, false, new ICallback() {
				@Override
				public void execute(IServer liveReloadServer) {
					try {
						OpenInWebBrowserViaLiveReloadUtils.openInBrowser(appModule);
					} catch (Exception e) {
						Logger.error("Failed to Open in Web Browser via LiveReload", e);
					}
				}
			});
		} catch (Exception e) {
			Logger.error("Failed to Open in Web Browser via LiveReload", e);
		}
		return null;
	}

}
