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

package org.jboss.tools.livereload.internal.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.util.PublishAdapter;
import org.jboss.tools.livereload.internal.LiveReloadActivator;
import org.jboss.tools.livereload.internal.configuration.ILiveReloadWebServerConfiguration;
import org.jboss.tools.livereload.internal.io.LiveReloadWebServer;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.WtpUtils;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadService implements IResourceChangeListener, IServerListener {

	public static final QualifiedName WEB_RESOURCE_CHANGE_LISTENER = new QualifiedName(
			LiveReloadActivator.PLUGIN_ID, LiveReloadService.class.getName());

	private final IServer server;
	private final Map<IFolder, IModule> webappFolders = new HashMap<IFolder, IModule>();
	private final ArrayBlockingQueue<String> pendingChanges = new ArrayBlockingQueue<String>(1000);
	private final LiveReloadWebServer liveReloadServer;

	public static void enableLiveReload(final IServer server, final ILiveReloadWebServerConfiguration liveReloadConfiguration) {
		try {
			final LiveReloadService service = new LiveReloadService(server, liveReloadConfiguration);
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.addResourceChangeListener(service, IResourceChangeEvent.POST_CHANGE);
			workspace.getRoot().setSessionProperty(WEB_RESOURCE_CHANGE_LISTENER, service);
			for (IModule module : server.getModules()) {
				final IProject project = module.getProject();
				final IFolder webappFolder = WtpUtils.getWebappFolder(project);
				service.watch(module, webappFolder);
			}
			service.start();

		} catch (Exception e) {
			Logger.error("Failed to register observer for " + server, e);
		}
	}

	public static void disableLiveReload(final IServer server) {
		try {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			final IWorkspaceRoot workspaceRoot = workspace.getRoot();
			final LiveReloadService webResourceChangeListener = (LiveReloadService) workspaceRoot
					.getSessionProperty(WEB_RESOURCE_CHANGE_LISTENER);
			if (webResourceChangeListener != null) {
				webResourceChangeListener.stopEmbeddedWebSocketServer();
				workspace.removeResourceChangeListener(webResourceChangeListener);
				workspaceRoot.setSessionProperty(WEB_RESOURCE_CHANGE_LISTENER, null);
				// make sure the command state is set to 'false' (unchecked)
				ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
				Command command = service.getCommand("org.jboss.tools.web.pagereloader.liveReloadCommand");
				State state = command.getState("org.eclipse.ui.commands.toggleState");
				state.setValue(false);
				Logger.debug("LiveReload Websocket Server stopped.");
			}
		} catch (Exception e) {
			Logger.error("Failed to register observer for " + server, e);
		}
	}

	/**
	 * Internal Constructor.
	 * 
	 * @param server
	 * @throws Exception
	 */
	private LiveReloadService(final IServer server, final ILiveReloadWebServerConfiguration configuration) throws Exception {
		this.server = server;
		this.liveReloadServer = new LiveReloadWebServer(server, configuration);
		server.addPublishListener(new PageReloadPublishAdapter());
	}

	/**
	 * Adds the given webappFolder from the given module to the list of
	 * resources that must be looked after changes.
	 * 
	 * @param module
	 * @param webappFolder
	 */
	public void watch(final IModule module, final IFolder webappFolder) {
		webappFolders.put(webappFolder, module);
	}

	private void start() throws Exception {
		liveReloadServer.start();
		this.server.addServerListener(this);
	}

	private void stopEmbeddedWebSocketServer() throws Exception {
		liveReloadServer.stop();
		this.server.removeServerListener(this);
	}

	/**
	 * Returns true if there is a listener for the given server, false
	 * otherwise.
	 * 
	 * @param server
	 *            the server on which LiveReload may be started
	 * @return true or false
	 * @throws CoreException
	 */
	public static boolean isStarted(IServer server) {
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		try {
			return (workspaceRoot.getSessionProperty(WEB_RESOURCE_CHANGE_LISTENER) != null);
		} catch (CoreException e) {
			Logger.error("Failed to retrieve LiveReload status on selected server", e);
		}
		return false;
	}

	/**
	 * Receives a notification event each time a resource changed. If the
	 * resource is a subresource of the observed location, then the event is
	 * propagated.
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		final IResource resource = findChangedResource(event.getDelta());
		for (Entry<IFolder, IModule> entry : webappFolders.entrySet()) {
			final IFolder webappFolder = entry.getKey();
			final IModule module = entry.getValue();
			if (webappFolder.getFullPath().isPrefixOf(resource.getFullPath())) {
				try {
					final IPath changedPath = resource.getFullPath().makeRelativeTo(webappFolder.getFullPath());
					final String path = "http://" + server.getHost() + ":" + getServerPort() + "/" + module.getName()
							+ "/" + changedPath.toString();
					//System.out.println("Putting '" + path + "' on wait queue until server publish is done.");
					pendingChanges.offer(path);
				} catch (Exception e) {
					Logger.error("Failed to send Page.Reload command over websocket", e);
				}
				break;
			}
		}
	}
	
	/**
	 * Stops the LiveReload WebSocket Server when the JEE Server is stopped
	 */
	@Override
	public void serverChanged(ServerEvent event) {
		if(event.getState() == IServer.STATE_STOPPED) {
			disableLiveReload(event.getServer());
		}
	}

	/**
	 * @return
	 */
	private String getServerPort() {
		return server.getAttribute("org.jboss.ide.eclipse.as.core.server.webPort", "8080");
	}

	private IResource findChangedResource(IResourceDelta delta) {
		if (delta.getAffectedChildren().length > 0) {
			return findChangedResource(delta.getAffectedChildren()[0]);
		}
		return delta.getResource();
	}

	class PageReloadPublishAdapter extends PublishAdapter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.wst.server.core.util.PublishAdapter#publishFinished(org
		 * .eclipse.wst.server.core.IServer, org.eclipse.core.runtime.IStatus)
		 */
		@Override
		public void publishFinished(IServer server, IStatus status) {
			if (!status.isOK()) {
				return;
			}
			try {
				while (!pendingChanges.isEmpty()) {
					String changedPath = pendingChanges.take();
					liveReloadServer.notifyResourceChange(changedPath);
				}
			} catch (Exception e) {
				Logger.error("Failed to send notifications for pending changes", e);
			}
		}

	}

}
