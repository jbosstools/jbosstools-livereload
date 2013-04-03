package org.jboss.tools.livereload.internal.server.configuration;

import org.eclipse.core.resources.IFolder;
import org.eclipse.wst.server.core.IServer;

public interface ILiveReloadConfiguration {

	/**
	 * Return the root folder that the server should manage (ie: watch and notify for changes)
	 * @return
	 */
	public abstract IFolder getRootFolder();
	
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