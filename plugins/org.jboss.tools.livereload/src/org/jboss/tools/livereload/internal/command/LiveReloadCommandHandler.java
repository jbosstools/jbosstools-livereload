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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.internal.configuration.ILiveReloadWebServerConfiguration;
import org.jboss.tools.livereload.internal.configuration.LiveReloadLaunchWizard;
import org.jboss.tools.livereload.internal.configuration.StartCancelButtonWizardDialog;
import org.jboss.tools.livereload.internal.service.LiveReloadService;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Command command = event.getCommand();
		State state = command.getState(RegistryToggleState.STATE_ID);
		boolean alreadyEnabled = ((Boolean) state.getValue()).booleanValue();
		final IServer appServer = ServerUtils.getSelectedServer();
		if (appServer != null && alreadyEnabled) {
			LiveReloadService.disableLiveReload(appServer);
			HandlerUtil.toggleCommandState(command);
		} else if (appServer != null && !alreadyEnabled) {
			final ILiveReloadWebServerConfiguration liveReloadConfiguration = openWizardDialog(appServer);
			if(liveReloadConfiguration != null) {
				LiveReloadService.enableLiveReload(appServer, liveReloadConfiguration);
				HandlerUtil.toggleCommandState(command);
			}
		}
		// must return null
		return null;
	}

	/**
	 * Opens the LiveReload launch wizard
	 * 
	 * @param server
	 *            the Application Server that will have LiveReload support
	 * @return the LiveReload server configuration or null if the user cancelled
	 *         the operation.
	 */
	private ILiveReloadWebServerConfiguration openWizardDialog(final IServer server) {
		final LiveReloadLaunchWizard liveReloadWizard = new LiveReloadLaunchWizard(server);
		final AtomicInteger state = new AtomicInteger(0);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
				WizardDialog dialog = new StartCancelButtonWizardDialog(shell, liveReloadWizard);
				dialog.setMinimumPageSize(100, 200);
				dialog.create();
				dialog.open();
				state.set(dialog.getReturnCode());
			}
		});
		if (state.get() == IStatus.OK) {
			return liveReloadWizard.getConfiguration();
		}
		return null;
	}

}
