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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;

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
	
	@Test
	public void shouldExtractCharset() {
		// pre-condition
		final String contentType = "text/html; charset=UTF-8";
		// operation
		final Charset charset = HttpUtils.getContentCharSet(contentType, "ISO-8859-1");
		// verification
		assertThat(charset.name()).isEqualTo("UTF-8");
	}
	
	@Test
	public void shouldReturnDefaultCharset() {
		// pre-condition
		final String contentType = "text/css";
		// operation
		final Charset charset = HttpUtils.getContentCharSet(contentType, "ISO-8859-1");
		// verification
		assertThat(charset.name()).isEqualTo("ISO-8859-1");
	}

	@Test
	public void shouldReturnUTF8Charset() {
		// pre-condition
		final String contentType = "text/css";
		// operation
		final Charset charset = HttpUtils.getContentCharSet(contentType, "foobar");
		// verification
		assertThat(charset.name()).isEqualTo("UTF-8");
	}
}
