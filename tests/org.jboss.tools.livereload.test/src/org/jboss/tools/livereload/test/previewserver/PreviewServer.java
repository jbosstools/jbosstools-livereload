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

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.tools.livereload.core.internal.server.jetty.JettyServerRunner;
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
		Connector connector = new SelectChannelConnector();
		connector.setHost("localhost");
		connector.setStatsOn(true);
		connector.setPort(port);
		connector.setMaxIdleTime(0);
		addConnector(connector);
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setResourceBase(baseLocation);
		
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/foo");
		context.addServlet(new ServletHolder(new QueryParamVerifierServlet()), "/bar");
		LOGGER.info("serving {} on port {}", resourceHandler.getBaseResource(), port );
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resourceHandler, context, new DefaultHandler() });
		setHandler(handlers);
	}
	
	@Override
	public String toString() {
		return "Test Preview Server";
	}
	
}
