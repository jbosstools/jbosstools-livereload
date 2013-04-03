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

package org.jboss.tools.livereload.internal.server.jetty;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.jboss.tools.livereload.internal.server.jetty.LiveReloadWebSocketServlet.LiveReloadSocket;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.URIUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author xcoulon
 * 
 */
public class ResourcePublishedCommandBroadcaster extends AbstractCommandBroadcaster {

	private final int port;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public ResourcePublishedCommandBroadcaster(int port) {
		this.port = port;
	}

	@Override
	String buildRefreshCommand(IPath path) throws JsonGenerationException, JsonMappingException, IOException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}


}
