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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;

import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.StreamedSource;

/**
 * @author xcoulon
 *
 */
public class ScriptInjectionUtils {
	
	/**
	 * Inject the given 'addition' into the given source, just before the
	 * <code>&lt;/head&gt;</code> end tag (or the <code>&lt;/body&gt;</code> end tag if not {@code head} element exists). 
	 * If none pf those tags are found, the return value equals the given source.
	 * 
	 * @param source
	 * @param addition
	 * @return the modified source (or equal if not end tag was found in the source)
	 * @throws IOException
	 */
	public static char[] injectContent(final InputStream source, final String addition) throws IOException {
		boolean tagFound = false;
		final StreamedSource streamedSource = new StreamedSource(source);
		CharArrayWriter writer = new CharArrayWriter();
		for (Segment segment : streamedSource) {
			if (segment instanceof EndTag && ((EndTag) segment).getName().equals("head")) {
				writer.write(addition);
				tagFound = true;
			}
			else if (!tagFound && segment instanceof EndTag && ((EndTag) segment).getName().equals("body")) {
				writer.write(addition);
				tagFound = true;
			}
			writer.write(segment.toString());
		}
		writer.close();
		streamedSource.close();
		return writer.toCharArray();
	}

}
