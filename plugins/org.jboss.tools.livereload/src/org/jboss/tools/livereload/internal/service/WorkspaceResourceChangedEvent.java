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

package org.jboss.tools.livereload.internal.service;

import java.util.EventObject;
import java.util.List;

import org.eclipse.core.resources.IResource;

/**
 * @author xcoulon
 * 
 */
public class WorkspaceResourceChangedEvent extends EventObject {

	/** serialVersionUID */
	private static final long serialVersionUID = 3223885950785131235L;

	/**
	 * Construtor
	 * 
	 * @param changedResources
	 *            the resource that changed in the workspace
	 */
	public WorkspaceResourceChangedEvent(final List<IResource> changedResources) {
		super(changedResources);
	}

	/**
	 * Handy method to retrieve the changed resource
	 * 
	 * @return the resource that changed in the workspace
	 */
	@SuppressWarnings("unchecked")
	public List<IResource> getChangedResources() {
		return (List<IResource>) getSource();
	}
	
	@Override
	public String toString() {
		return WorkspaceResourceChangedEvent.class.getSimpleName() + ": " + getSource(); 
	}
	

}
