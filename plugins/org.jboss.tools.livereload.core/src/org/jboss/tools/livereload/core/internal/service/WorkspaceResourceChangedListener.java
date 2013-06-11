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

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.jboss.tools.livereload.core.internal.util.Logger;
import org.jboss.tools.livereload.core.internal.util.ResourceChangeEventVisitor;

/**
 * Listener to workspace changes (during builds). Filters the events so that
 * only 1 event is forwarded during each build.
 * 
 * @author xcoulon
 * 
 */
public class WorkspaceResourceChangedListener implements IResourceChangeListener {

	@Override
	public void resourceChanged(final IResourceChangeEvent e) {
		final List<IResource> changedResources = ResourceChangeEventVisitor.getAffectedFiles(e);
		Logger.trace("Received event of type {} and kind {} on changed files {}", e.getType(), e.getBuildKind(), changedResources);
		EventService.getInstance().publish(new WorkspaceResourceChangedEvent(changedResources));
	}

}
