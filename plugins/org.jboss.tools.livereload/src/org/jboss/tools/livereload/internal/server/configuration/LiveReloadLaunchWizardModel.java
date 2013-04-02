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

	public static final String PROPERTY_USE_HTTP_PROXY_SERVER = "useHttpProxyServer";

	public static final String PROPERTY_HTTP_PROXY_SERVER_PORT = "httpProxyServerPort";
	
	private int websocketServerPort = 35729;

	private boolean useHttpProxyServer = true;

	private int httpProxyServerPort = 8081;

	public int getWebsocketServerPort() {
		return websocketServerPort;
	}

	public void setWebsocketServerPort(int websocketServerPort) {
		firePropertyChange(PROPERTY_WEBSOCKET_SERVER_PORT, this.websocketServerPort, this.websocketServerPort = websocketServerPort);
	}

	public boolean isUseHttpProxyServer() {
		return useHttpProxyServer;
	}

	/**
	 * @param useProxyServer the useProxyServer to set
	 */
	public void setUseHttpProxyServer(boolean useHttpProxyServer) {
		firePropertyChange(PROPERTY_USE_HTTP_PROXY_SERVER, this.useHttpProxyServer, this.useHttpProxyServer = useHttpProxyServer);
	}

	public int getHttpProxyServerPort() {
		return httpProxyServerPort;
	}

	/**
	 * @param httpProxyServerPort the httpProxyServerPort to set
	 */
	public void setHttpProxyServerPort(int httpProxyServerPort) {
		firePropertyChange(PROPERTY_HTTP_PROXY_SERVER_PORT, this.httpProxyServerPort, this.httpProxyServerPort = httpProxyServerPort);
	}
	
	
	

}
