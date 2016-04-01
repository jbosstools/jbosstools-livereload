package org.jboss.tools.livereload.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.core.internal.server.jetty.LiveReloadServer;
import org.jboss.tools.livereload.core.internal.util.Logger;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.jboss.tools.usage.event.UsageEvent;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JBossLiveReloadCoreActivator extends Plugin {

	/** The plug-in ID. */
	public static final String PLUGIN_ID = "org.jboss.tools.livereload.core"; //$NON-NLS-1$

	/** The shared instance. */
	private static JBossLiveReloadCoreActivator plugin;
	
	/** Event type for the number of LiveReload server creations. */
	private final UsageEventType liveReloadServerCreationEventType;
	
	/** Event type for the number of LiveReload server startups. */
	private final UsageEventType liveReloadServerStartEventType;
	
	/** Event type for the number of LiveReload signals sent to the clients. */
	private final UsageEventType liveReloadMessageSentToClientEventType;
	
	/**
	 * The constructor
	 */
	public JBossLiveReloadCoreActivator() {
		this.liveReloadServerCreationEventType = new UsageEventType(this, "serverCreated", "Number of LiveReload server creations", UsageEventType.HOW_MANY_TIMES_VALUE_DESCRIPTION); //$NON-NLS-1$ //$NON-NLS-2$
		this.liveReloadServerStartEventType = new UsageEventType(this, "serverStarted", "Number of LiveReload server starts", UsageEventType.HOW_MANY_TIMES_VALUE_DESCRIPTION); //$NON-NLS-1$ //$NON-NLS-2$
		this.liveReloadMessageSentToClientEventType = new UsageEventType(this, "reloadCommandSent", "Number of 'reload' command sent to the browsers", UsageEventType.HOW_MANY_TIMES_VALUE_DESCRIPTION); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		UsageReporter.getInstance().registerEvent(this.liveReloadServerCreationEventType);
		UsageReporter.getInstance().registerEvent(this.liveReloadServerStartEventType);
		UsageReporter.getInstance().registerEvent(this.liveReloadMessageSentToClientEventType);
	}

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

	/**
	 * Gets the content of the resource given its path.
	 * @param path the path to the resource
	 * @return the resource content, as an {@link InputStream}
	 * @throws IOException if a problem occurred while opening the {@link InputStream}
	 */
	public InputStream getResourceContent(final String path) throws IOException {
		final URL resource = getBundle().getResource(path);
		if(resource == null) {
			return null;
		}
		return resource.openStream();
	}
	
	/**
	 * Counts the {@link LiveReloadServer} creations.
	 * 
	 */
	public void countLiveReloadServerCreation() {
		final UsageEvent liveReloadServerCreationEvent = this.liveReloadServerCreationEventType.event();
		UsageReporter.getInstance().countEvent(liveReloadServerCreationEvent);
	}

	/**
	 * Counts the {@link LiveReloadServer} starts.
	 * 
	 */
	public void countLiveReloadServerStart() {
		final UsageEvent liveReloadServerStartEvent = this.liveReloadServerStartEventType.event();
		UsageReporter.getInstance().countEvent(liveReloadServerStartEvent);
	}

	/**
	 * Counts a new LiveReload command sent to a browser, signaling that a
	 * resource was re-deployed on the server of the file system.
	 * 
	 */
	public void countLiveReloadMessageSentToClient() {
		final UsageEvent liveReloadMessageSentToClientEvent = this.liveReloadMessageSentToClientEventType.event();
		UsageReporter.getInstance().countEvent(liveReloadMessageSentToClientEvent);
	}

}
