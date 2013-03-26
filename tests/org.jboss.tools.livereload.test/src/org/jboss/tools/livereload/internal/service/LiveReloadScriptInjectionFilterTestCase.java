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

package org.jboss.tools.livereload.internal.service;

import java.io.IOException;
import java.io.InputStream;

import org.fest.assertions.Assertions;
import org.jboss.tools.livereload.internal.server.jetty.LiveReloadScriptInjectionFilter;
import org.junit.Test;

/**
 * @author xcoulon
 *
 */
public class LiveReloadScriptInjectionFilterTestCase {
	
	@Test
	public void shouldInjectScriptAtEndOfBody() throws IOException {
		// pre-conditions
		final InputStream sourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("aerogear-index.txt");
		final String addition = "<script src='foo!'/>";
		// operation
		final char[] modifiedContent = LiveReloadScriptInjectionFilter.injectContent(sourceStream, addition);
		// verifications
		Assertions.assertThat(new String(modifiedContent)).contains(addition + "</body>");
	}

	@Test
	public void shouldNotInjectScriptOnHtmlPageFragment() throws IOException {
		// pre-conditions
		final InputStream sourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("aerogear-members-template.txt");
		final String addition = "<script src='foo!'/>";
		// operation
		final char[] modifiedContent = LiveReloadScriptInjectionFilter.injectContent(sourceStream, addition);
		// verifications
		Assertions.assertThat(new String(modifiedContent)).doesNotContain(addition + "</body>");
	}

}
