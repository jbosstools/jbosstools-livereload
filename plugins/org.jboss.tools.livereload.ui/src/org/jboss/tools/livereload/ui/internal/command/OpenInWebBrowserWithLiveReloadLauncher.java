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

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.jboss.tools.livereload.ui.internal.util.Logger;
import org.jboss.tools.livereload.ui.internal.util.Pair;

/**
 * @author xcoulon
 * 
 */
public class OpenInWebBrowserWithLiveReloadLauncher implements IEditorLauncher {

	/**
	 * Opens the given file using the file:// protocol
	 */
	public void open(final IPath file) {
		try {
			final Pair<IServer, Boolean> result = OpenInWebBrowserViaLiveReloadUtils.getLiveReloadServer(true, false);
			if(result != null) {
				final IServer liveReloadServer = result.left;
				final boolean needsStartOrRestart = result.right;
				if(needsStartOrRestart) {
					final Job startOrRestartJob = WSTUtils.startOrRestartServer(liveReloadServer, 30, TimeUnit.SECONDS);
					startOrRestartJob.addJobChangeListener(new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							if (event.getResult().isOK()) {
								OpenInWebBrowserViaLiveReloadUtils.openInWebBrowser(file, liveReloadServer);
							}
						}
					});
					startOrRestartJob.schedule();
				} else {
					OpenInWebBrowserViaLiveReloadUtils.openInWebBrowser(file, liveReloadServer);
				}
			}
		} catch (Exception e) {
			Logger.error("Failed to Open in Web Browser via LiveReload", e);
		}
	}

	
}
