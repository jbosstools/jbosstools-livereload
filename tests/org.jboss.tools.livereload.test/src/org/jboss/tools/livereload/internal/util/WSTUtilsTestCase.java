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

package org.jboss.tools.livereload.internal.util;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jst.server.tomcat.core.internal.TomcatServerBehaviour;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.SocketUtil;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.tools.livereload.core.internal.server.jetty.LiveReloadProxyServer;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadServerBehaviour;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.jboss.tools.livereload.internal.AbstractCommonTestCase;
import org.jboss.tools.livereload.test.previewserver.PreviewServerFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xcoulon
 * 
 */
@SuppressWarnings("restriction")
public class WSTUtilsTestCase extends AbstractCommonTestCase {

	private IServer jbossasServer = null;
	private IServer tomcat6Server = null;
	private IServer tomcat7Server = null;
	private List<IServer> servers = null;

	@Before
	public void setup() throws CoreException {
		jbossasServer = mock(IServer.class, RETURNS_DEEP_STUBS);
		final IJBossServer jbossServer = mock(IJBossServer.class);
		assertThat(jbossServer).isInstanceOf(IJBossServer.class);
		when(jbossServer.getJBossWebPort()).thenReturn(8080);
		when(jbossasServer.getServerType().getId()).thenReturn(WSTUtils.JBOSSASAS_SERVER_PREFIX + "71");
		when(jbossasServer.getHost()).thenReturn("localhost");
		when(jbossasServer.getAttribute(WSTUtils.JBOSSAS_SERVER_PORT, -1)).thenReturn(8080);
		when(jbossasServer.loadAdapter(IJBossServer.class, null)).thenReturn(jbossServer);
		when(jbossasServer.getServerState()).thenReturn(IServer.STATE_STARTED);
		
		tomcat6Server = mock(IServer.class, RETURNS_DEEP_STUBS);
		when(tomcat6Server.getServerType().getId()).thenReturn(WSTUtils.TOMCAT_60_SERVER_TYPE);
		when(tomcat6Server.getHost()).thenReturn("localhost");
		final TomcatServerBehaviour tomcat6ServerBehaviour = mock(TomcatServerBehaviour.class, RETURNS_DEEP_STUBS);
		when(tomcat6ServerBehaviour.getTomcatServer().getServerPorts()).thenReturn(new ServerPort[]{new ServerPort("foo", "foo", 8086, "HTTP"), new ServerPort("foo", "foo", 8009, "AJP")});
		when(tomcat6Server.getServerState()).thenReturn(IServer.STATE_STARTED);
		// upcasting to the return value to Object to avoid ClassCastException as mentionned on http://stackoverflow.com/questions/10324063/mockito-classcastexception
		when((Object)tomcat6Server.getAdapter(ServerBehaviourDelegate.class)).thenReturn(tomcat6ServerBehaviour);
		
		tomcat7Server = mock(IServer.class, RETURNS_DEEP_STUBS);
		when(tomcat7Server.getServerType().getId()).thenReturn(WSTUtils.TOMCAT_70_SERVER_TYPE);
		when(tomcat7Server.getHost()).thenReturn("localhost");
		final TomcatServerBehaviour tomcat7ServerBehaviour = mock(TomcatServerBehaviour.class, RETURNS_DEEP_STUBS);
		when(tomcat7ServerBehaviour.getTomcatServer().getServerPorts()).thenReturn(new ServerPort[]{new ServerPort("foo", "foo", 8087, "HTTP"), new ServerPort("foo", "foo", 8009, "AJP")});
		when((Object)tomcat7Server.getAdapter(ServerBehaviourDelegate.class)).thenReturn(tomcat7ServerBehaviour);
		when(tomcat7Server.getServerState()).thenReturn(IServer.STATE_STARTED);
		
		servers = Arrays.asList(jbossasServer, tomcat6Server, tomcat7Server);
		// remove all existing servers in the workspace
		for (IServer server : ServerCore.getServers()) {
			server.delete();
		}
	}
	
	@Test
	public void shouldRetrieveJBossAS7ServerFromBrowserLocation() {
		// pre-conditions
		final String browserLocation = "http://localhost:8080/foo";
		// operation
		final IServer server = WSTUtils.extractServer(browserLocation, servers);
		// verifications
		assertThat(server).isEqualTo(jbossasServer);
	}

	@Test
	public void shouldRetrieveTomcat7ServerFromBrowserLocation() {
		// pre-conditions
		final String browserLocation = "http://localhost:8087/bar";
		// operation
		final IServer server = WSTUtils.extractServer(browserLocation, servers);
		// verifications
		assertThat(server).isEqualTo(tomcat7Server);
	}

	@Test
	public void shouldRetrieveTomcat6ServerFromBrowserLocation() {
		// pre-conditions
		final String browserLocation = "http://localhost:8086/bar";
		// operation
		final IServer server = WSTUtils.extractServer(browserLocation, servers);
		// verifications
		assertThat(server).isEqualTo(tomcat6Server);
	}

	@Test
	public void shouldNotRetrieveServerFromBrowserLocation() {
		// pre-conditions
		final String browserLocation = "http://localhost:9090/baz";
		// operation
		final IServer server = WSTUtils.extractServer(browserLocation, servers);
		// verifications
		assertThat(server).isNull();
	}

	@Test
	public void shouldRetrieveOneLiveReloadServer() throws CoreException {
		// pre-condition
		WSTUtils.createLiveReloadServer(50000, false, false);
		// operation
		final List<IServer> liveReloadServers = WSTUtils.findLiveReloadServers();
		// verification
		assertThat(liveReloadServers).hasSize(1);
	}

	@Test
	public void shouldNotRetrieveAnyLiveReloadServer() {
		// pre-condition (none)
		// operation
		final List<IServer> liveReloadServers = WSTUtils.findLiveReloadServers();
		// verification
		assertThat(liveReloadServers).hasSize(0);
	}

	@Test
	public void shouldFindOneLiveReloadServer() throws CoreException {
		// pre-condition
		WSTUtils.createLiveReloadServer(50000, false, false);
		// operation
		final IServer liveReloadServer = WSTUtils.findLiveReloadServer();
		// verification
		assertThat(liveReloadServer).isNotNull();
	}
	
	@Test
	public void shouldNotRetrieveLiveReloadServer() {
		// pre-condition (none)
		// operation
		final IServer liveReloadServer = WSTUtils.findLiveReloadServer();
		// verification
		assertThat(liveReloadServer).isNull();
	}

	@Test
	public void shouldFilterStartedServersAndFindOne() throws Exception {
		// pre-condition
		final IServer createdLiveReloadServer = WSTUtils.createLiveReloadServer(SocketUtil.findUnusedPort(50000, 60000), false, false);
		startServer(createdLiveReloadServer, 30, TimeUnit.SECONDS);
		final List<IServer> existingServers = WSTUtils.findLiveReloadServers();
		assertThat(existingServers).hasSize(1);
		// operation
		final List<IServer> startedServers = WSTUtils.filterStartedServers(existingServers);
		// verification
		assertThat(startedServers).hasSize(1);
	}

	@Test
	public void shouldFilterStartedServersAndFindNone() throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		WSTUtils.createLiveReloadServer(SocketUtil.findUnusedPort(50000, 60000), false, false);
		final List<IServer> existingServers = WSTUtils.findLiveReloadServers();
		assertThat(existingServers).hasSize(1);
		// operation
		final List<IServer> startedServers = WSTUtils.filterStartedServers(existingServers);
		// verification
		assertThat(startedServers).hasSize(0);
	}

	@Test
	public void shouldFindLiveReloadServerForPreviewServer() throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		final IServer previewServer = PreviewServerFactory.createServer(project);
		startServer(previewServer, 30, TimeUnit.SECONDS);
		final IServer liveReloadServer = WSTUtils.createLiveReloadServer(SocketUtil.findUnusedPort(50000, 60000), false, false);
		startServer(liveReloadServer, 30, TimeUnit.SECONDS);
		// operation
		final LiveReloadProxyServer liveReloadProxyServer = WSTUtils.findLiveReloadProxyServer(previewServer);
		// verification
		assertThat(liveReloadProxyServer).isNotNull();
	}

	@Test(timeout=60*1000) // 1 min timeout
	public void shouldStartServer() throws CoreException, TimeoutException, InterruptedException, ExecutionException {
		// pre-condition
		final IServer liveReloadServer = WSTUtils.createLiveReloadServer(SocketUtil.findUnusedPort(50000, 60000),
				false, false);
		assertThat(liveReloadServer.getServerState()).isEqualTo(IServer.STATE_STOPPED);
		// operation
		final Job job = startServer(liveReloadServer, 30, TimeUnit.SECONDS);
		// verification
		assertThat(job.getResult().isOK()).isTrue();
		assertThat(liveReloadServer.getServerState()).isEqualTo(IServer.STATE_STARTED);
	}

	@Test
	public void shouldRestartServer() throws CoreException, TimeoutException, InterruptedException, ExecutionException {
		// pre-condition
		final IServer liveReloadServer = WSTUtils.createLiveReloadServer(SocketUtil.findUnusedPort(50000, 60000),
				false, false);
		startServer(liveReloadServer, 30, TimeUnit.SECONDS);
		// operation
		// force restart state by changing some configuration
		((LiveReloadServerBehaviour)liveReloadServer.getAdapter(ServerBehaviourDelegate.class)).setRemoteConnectionsAllowed(true);
		WSTUtils.startOrRestartServer(liveReloadServer, 30, TimeUnit.SECONDS);
		// verification
		assertThat(liveReloadServer.getServerState()).isEqualTo(IServer.STATE_STARTED);
	}
	
}
