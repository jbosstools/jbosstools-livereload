/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.jboss.tools.livereload.ui.internal.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.eclipse.wst.server.ui.IServerModule;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Dmitry Bocharov
 *
 */
public class OpenInWebBrowserViaLiveReloadProxyEnablementTesterTest {
	
	private IServer server;
	private IURLProvider deployableServer;
	private IModule module;
	
	
	@After
	public void stopServers() {
		server.stop(true);
	}
	
	@Before
	public void initialize() {
		server = mock(IServer.class);
		module = mock(IModule.class);
		IServerType serverType = mock(IServerType.class);
		deployableServer = mock(IURLProvider.class, RETURNS_DEEP_STUBS);
		ServerExtendedProperties props = mock(ServerExtendedProperties.class);
		IModuleType moduleType = mock(IModuleType.class);
		when(server.getServerType()).thenReturn(serverType);
		when(server.loadAdapter(eq(IURLProvider.class), any(IProgressMonitor.class))).thenReturn(deployableServer);
		when(server.loadAdapter(eq(ServerExtendedProperties.class), any(IProgressMonitor.class))).thenReturn(props);
		when(serverType.getId()).thenReturn("mock!");
		when(module.getModuleType()).thenReturn(moduleType);
	}

	@Test
	public void liveReloadEnabledWhenModuleIsStarted() throws MalformedURLException {	
		when(server.getModuleState(any(IModule[].class))).thenReturn(IServer.STATE_STARTED);
		when(server.getServerState()).thenReturn(IServer.STATE_STARTED);
		when(deployableServer.getModuleRootURL(module)).thenReturn(new URL("http", "foo", 9090, "/module"));
		when(module.getModuleType().getId()).thenReturn(IWTPConstants.FACET_WEB);
		
		OpenInWebBrowserViaLiveReloadProxyEnablementTester tester = new OpenInWebBrowserViaLiveReloadProxyEnablementTester();
		IServerModule serverModule = new ModuleServer(server, new IModule[] {module});
		assertTrue(tester.test(new StructuredSelection(serverModule), "", null, null));
	}
	
	@Test
	public void liveReloadEnabledWhenModuleIsInUnknownState() throws MalformedURLException {	
		when(server.getModuleState(any(IModule[].class))).thenReturn(IServer.STATE_UNKNOWN);
		when(server.getServerState()).thenReturn(IServer.STATE_STARTED);
		when(deployableServer.getModuleRootURL(module)).thenReturn(new URL("http", "foo", 9090, "/module"));
		when(module.getModuleType().getId()).thenReturn(IWTPConstants.FACET_WEB);
		
		OpenInWebBrowserViaLiveReloadProxyEnablementTester tester = new OpenInWebBrowserViaLiveReloadProxyEnablementTester();
		IServerModule serverModule = new ModuleServer(server, new IModule[] {module});
		assertTrue(tester.test(new StructuredSelection(serverModule), "", null, null));
	}
	
	@Test
	public void liveReoladDisabledWhenModuleNotStarted() throws MalformedURLException {	
		when(server.getModuleState(any(IModule[].class))).thenReturn(IServer.STATE_STOPPED);
		when(server.getServerState()).thenReturn(IServer.STATE_STARTED);
		when(deployableServer.getModuleRootURL(module)).thenReturn(new URL("http", "foo", 9090, "/module"));
		when(module.getModuleType().getId()).thenReturn(IWTPConstants.FACET_WEB);
		
		OpenInWebBrowserViaLiveReloadProxyEnablementTester tester = new OpenInWebBrowserViaLiveReloadProxyEnablementTester();
		IServerModule serverModule = new ModuleServer(server, new IModule[] {module});
		assertFalse(tester.test(new StructuredSelection(serverModule), "", null, null));
	}
	
	@Test
	public void liveReloadDisabledWhenServerNotStarted() throws MalformedURLException {	
		when(server.getModuleState(any(IModule[].class))).thenReturn(IServer.STATE_STARTED);
		when(server.getServerState()).thenReturn(IServer.STATE_STOPPED);
		when(deployableServer.getModuleRootURL(module)).thenReturn(new URL("http", "foo", 9090, "/module"));
		when(module.getModuleType().getId()).thenReturn(IWTPConstants.FACET_WEB);
		
		OpenInWebBrowserViaLiveReloadProxyEnablementTester tester = new OpenInWebBrowserViaLiveReloadProxyEnablementTester();
		IServerModule serverModule = new ModuleServer(server, new IModule[] {module});
		assertFalse(tester.test(new StructuredSelection(serverModule), "", null, null));
	}
	
	@Test
	public void liveReloadDisabledWhenURLisNull() throws MalformedURLException {	
		when(server.getModuleState(any(IModule[].class))).thenReturn(IServer.STATE_STARTED);
		when(server.getServerState()).thenReturn(IServer.STATE_STARTED);
		when(deployableServer.getModuleRootURL(module)).thenReturn(null);
		when(module.getModuleType().getId()).thenReturn(IWTPConstants.FACET_WEB);
		
		OpenInWebBrowserViaLiveReloadProxyEnablementTester tester = new OpenInWebBrowserViaLiveReloadProxyEnablementTester();
		IServerModule serverModule = new ModuleServer(server, new IModule[] {module});
		assertFalse(tester.test(new StructuredSelection(serverModule), "", null, null));
	}
	
	@Test
	public void liveReloadDisabledWhenModuleIsEjb() throws MalformedURLException {	
		when(server.getModuleState(any(IModule[].class))).thenReturn(IServer.STATE_STARTED);
		when(server.getServerState()).thenReturn(IServer.STATE_STARTED);
		when(deployableServer.getModuleRootURL(module)).thenReturn(new URL("http", "foo", 9090, "/module"));
		when(module.getModuleType().getId()).thenReturn(IWTPConstants.FACET_EJB);
		
		OpenInWebBrowserViaLiveReloadProxyEnablementTester tester = new OpenInWebBrowserViaLiveReloadProxyEnablementTester();
		IServerModule serverModule = new ModuleServer(server, new IModule[] {module});
		assertFalse(tester.test(new StructuredSelection(serverModule), "", null, null));
	}

}
