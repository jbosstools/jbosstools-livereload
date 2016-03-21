package org.jboss.tools.livereload.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JBossLiveReloadUIActivator extends AbstractUIPlugin {

	/** The plug-in ID. */
	public static final String PLUGIN_ID = "org.jboss.tools.livereload.ui"; //$NON-NLS-1$

	/** The shared instance. */
	private static JBossLiveReloadUIActivator plugin;
	
	/**
	 * The constructor
	 */
	public JBossLiveReloadUIActivator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static JBossLiveReloadUIActivator getDefault() {
		return plugin;
	}
	
}
