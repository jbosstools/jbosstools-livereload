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

import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * @author xcoulon
 * 
 */
public class WorkspaceResourceChangedEventFilter implements EventFilter {

	/**
	 * The list of file extensions that are allowed. Typically, these correspond
	 * to files that are part of HTML pages (HTML, CSS, JS, Images).
	 */
	private static final List<String> acceptedFileTypes = Arrays.asList("html", "htm", "css", "js", "gif", "png",
			"jpg", "jpeg", "bmp", "ico");

	/**
	 * The Eclipse project for which this filter should allow events. Events
	 * related to files in other projects should be discarded by this filter, no
	 * matter which extension they have.
	 */
	private final IProject project;

	/**
	 * Default constructor.
	 * 
	 * @param project
	 */
	public WorkspaceResourceChangedEventFilter(final IProject project) {
		this.project = project;
	}

	/**
	 * Returns true if the Event is an {@link WorkspaceResourceChangedEvent} and
	 * the underlying changed resource is a static web resource
	 * 
	 * @see {@link WorkspaceResourceChangedEventFilter.acceptedFileTypes}
	 */
	@Override
	public boolean accept(EventObject e) {
		if (e instanceof WorkspaceResourceChangedEvent) {
			WorkspaceResourceChangedEvent event = (WorkspaceResourceChangedEvent) e;
			for (IResource resource : event.getChangedResources()) {
				if (resource.getProject().equals(project) && acceptedFileTypes.contains(resource.getFileExtension())) {
					return true;
				}
			}
		}
		return false;
	}

}
