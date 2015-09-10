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

import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.IServerModule;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.jboss.tools.livereload.ui.internal.util.Logger;
import org.jboss.tools.livereload.ui.internal.util.Pair;

/**
 * Command to open the Web Browser at the location computed from the selected
 * {@link IServerModule} via the associated LiveReload Proxy if it exists.
 * 
 * @author xcoulon
 * 
 */
public class OpenInExternalDeviceWebBrowserViaQRCodeCommandHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IServerModule appModule = retrieveServerModuleFromSelectedElement(HandlerUtil.getCurrentSelection(event));
		try {
			final Pair<IServer, Boolean> result = OpenInWebBrowserViaLiveReloadUtils.getLiveReloadServer(true, true);
			if (result != null) {
				final IServer liveReloadServer = result.left;
				final boolean needsStartOrRestart = result.right;
				if (needsStartOrRestart) {
					final Job startOrRestartJob = WSTUtils.startOrRestartServer(liveReloadServer, 30, TimeUnit.SECONDS);
					startOrRestartJob.addJobChangeListener(new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							if (event.getResult().isOK()) {
								openQRCodeDialog(appModule);
							}
						}
					});
					startOrRestartJob.schedule();
				} else {
					openQRCodeDialog(appModule);
				}
			}
		} catch (Exception e) {
			Logger.error("Failed to Open in Web Browser via LiveReload", e);
		}
		return null;
	}

	/**
	 * @param appModule
	 */
	private void openQRCodeDialog(final IServerModule appModule) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				new OpenInExternalDeviceWebBrowserViaQRCodeDialog(appModule, Display.getDefault().getActiveShell()).open();
			}
		});
	}

}
