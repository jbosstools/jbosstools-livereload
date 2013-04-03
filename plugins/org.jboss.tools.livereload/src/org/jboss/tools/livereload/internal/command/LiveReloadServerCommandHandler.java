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

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizard;
import org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizardModel;
import org.jboss.tools.livereload.internal.service.LiveReloadService;
import org.jboss.tools.livereload.internal.util.WSTUtils;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadServerCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Command command = event.getCommand();
		State state = command.getState(RegistryToggleState.STATE_ID);
		boolean alreadyEnabled = ((Boolean) state.getValue()).booleanValue();
		final IServer server = (IServer) SelectionUtils.getSelectedElement();
		if (server != null && alreadyEnabled) {
			//LiveReloadService.disableLiveReload(server);
			HandlerUtil.toggleCommandState(command);
		} else if (server != null && !alreadyEnabled) {
			final LiveReloadLaunchWizardModel liveReloadConfiguration = openWizardDialog(server);
			try {
				WSTUtils.createRuntime("org.jboss.tools.livereload.serverTypeRuntime");
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(liveReloadConfiguration != null) {
				//LiveReloadService.enableLiveReload(server, liveReloadConfiguration);
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
	private LiveReloadLaunchWizardModel openWizardDialog(final IServer server) {
		final LiveReloadLaunchWizard liveReloadWizard = new LiveReloadLaunchWizard(server);
		final AtomicInteger state = new AtomicInteger(0);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
				WizardDialog dialog = new WizardDialog(shell, liveReloadWizard);
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
