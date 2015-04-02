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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * This Servlet allows for WebSocket connections.
 * 
 * @author xcoulon
 * 
 */
@WebServlet(name="Livereload WebSocker Servlet", urlPatterns={"/livereload"})
public class LiveReloadWebSocketServlet extends WebSocketServlet {

	/** serialVersionUID */
	private static final long serialVersionUID = 2515781694370015615L;

	/** List of the LiveReloadWebSocket created by this LiveReloadWebSocketServlet. */
	private final List<LiveReloadWebSocket> webSockets = new ArrayList<LiveReloadWebSocket>();
	
	@Override
	public void init() throws ServletException {
		// see http://stackoverflow.com/questions/29099699/osgi-bundle-in-felix-classnotfoundexception-for-jetty-class-loaded-by-name
		final ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		// Find the classloader used by the bundle providing jetty
		final ClassLoader jettyClassLoader = WebSocketServerFactory.class.getClassLoader();
		// Set the classloader
		Thread.currentThread().setContextClassLoader(jettyClassLoader);
		super.init();
		// Restore the classloader
		Thread.currentThread().setContextClassLoader(ccl);
	}
	
	@Override
	public void configure(final WebSocketServletFactory factory) {
        factory.setCreator(new LiveReloadWebSocketCreator());
	}
	
	@Override
	public void destroy() {
		super.destroy();
		for(LiveReloadWebSocket webSocket : webSockets) {
			webSocket.destroy();
		}
		webSockets.clear();
	}
}
