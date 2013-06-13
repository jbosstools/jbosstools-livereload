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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.ui.IServerModule;
import org.jboss.tools.livereload.core.internal.server.jetty.LiveReloadProxyServer;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadLaunchConfiguration;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadServerBehaviour;
import org.jboss.tools.livereload.core.internal.util.Logger;
import org.jboss.tools.livereload.core.internal.util.ProjectUtils;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.jboss.tools.livereload.ui.internal.configuration.LiveReloadServerConfigurationMessages;
import org.jboss.tools.livereload.ui.internal.util.ImageRepository;

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
	 * Opens the given URL in an external browser
	 * 
	 * @param url
	 * @throws PartInitException
	 */
	public static void openInBrowser(final URL url) throws PartInitException {
		PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
	}

	/**
	 * Opens a Message Dialog that asks to the user if he wants to start the
	 * LiveReload server with Remote Connections enabled. The user can accept
	 * and use the toggle button to disable the Remote Connections at the same
	 * time.
	 * 
	 * @param liveReloadServerBehaviour
	 * @throws CoreException
	 */
	public static boolean promptRemoteConnections(final LiveReloadServerBehaviour liveReloadServerBehaviour)
			throws CoreException {
		MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(),
				DialogMessages.REMOTE_CONNECTIONS_DIALOG_TITLE, ImageRepository.getInstance().getImage(
						"livereload_wiz.png"), DialogMessages.REMOTE_CONNECTIONS_DIALOG_MESSAGE, MessageDialog.WARNING,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL },
				2);
		int result = dialog.open();
		if (result == 0) {
			return true;
		} else if (result == 1) {
			liveReloadServerBehaviour.setRemoteConnectionsAllowed(false);
			return true;
		}
		// cancel
		return false;
	}

	/**
	 * Opens a Message Dialog that asks to the user if he wants to enable script
	 * injection before starting the LiveReload server.
	 * 
	 * @param liveReloadServerBehaviour
	 * @throws CoreException
	 */
	public static boolean promptForScriptInjection(final LiveReloadServerBehaviour liveReloadServerBehaviour)
			throws CoreException {
		final String message = NLS.bind(DialogMessages.SCRIPT_INJECTION_DIALOG_MESSAGE,
				LiveReloadServerConfigurationMessages.ALLOW_REMOTE_CONNECTIONS_LABEL);
		MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(),
				DialogMessages.SCRIPT_INJECTION_DIALOG_TITLE, ImageRepository.getInstance().getImage(
						"livereload_wiz.png"), message, MessageDialog.WARNING, new String[] {
						IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 2);
		int result = dialog.open();
		if (result == 0) {
			liveReloadServerBehaviour.setScriptInjectionAllowed(true);
			return true;
		} else if (result == 1) {
			return true;
		}
		// cancel
		return false;
	}

	public static void openInBrowserAfterStartup(final IPath file, final IServer liveReloadServer, final int timeout,
			final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException, CoreException {
		Executors.newSingleThreadExecutor().submit(new Runnable() {
			@Override
			public void run() {
				try {
					startOrRestartServer(liveReloadServer, timeout, unit);
					final LiveReloadServerBehaviour liveReloadServerBehaviour = (LiveReloadServerBehaviour) liveReloadServer
							.loadAdapter(ServerBehaviourDelegate.class, new NullProgressMonitor());
					if (liveReloadServerBehaviour.isProxyEnabled()) {
						openInBrowser(computeURL(file, liveReloadServer));
					} else {
						openInBrowser(new URL("file", null, -1, file.toOSString()));
					}
		
				} catch (Exception e) {
					Logger.error("Failed to open '" + file.toOSString() + "' in external browser", e);
				}
			}
		});
	}

	public static void openInBrowserAfterStartup(final IServerModule module, final IServer liveReloadServer,
			final int timeout, final TimeUnit unit) {
		Executors.newSingleThreadExecutor().submit(new Runnable() {
			@Override
			public void run() {
				try {
					startOrRestartServer(liveReloadServer, timeout, unit);
					final URL url = computeURL(module);
					openInBrowser(url);
				} catch (Exception e) {
					Logger.error("Failed to open URL for module '" + module.getModule()[0].getName()
							+ "' in external browser", e);
				}
			}
		});
	}

	/**
	 * @param liveReloadServer
	 * @param timeout
	 * @param unit
	 * @throws CoreException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static void startOrRestartServer(final IServer liveReloadServer, final int timeout, final TimeUnit unit)
			throws TimeoutException, InterruptedException, ExecutionException {
		Future<?> future = Executors.newSingleThreadExecutor().submit(new Runnable() {
			@Override
			public void run() {
				try {
					if (liveReloadServer.getServerState() == IServer.STATE_STARTED
							&& liveReloadServer.getServerRestartState()) {
						Logger.info("Restarting the server {}", liveReloadServer.getName());
						liveReloadServer.restart(ILaunchManager.RUN_MODE, new NullProgressMonitor());
					} else {
						Logger.info("Starting the server {}", liveReloadServer.getName());
						liveReloadServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
					}
					while (liveReloadServer.getServerState() != IServer.STATE_STARTED) {
						Thread.sleep(500);
					}
				} catch (Exception e) {
					Logger.error("Failed (re)start Livereload Server", e);
				}

			}
		});
		future.get(timeout, unit);
	}

	/**
	 * @param liveReloadServer
	 */
	private static URL computeURL(final IPath file, final IServer liveReloadServer) throws MalformedURLException {
		final String host = liveReloadServer.getHost();
		final int port = liveReloadServer.getAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, -1);
		final IProject project = ProjectUtils.findProjectFromAbsolutePath(file);
		final IPath location = new Path("/").append(project.getName()).append(
				file.makeRelativeTo(project.getLocation()));
		return new URL("http", host, port, location.toString());
	}

	/**
	 * @param appModule
	 * @return
	 * @throws MalformedURLException
	 */
	private static URL computeURL(final IServerModule appModule) throws MalformedURLException {
		final LiveReloadProxyServer liveReloadProxyServer = WSTUtils.findLiveReloadProxyServer(appModule.getServer());
		final int proxyPort = liveReloadProxyServer.getProxyPort();
		final String host = liveReloadProxyServer.getProxyHost();
		final URL url = new URL("http", host, proxyPort, "/" + appModule.getModule()[0].getName());
		return url;
	}

}
