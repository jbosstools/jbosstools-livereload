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
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.livereload.internal.server.jetty.LiveReloadWebSocketServlet.LiveReloadSocket;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.URIUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadCommandBroadcaster {

	private final Set<LiveReloadSocket> connections = new HashSet<LiveReloadSocket>();

	private final int port;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public LiveReloadCommandBroadcaster(int port) {
		this.port = port;
	}

	public void notifyResourceChange(final String path) {
		try {
			String cmd = buildRefreshCommand(path);
			Logger.debug("Sending {} to {} client{}.", cmd, connections.size(), (connections.size() > 1 ? "s" : ""));
			for (LiveReloadSocket connection : connections) {
				if (connection.isOpen()) {
					connection.sendMessage(cmd);
				} else {
					Logger.debug(" (removing a uncaught closed connection...)");
				}
			}
		} catch (Exception e) {
			Logger.error("Failed to notify browser(s)", e);
		}
	}

	private String buildRefreshCommand(String path) throws JsonGenerationException, JsonMappingException,
			IOException, URISyntaxException {
		final List<Object> command = new ArrayList<Object>();
		final Map<String, Object> refreshArgs = new HashMap<String, Object>();
		
		command.add("refresh");
		refreshArgs.put("path", URIUtils.convert(path).toPort(port));
		refreshArgs.put("apply_js_live", true);
		refreshArgs.put("apply_css_live", true);
		command.add(refreshArgs);
		final StringWriter commandWriter = new StringWriter();
		objectMapper.writeValue(commandWriter, command);
		return commandWriter.toString();
	}

	public void add(LiveReloadSocket liveReloadSocket) {
		connections.add(liveReloadSocket);
		Logger.debug("A new LiveReload client ({}) established a connection. Now serving {} client{}.", liveReloadSocket.getUserAgent(), connections.size(),
				(connections.size() > 1 ? "s" : ""));
	}

	public void remove(LiveReloadSocket liveReloadSocket) {
		connections.remove(liveReloadSocket);
		Logger.debug("A LiveReload client ({}) closed its connection. Now serving {} client{}.", liveReloadSocket.getUserAgent(), connections.size(),
				(connections.size() > 1 ? "s" : ""));

	}

}
