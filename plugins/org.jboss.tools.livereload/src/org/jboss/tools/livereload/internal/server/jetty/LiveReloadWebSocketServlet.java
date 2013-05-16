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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

/**
 * This Servlet has 2 purposes: it serves the livereload.js script to the
 * browsers and then, support the connection upgrade to websocket and handles
 * the connection that can be used to push reload commands into the HTML pages.
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadWebSocketServlet extends WebSocketServlet {

	/** serialVersionUID */
	private static final long serialVersionUID = 2515781694370015615L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(req, resp);
	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new LiveReloadWebSocket((String) request.getHeader("User-Agent"), request.getRemoteAddr());
	}

}
