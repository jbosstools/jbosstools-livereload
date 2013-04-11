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

package org.jboss.tools.livereload.internal.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.livereload.internal.server.wst.LiveReloadLaunchConfiguration;

/**
 * Utility class to create a new LiveReload Server during tests
 * 
 * @author xcoulon
 * 
 */
public class WSTTestUtils {

	/**
	 * Create a new LiveReload Server during tests.
	 * 
	 * @param serverName
	 * @param websocketPort
	 * @return
	 * @throws CoreException
	 */
	public static IServer createLiveReloadServer(final String serverName, final int websocketPort)
			throws CoreException {
		IRuntimeType rt = ServerCore.findRuntimeType(WSTUtils.LIVERELOAD_RUNTIME_TYPE);
		IRuntimeWorkingCopy rwc = rt.createRuntime(null, null);
		IRuntime runtime = rwc.save(true, null);
		IServerType st = ServerCore.findServerType(WSTUtils.LIVERELOAD_SERVER_TYPE);
		IServerWorkingCopy swc = (IServerWorkingCopy) st.createServer(serverName, null, null);
		swc.setServerConfiguration(null);
		swc.setName(serverName);
		swc.setRuntime(runtime);
		swc.setAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, websocketPort);
		return swc.save(true, new NullProgressMonitor());
	}
}
