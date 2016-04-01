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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.IServerModule;
import org.jboss.tools.livereload.core.internal.server.jetty.LiveReloadProxyServer;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadLaunchConfiguration;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadServerBehaviour;
import org.jboss.tools.livereload.core.internal.util.Logger;
import org.jboss.tools.livereload.core.internal.util.ProjectUtils;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.jboss.tools.livereload.ui.internal.util.Pair;

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
	}

	/**
	 * Returns the {@link IServerModule} from the selection if this selection's
	 * first element is an {@link IServerModule}, null otherwise.
	 * 
	 * @param selection
	 * @return the {@link IServerModule} from the selection if this selection's
	 *         first element is an {@link IServerModule}, null otherwise.
	 */
	public static IServerModule retrieveServerModuleFromSelectedElement(final Object selection) {
		if (selection instanceof IStructuredSelection) {
			final Object selectedObject = ((IStructuredSelection) selection).getFirstElement();
			if (selectedObject instanceof IServerModule) {
				return (IServerModule) selectedObject;
			} else if (selectedObject instanceof IAdaptable) {
				return ((IAdaptable) selectedObject).getAdapter(IServerModule.class);
			} 
		}
		return null;
	}

	/**
	 * Returns a {@link Pair} containing the found/created LiveReload
	 * {@link IServer} and a {@link Boolean} indicating whether this server
	 * should be started or restarted (because the user changed its
	 * configuration).
	 * 
	 * @param shouldEnableScriptInjection
	 * @param shouldAllowRemoteConnections
	 * @throws CoreException
	 */
	public static Pair<IServer, Boolean> getLiveReloadServer(final boolean shouldEnableScriptInjection,
			final boolean shouldAllowRemoteConnections) throws CoreException {
		final IServer liveReloadServer = WSTUtils.findLiveReloadServer();
		if (liveReloadServer == null) {
			final LiveReloadServerConfigurationDialogModel model = new LiveReloadServerConfigurationDialogModel(
					shouldEnableScriptInjection, shouldAllowRemoteConnections);
			final LiveReloadServerConfigurationDialog dialog = new LiveReloadServerConfigurationDialog(model,
					DialogMessages.LIVERELOAD_SERVER_DIALOG_TITLE, 
					DialogMessages.LIVERELOAD_SERVER_DIALOG_MESSAGE);
			int result = dialog.open();
			if (result == IDialogConstants.NO_ID) {
				return null;
			}
			final IServer createdLiveReloadServer = WSTUtils.createLiveReloadServer(
					LiveReloadLaunchConfiguration.DEFAULT_WEBSOCKET_PORT, model.isScriptInjectionEnabled(),
					model.isRemoteConnectionsAllowed());
			return new Pair<IServer, Boolean>(createdLiveReloadServer, Boolean.TRUE);
		} else {
			final LiveReloadServerBehaviour liveReloadServerBehaviour = (LiveReloadServerBehaviour) WSTUtils
					.findServerBehaviour(liveReloadServer);
			final boolean scriptInjectionEnabled = liveReloadServerBehaviour.isScriptInjectionEnabled();
			final boolean remoteConnectionsAllowed = liveReloadServerBehaviour.isRemoteConnectionsAllowed();
			final boolean serverStopped = liveReloadServer.getServerState() != IServer.STATE_STARTED;
			if (serverStopped || (shouldEnableScriptInjection && !scriptInjectionEnabled)
					|| (shouldAllowRemoteConnections && !remoteConnectionsAllowed)) {
				final LiveReloadServerConfigurationDialogModel model = new LiveReloadServerConfigurationDialogModel(
						scriptInjectionEnabled || shouldEnableScriptInjection, remoteConnectionsAllowed
								|| shouldAllowRemoteConnections);
				final LiveReloadServerConfigurationDialog dialog = new LiveReloadServerConfigurationDialog(model,
						DialogMessages.LIVERELOAD_SERVER_DIALOG_TITLE, NLS.bind(
								DialogMessages.LIVERELOAD_SERVER_DIALOG_MESSAGE, new Object[] {
										IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }));
				int result = dialog.open();
				if (result == IDialogConstants.CANCEL_ID) {
					return null;
				}
				liveReloadServerBehaviour.setScriptInjectionAllowed(model.isScriptInjectionEnabled());
				liveReloadServerBehaviour.setRemoteConnectionsAllowed(model.isRemoteConnectionsAllowed());
				return new Pair<IServer, Boolean>(liveReloadServer, Boolean.TRUE);
			} else {
				return new Pair<IServer, Boolean>(liveReloadServer, Boolean.FALSE);
			}
		}
			
	}
	
	

	/**
	 * Opens the given {@link URL} in an external browser
	 * 
	 * @param module
	 * @throws PartInitException
	 * @throws MalformedURLException
	 */
	public static void openInWebBrowser(final URL url) throws PartInitException, MalformedURLException {
		PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
	}

	/**
	 * Opens the given {@link IPath} using the given LiveReload {@link IServer}
	 * in an external browser
	 * 
	 * @param location the {@link IPath} location to display in the Web browser
	 * @param liveReloadServer the LiveReload Server to use
	 * @throws PartInitException
	 * @throws MalformedURLException
	 */
	public static void openInWebBrowser(final IPath location, final IServer liveReloadServer)  {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
							.openURL(computeURL(location, liveReloadServer));
				} catch (Exception e) {
					Logger.error("Failed to open selected Server Module in external Web Browser...");
				}
			}
		});
	}

	/**
	 * Opens the given {@link IServerModule} in an external browser
	 * 
	 * @param module
	 * @throws PartInitException
	 * @throws MalformedURLException
	 */
	public static void openInWebBrowser(final IServerModule module) {
		try {
			final URL url = computeURL(module);
			if (url != null) {
				PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
			} else {
				final String moduleName = (module != null && module.getModule().length > 0)
						? module.getModule()[0].getName() : "unknown";
				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						MessageDialog.openError(Display.getDefault().getActiveShell(), "LiveReload", "Unable to open the selected module '" + moduleName + "' in an external browser.");
					}
				});
			}
		} catch (MalformedURLException | CoreException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "LiveReload Error", e.getMessage());
			Logger.error("Failed to Open in Web Browser via LiveReload", e);
		}
	}

	private static URL computeURL(final IPath file, final IServer liveReloadServer) throws MalformedURLException {
		final String host = liveReloadServer.getHost();
		final int port = liveReloadServer.getAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, -1);
		final IProject project = ProjectUtils.findProjectFromAbsolutePath(file);
		final IPath location = new Path("/").append(project.getName()).append(
				file.makeRelativeTo(project.getLocation()));
		return new URL("http", host, port, location.toString());
	}

	private static URL computeURL(final IServerModule appModule) throws MalformedURLException, CoreException {
		final LiveReloadProxyServer liveReloadProxyServer = WSTUtils.findLiveReloadProxyServer(appModule.getServer());
		if(liveReloadProxyServer == null) {
			return null;
		}
		final int proxyPort = liveReloadProxyServer.getProxyPort();
		final String host = liveReloadProxyServer.getProxyHost();
		final URL moduleURL = WSTUtils.getModuleURL(host, proxyPort, appModule.getServer(), appModule.getModule()[0]);
		return moduleURL;
	}

}
