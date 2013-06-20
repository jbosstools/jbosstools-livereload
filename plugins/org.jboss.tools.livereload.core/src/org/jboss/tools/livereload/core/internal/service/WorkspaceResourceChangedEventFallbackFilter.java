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

import java.util.EventObject;

import org.eclipse.core.resources.IResource;

/**
 * A fallback filter in case where the client establishes a connection from a
 * browser location that cannot be resolved to an existing server or project. In
 * that case, the filter will allow any {@link WorkspaceResourceChangedEvent}.
 * This is sub-optimal because *any workspace change* will end-up in a browser
 * notification, but still, it allows for LiveReload support.
 * 
 * @author xcoulon
 * 
 */
public class WorkspaceResourceChangedEventFallbackFilter extends WorkspaceResourceChangedEventFilter {

	/**
	 * Default constructor.
	 * 
	 * @param project
	 */
	public WorkspaceResourceChangedEventFallbackFilter() {
		super(null);
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
				if (acceptedFileTypes.contains(fileExtension)) {
					return true;
				}
			}
		}
		return false;
	}

}
