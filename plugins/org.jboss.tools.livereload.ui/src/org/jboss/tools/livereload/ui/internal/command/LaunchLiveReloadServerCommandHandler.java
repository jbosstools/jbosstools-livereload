/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.livereload.ui.internal.command;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadLaunchConfiguration;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.jboss.tools.livereload.ui.internal.util.Logger;

public class LaunchLiveReloadServerCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean shouldEnableScriptInjection = true, shouldAllowRemoteConnections = true;
		IServer liveReloadServer = WSTUtils.findLiveReloadServer();
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
			IServer createdLiveReloadServer = null;
			try {
				createdLiveReloadServer = WSTUtils.createLiveReloadServer(
						LiveReloadLaunchConfiguration.DEFAULT_WEBSOCKET_PORT, model.isScriptInjectionEnabled(),
						model.isRemoteConnectionsAllowed());
			} catch (CoreException e) {
				Logger.error("Failed to Create LiveReload Server", e);
			}
			liveReloadServer = createdLiveReloadServer;
			
		}
		if(liveReloadServer != null && !WSTUtils.isServerStarted(liveReloadServer)){
			try {
				Job startOrRestartJob = WSTUtils.startOrRestartServer(liveReloadServer, 30, TimeUnit.SECONDS);
				startOrRestartJob.schedule();
			} catch (TimeoutException e) {
				Logger.error("Failed to Start LiveReload Server", e);
			} catch (InterruptedException e) {
				Logger.error("Failed to Start LiveReload Server", e);
			} catch (java.util.concurrent.ExecutionException e) {
				Logger.error("Failed to Start LiveReload Server", e);			}
		}
		return null;
	}

}
