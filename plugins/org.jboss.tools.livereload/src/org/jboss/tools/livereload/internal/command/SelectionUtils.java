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

/**
 * Utility class
 * @author xcoulon
 *
 */
public class SelectionUtils {
	
	/** 
	 * Utility class: no public constructor.
	 */
	private SelectionUtils() {
		
	}
	
	/**
	 * @returns the Selected Element 
	 */
	public static Object getSelectedElement() {
		IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart();
		IStructuredSelection selection = (IStructuredSelection) activePart.getSite().getSelectionProvider()
				.getSelection();
		return selection.getFirstElement();
	}

}
