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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.StreamedSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.jboss.tools.livereload.internal.util.Logger;

/**
 * Filter that injects the link to the Livereload script at the bottom of the
 * returned HTML entities.
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadScriptInjectionFilter implements Filter {

	/** The port on which to connect to use the LiveReload support. */
	private final String scriptContent;

	/**
	 * Constructor.
	 * 
	 * @param livereloadPort
	 * @throws UnknownHostException
	 */
	public LiveReloadScriptInjectionFilter(final int livereloadPort) {
		scriptContent = new StringBuilder("<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':").append(livereloadPort)
				.append("/livereload.js\"></'+ 'script>')</script>").toString();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final String acceptedContentTypes = httpRequest.getHeader("Accept");
		Logger.trace("Processing request {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());
		if (!"/livereload".equals(httpRequest.getRequestURI()) && isHtmlContentType(acceptedContentTypes)) {
			Continuation continuation = ContinuationSupport.getContinuation(request);
			final ModifiableHttpServletResponse responseWrapper = new ModifiableHttpServletResponse(
					(HttpServletResponse) response);
			// follow the chain
			try {
				chain.doFilter(request, responseWrapper);
			} finally {
				if (!continuation.isResponseWrapped()) {
					terminate(httpRequest, responseWrapper);
				} else {
					continuation.addContinuationListener(new ContinuationListener() {
						
						@Override
						public void onTimeout(Continuation continuation) {
							try {
								//TODO: should change the response code and display an appropriate message
								responseWrapper.terminate();
							} catch (IOException e) {
								Logger.error("Failed to terminate the response", e);
							}
						}
						
						@Override
						public void onComplete(Continuation continuation) {
							try {
								terminate(httpRequest, responseWrapper);
							} catch (IOException e) {
								Logger.error("Failed to terminate the response", e);
							}
						}
					});
				}
			}
		} else {
			// don't even try to modify the response content
			chain.doFilter(request, response);
		}
	}

	/**
	 * @param response
	 * @param httpRequest
	 * @param responseWrapper
	 * @throws IOException
	 */
	private void terminate(final HttpServletRequest httpRequest,
			final ModifiableHttpServletResponse responseWrapper) throws IOException {
		// post-process the response
		// retrieving the content-type header from the response
		// (HttpServletResponse#getContentType() returns null !)
		final String returnedContentType = responseWrapper.getHeader("Content-Type");
		Logger.trace(" response type: {}", returnedContentType);
		if (isHtmlContentType(returnedContentType)) {
			Logger.debug("Injecting livereload.js <script> in response for {} ({})", httpRequest.getRequestURI(),
					returnedContentType);
			final InputStream responseStream = responseWrapper.getResponseAsStream();
			final char[] modifiedResponseContent = injectContent(responseStream, scriptContent);
			responseWrapper.terminate(modifiedResponseContent);
		}
		// finalize the responseWrapper by copying the wrapper's
		// outputstream into the response outputstream that will be returned
		// to the client.
		else {
			Logger.trace("*Not* injecting livereload.js <script> in response for {} ({})",
					httpRequest.getRequestURI(), returnedContentType);
			responseWrapper.terminate();
		}
	}

	
	/**
	 * Inject the given 'addition' into the gievne source, just before the
	 * <code>&lt;/body&gt;</code> ent tag. If no such end tag is found, the
	 * return value equals the given source.
	 * 
	 * @param source
	 * @param addition
	 * @return the modified source (or equal if not end tag was found in the source)
	 * @throws IOException
	 */
	public static char[] injectContent(final InputStream source, final String addition) throws IOException {
		final StreamedSource streamedSource = new StreamedSource(source);
		CharArrayWriter writer = new CharArrayWriter();
		for (Segment segment : streamedSource) {
			if (segment instanceof EndTag && ((EndTag) segment).getName().equals("body")) {
				writer.write(addition);
			}
			writer.write(segment.toString());
		}
		writer.close();
		streamedSource.close();
		return writer.toCharArray();
	}
	/**
	 * <p>
	 * Iterates over the given acceptedContentTypes, looking for one of those
	 * values:
	 * <ul>
	 * <li>text/html</li>
	 * <li>application/xhtml+xml</li>
	 * <li>application/xml</li>
	 * </ul>
	 * </p>
	 * 
	 * @param acceptedContentTypes
	 * @return true if one of the values above was found, false otherwise
	 */
	private static boolean isHtmlContentType(final String acceptedContentTypes) {
		if (acceptedContentTypes == null) {
			return false;
		}
		final StringTokenizer tokenizer = new StringTokenizer(acceptedContentTypes, ",");
		while (tokenizer.hasMoreElements()) {
			final String acceptedContentType = tokenizer.nextToken();
			if ("text/html".equals(acceptedContentType) || "application/xhtml+xml".equals(acceptedContentType)
					|| "application/xml".equals(acceptedContentType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	/**
	 * Modifiable Http Servlet Response: extending the usual
	 * {@link HttpServletResponseWrapper} but using an internal and temporary
	 * {@link ByteArrayOutputStream} and allow for response content modification
	 * *after* the call to
	 * {@link FilterChain#doFilter(ServletRequest, ServletResponse)}.
	 * 
	 * @author xcoulon
	 * 
	 */
	public class ModifiableHttpServletResponse extends HttpServletResponseWrapper {

		private ByteArrayServletOutputStream responseOutputStream;

		public ModifiableHttpServletResponse(HttpServletResponse response) {
			super(response);
			this.responseOutputStream = new ByteArrayServletOutputStream();
		}

		/**
		 * Returns the internal {@link ServletOutputStream} instead of the
		 * wrapped {@link HttpServletResponse}'s one.
		 * 
		 * @see javax.servlet.ServletResponseWrapper#getOutputStream()
		 */
		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			return responseOutputStream;
		}

		/**
		 * Returns the content of the underlying response outputstream as an
		 * readable inputstream. A the same time, the current outputstream is
		 * closed.
		 * 
		 * @return
		 * @throws IOException
		 */
		public InputStream getResponseAsStream() throws IOException {
			final byte[] byteArray = responseOutputStream.toByteArray();
			responseOutputStream.close();
			return IOUtils.toInputStream(new String(byteArray), getCharacterEncoding());
		}

		/**
		 * Writes the given content into the wrapped {@link HttpServletResponse}
		 * and adjust the 'content-length' response header as well (in case
		 * content modification occurred in the response entity).
		 * 
		 * @throws IOException
		 */
		public void terminate(final char[] responseContent) throws IOException {
			((HttpServletResponse) getResponse()).setHeader("Content-length", Integer.toString(responseContent.length));
			IOUtils.write(responseContent, getResponse().getOutputStream());
			// getResponse().getOutputStream().flush();
			// getResponse().getOutputStream().close();
			responseOutputStream.close();
		}

		/**
		 * Writes the content of the internal and temporary
		 * {@link ByteArrayOutputStream} into the wrapped
		 * {@link HttpServletResponse}.
		 * 
		 * @throws IOException
		 */
		public void terminate() throws IOException {
			final byte[] responseContent = this.responseOutputStream.toByteArray();
			responseOutputStream.close();
			IOUtils.write(responseContent, getResponse().getOutputStream());
			getResponse().getOutputStream().flush();
			getResponse().getOutputStream().close();
		}

	}

	/**
	 * @author xcoulon
	 * 
	 */
	public class ByteArrayServletOutputStream extends ServletOutputStream {

		private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		@Override
		public void write(int b) throws IOException {
			write(new byte[] { (byte) b });
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this.outputStream.write(b, off, len);
		}

		@Override
		public void write(byte[] b) throws IOException {
			this.outputStream.write(b);
		}

		public synchronized byte[] toByteArray() {
			return this.outputStream.toByteArray();
		}

		public void replaceOutputStreamContent(char[] modifiedContent) throws IOException {
			this.outputStream = new ByteArrayOutputStream();
			IOUtils.write(modifiedContent, outputStream);
		}
	}

	static class Messages {
		// property file is: package/name/messages.properties
		private static final String BUNDLE_NAME = "script.properties";
		private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

		private Messages() {
		}

		public static String getString(String key, Object... params) {
			try {
				return MessageFormat.format(RESOURCE_BUNDLE.getString(key), params);
			} catch (MissingResourceException e) {
				return '!' + key + '!';
			}
		}
	}

}
