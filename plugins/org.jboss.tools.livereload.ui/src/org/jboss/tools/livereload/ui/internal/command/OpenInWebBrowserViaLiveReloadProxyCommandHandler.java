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

import static org.jboss.tools.livereload.ui.internal.command.OpenInWebBrowserViaLiveReloadUtils.findLiveReloadProxyServer;
import static org.jboss.tools.livereload.ui.internal.command.OpenInWebBrowserViaLiveReloadUtils.retrieveServerModuleFromSelectedElement;

import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.wst.server.ui.IServerModule;
import org.jboss.tools.livereload.core.internal.server.jetty.LiveReloadProxyServer;
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
		if (appModule != null) {
			try {
				final LiveReloadProxyServer liveReloadProxyServer = findLiveReloadProxyServer(appModule.getServer());
				final int proxyPort = liveReloadProxyServer.getProxyPort();
				final String host = liveReloadProxyServer.getProxyHost();
				final URL url = new URL("http", host, proxyPort, "/" + appModule.getModule()[0].getName());
				PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
			} catch (Exception e) {
				Logger.error("Failed to open Web Browser via LiveReload command for selected module " + appModule, e);
			}

		}
		return null;
	}

}
