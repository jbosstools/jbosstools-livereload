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

package org.jboss.tools.livereload.ui.internal.util;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;

/**
 * Job to execute the given {@link ICallback} and optionally start/restart the
 * given LiveReload {@link IServer}
 * 
 * @author xcoulon
 * 
 */
public class CallbackJob extends Job {

	private final ICallback callback;

	private final IServer liveReloadServer;

	private final boolean startOrRestart;

	/**
	 * 
	 */
	public CallbackJob(final ICallback callback, final IServer liveReloadServer, final boolean startOrRestart) {
		super("Opening element with LiveReload Server...");
		this.callback = callback;
		this.liveReloadServer = liveReloadServer;
		this.startOrRestart = startOrRestart;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			if (startOrRestart) {
				WSTUtils.startOrRestartServer(liveReloadServer, 30, TimeUnit.SECONDS);
			}
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					callback.execute(liveReloadServer);
				}
			});
		} catch (Exception e) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

}
