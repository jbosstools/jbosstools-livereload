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

import org.jboss.tools.livereload.core.internal.util.HttpUtils;
import org.junit.Test;

/**
 * @author xcoulon
 *
 */
public class HttpUtilsTestCase {

	@Test
	public void shouldAcceptSimpleType() {
		// pre-condition
		final String acceptType = "text/html";
		// operation
		final boolean isHtmlContentType = HttpUtils.isHtmlContentType(acceptType);
		// verification
		assertThat(isHtmlContentType).isTrue();
	}

	@Test
	public void shouldAcceptMultipleTypesWithQualityFactors() {
		// pre-condition
		final String acceptType = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
		// operation
		final boolean isHtmlContentType = HttpUtils.isHtmlContentType(acceptType);
		// verification
		assertThat(isHtmlContentType).isTrue();
	}

	@Test
	public void shouldAcceptSimpleTypeWithCharset() {
		// pre-condition
		final String acceptType = "text/html;charset=UTF-8";
		// operation
		final boolean isHtmlContentType = HttpUtils.isHtmlContentType(acceptType);
		// verification
		assertThat(isHtmlContentType).isTrue();
	}
	
	@Test
	public void shouldNotAcceptSimpleType() {
		// pre-condition
		final String acceptType = "text/css";
		// operation
		final boolean isHtmlContentType = HttpUtils.isHtmlContentType(acceptType);
		// verification
		assertThat(isHtmlContentType).isFalse();
	}
	
}
