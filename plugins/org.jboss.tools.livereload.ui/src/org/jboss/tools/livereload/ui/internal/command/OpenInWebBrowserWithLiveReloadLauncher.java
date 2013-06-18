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

package org.jboss.tools.livereload.ui.internal.command;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.ui.internal.util.ICallback;
import org.jboss.tools.livereload.ui.internal.util.Logger;

/**
 * @author xcoulon
 * 
 */
public class OpenInWebBrowserWithLiveReloadLauncher implements IEditorLauncher {

	/**
	 * Opens the given file using the file:// protocol
	 */
	public void open(final IPath file) {
		try {
			OpenInWebBrowserViaLiveReloadUtils.openWithLiveReloadServer(file, true, false, new ICallback() {
				@Override
				public void execute(IServer liveReloadServer) {
					try {
						OpenInWebBrowserViaLiveReloadUtils.openInBrowser(file, liveReloadServer);
					} catch (Exception e) {
						Logger.error("Failed to open file in Web Browser via LiveReload Server", e);
					}
				}
			});
		} catch (Exception e) {
			Logger.error("Failed to Open in Web Browser via LiveReload", e);
		}
	}
}
