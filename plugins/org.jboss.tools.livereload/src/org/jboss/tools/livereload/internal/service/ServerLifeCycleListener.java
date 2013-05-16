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

package org.jboss.tools.livereload.internal.service;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.util.ServerLifecycleAdapter;
import org.jboss.tools.livereload.internal.util.Logger;

/**
 * <p>
 * Listener to some Server LifeCycle events, especially when a server is
 * created.
 * </p>
 * <p>
 * There's no real need to catch server start/stop notifications, since when a
 * server is stopped, there'll be no publication anyway.
 * </p>
 * 
 * @author xcoulon
 * 
 */
public class ServerLifeCycleListener extends ServerLifecycleAdapter {

	private final Map<String, IPublishListener> publishListeners = new HashMap<String, IPublishListener>();

	/**
	 * Called when a new server is added. Adds a new {@link IPublishListener} to
	 * the given {@link IServer}.
	 */
	@Override
	public void serverAdded(final IServer server) {
		Logger.info("New Server Publish Listener added for new server:" + server.getName());
		final ServerResourcePublishedListener listener = new ServerResourcePublishedListener();
		publishListeners.put(server.getId(), listener);
		server.addPublishListener(listener);
	}

	/**
	 * Called when an existing server is removed. Removes the
	 * {@link IPublishListener} associated with the given {@link IServer}.
	 */
	@Override
	public void serverRemoved(final IServer server) {
		Logger.info("Server Publish Listener removed for server:" + server.getName());
		final IPublishListener listener = publishListeners.get(server.getId());
		server.addPublishListener(listener);
		publishListeners.remove(server.getId());
	}

}
