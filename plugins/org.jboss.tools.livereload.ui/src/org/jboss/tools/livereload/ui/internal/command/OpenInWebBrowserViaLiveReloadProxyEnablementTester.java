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

import static org.jboss.tools.livereload.ui.internal.command.OpenInWebBrowserViaLiveReloadUtils.retrieveServerModuleFromSelectedElement;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.IServerModule;
import org.jboss.tools.livereload.core.internal.server.jetty.LiveReloadProxyServer;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;

/**
 * Checks if the selected {@link IServerModule} is deployed on a {@link IServer}
 * that is started and has a {@link LiveReloadProxyServer} associated with it.
 * 
 * @author xcoulon
 */
public class OpenInWebBrowserViaLiveReloadProxyEnablementTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		final IServerModule appModule = retrieveServerModuleFromSelectedElement(receiver);
		if(appModule == null) {
			return false;
		}
		final IServer appServer = appModule.getServer();
		return appServer != null && WSTUtils.isServerStarted(appServer);
	}

}
