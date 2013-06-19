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

package org.jboss.tools.livereload.core.internal.util;

import java.util.StringTokenizer;

/**
 * Utility for HTTP Requests
 * @author xcoulon
 *
 */
public class HttpUtils {

	/**
	 * Private constructor of this utiliy class
	 */
	private HttpUtils() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * <p>
	 * Iterates over the given acceptedContentTypes, looking for one of those
	 * values:
	 * <ul>
	 * <li>text/html</li>
	 * <li>application/xhtml+xml</li>
	 * <li>application/xml</li>
	 * </ul>
	 * </p>
	 * 
	 * @param acceptedContentTypes
	 * @return true if one of the values above was found, false otherwise
	 */
	public static boolean isHtmlContentType(final String acceptedContentTypes) {
		if (acceptedContentTypes == null) {
			return false;
		}
		// first, let's remove everything after the coma character
		int location = acceptedContentTypes.indexOf(";");
		final String contentTypes = (location != -1) ? acceptedContentTypes.substring(0, location):acceptedContentTypes; 
		// now, let's analyze each type
		final StringTokenizer tokenizer = new StringTokenizer(contentTypes, ",");
		while (tokenizer.hasMoreElements()) {
			final String acceptedContentType = tokenizer.nextToken();
			if ("text/html".equals(acceptedContentType) || "application/xhtml+xml".equals(acceptedContentType)
					|| "application/xml".equals(acceptedContentType)) {
				return true;
			}
		}
		return false;
	}

}
