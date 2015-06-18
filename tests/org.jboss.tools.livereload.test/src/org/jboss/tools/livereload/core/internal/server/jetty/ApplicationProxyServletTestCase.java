/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.jboss.tools.livereload.core.internal.server.jetty;

import static org.fest.assertions.Assertions.assertThat;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author xcoulon
 *
 */
public class ApplicationProxyServletTestCase {
	
	@Test
	public void shouldFilterLocation() throws Exception {
		// given
		final ApplicationsProxyServlet proxyServlet = new ApplicationsProxyServlet("localhost", 54321, "dockerhost", 8080);
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		// when
		final String locationHeaderValue = proxyServlet.filterResponseHeader(request, "Location", "http://dockerhost:8080/foo/bar/");
		// then
		assertThat(locationHeaderValue).isEqualTo("http://localhost:54321/foo/bar/");
	}

}
