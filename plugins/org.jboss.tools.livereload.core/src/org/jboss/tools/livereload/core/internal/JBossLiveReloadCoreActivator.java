package org.jboss.tools.livereload.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.core.internal.util.Logger;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JBossLiveReloadCoreActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.livereload.core"; //$NON-NLS-1$

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
		// stop all servers while the plugin is stopped
		for (IServer liveReloadServer : WSTUtils.findLiveReloadServers()) {
			try {
				WSTUtils.stop(liveReloadServer, 10, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				Logger.error("Failed to stop '" + liveReloadServer.getName() + "' within expected duration.", e);
			} catch (RuntimeException e) {
				Logger.error("Failed to stop '" + liveReloadServer.getName() + "'", e);
			}
		}
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
