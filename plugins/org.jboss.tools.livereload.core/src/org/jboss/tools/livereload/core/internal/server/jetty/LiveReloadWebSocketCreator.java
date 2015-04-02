/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.jboss.tools.livereload.core.internal.server.jetty;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

/**
 * @author xcoulon
 *
 */
public class LiveReloadWebSocketCreator implements WebSocketCreator {

	@Override
	public Object createWebSocket(final ServletUpgradeRequest request, final ServletUpgradeResponse response) {
		return new LiveReloadWebSocket(request.getRemoteAddress());
	}

}
