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

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.client.api.Response;
import org.junit.Test;
import org.mockito.Mockito;
import static org.assertj.core.api.Assertions.assertThat;
/**
 * @author xcoulon
 *
 */
public class LiveReloadScriptInjectionMiddleManServletTestCase {
	
	@Test
	public void shouldFilterLocation() throws Exception {
		// given
		final LiveReloadScriptInjectionMiddleManServlet proxyServlet = new LiveReloadScriptInjectionMiddleManServlet("localhost", 54321, "/", "dockerhost", 8080, "/", 35729, false);
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		final Response response = Mockito.mock(Response.class);
		// when
		final String locationHeaderValue = proxyServlet.filterServerResponseHeader(request, response, "Location", "http://dockerhost:8080/foo/bar/");
		// then
		assertThat(locationHeaderValue).isEqualTo("http://localhost:54321/foo/bar/");
	}

}
