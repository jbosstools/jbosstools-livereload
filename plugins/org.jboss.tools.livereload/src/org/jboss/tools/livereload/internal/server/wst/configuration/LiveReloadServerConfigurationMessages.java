package org.jboss.tools.livereload.internal.server.wst.configuration;

import org.eclipse.osgi.util.NLS;

public class LiveReloadServerConfigurationMessages extends NLS {

	private static final String BUNDLE_NAME = LiveReloadServerConfigurationMessages.class.getName();

	static {
		NLS.initializeMessages(BUNDLE_NAME, LiveReloadServerConfigurationMessages.class);
	}

	private LiveReloadServerConfigurationMessages() {
		// Do not instantiate
	}

	public static String WEBSOCKET_SERVER_CONFIGURATION_TITLE;
	public static String WEBSOCKET_SERVER_CONFIGURATION_DESCRIPTION;

	public static String WEBSOCKET_SERVER_PORT_LABEL;
	public static String WEBSOCKET_SERVER_PORT_COMMAND;
	
	public static String PROXY_SERVER_CONFIGURATION_TITLE;
	public static String PROXY_CONFIGURATION_DESCRIPTION;
	
	public static String ENABLE_PROXY_SERVER_LABEL;
	public static String ENABLE_PROXY_SERVER_COMMAND;
	public static String ALLOW_REMOTE_CONNECTIONS_LABEL;
	public static String ALLOW_REMOTE_CONNECTIONS_COMMAND;
	public static String ENABLE_SCRIPT_INJECTION_LABEL;
	public static String ENABLE_SCRIPT_INJECTION_COMMAND;

}
