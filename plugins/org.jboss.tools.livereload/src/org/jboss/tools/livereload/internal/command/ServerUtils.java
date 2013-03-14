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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;

/**
 * Utility class
 * @author xcoulon
 *
 */
public class ServerUtils {
	
	/** 
	 * Utility class: no public constructor.
	 */
	private ServerUtils() {
		
	}
	
	/**
	 * @returns the Selected Server 
	 */
	public static IServer getSelectedServer() {
		IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart();
		IStructuredSelection selection = (IStructuredSelection) activePart.getSite().getSelectionProvider()
				.getSelection();
		final IServer server = (IServer) (selection.getFirstElement());
		return server;
	}

	/**
	 * Returns the HTTP port for the given {@link IServer}
	 * @param server
	 * @return
	 */
	public static int getPort(IServer server) {
		return 0;
	}

}
