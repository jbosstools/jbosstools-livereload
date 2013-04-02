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

package org.jboss.tools.livereload.internal.server.configuration;

import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.common.databinding.ObservablePojo;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadConfigurationWizardModel extends ObservablePojo implements ILiveReloadConfiguration {

	public static final String PROPERTY_CREATE_NEW_SERVER = "createNewServer";

	public static final String PROPERTY_NEW_SERVER_NAME = "newServerName";

	public static final String PROPERTY_NEW_SERVER_ENABLE_HTTP_PROXY = "enableHttpProxyPort";

	public static final String PROPERTY_NEW_SERVER_HTTP_PROXY_PORT = "newServerHttpPort";
	
	public static final String PROPERTY_NEW_SERVER_WEBSOCKET_PORT = "newServerWebsocketPort";

	public static final String PROPERTY_EXISTING_SERVERS = "existingServers";

	public static final String PROPERTY_SELECTED_SERVER = "selectedServer";

	/** Flag to create a new server or use an existing one. */
	private boolean createNewServer = true;

	/** Name of the new server to create. */
	private String newServerName = null;

	/** Flag to indicate if the HTTP Proxy should be created as well. */
	private boolean enableHttpProxyPort = false;
	
	/** HTTP Port of the new server to create. */
	private int newServerHttpPort = 8080;

	/** WebSocket Port of the new server to create. */
	private int newServerWebsocketPort = 35729;

	/** Selected server. */
	private IServer selectedServer = null;

	@Override
	public boolean isCreateNewServer() {
		return createNewServer;
	}

	/**
	 * @param createNewServer
	 *            the createNewServer to set
	 */
	public void setCreateNewServer(boolean createNewServer) {
		firePropertyChange(PROPERTY_CREATE_NEW_SERVER, this.createNewServer, this.createNewServer = createNewServer);
	}

	@Override
	public String getNewServerName() {
		return newServerName;
	}

	/**
	 * @param newServerName
	 *            the newServerName to set
	 */
	public void setNewServerName(String newServerName) {
		firePropertyChange(PROPERTY_NEW_SERVER_NAME, this.newServerName, this.newServerName = newServerName);
	}
	
	/**
	 * @return the enableHttpProxyPort
	 */
	public boolean isEnableHttpProxyPort() {
		return enableHttpProxyPort;
	}

	/**
	 * @param enableHttpProxyPort the enableHttpProxyPort to set
	 */
	public void setEnableHttpProxyPort(boolean enableHttpProxyPort) {
		firePropertyChange(PROPERTY_NEW_SERVER_ENABLE_HTTP_PROXY, this.enableHttpProxyPort, this.enableHttpProxyPort = enableHttpProxyPort);
		;
	}

	@Override
	public int getNewServerHttpPort() {
		return newServerHttpPort;
	}

	/**
	 * @param newServerHttpPort
	 *            the newServerHttpPort to set
	 */
	public void setNewServerHttpPort(int newServerHttpPort) {
		firePropertyChange(PROPERTY_NEW_SERVER_HTTP_PROXY_PORT, this.newServerHttpPort,
				this.newServerHttpPort = newServerHttpPort);
	}

	@Override
	public int getNewServerWebsocketPort() {
		return newServerWebsocketPort;
	}

	/**
	 * @param newServerWebsocketPort
	 *            the newServerWebsocketPort to set
	 */
	public void setNewServerWebsocketPort(int newServerWebsocketPort) {
		firePropertyChange(PROPERTY_NEW_SERVER_WEBSOCKET_PORT, this.newServerWebsocketPort,
				this.newServerWebsocketPort = newServerWebsocketPort);
	}

	@Override
	public IServer getSelectedServer() {
		return selectedServer;
	}

	/**
	 * @param selectedServer
	 *            the selectedServer to set
	 */
	public void setSelectedServer(IServer selectedServer) {
		firePropertyChange(PROPERTY_SELECTED_SERVER, this.selectedServer, this.selectedServer = selectedServer);
	}

}
