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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.jboss.tools.livereload.internal.LiveReloadActivator;
import org.jboss.tools.livereload.internal.util.Logger;

/**
 * @author xcoulon
 *
 */
public class LiveReloadScriptFileServlet extends HttpServlet {

	/** serialVersionUID */
	private static final long serialVersionUID = 163695311668462503L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Logger.trace("Serving embedded /livereload.js");
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		final InputStream scriptContent = LiveReloadActivator.getDefault().getResourceContent("/script/livereload.js");
		httpServletResponse.getOutputStream().write(IOUtils.toByteArray(scriptContent));
		httpServletResponse.setStatus(200);
		httpServletResponse.setContentType("text/javascript");
	}

}
