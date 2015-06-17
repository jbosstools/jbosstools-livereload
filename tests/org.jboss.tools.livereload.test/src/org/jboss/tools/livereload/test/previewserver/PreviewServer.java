//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.jboss.tools.livereload.test.previewserver;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.tools.livereload.core.internal.server.jetty.JettyServerRunner;
import org.jboss.tools.livereload.core.internal.server.jetty.WorkspaceFileServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Preview server that serves all the content from a given base location.
 * 
 * @author xcoulon
 *
 */
public class PreviewServer extends Server {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PreviewServer.class);

	public void configure(final int port, final String baseLocation) throws Exception {
		setAttribute(JettyServerRunner.NAME, "Test Preview Server");
		final ServerConnector connector = new ServerConnector(this);
		connector.setHost("localhost");
		connector.setPort(port);
		addConnector(connector);
		final ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setResourceBase(baseLocation);
		
		final ServletHandler workspaceServletHandler = new ServletHandler();
		workspaceServletHandler.addServletWithMapping(new ServletHolder(new WorkspaceFileServlet()), "/");
		
		final ServletContextHandler webHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        webHandler.setContextPath("/foo");
        webHandler.addServlet(new ServletHolder(new RedirectServlet()), "/baz");
		webHandler.addServlet(new ServletHolder(new QueryParamVerifierServlet()), "/bar");
		LOGGER.info("serving {} on port {}", resourceHandler.getBaseResource(), port );
		final HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] {webHandler, workspaceServletHandler, new DefaultHandler() });
		setHandler(handlers);
	}
	
	@Override
	public String toString() {
		return "Test Preview Server";
	}
	
}
