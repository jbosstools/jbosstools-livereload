package org.jboss.tools.livereload.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JBossLiveReloadCoreActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.livereload"; //$NON-NLS-1$

	// The shared instance
	private static JBossLiveReloadCoreActivator plugin;
	
	/**
	 * The constructor
	 */
	public JBossLiveReloadCoreActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static JBossLiveReloadCoreActivator getDefault() {
		return plugin;
	}

	public InputStream getResourceContent(String path) throws IOException {
		final URL resource = getBundle().getResource(path);
		return resource.openStream();
	}
}
