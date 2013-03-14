package org.jboss.tools.livereload.internal.configuration;

public interface ILiveReloadWebServerConfiguration {

	/**
	 * @return the websocketServerPort
	 */
	public abstract int getWebsocketServerPort();

	/**
	 * @return the useProxyServer
	 */
	public abstract boolean isUseProxyServer();

	/**
	 * @return the proxyServerPort
	 */
	public abstract int getProxyServerPort();

}