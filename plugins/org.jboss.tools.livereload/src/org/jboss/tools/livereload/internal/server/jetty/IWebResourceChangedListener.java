package org.jboss.tools.livereload.internal.server.jetty;

import org.eclipse.core.runtime.IPath;

public interface IWebResourceChangedListener {

	/**
	 * Receives notification when some resource with the given path changed.
	 * 
	 * @param path
	 *            the path of the resource that changed.
	 */
	public abstract void notifyResourceChange(IPath path);

}