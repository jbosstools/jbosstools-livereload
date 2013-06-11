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

package org.jboss.tools.livereload.internal.command;

import static org.jboss.tools.livereload.internal.command.OpenInWebBrowserViaLiveReloadUtils.checkAppServerStarted;
import static org.jboss.tools.livereload.internal.command.OpenInWebBrowserViaLiveReloadUtils.checkAppServerWatched;
import static org.jboss.tools.livereload.internal.command.OpenInWebBrowserViaLiveReloadUtils.retrieveServerFromSelectedElement;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.wst.server.core.IServer;

/**
 * @author xcoulon
 * 
 */
public class OpenInWebBrowserViaLiveReloadProxyEnablementTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		final IServer appServer = retrieveServerFromSelectedElement(receiver);
		return appServer != null && checkAppServerStarted(appServer) && checkAppServerWatched(appServer);
	}

}
