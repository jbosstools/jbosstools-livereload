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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.ui.IServerModule;
import org.jboss.tools.livereload.internal.server.jetty.LiveReloadProxyServer;
import org.jboss.tools.livereload.internal.server.wst.LiveReloadServerBehaviour;
import org.jboss.tools.livereload.internal.util.WSTUtils;

/**
 * Utility class
 * 
 * @author xcoulon
 * 
 */
public class OpenInWebBrowserViaLiveReloadUtils {

	/**
	 * Private constructor of this utility class
	 */
	private OpenInWebBrowserViaLiveReloadUtils() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns the {@link IServer} from the selection if this selection's first
	 * element is an {@link IServerModule}, null otherwise.
	 * 
	 * @param selection
	 * @return the {@link IServer} from the selection if this selection's first
	 *         element is an {@link IServerModule}, null otherwise.
	 */
	public static IServer retrieveServerFromSelectedElement(final Object selection) {
		if (selection instanceof IStructuredSelection) {
			final Object selectedObject = ((IStructuredSelection) selection).getFirstElement();
			if (selectedObject instanceof IServerModule) {
				final IServerModule selectedModule = (IServerModule) selectedObject;
				return selectedModule.getServer();
			}
		}
		return null;
	}

	/**
	 * Returns the {@link IServerModule} from the selection if this selection's first
	 * element is an {@link IServerModule}, null otherwise.
	 * 
	 * @param selection
	 * @return the {@link IServerModule} from the selection if this selection's first
	 *         element is an {@link IServerModule}, null otherwise.
	 */
	public static IServerModule retrieveServerModuleFromSelectedElement(final Object selection) {
		if (selection instanceof IStructuredSelection) {
			final Object selectedObject = ((IStructuredSelection) selection).getFirstElement();
			if (selectedObject instanceof IServerModule) {
				final IServerModule selectedModule = (IServerModule) selectedObject;
				return selectedModule;
			}
		}
		return null;
	}

	/**
	 * Checks if the given {@link IServer} is "watched" by at least one
	 * *started* LiveReload server in the workspace.
	 * 
	 * @param appServer
	 *            the server to check
	 * @return true if the given server is watch by a started LiveReload server,
	 *         false otherwise.
	 */
	public static boolean checkAppServerWatched(final IServer appServer) {
		return findLiveReloadProxyServer(appServer) != null;
	}

	/**
	 * finds the {@link LiveReloadProxyServer} for the given {@link IServer}
	 * 
	 * @param appServer
	 *            the server that is proxied
	 * @return the proxy server or null if none exists
	 */
	public static LiveReloadProxyServer findLiveReloadProxyServer(final IServer appServer) {
		for (IServer liveReloadServer : WSTUtils.retrieveLiveReloadServers()) {
			if (checkAppServerStarted(liveReloadServer)) {
				LiveReloadServerBehaviour serverBehaviour = (LiveReloadServerBehaviour) liveReloadServer
						.getAdapter(ServerBehaviourDelegate.class);
				if (serverBehaviour != null && serverBehaviour.getProxyServers().containsKey(appServer)) {
					return serverBehaviour.getProxyServers().get(appServer);
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the given {@link IServer} is started.
	 * 
	 * @param appServer
	 *            the server to check
	 * @return true if the given server is started, false oherwise.
	 */
	public static boolean checkAppServerStarted(final IServer appServer) {
		return appServer.getServerState() == IServer.STATE_STARTED;
	}

}
