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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.jboss.tools.livereload.internal.server.jetty.LiveReloadWebSocketServlet.LiveReloadSocket;
import org.jboss.tools.livereload.internal.util.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * @author xcoulon
 * 
 */
public abstract class AbstractCommandBroadcaster {

	private final Set<LiveReloadSocket> connections = new HashSet<LiveReloadSocket>();

	private static final List<String> acceptedFileTypes = Arrays.asList("html", "htm", "css", "js", "gif", "png",
			"jpg", "jpeg", "bmp", "ico");

	public void notifyResourceChange(final IPath path) {
		try {
			if (isRelevantFileType(path.getFileExtension())) {
				String cmd = buildRefreshCommand(path);
				Logger.debug("Sending {} to {} client{}.", cmd, connections.size(), (connections.size() > 1 ? "s" : ""));
				for (LiveReloadSocket connection : connections) {
					if (connection.isOpen()) {
						connection.sendMessage(cmd);
					} else {
						Logger.debug(" (removing a uncaught closed connection...)");
					}
				}
			}
		} catch (Exception e) {
			Logger.error("Failed to notify browser(s)", e);
		}
	}

	private static boolean isRelevantFileType(final String fileExtension) {
		return acceptedFileTypes.contains(fileExtension);
	}

	abstract String buildRefreshCommand(IPath path) throws JsonGenerationException, JsonMappingException, IOException,
			URISyntaxException;

	public void add(LiveReloadSocket liveReloadSocket) {
		connections.add(liveReloadSocket);
		Logger.debug("A new LiveReload client ({}) established a connection. Now serving {} client{}.",
				liveReloadSocket.getUserAgent(), connections.size(), (connections.size() > 1 ? "s" : ""));
	}

	public void remove(LiveReloadSocket liveReloadSocket) {
		connections.remove(liveReloadSocket);
		Logger.debug("A LiveReload client ({}) closed its connection. Now serving {} client{}.",
				liveReloadSocket.getUserAgent(), connections.size(), (connections.size() > 1 ? "s" : ""));

	}

}
