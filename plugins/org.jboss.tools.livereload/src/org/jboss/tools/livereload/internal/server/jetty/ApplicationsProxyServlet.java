package org.jboss.tools.livereload.internal.server.jetty;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.servlets.ProxyServlet;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.URIUtils;

public class ApplicationsProxyServlet extends ProxyServlet {

	private final int targetWebPort;
	
	public ApplicationsProxyServlet(final int targetWebPort) {
		this.targetWebPort = targetWebPort;
	}

	@Override
	protected HttpURI proxyHttpURI(HttpServletRequest request, String uri) throws MalformedURLException {
		try {
			final URI requestURI = new URI(request.getRequestURI());
			final URI originalURI = new URI(request.getScheme(), requestURI.getUserInfo(), request.getServerName(), request.getLocalPort(), requestURI.getPath(), requestURI.getQuery(), requestURI.getFragment());
			final String proxiedURI = URIUtils.convert(originalURI).toPort(targetWebPort);
			return new HttpURI(proxiedURI);
		} catch (URISyntaxException e) {
			Logger.error("Failed to parse the requested URI", e);
		}
		return null;
	}
	
	

}