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
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.StructureEdit;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
import org.eclipse.wst.web.internal.deployables.StaticWebDeployable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xcoulon
 *
 */
@SuppressWarnings("restriction")
public class StaticWebDeployableTestFactory extends ProjectModuleFactoryDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(StaticWebDeployableTestFactory.class);
	
	public final static String WST_WEB_MODULE = "wst.web";
			
	private static final String ID = "org.eclipse.wst.web.internal.deployables.static"; //$NON-NLS-1$
	protected ArrayList<ModuleDelegate> moduleDelegates = new ArrayList<ModuleDelegate>();

	/*
	 * @see DeployableProjectFactoryDelegate#getFactoryID()
	 */
	public static String getFactoryId() {
		return ID;
	}
	
	/**
	 * Returns true if the project represents a deployable project of this type.
	 * 
	 * @param project
	 *            org.eclipse.core.resources.IProject
	 * @return boolean
	 */
	protected boolean isValidModule(IProject project) {
		try {
			IFacetedProject facetedProject = ProjectFacetsManager.create(project);
			if (facetedProject == null)
				return false;
			IProjectFacet webFacet = ProjectFacetsManager.getProjectFacet(WST_WEB_MODULE);
			return facetedProject.hasProjectFacet(webFacet);
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.server.core.model.ModuleFactoryDelegate#getModuleDelegate(org.eclipse.wst.server.core.IModule)
	 */
	@Override
	public ModuleDelegate getModuleDelegate(IModule module) {
		for (Iterator<ModuleDelegate> iter = moduleDelegates.iterator(); iter.hasNext();) {
			ModuleDelegate element = iter.next();
			if (module == element.getModule())
				return element;
		}
		return null;

	}

	@Override
	protected IModule[] createModules(IProject project) {
		IVirtualComponent component = ComponentCore.createComponent(project);
		if(component != null){
			try {
				return createModuleDelegates(component);
			} catch (CoreException e) {
				LOGGER.error("Failed to create modules for project", e);
			}
		}
		return null;
	}

	protected IModule[] createModuleDelegates(IVirtualComponent component) throws CoreException {
		if(component == null){
			return null;
		}
		StaticWebDeployable moduleDelegate = null;
		IModule module = null;
		try {
			if(isValidModule(component.getProject())) {
				moduleDelegate = new StaticWebDeployable(component.getProject(),component);
				module = createModule(component.getName(), component.getName(), IModuleConstants.WST_WEB_MODULE, moduleDelegate.getVersion(), moduleDelegate.getProject());
				moduleDelegate.initialize(module);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to create modules delegates for project", e);
		} finally {
			if (module != null) {
				if (getModuleDelegate(module) == null)
					moduleDelegates.add(moduleDelegate);
			}
		}
		if (module == null)
			return null;
		return new IModule[] {module};
	}
	
	/**
	 * Returns the list of resources that the module should listen to
	 * for state changes. The paths should be project relative paths.
	 * Subclasses can override this method to provide the paths.
	 *
	 * @return a possibly empty array of paths
	 */
	@Override
	protected IPath[] getListenerPaths() {
		return new IPath[] {
			new Path(".project"), // nature //$NON-NLS-1$
			new Path(StructureEdit.MODULE_META_FILE_NAME), // component
			new Path(".settings/org.eclipse.wst.common.project.facet.core.xml") // facets //$NON-NLS-1$
		};
	}
}
