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
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.jboss.tools.livereload.core.internal.util.Logger;
import org.jboss.tools.livereload.core.internal.util.URIUtils;

/**
 * Application Proxy Servlet: forward all incoming requests on this proxy to the
 * associated host/port
 * 
 * @author xcoulon
 *
 */
public class ApplicationsProxyServlet extends ProxyServlet {

	private static final long serialVersionUID = -743475231540209788L;

	private final String proxyHost;

	private final int proxyPort;
	
	private final int targetPort;

	private final String targetHost;

	/**
	 * Constructor
	 * @param proxyHost name of the host running the proxy  
	 * @param proxyPort port of the proxy
	 * @param targetHost name or address of the host running the actual app server
	 * @param targetPort port of the actual app server
	 */
	public ApplicationsProxyServlet(final String proxyHost, final int proxyPort, final String targetHost, final int targetPort) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.targetHost = targetHost;
		this.targetPort = targetPort;
	}
	
	@Override
	protected String rewriteTarget(HttpServletRequest request) {
		try {
			final URI requestURI = new URI(request.getRequestURI());
			final URI originalURI = new URI(request.getScheme(), requestURI.getUserInfo(), request.getServerName(),
					request.getLocalPort(), requestURI.getPath(), request.getQueryString(), requestURI.getFragment());
			return URIUtils.convert(originalURI).toHost(targetHost).toPort(targetPort);
		} catch (URISyntaxException e) {
			Logger.error("Failed to parse the requested URI", e);
		}
		return null;
	}
	
	/**
	 * Customize the returned 'location' header to replace the app server port with the proxy port
	 */
	@Override
	public String filterServerResponseHeader(HttpServletRequest request, Response serverResponse, String headerName, String headerValue) {
		if("Location".equals(headerName)) {
			try {
				return URIUtils.convert(headerValue).toHost(this.proxyHost).toPort(this.proxyPort);
			} catch (URISyntaxException e) {
				Logger.error("Failed to rewrite the 'Location' response header value '" + headerValue + "'",e);
			}
		}
		return super.filterServerResponseHeader(request, serverResponse, headerName, headerValue);
	}

}