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
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadLaunchConfiguration;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;

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
	 * @return the server id
	 * @throws CoreException
	 */
	public static String createLiveReloadServer(final int websocketPort, final boolean enableProxy, final boolean injectScript)
			throws CoreException {
		final String serverName = "LiveReload test server";
		IRuntimeType rt = ServerCore.findRuntimeType(WSTUtils.LIVERELOAD_RUNTIME_TYPE);
		IRuntimeWorkingCopy rwc = rt.createRuntime(null, null);
		IRuntime runtime = rwc.save(true, null);
		IServerType st = ServerCore.findServerType(WSTUtils.LIVERELOAD_SERVER_TYPE);
		IServerWorkingCopy swc = (IServerWorkingCopy) st.createServer(serverName, null, null);
		swc.setServerConfiguration(null);
		swc.setName(serverName);
		swc.setRuntime(runtime);
		swc.setAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, websocketPort);
		swc.setAttribute(LiveReloadLaunchConfiguration.ENABLE_PROXY_SERVER, enableProxy);
		swc.setAttribute(LiveReloadLaunchConfiguration.ENABLE_SCRIPT_INJECTION, injectScript);
		return swc.save(true, new NullProgressMonitor()).getId();
	}
}
