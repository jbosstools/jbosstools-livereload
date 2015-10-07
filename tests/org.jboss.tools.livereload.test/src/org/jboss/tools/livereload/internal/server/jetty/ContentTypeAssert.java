/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.livereload.internal.server.jetty;

import org.assertj.core.api.AbstractAssert;

/**Class for Content-Type(from response header) assertion.
 * 
 * @author Konstantin Marmalyukov
 *
 */
public class ContentTypeAssert extends AbstractAssert<ContentTypeAssert, String>{

	protected ContentTypeAssert(String actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public ContentTypeAssert(String actual) {
		super(actual, ContentTypeAssert.class);
	}
	
	public static ContentTypeAssert assertThat(String actual) {
	    return new ContentTypeAssert(actual);
	  }
	
	/** Verifies that the actual Content-Type value is equal to the given content type value ignoring case considerations and spaces.
	 * 
	 * Case and spaces are ignored cause it doesn't matter for browser. 
	 * 
	 */
	public ContentTypeAssert isEqualTo(String contentType) {
		if(!actual.replace(" ", "").equalsIgnoreCase(contentType.replace(" ", ""))) {
			failWithMessage("Expecting: <%s> to be equal to: <%s> ignoring case and whitespace difference.", actual, contentType);
		}
		return this;
	}
}
