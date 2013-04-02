package org.jboss.tools.livereload.internal.server.configuration;

import org.eclipse.wst.server.core.IServer;

public interface ILiveReloadConfiguration {

	/**
	 * @return the createNewServer
	 */
	public abstract boolean isCreateNewServer();

	/**
	 * @return the newServerName
	 */
	public abstract String getNewServerName();


	/**
	 * @return tru if the proxy should be created/started
	 */
	public abstract boolean isEnableHttpProxyPort();
	
	/**
	 * @return the newServerHttpPort
	 */
	public abstract int getNewServerHttpPort();

	/**
	 * @return the newServerWebsocketPort
	 */
	public abstract int getNewServerWebsocketPort();

	/**
	 * @return the selectedServer
	 */
	public abstract IServer getSelectedServer();

}