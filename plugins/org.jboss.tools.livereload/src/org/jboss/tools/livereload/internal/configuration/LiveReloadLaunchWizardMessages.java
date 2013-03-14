package org.jboss.tools.livereload.internal.configuration;

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
	public static String WEBSOCKET_SERVER_PORT_LABEL;
	public static String WEBSOCKET_SERVER_PORT_DEFAULT_VALUE;
	public static String WEBSOCKET_SERVER_PORT_INVALID_VALUE;
	public static String PROXY_SERVER_CHECKBOX;
	public static String PROXY_SERVER_PORT_LABEL;
	public static String PROXY_SERVER_PORT_DEFAULT_VALUE;
	public static String PROXY_SERVER_PORT_INVALID_VALUE;
	public static String PROXY_SERVER_PORT_DUPLICATE_VALUE;
	public static String PROXY_SERVER_DESCRIPTION;

}
