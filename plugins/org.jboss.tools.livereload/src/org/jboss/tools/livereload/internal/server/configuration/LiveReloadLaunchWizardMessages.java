package org.jboss.tools.livereload.internal.server.configuration;

import org.eclipse.osgi.util.NLS;

public class LiveReloadLaunchWizardMessages extends NLS {

	private static final String BUNDLE_NAME = LiveReloadLaunchWizardMessages.class.getName();

	static {
		NLS.initializeMessages(BUNDLE_NAME, LiveReloadLaunchWizardMessages.class);
	}

	private LiveReloadLaunchWizardMessages() {
		// Do not instantiate
	}

	public static String TITLE;
	public static String DESCRIPTION;
	public static String USE_EXISTING_SERVER;
	public static String CREATE_NEW_SERVER;
	public static String SELECT_SERVER;
	public static String SERVER_ALREADY_EXISTS;
	public static String HTTP_SERVER_NAME_LABEL;
	public static String HTTP_SERVER_PORT_LABEL;
	public static String HTTP_SERVER_PORT_DEFAULT_VALUE;
	public static String HTTP_SERVER_PORT_INVALID_VALUE;
	public static String WEBSOCKET_SERVER_PORT_LABEL;
	public static String WEBSOCKET_SERVER_PORT_DEFAULT_VALUE;
	public static String WEBSOCKET_SERVER_PORT_INVALID_VALUE;
	public static String SERVER_PORTS_DUPLICATE_VALUES;
	public static String NO_SELECTED_SERVER;
	
	public static String PROXY_SERVER_CHECKBOX;
	public static String PROXY_SERVER_PORT_LABEL;
	public static String PROXY_SERVER_PORT_DEFAULT_VALUE;
	public static String PROXY_SERVER_PORT_INVALID_VALUE;
	public static String PROXY_SERVER_DESCRIPTION;

}
