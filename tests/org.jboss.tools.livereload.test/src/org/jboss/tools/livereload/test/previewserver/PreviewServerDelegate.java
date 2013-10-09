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

package org.jboss.tools.livereload.test.previewserver;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.tools.livereload.internal.LiveReloadTestActivator;

/**
 * @author xcoulon
 *
 */
public class PreviewServerDelegate extends ServerDelegate {

private final List<IProject> projects = new ArrayList<IProject>();
	
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		return new Status(IStatus.OK, LiveReloadTestActivator.PLUGIN_ID, "foo");
	}

	public IModule[] getChildModules(IModule[] module) {
		return new IModule[0];
	}

	public IModule[] getRootModules(IModule module) throws CoreException {
		return new IModule[] { module };
	}

	@Override
	public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {

	}
	
	public void addProject(final IProject project) {
		projects.add(project);
	}
	
	

}
