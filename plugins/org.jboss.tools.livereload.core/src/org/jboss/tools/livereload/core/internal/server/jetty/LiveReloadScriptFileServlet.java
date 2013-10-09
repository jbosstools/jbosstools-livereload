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
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.jboss.tools.livereload.core.internal.JBossLiveReloadCoreActivator;
import org.jboss.tools.livereload.core.internal.util.HttpUtils;
import org.jboss.tools.livereload.core.internal.util.Logger;

/**
 * @author xcoulon
 *
 */
public class LiveReloadScriptFileServlet extends HttpServlet {

	/** serialVersionUID */
	private static final long serialVersionUID = 163695311668462503L;

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		InputStream scriptContent = null;
		try {
			Logger.debug("Serving embedded /livereload.js");
			final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
			scriptContent = JBossLiveReloadCoreActivator.getDefault().getResourceContent("/script/livereload.js");
			if(scriptContent == null) {
				httpServletResponse.setStatus(404);
			} else {
				final Charset charset = HttpUtils.getContentCharSet(request.getHeader("accept"), "UTF-8");
				httpServletResponse.setContentType("text/javascript; charset=" + charset.name());
				// output content will use the charset defined in the content type above.
				httpServletResponse.getOutputStream().write(IOUtils.toByteArray(scriptContent));
				httpServletResponse.setStatus(200);
			}
		} finally {
			if(scriptContent != null) {
				scriptContent.close();
			}
		}
	}

}
