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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
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
import org.jboss.tools.livereload.ui.internal.util.CallbackJob;
import org.jboss.tools.livereload.ui.internal.util.ICallback;

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
				final IServerModule selectedModule = (IServerModule) selectedObject;
				return selectedModule;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param location
	 * @param shouldEnableScriptInjection
	 * @param shouldAllowRemoteConnections
	 * @param callback
	 * @throws CoreException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 * @throws MalformedURLException
	 */
	public static void openWithLiveReloadServer(final Object location, final boolean shouldEnableScriptInjection,
			final boolean shouldAllowRemoteConnections, final ICallback callback) {
		try {
			final IServer liveReloadServer = WSTUtils.findLiveReloadServer();
			if (liveReloadServer == null) {
				final LiveReloadServerConfigurationDialogModel model = new LiveReloadServerConfigurationDialogModel(
						shouldEnableScriptInjection, shouldAllowRemoteConnections);
				final LiveReloadServerConfigurationDialog dialog = new LiveReloadServerConfigurationDialog(model,
						DialogMessages.LIVERELOAD_SERVER_DIALOG_TITLE, NLS.bind(
								DialogMessages.LIVERELOAD_SERVER_CREATION_DIALOG_MESSAGE, new Object[] {
										IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }));
				int result = dialog.open();
				if (result == IDialogConstants.CANCEL_ID) {
					return;
				}
				final IServer createdLiveReloadServer = WSTUtils.createLiveReloadServer(
						LiveReloadLaunchConfiguration.DEFAULT_WEBSOCKET_PORT, model.isScriptInjectionEnabled(),
						model.isRemoteConnectionsAllowed());
				final Job job = new CallbackJob(callback, createdLiveReloadServer, true);
				job.schedule();
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
									DialogMessages.LIVERELOAD_SERVER_STARTUP_DIALOG_MESSAGE, new Object[] {
											IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }));
					int result = dialog.open();
					if (result == IDialogConstants.CANCEL_ID) {
						return;
					}
					liveReloadServerBehaviour.setScriptInjectionAllowed(model.isScriptInjectionEnabled());
					liveReloadServerBehaviour.setRemoteConnectionsAllowed(model.isRemoteConnectionsAllowed());
					final Job job = new CallbackJob(callback, liveReloadServer, true);
					job.schedule();
				} else {
					final Job job = new CallbackJob(callback, liveReloadServer, false);
					job.schedule();
				}
			}
			
			
		} catch (Exception e) {
			Logger.error("Failed to open selected element in Web Browser via LiveReload Server or via QR Code", e);
		}
	}
	
	

	/**
	 * Opens the given {@link URL} in an external browser
	 * 
	 * @param module
	 * @throws PartInitException
	 * @throws MalformedURLException
	 */
	public static void openInBrowser(final URL url) throws PartInitException, MalformedURLException {
		PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
	}

	/**
	 * Opens the given {@link IPath} using the given LiveReload {@link IServer}
	 * in an external browser
	 * 
	 * @param module
	 * @throws PartInitException
	 * @throws MalformedURLException
	 */
	public static void openInBrowser(final IPath location, final IServer liveReloadServer) throws PartInitException,
			MalformedURLException {
		PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
				.openURL(computeURL(location, liveReloadServer));
	}

	/**
	 * Opens the given {@link IServerModule} in an external browser
	 * 
	 * @param module
	 * @throws PartInitException
	 * @throws MalformedURLException
	 */
	public static void openInBrowser(final IServerModule module) throws PartInitException, MalformedURLException {
		final URL url = computeURL(module);
		if (url != null) {
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
		} else {
			Logger.warn("Unable to open the selected module '" + module.getModule()[0].getName() + "' in an external browser.");
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

	private static URL computeURL(final IServerModule appModule) throws MalformedURLException {
		final LiveReloadProxyServer liveReloadProxyServer = WSTUtils.findLiveReloadProxyServer(appModule.getServer());
		if(liveReloadProxyServer == null) {
			return null;
		}
		final int proxyPort = liveReloadProxyServer.getProxyPort();
		final String host = liveReloadProxyServer.getProxyHost();
		final URL url = new URL("http", host, proxyPort, "/" + appModule.getModule()[0].getName());
		return url;
	}

}
