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

package org.jboss.tools.livereload.core.internal.service;

import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Filter to allow event of type {@link WorkspaceResourceChangedEvent} if the parent project matches the expected one.
 * @author xcoulon
 * 
 */
public class WorkspaceResourceChangedEventFilter implements EventFilter {

	/**
	 * The list of file extensions that are allowed. Typically, these correspond
	 * to files that are part of HTML pages (HTML, CSS, JS, Images).
	 */
	static final List<String> acceptedFileTypes = Arrays.asList("html", "xhtml", "htm", "css", "js", "gif", "png",
			"jpg", "jpeg", "bmp", "ico", "adoc", "asciidoc");

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
				final String fileExtension = resource.getFileExtension() != null ? resource.getFileExtension().toLowerCase() : null;
				if (resource.getProject().equals(project) && fileExtension != null && acceptedFileTypes.contains(fileExtension)) {
					return true;
				}
			}
		}
		return false;
	}

}
