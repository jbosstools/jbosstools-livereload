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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.servlets.ProxyServlet;
import org.jboss.tools.livereload.core.internal.util.Logger;
import org.jboss.tools.livereload.core.internal.util.URIUtils;

/**
 * Application Proxy Servlet: forward all incoming requests on this proxy to the associated host/port
 * @author xcoulon
 *
 */
public class ApplicationsProxyServlet extends ProxyServlet {

	private final int targetPort;
	
	private final String targetHost;
	
	public ApplicationsProxyServlet(final String targetHost, final int targetPort) {
		this.targetHost = targetHost;
		this.targetPort = targetPort;
	}

	@Override
	protected HttpURI proxyHttpURI(HttpServletRequest request, String uri) throws MalformedURLException {
		try {
			final URI requestURI = new URI(uri);
			final URI originalURI = new URI(request.getScheme(), requestURI.getUserInfo(), request.getServerName(), request.getLocalPort(), requestURI.getPath(), requestURI.getQuery(), requestURI.getFragment());
			final String proxiedURI = URIUtils.convert(originalURI).toHost(targetHost).toPort(targetPort);
			return new HttpURI(proxiedURI);
		} catch (URISyntaxException e) {
			Logger.error("Failed to parse the requested URI", e);
		}
		return null;
	}
	
	

}