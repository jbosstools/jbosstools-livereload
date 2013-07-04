/******************************************************************************* 
 * Copyright (c) 2008 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Xavier Coulon - Initial API and implementation 
 ******************************************************************************/

package org.jboss.tools.livereload.internal;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.util.SocketUtil;
import org.jboss.tools.livereload.core.internal.JBossLiveReloadCoreActivator;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadServerBehaviour;
import org.jboss.tools.livereload.core.internal.service.EventService;
import org.jboss.tools.livereload.core.internal.util.TimeoutUtils;
import org.jboss.tools.livereload.core.internal.util.TimeoutUtils.TaskMonitor;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * @author xcoulon
 *
 */
public class JBossLiveReloadCoreActivatorTestCase extends AbstractCommonTestCase {

	/** LiveReload server to test. */
	private IServer liveReloadServer = null;

	private int liveReloadServerPort = -1;

	private LiveReloadServerBehaviour liveReloadServerBehaviour;

	@Before
	public void setup() throws IOException, CoreException {
		// remove all servers
		for (final IServer server : ServerCore.getServers()) {
			server.stop(true);
			TaskMonitor monitor = new TaskMonitor() {
				@Override
				public boolean isComplete() {
					return !(server.canStop().isOK());
				}
			};
			TimeoutUtils.timeout(monitor, 2, TimeUnit.SECONDS);
			server.delete();
		}
		//
		EventService.getInstance().resetSubscribers();
		liveReloadServerPort = SocketUtil.findUnusedPort(50000, 55000);
	}

	@After
	public void restartPlugin() throws BundleException {
		Platform.getBundle(JBossLiveReloadCoreActivator.PLUGIN_ID).stop();
		Platform.getBundle(JBossLiveReloadCoreActivator.PLUGIN_ID).start(Bundle.START_TRANSIENT);
		final JBossLiveReloadCoreActivator restartedPlugin = JBossLiveReloadCoreActivator.getDefault();
		assertThat(restartedPlugin).isNotNull();
	}
	
	
	/**
	 * @throws CoreException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * 
	 */
	private IServer createAndLaunchLiveReloadServer(final String serverName, final boolean injectScript)
			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		createLiveReloadServer(serverName, injectScript);
		startServer(liveReloadServer, 60, TimeUnit.SECONDS);
		return liveReloadServer;
	}

	/**
	 * @throws CoreException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * 
	 */
	private IServer createLiveReloadServer(final String serverName, final boolean injectScript)
			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		final IServer server = WSTUtils.createLiveReloadServer(serverName, liveReloadServerPort,
				injectScript, false);
		liveReloadServerBehaviour = (LiveReloadServerBehaviour) WSTUtils.findServerBehaviour(server);
		assertThat(liveReloadServerBehaviour).isNotNull();
		liveReloadServer = liveReloadServerBehaviour.getServer();
		assertThat(liveReloadServer).isNotNull();
		assertThat(liveReloadServer.canStart(ILaunchManager.RUN_MODE).isOK()).isTrue();
		return liveReloadServer;
	}

	@Test
	public void shouldStopRunningServerWhenStoppingBundler() throws Exception {
		// pre-condition: create a first server (no need for script injection)
		createAndLaunchLiveReloadServer("Server 1", false);
		assertThat(liveReloadServer.getServerState()).isEqualTo(IServer.STATE_STARTED);
		// operation: stop the core bundle
		JBossLiveReloadCoreActivator.getDefault().stop(JBossLiveReloadCoreActivator.getDefault().getBundle().getBundleContext());
		// verification
		assertThat(liveReloadServer.getServerState()).isEqualTo(IServer.STATE_STOPPED);
	}

	@Test
	public void shouldNotStopServerWhenStoppingBundler() throws Exception {
		// pre-condition: create a first server (no need for script injection)
		createAndLaunchLiveReloadServer("Server 1", false);
		assertThat(liveReloadServer.getServerState()).isEqualTo(IServer.STATE_STARTED);
		
		// operation: stop the core bundle
		JBossLiveReloadCoreActivator.getDefault().stop(JBossLiveReloadCoreActivator.getDefault().getBundle().getBundleContext());
		// verification
		assertThat(liveReloadServer.getServerState()).isEqualTo(IServer.STATE_STOPPED);
	}

}
