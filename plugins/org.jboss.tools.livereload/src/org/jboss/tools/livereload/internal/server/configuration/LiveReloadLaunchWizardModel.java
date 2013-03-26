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

import org.jboss.tools.common.databinding.ObservablePojo;


/**
 * @author xcoulon
 *
 */
public class LiveReloadLaunchWizardModel extends ObservablePojo {

	public static final String PROPERTY_WEBSOCKET_SERVER_PORT = "websocketServerPort";

	public static final String PROPERTY_USE_PROXY_SERVER = "useProxyServer";

	public static final String PROPERTY_PROXY_SERVER_PORT = "proxyServerPort";
	
	private int websocketServerPort = 35729;

	private boolean useProxyServer = true;

	private int proxyServerPort = 8081;

	/* (non-Javadoc)
	 * @see org.jboss.tools.web.pagereloader.internal.configuration.ILiveReloadConfiguration#getWebsocketServerPort()
	 */
	public int getWebsocketServerPort() {
		return websocketServerPort;
	}

	/**
	 * @param websocketServerPort the websocketServerPort to set
	 */
	public void setWebsocketServerPort(int websocketServerPort) {
		firePropertyChange(PROPERTY_WEBSOCKET_SERVER_PORT, this.websocketServerPort, this.websocketServerPort = websocketServerPort);
	}

	/* (non-Javadoc)
	 * @see org.jboss.tools.web.pagereloader.internal.configuration.ILiveReloadConfiguration#isUseProxyServer()
	 */
	public boolean isUseProxyServer() {
		return useProxyServer;
	}

	/**
	 * @param useProxyServer the useProxyServer to set
	 */
	public void setUseProxyServer(boolean useProxyServer) {
		firePropertyChange(PROPERTY_USE_PROXY_SERVER, this.useProxyServer, this.useProxyServer = useProxyServer);
	}

	/* (non-Javadoc)
	 * @see org.jboss.tools.web.pagereloader.internal.configuration.ILiveReloadConfiguration#getProxyServerPort()
	 */
	public int getProxyServerPort() {
		return proxyServerPort;
	}

	/**
	 * @param proxyServerPort the proxyServerPort to set
	 */
	public void setProxyServerPort(int proxyServerPort) {
		firePropertyChange(PROPERTY_PROXY_SERVER_PORT, this.proxyServerPort, this.proxyServerPort = proxyServerPort);
	}
	
	
	

}
