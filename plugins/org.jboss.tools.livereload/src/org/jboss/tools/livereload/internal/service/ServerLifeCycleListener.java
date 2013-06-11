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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.WSTUtils;

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
public class ServerLifeCycleListener implements IServerListener, IServerLifecycleListener, IPublishListener {

	public static final int SERVER_LISTENER = 1;
	public static final int PUBLISH_LISTENER = 2;

	private final Map<IServer, Integer> supervisedServers = new HashMap<IServer, Integer>();

	public ServerLifeCycleListener() {
		start();
	}

	private void start() {
		for (IServer server : ServerCore.getServers()) {
			if (server.getServerType().getId().equals(WSTUtils.LIVERELOAD_SERVER_TYPE)) {
				continue;
			}
			Logger.info("Adding ServerListener to existing server: " + server.getName());
			addServerListener(server);
			if (server.getServerState() == IServer.STATE_STARTED) {
				Logger.info("Also adding PublishListener to existing server: " + server.getName());
				addPublishListener(server);
			}
		}
	}

	public void stop() {
		for (IServer server : ServerCore.getServers()) {
			if (server.getServerType().getId().equals(WSTUtils.LIVERELOAD_SERVER_TYPE)) {
				continue;
			}
			Logger.info("Adding ServerListener to existing server: " + server.getName());
			removeServerListener(server);
			if (server.getServerState() == IServer.STATE_STARTED) {
				Logger.info("Also adding PublishListener to existing server: " + server.getName());
				removePublishListener(server);
			}
		}
	}

	/**
	 * Called when a new server is added. Adds a new {@link IPublishListener} to
	 * the given {@link IServer}.
	 */
	@Override
	public void serverAdded(final IServer server) {
		Logger.info("New Server Listener added for new server:" + server.getName());
		addServerListener(server);
	}

	/**
	 * Called when an existing server is removed. Removes the
	 * {@link IPublishListener} associated with the given {@link IServer}.
	 */
	@Override
	public void serverRemoved(final IServer server) {
		Logger.info("Server Listener removed for server:" + server.getName());
		removeServerListener(server);
	}

	@Override
	public void serverChanged(IServer server) {
		// nothing to do
	}

	@Override
	public void serverChanged(ServerEvent event) {
		final IServer server = event.getServer();
		if (server.getServerState() == IServer.STATE_STARTED) {
			Logger.debug("Server {} started", server.getName());
			addPublishListener(server);
			EventService.getInstance().publish(new ServerStartedEvent(server));
		} else if (server.getServerState() == IServer.STATE_STOPPED) {
			Logger.debug("Server {} stopped", server.getName());
			removePublishListener(server);
			EventService.getInstance().publish(new ServerStoppedEvent(server));
		}
	}

	@Override
	public void publishStarted(IServer server) {
		// nothing to do
	}

	@Override
	public void publishFinished(IServer server, IStatus status) {
		Logger.trace("Received notification after publish on server '{}' (started={}) finished with status {}",
				server.getName(), (server.getServerState() == IServer.STATE_STARTED), status.getSeverity());
		if (server.getServerState() == IServer.STATE_STARTED && status.isOK()) {
			EventService.getInstance().publish(new ServerResourcePublishedEvent(server));
		} else {
			Logger.debug("Ignoring this publish notification..");
		}
	}

	/**
	 * Register <code>this</code> as a {@link IServerListener} to the given
	 * {@link IServer}.
	 * 
	 * @param server
	 */
	private void addServerListener(final IServer server) {
		server.addServerListener(this);
		addFlag(server, SERVER_LISTENER);
		if(server.getServerState() == IServer.STATE_STARTED) {
			EventService.getInstance().publish(new ServerStartedEvent(server));
		}
	}

	/**
	 * Register <code>this</code> as a {@link IPublishListener} to the given
	 * {@link IServer}.
	 * 
	 * @param server
	 */
	private void addPublishListener(final IServer server) {
		server.addPublishListener(this);
		addFlag(server, PUBLISH_LISTENER);
	}

	/**
	 * Unregister <code>this</code> as a {@link IServerListener} from the given
	 * {@link IServer}.
	 * 
	 * @param server
	 */
	private void removeServerListener(final IServer server) {
		server.removeServerListener(this);
		removeFlag(server, SERVER_LISTENER);
		EventService.getInstance().publish(new ServerStoppedEvent(server));
	}

	/**
	 * Unregister <code>this</code> as a {@link IPublishListener} from the given
	 * {@link IServer}.
	 * 
	 * @param server
	 */
	private void removePublishListener(final IServer server) {
		server.removePublishListener(this);
		removeFlag(server, PUBLISH_LISTENER);
	}

	/**
	 * Adds the given flag to the given {@link IServer} in the map of supervised
	 * servers.
	 * 
	 * @param server
	 * @param flag
	 */
	private void addFlag(final IServer server, final int flag) {
		if (supervisedServers.containsKey(server)) {
			int value = supervisedServers.get(server);
			if ((value & flag) == 0) {
				value += flag;
				supervisedServers.put(server, value);
			}
		} else {
			supervisedServers.put(server, flag);
		}
	}

	/**
	 * Removes the given flag from the given {@link IServer} in the map of
	 * supervised servers.
	 * 
	 * @param server
	 * @param flag
	 */
	private void removeFlag(final IServer server, final int flag) {
		if (supervisedServers.containsKey(server)) {
			int value = supervisedServers.get(server);
			if ((value & flag) > 0) {
				value -= flag;
				supervisedServers.put(server, value);
			}
		}
	}

	/**
	 * Returns a list containing all the existing {@link IServer} that are
	 * supervised with the given flag.
	 * 
	 * @param state
	 * @return a list of {@link IServer} or empty list if none matched.
	 */
	public List<IServer> getSupervisedServers(final int state) {
		final List<IServer> servers = new ArrayList<IServer>();
		for (Entry<IServer, Integer> entry : supervisedServers.entrySet()) {
			int value = entry.getValue();
			if ((value & state) > 0) {
				servers.add(entry.getKey());
			}
		}
		return servers;
	}

}
