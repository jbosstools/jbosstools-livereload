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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author xcoulon
 * 
 */
public class URIUtils {

	public static URIConverter convert(String path) throws URISyntaxException {
		return new URIConverter(new URI(path));
	}

	public static URIConverter convert(URI uri) {
		return new URIConverter(uri);
	}

	public static class URIConverter {

		final URI originalURI;

		/**
		 * Constructor
		 * 
		 * @param path
		 */
		public URIConverter(final URI originalURI) {
			this.originalURI = originalURI;
		}

		public String toPort(int newPort) throws URISyntaxException {
			final URI modifiedURI = new URI(originalURI.getScheme(), null,
					originalURI.getHost() != null ? originalURI.getHost() : "localhost", newPort,
					originalURI.getPath(), originalURI.getQuery(), originalURI.getFragment());
			return modifiedURI.toString();
		}

	}
}
