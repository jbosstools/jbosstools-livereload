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

import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;

/**
 * This Servlet allows for WebSocket connections.
 * 
 * @author xcoulon
 * 
 */
@WebServlet(name="Livereload WebSocker Servlet", urlPatterns={"/livereload"})
public class LiveReloadWebSocketServlet extends JettyWebSocketServlet {

	/** serialVersionUID */
	private static final long serialVersionUID = 2515781694370015615L;

	/** List of the LiveReloadWebSocket created by this LiveReloadWebSocketServlet. */
	private final List<LiveReloadWebSocket> webSockets = new ArrayList<LiveReloadWebSocket>();

	@Override
	public void configure(final JettyWebSocketServletFactory factory) {
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
