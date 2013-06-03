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

package org.jboss.tools.livereload.test.previewserver;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.jboss.tools.livereload.internal.LiveReloadTestActivator;
import org.jboss.tools.livereload.internal.util.WSTUtils;

/**
 * @author xcoulon
 * 
 */
public class PreviewServerLaunchConfiguration implements ILaunchConfigurationDelegate {

	private static final String SERVER_ID = "server-id"; //$NON-NLS-1$

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		final String serverId = configuration.getAttribute(SERVER_ID, (String) null);
		PreviewServerBehaviour serverBehaviour = (PreviewServerBehaviour) WSTUtils.findServerBehaviour(serverId);
		if (serverBehaviour == null) {
			// can't carry on if ServerBehaviour is not found
			return;
		}
		try {
			serverBehaviour.startServer();
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, LiveReloadTestActivator.PLUGIN_ID, e.getMessage(), e));
		}
	}

}
