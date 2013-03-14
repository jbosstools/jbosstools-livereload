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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.wst.server.core.IServer;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof ITreeSelection && ((ITreeSelection) receiver).getFirstElement() instanceof IServer) {
			final IServer server = (IServer) ((ITreeSelection) receiver).getFirstElement();
			if ("serverStarted".equals(property)) {
				return server.getServerState() == IServer.STATE_STARTED;
			}
			return true;
		}
		return false;
	}

}
