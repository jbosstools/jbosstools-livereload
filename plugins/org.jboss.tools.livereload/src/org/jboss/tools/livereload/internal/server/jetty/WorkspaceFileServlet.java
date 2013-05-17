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

package org.jboss.tools.livereload.internal.server.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.livereload.internal.LiveReloadActivator;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.ResourceUtils;

/**
 * @author xcoulon
 * 
 */
public class WorkspaceFileServlet extends HttpServlet {

	/** serialVersionUID */
	private static final long serialVersionUID = 163695311668462503L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		final String requestURI = request.getRequestURI();
		Logger.info("Serving " + requestURI);
		if (requestURI == null) {
			httpServletResponse.setStatus(400);
		} else {
			final IResource resource = ResourceUtils.locateResource(requestURI);
			if (resource != null && resource.getType() == IResource.FILE) {
				try {
					final InputStream scriptContent = ((IFile) resource).getContents();
					httpServletResponse.getOutputStream().write(IOUtils.toByteArray(scriptContent));
					httpServletResponse.setStatus(200);
					httpServletResponse.setContentType(URLConnection.guessContentTypeFromName(resource.getName()));
					// httpServletResponse.setContentType("text/javascript");
				} catch (CoreException e) {
					httpServletResponse.setStatus(500);
				}
			} else {
				httpServletResponse.setStatus(400);
			}
		}
	}

}
