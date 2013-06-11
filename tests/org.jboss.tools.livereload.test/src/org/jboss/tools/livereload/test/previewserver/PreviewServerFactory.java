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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.util.SocketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xcoulon
 * 
 */
public class PreviewServerFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(PreviewServerFactory.class);
	
	private static final String HTTP_PREVIEW_SERVER_TYPE = "org.jboss.tools.livereload.test.previewServerType";
	private static final String HTTP_PREVIEW_RUNTIME_TYPE = "org.jboss.tools.livereload.test.previewRuntimeType";

	/**
	 * Creates an HTTP Preview server if it does not exist yet, and deploys the
	 * given project on it.
	 * 
	 * @return
	 * @throws CoreException 
	 */
	public static IServer createServer(final IProject projectToDeploy) throws CoreException {
		final IServer httpPreviewServer = createOrRetrieveHttpPreviewServer();
		deployProject(httpPreviewServer, projectToDeploy);
		return httpPreviewServer;
	}

	private static void deployProject(final IServer previewServer, final IProject projectToDeploy) throws CoreException {
		final IServerWorkingCopy swc = previewServer.createWorkingCopy();
		try {
			swc.setAttribute(PreviewServerBehaviour.BASE_LOCATION, projectToDeploy.getLocation().removeLastSegments(1).toOSString());
			swc.saveAll(true, null);
		} catch (CoreException e) {
			LOGGER.error("Failed to save the new Preview server configuration", e);
		}
	}

	/**
	 * Returns the list of the existing LiveReload servers.
	 * 
	 * @return the list of the existing LiveReload servers, or an empty list if
	 *         none exists yet.
	 * @throws CoreException 
	 */
	private static IServer createOrRetrieveHttpPreviewServer() throws CoreException {
		for (IServer server : ServerCore.getServers()) {
			if (server.getServerType().getId().equals(HTTP_PREVIEW_SERVER_TYPE)) {
				LOGGER.info("Reusing existing HTTP Preview Server");
				return server;
			}
		}

		return createHttpPreviewServer();
	}

	private static IServer createHttpPreviewServer() throws CoreException {
		LOGGER.info("Creating a new HTTP Preview Server");
		final String serverName = "HttpPreview Test Server";
		IRuntimeType rt = ServerCore.findRuntimeType(HTTP_PREVIEW_RUNTIME_TYPE);
		IRuntimeWorkingCopy rwc = rt.createRuntime(null, null);
		IRuntime runtime = rwc.save(true, null);
		IServerType st = ServerCore.findServerType(HTTP_PREVIEW_SERVER_TYPE);
		IServerWorkingCopy swc = (IServerWorkingCopy) st.createServer(serverName, null, null);
		swc.setServerConfiguration(null);
		swc.setName(serverName);
		swc.setRuntime(runtime);
		swc.setAttribute(PreviewServerBehaviour.PORT, SocketUtil.findUnusedPort(50000, 60000));
		return swc.save(true, new NullProgressMonitor());
	}

}
