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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.tools.livereload.internal.server.wst.LiveReloadLaunchConfiguration;
import org.jboss.tools.livereload.internal.server.wst.LiveReloadServerBehaviour;

/**
 * @author xcoulon
 * 
 */
public class WSTUtils {

	public static final String LIVERELOAD_RUNTIME_TYPE = "org.jboss.tools.livereload.serverTypeRuntime";
	public static final String LIVERELOAD_SERVER_TYPE = "org.jboss.tools.livereload.serverType";
	public static final String JBOSSASAS_SERVER_PREFIX = "org.jboss.ide.eclipse.as.";
	public static final String JBOSSAS_SERVER_PORT = "org.jboss.ide.as.serverport";
	public static final String TOMCAT_SERVER_TYPE = "org.apache.tomcat.servertype";
	public static final String TOMCAT_SERVER_PORT = "org.apache.tomcat.serverport";

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

	public static LiveReloadServerBehaviour findServerBehaviour(final String serverId) {
		final IServer server = ServerCore.findServer(serverId);
		if (server != null) {
			return (LiveReloadServerBehaviour) server.getAdapter(LiveReloadServerBehaviour.class);
		}
		return null;
	}

	/**
	 * Attempts to retrieve the associated {@link IServer} for a given URL (with
	 * support for http:// and https:// schemes)
	 * 
	 * @param browserLocation
	 *            the browser location
	 * @return the enclosing Server or null if nothing matches.
	 */
	public static IServer extractServer(final String browserLocation) {
		return extractServer(browserLocation, Arrays.asList(ServerCore.getServers()));
	}

	/**
	 * Attempts to retrieve the associated {@link IServer} for a given URL (with
	 * support for http:// and https:// schemes) This method allows us to use a
	 * list of mock servers during the JUnit tests.
	 * 
	 * @param browserLocation
	 *            the browser location
	 * @param servers
	 *            the list of existing servers
	 * @return the enclosing Server or null if nothing matches.
	 */
	public static IServer extractServer(final String browserLocation, final List<IServer> servers) {
		try {
			final URL url = new URL(browserLocation);
			final String host = url.getHost();
			final int port = url.getPort();
			for (IServer server : servers) {
				// removed the host.equals(server.getHost()) comparison
				// FIXME support comparison between IP address and 'localhost' ?
				if (port == getPort(server)) {
					return server;
				}
			}
		} catch (MalformedURLException e) {
			Logger.error("Unable to parse URL '" + browserLocation + "'", e);
		}
		return null;
	}

	protected static int getPort(final IServer server) {
		final String serverType = server.getServerType().getId();
		if (serverType.startsWith(JBOSSASAS_SERVER_PREFIX)) {
			JBossServer jBossServer = (JBossServer) server.getAdapter(ServerDelegate.class);
			return jBossServer.getJBossWebPort();
		}
		if (serverType.equals(LIVERELOAD_SERVER_TYPE)) {
			return server.getAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, -1);
		}
//		if (TOMCAT_SERVER_TYPE.equals(serverType)) {
//			return server.getAttribute(TOMCAT_SERVER_PORT, -1);
//		}
		//Logger.warn("Unsupported server type: " + server.getServerType().getName());
		return -1;
	}

	/**
	 * Returns true if the given server type is LiveReload
	 * @param server
	 * @return
	 */
	public static boolean isLiveReloadServer(final IServer server) {
		return server != null && server.getServerType().getId().equals(LIVERELOAD_SERVER_TYPE);
	}
}
