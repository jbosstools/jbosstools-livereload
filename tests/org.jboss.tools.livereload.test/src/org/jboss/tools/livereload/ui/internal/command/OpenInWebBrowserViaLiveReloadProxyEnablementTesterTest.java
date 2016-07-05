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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Dmitry Bocharov
 *
 */
public class OpenInWebBrowserViaLiveReloadProxyEnablementTesterTest {
	
	private IServer server;
	private IURLProvider deployableServer;
	private IModule module;
	
	@Before
	public void initialize() {
		String host = "hostname";
		int port = 8080;
		server = Mockito.mock(IServer.class);
		module = Mockito.mock(IModule.class);
		IServerType serverType = Mockito.mock(IServerType.class);
		deployableServer = Mockito.mock(IURLProvider.class, Mockito.RETURNS_DEEP_STUBS);
		ServerExtendedProperties props = Mockito.mock(ServerExtendedProperties.class);
		IModuleType moduleType = Mockito.mock(IModuleType.class);
		Mockito.when(server.getServerType()).thenReturn(serverType);
		Mockito.when(server.loadAdapter(Mockito.eq(IURLProvider.class), Mockito.any(IProgressMonitor.class))).thenReturn(deployableServer);
		Mockito.when(server.loadAdapter(Mockito.eq(ServerExtendedProperties.class), Mockito.any(IProgressMonitor.class))).thenReturn(props);
		Mockito.when(serverType.getId()).thenReturn("mock!");
		Mockito.when(module.getModuleType()).thenReturn(moduleType);
	}

	@Test
	public void liveReloadEnabledWhenModuleIsStarted() throws MalformedURLException {	
		Mockito.when(server.getModuleState(Mockito.any(IModule[].class))).thenReturn(IServer.STATE_STARTED);
		Mockito.when(server.getServerState()).thenReturn(IServer.STATE_STARTED);
		Mockito.when(deployableServer.getModuleRootURL(module)).thenReturn(new URL("http", "foo", 9090, "/module"));
		Mockito.when(module.getModuleType().getId()).thenReturn(IWTPConstants.FACET_WEB);
		
		OpenInWebBrowserViaLiveReloadProxyEnablementTester tester = new OpenInWebBrowserViaLiveReloadProxyEnablementTester();
		IServerModule serverModule = new ModuleServer(server, new IModule[] {module});
		assertTrue(tester.test(new StructuredSelection(serverModule), "", null, null));
	}
	
	@Test
	public void liveReloadEnabledWhenModuleIsInUnknownState() throws MalformedURLException {	
		Mockito.when(server.getModuleState(Mockito.any(IModule[].class))).thenReturn(IServer.STATE_UNKNOWN);
		Mockito.when(server.getServerState()).thenReturn(IServer.STATE_STARTED);
		Mockito.when(deployableServer.getModuleRootURL(module)).thenReturn(new URL("http", "foo", 9090, "/module"));
		Mockito.when(module.getModuleType().getId()).thenReturn(IWTPConstants.FACET_WEB);
		
		OpenInWebBrowserViaLiveReloadProxyEnablementTester tester = new OpenInWebBrowserViaLiveReloadProxyEnablementTester();
		IServerModule serverModule = new ModuleServer(server, new IModule[] {module});
		assertTrue(tester.test(new StructuredSelection(serverModule), "", null, null));
	}
	
	@Test
	public void liveReoladDisabledWhenModuleNotStarted() throws MalformedURLException {	
		Mockito.when(server.getModuleState(Mockito.any(IModule[].class))).thenReturn(IServer.STATE_STOPPED);
		Mockito.when(server.getServerState()).thenReturn(IServer.STATE_STARTED);
		Mockito.when(deployableServer.getModuleRootURL(module)).thenReturn(new URL("http", "foo", 9090, "/module"));
		Mockito.when(module.getModuleType().getId()).thenReturn(IWTPConstants.FACET_WEB);
		
		OpenInWebBrowserViaLiveReloadProxyEnablementTester tester = new OpenInWebBrowserViaLiveReloadProxyEnablementTester();
		IServerModule serverModule = new ModuleServer(server, new IModule[] {module});
		assertFalse(tester.test(new StructuredSelection(serverModule), "", null, null));
	}
	
	@Test
	public void liveReloadDisabledWhenServerNotStarted() throws MalformedURLException {	
		Mockito.when(server.getModuleState(Mockito.any(IModule[].class))).thenReturn(IServer.STATE_STARTED);
		Mockito.when(server.getServerState()).thenReturn(IServer.STATE_STOPPED);
		Mockito.when(deployableServer.getModuleRootURL(module)).thenReturn(new URL("http", "foo", 9090, "/module"));
		Mockito.when(module.getModuleType().getId()).thenReturn(IWTPConstants.FACET_WEB);
		
		OpenInWebBrowserViaLiveReloadProxyEnablementTester tester = new OpenInWebBrowserViaLiveReloadProxyEnablementTester();
		IServerModule serverModule = new ModuleServer(server, new IModule[] {module});
		assertFalse(tester.test(new StructuredSelection(serverModule), "", null, null));
	}
	
	@Test
	public void liveReloadDisabledWhenURLisNull() throws MalformedURLException {	
		Mockito.when(server.getModuleState(Mockito.any(IModule[].class))).thenReturn(IServer.STATE_STARTED);
		Mockito.when(server.getServerState()).thenReturn(IServer.STATE_STARTED);
		Mockito.when(deployableServer.getModuleRootURL(module)).thenReturn(null);
		Mockito.when(module.getModuleType().getId()).thenReturn(IWTPConstants.FACET_WEB);
		
		OpenInWebBrowserViaLiveReloadProxyEnablementTester tester = new OpenInWebBrowserViaLiveReloadProxyEnablementTester();
		IServerModule serverModule = new ModuleServer(server, new IModule[] {module});
		assertFalse(tester.test(new StructuredSelection(serverModule), "", null, null));
	}
	
	@Test
	public void liveReloadDisabledWhenModuleIsEjb() throws MalformedURLException {	
		Mockito.when(server.getModuleState(Mockito.any(IModule[].class))).thenReturn(IServer.STATE_STARTED);
		Mockito.when(server.getServerState()).thenReturn(IServer.STATE_STARTED);
		Mockito.when(deployableServer.getModuleRootURL(module)).thenReturn(new URL("http", "foo", 9090, "/module"));
		Mockito.when(module.getModuleType().getId()).thenReturn(IWTPConstants.FACET_EJB);
		
		OpenInWebBrowserViaLiveReloadProxyEnablementTester tester = new OpenInWebBrowserViaLiveReloadProxyEnablementTester();
		IServerModule serverModule = new ModuleServer(server, new IModule[] {module});
		assertFalse(tester.test(new StructuredSelection(serverModule), "", null, null));
	}

}
