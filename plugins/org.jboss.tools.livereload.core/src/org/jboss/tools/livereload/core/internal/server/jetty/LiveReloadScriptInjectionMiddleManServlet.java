/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.jboss.tools.livereload.core.internal.server.jetty;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.proxy.AsyncMiddleManServlet;
import org.jboss.tools.livereload.core.internal.util.HttpUtils;
import org.jboss.tools.livereload.core.internal.util.Logger;
import org.jboss.tools.livereload.core.internal.util.ScriptInjectionUtils;
import org.jboss.tools.livereload.core.internal.util.URIUtils;

/**
 * 
 * See http://stackoverflow.com/questions/28694124/how-do-i-modify-proxy-responses-using-asynchronous-servlets
 */
public class LiveReloadScriptInjectionMiddleManServlet extends AsyncMiddleManServlet {

	/** Generated UUID. */
	private static final long serialVersionUID = -5338002334208801491L;

	/** The port on which to connect to use the LiveReload support. */
	private final String scriptContent;

	private final String proxyHost;

	private final int proxyPort;
	
	private final String proxyBasePath;
	
	private final int targetPort;

	private final String targetHost;

	private final String targetBasePath;
	
	private final boolean enableScriptInjection;
	
	/**
	 * Constructor.
	 * 
	 * @param livereloadPort
	 */
	public LiveReloadScriptInjectionMiddleManServlet(final String proxyHost, final int proxyPort,
			final String proxyBasePath, final String targetHost, final int targetPort, final String targetBasePath,
			final int livereloadPort, final boolean enableScriptInjection) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.proxyBasePath = proxyBasePath;
		this.targetHost = targetHost;
		this.targetPort = targetPort;
		this.targetBasePath = targetBasePath.endsWith("/") ? targetBasePath : targetBasePath + "/";
		this.enableScriptInjection = enableScriptInjection;
		this.scriptContent = new StringBuilder(
				//TODO replace with parameterized message
				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':") //$NON-NLS-1$
						.append(livereloadPort).append("/livereload.js\"></'+ 'script>')</script>").toString(); //$NON-NLS-1$
	}

	
	@Override
	protected ContentTransformer newServerResponseContentTransformer(final HttpServletRequest clientRequest,
			final HttpServletResponse proxyResponse, final Response serverResponse) {
		final String acceptedContentTypes = clientRequest.getHeader("Accept");
		if (enableScriptInjection && HttpStatus.isSuccess(serverResponse.getStatus())
				&& !"/livereload".equals(clientRequest.getRequestURI())
				&& HttpUtils.isHtmlContentType(acceptedContentTypes)) {
			return new ScriptInjectionContentTransformer(serverResponse);
		}
		// will return ContentTransformer.IDENTITY to keep server response unchanged.
		return super.newServerResponseContentTransformer(clientRequest, proxyResponse, serverResponse);
	}
	
	@Override
	protected String rewriteTarget(final HttpServletRequest request) {
		try {
			final URI requestURI = new URI(request.getRequestURI());
			final String userInfo = requestURI.getUserInfo();
			final String targetPath = this.targetBasePath + requestURI.getPath().substring(this.proxyBasePath.length());
			final URI rewrittenURI = new URI(request.getScheme(), userInfo, targetHost,
					targetPort, targetPath, request.getQueryString(), requestURI.getFragment());
			return rewrittenURI.toString();
			//return URIUtils.convert(originalURI).toHost(targetHost).toPort(targetPort);
		} catch (URISyntaxException e) {
			Logger.error("Failed to parse the requested URI", e);
		}
		return null;
	}
	
	/**
	 * Customize the returned 'location' header to replace the app server port with the proxy port
	 */
	@Override
	public String filterServerResponseHeader(final HttpServletRequest request, final Response serverResponse,
			final String headerName, final String headerValue) {
		if("Location".equals(headerName)) {
			try {
				return URIUtils.convert(headerValue).toHost(this.proxyHost).toPort(this.proxyPort);
			} catch (URISyntaxException e) {
				Logger.error("Failed to rewrite the 'Location' response header value '" + headerValue + "'",e);
			}
		}
		return super.filterServerResponseHeader(request, serverResponse, headerName, headerValue);
	}
	
	/**
	 * {@link ContentTransformer} that injects the <code>livereload.js</code>
	 * script declaration in the HTML response coming from the back-end server.
	 */
	private final class ScriptInjectionContentTransformer implements ContentTransformer {
		
		private final ByteBuffer buffer;
		
		private final Response serverResponse;

		/**
		 * Constructor
		 * @param contentLength of the back-end server response
		 */
		public ScriptInjectionContentTransformer(final Response serverResponse) {
			this.serverResponse = serverResponse;
			final int contentLength = Integer.parseInt(serverResponse.getHeaders().get("Content-Length"));
			this.buffer = ByteBuffer.allocate(contentLength);
		}

		@Override
		public void transform(final ByteBuffer input, final boolean finished, final List<ByteBuffer> output) throws IOException {
			// temporarily store the input
			buffer.put(input);
			if(finished) {
				Logger.debug("Writing response...");
				final String returnedContentType = serverResponse.getHeaders().get("Content-Type");
				final Charset charset = HttpUtils.getContentCharSet(returnedContentType, "UTF-8");
				final byte[] bytes = new byte[buffer.limit()];
				buffer.rewind();
				buffer.get(bytes);
				final InputStream serverResponseBody = new ByteArrayInputStream(bytes);
				final char[] modifiedResponseContent = ScriptInjectionUtils.injectContent(serverResponseBody , scriptContent);
				// see
				// http://mark.koli.ch/2009/09/remember-kids-an-http-content-length-is-the-number-of-bytes-not-the-number-of-characters.html
				final CharBuffer modifiedResponse = CharBuffer.wrap(modifiedResponseContent);
				output.add(charset.encode(modifiedResponse));
			}
		}
	}


}
