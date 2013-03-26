package org.jboss.tools.livereload.internal.server.configuration;

import java.util.List;

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