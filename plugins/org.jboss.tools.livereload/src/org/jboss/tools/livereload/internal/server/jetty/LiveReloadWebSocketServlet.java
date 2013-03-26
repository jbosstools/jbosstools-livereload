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
import org.jboss.tools.livereload.internal.util.Logger;

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

	private final LiveReloadCommandBroadcaster broadcaster;

	/**
	 * Constructor
	 * 
	 * @param broadcaster
	 */
	public LiveReloadWebSocketServlet(final LiveReloadCommandBroadcaster broadcaster) {
		this.broadcaster = broadcaster;
	}
	
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(req, resp);
	}



	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new LiveReloadSocket((String) request.getHeader("User-Agent"));
	}

	public class LiveReloadSocket implements WebSocket.OnTextMessage {
		
		private Connection connection;

		private final String userAgent;
		
		public LiveReloadSocket(final String userAgent) {
			this.userAgent = (userAgent != null) ? userAgent : "unknown User-Agent";
		}

		@Override
		public void onOpen(Connection connection) {
			this.connection = connection;
			try {
				connection.sendMessage("!!ver:1.6");
			} catch (IOException e) {
				Logger.error("LiveReload client connection failed", e);
			}
			broadcaster.add(this);
		}

		public boolean isOpen() {
			return connection.isOpen();
		}

		@Override
		public void onClose(int closeCode, String message) {
			broadcaster.remove(this);
		}

		public void sendMessage(String data) throws IOException {
			connection.sendMessage(data);
		}

		@Override
		public void onMessage(String data) {
		}

		/**
		 * @return the userAgent
		 */
		public String getUserAgent() {
			return userAgent;
		}

	}

}
