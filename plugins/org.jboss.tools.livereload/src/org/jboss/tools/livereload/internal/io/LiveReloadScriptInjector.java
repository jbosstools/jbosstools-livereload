package org.jboss.tools.livereload.internal.io;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;

import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.StreamedSource;

/**
 * Script injector in a HTML page source
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadScriptInjector {

	/**
	 * Inject the given 'addition' into the gievne source, just before the
	 * <code>&lt;/body&gt;</code> ent tag. If no such end tag is found, the
	 * return value equals the given source.
	 * 
	 * @param source
	 * @param addition
	 * @return the modified source (or equal if not end tag was found in the source)
	 * @throws IOException
	 */
	public static char[] inject(final InputStream source, final String addition) throws IOException {
		final StreamedSource streamedSource = new StreamedSource(source);
		CharArrayWriter writer = new CharArrayWriter();
		for (Segment segment : streamedSource) {
			if (segment instanceof EndTag && ((EndTag) segment).getName().equals("body")) {
				writer.write(addition);
			}
			writer.write(segment.toString());
		}
		writer.close();
		streamedSource.close();
		return writer.toCharArray();
	}
}
