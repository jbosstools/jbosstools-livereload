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

package org.jboss.tools.livereload.core.internal.server.jetty;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.livereload.core.internal.util.HttpUtils;
import org.jboss.tools.livereload.core.internal.util.Logger;
import org.jboss.tools.livereload.core.internal.util.ResourceUtils;

/**
 * @author xcoulon
 * 
 */
public class WorkspaceFileServlet extends HttpServlet {

	/** serialVersionUID */
	private static final long serialVersionUID = 163695311668462503L;
	
	public static final String BASE_PATH = "basePath";

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		final String requestURI = request.getRequestURI();
		Logger.info("Serving " + requestURI);
		if (requestURI == null) {
			httpServletResponse.setStatus(400);
		} else {
			final String baseURI = getInitParameter(BASE_PATH);
			if(baseURI != null && !requestURI.startsWith(baseURI)) {
				Logger.debug("Unknown location: {} (invalid base path)", requestURI);
				httpServletResponse.setStatus(404);
			}
			final String resourceURI = baseURI != null ? requestURI.substring(baseURI.length()) : requestURI;
			final IResource resource = ResourceUtils.retrieveResource(resourceURI);
			if (resource != null && resource.getType() == IResource.FILE) {
				try {
					final IFile workspaceFile = (IFile) resource;
					final byte[] scriptContent = IOUtils.toByteArray(workspaceFile.getContents());
					httpServletResponse.getOutputStream().write(scriptContent);
					httpServletResponse.setStatus(200);
					
					final Charset charset = HttpUtils.getContentCharSet(request.getHeader("Accept"), workspaceFile.getCharset());
					String guessedContentType = URLConnection.guessContentTypeFromName(resource.getName());
					if(guessedContentType != null && !guessedContentType.contains("charset")) {
						guessedContentType = guessedContentType.concat("; charset=").concat(charset.name());
					}
					httpServletResponse.setContentType(guessedContentType);
				} catch (CoreException e) {
					Logger.error("Error occurred while returning content at location: " + requestURI, e);
					httpServletResponse.setStatus(500);
				}
			} else if (resource != null && resource.getType() == IResource.FOLDER) {
				Logger.debug("Forbidden location: {} is a folder", requestURI);
				httpServletResponse.setStatus(403);
			} else {
				Logger.debug("Unknown location: {} ", requestURI);
				httpServletResponse.setStatus(404);
			}
		}
	}

}
