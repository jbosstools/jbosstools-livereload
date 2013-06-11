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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author xcoulon
 * 
 */
public class WSTUtilsTestCase {

	private IServer jbossasServer = null;
	private IServer tomcatServer = null;
	private List<IServer> servers = null;

	@Before
	public void setup() throws CoreException {
		jbossasServer = mock(IServer.class, RETURNS_DEEP_STUBS);
		JBossServer jbossServer = mock(JBossServer.class);
		when(jbossServer.getJBossWebPort()).thenReturn(8080);
		when(jbossasServer.getServerType().getId()).thenReturn(WSTUtils.JBOSSASAS_SERVER_PREFIX + "71");
		when(jbossasServer.getHost()).thenReturn("localhost");
		when(jbossasServer.getAttribute(WSTUtils.JBOSSAS_SERVER_PORT, -1)).thenReturn(8080);
		when(jbossasServer.getAdapter(ServerDelegate.class)).thenReturn(jbossServer);
		tomcatServer = mock(IServer.class, RETURNS_DEEP_STUBS);
		when(tomcatServer.getServerType().getId()).thenReturn(WSTUtils.TOMCAT_SERVER_TYPE);
		when(tomcatServer.getHost()).thenReturn("localhost");
		when(tomcatServer.getAttribute(WSTUtils.TOMCAT_SERVER_PORT, -1)).thenReturn(8081);
		servers = Arrays.asList(jbossasServer, tomcatServer);
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

	@Ignore("Tomcat is not  supported yet.")
	@Test
	public void shouldRetrieveTomcat7ServerFromBrowserLocation() {
		// pre-conditions
		final String browserLocation = "http://localhost:8081/bar";
		// operation
		final IServer server = WSTUtils.extractServer(browserLocation, servers);
		// verifications
		assertThat(server).isEqualTo(tomcatServer);
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
		WSTTestUtils.createLiveReloadServer(50000, false, false);
		// operation
		final List<IServer> liveReloadServers = WSTUtils.retrieveLiveReloadServers();
		// verification
		assertThat(liveReloadServers).hasSize(1);

	}

	@Test
	public void shouldNotRetrieveAnyLiveReloadServer() {
		// pre-condition (none)
		// operation
		final List<IServer> liveReloadServers = WSTUtils.retrieveLiveReloadServers();
		// verification
		assertThat(liveReloadServers).hasSize(0);
	}

}
