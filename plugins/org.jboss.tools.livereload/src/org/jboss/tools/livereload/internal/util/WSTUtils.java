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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;

/**
 * @author xcoulon
 * 
 */
public class WSTUtils {

	public static final String LIVERELOAD_RUNTIME_TYPE = "org.jboss.tools.livereload.serverTypeRuntime";
	public static final String LIVERELOAD_SERVER_TYPE = "org.jboss.tools.livereload.serverType";

	public static IRuntime createRuntime(String runtimeId) throws CoreException {
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null, null, runtimeId);
		IRuntimeType runtimeType = runtimeTypes[0];
		IRuntimeWorkingCopy runtimeWC = runtimeType.createRuntime(null, new NullProgressMonitor());
		runtimeWC.setName(runtimeId);
		IRuntime savedRuntime = runtimeWC.save(true, new NullProgressMonitor());
		return savedRuntime;
	}

	/**
	 * Returns the list of the existing LiveReload servers.
	 * 
	 * @return the list of the existing LiveReload servers, or an empty list if
	 *         none exists yet.
	 */
	public static List<IServer> retrieveLiveReloadServers() {
		final List<IServer> liveReloadServers = new ArrayList<IServer>();
		for (IServer server : ServerCore.getServers()) {
			if (server.getServerType().getId().equals(LIVERELOAD_SERVER_TYPE)) {
				liveReloadServers.add(server);
			}
		}
		return liveReloadServers;
	}

	/**
	 * Returns an array of String containing the given servers'names
	 * 
	 * @param existingServers
	 * @return
	 */
	public static String[] toString(final List<IServer> servers) {
		String[] names = new String[servers.size()];
		for (int i = 0; i < servers.size(); i++) {
			names[i] = servers.get(i).getName();
		}
		return names;
	}

	/**
	 * Returns a default name for a new LiveReload Server. The server name is a
	 * concatenation of {@link IServer#getName()} followed by
	 * <code>at localhost</code> followed by an optional counter if a server
	 * with the same name already exists.
	 * 
	 * @return a default name for a new server
	 */
	public static String generateDefaultServerName() {
		final IServerType liveReloadServerType = ServerCore.findServerType(LIVERELOAD_SERVER_TYPE);
		boolean valid = false;
		int counter = 0;
		while (!valid) {
			StringBuilder serverNameBuilder = new StringBuilder(liveReloadServerType.getName()).append(" at localhost");
			if (counter > 0) {
				serverNameBuilder.append(" (").append(counter).append(")");
			}
			final String serverName = serverNameBuilder.toString();
			if (serverExists(serverName)) {
				counter++;
				continue;
			}
			return serverName;
		}
		return null;
	}

	/**
	 * Verifies if a server with the same name already exists in the workspace.
	 * 
	 * @param serverName
	 * @return true if a server with the same name exists, false otherwise.
	 */
	public static boolean serverExists(final String serverName) {
		for (IServer server : ServerCore.getServers()) {
			if (server.getName().equals(serverName)) {
				return true;
			}
		}
		return false;
	}

	public static IServer createLiveReloadServerWorkingCopy(final String serverName) throws CoreException {
		IRuntimeType rt = ServerCore.findRuntimeType(LIVERELOAD_RUNTIME_TYPE);
		IRuntimeWorkingCopy wc = rt.createRuntime(null, null);
		IRuntime runtime = wc.save(true, null);
		IServerType st = ServerCore.findServerType(LIVERELOAD_SERVER_TYPE);
		IServerWorkingCopy swc = (IServerWorkingCopy) st.createServer(serverName, null, null);
		swc.setServerConfiguration(null);
		swc.setName(serverName);
		swc.setRuntime(runtime);
		// swc.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, deployLocation);
		// swc.setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY,
		// tempDeployLocation);
		return swc.save(true, new NullProgressMonitor());
	}
}
