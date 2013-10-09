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

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

/**
 * This Servlet allows for WebSocket connections.
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadWebSocketServlet extends WebSocketServlet {

	/** serialVersionUID */
	private static final long serialVersionUID = 2515781694370015615L;

	/** List of the LiveReloadWebSocket created by this LiveReloadWebSocketServlet. */
	private final List<LiveReloadWebSocket> webSockets = new ArrayList<LiveReloadWebSocket>();
	
	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		final LiveReloadWebSocket liveReloadWebSocket = new LiveReloadWebSocket((String) request.getHeader("User-Agent"), request.getRemoteAddr());
		webSockets.add(liveReloadWebSocket);
		return liveReloadWebSocket;
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
