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

package org.jboss.tools.livereload.core.internal.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Utility class
 * 
 * @author xcoulon
 * 
 */
public class ProjectUtils {

	/**
	 * Attempts to retrieve the enclosing {@link IProject} for a given absolute
	 * path on the OS.
	 * 
	 * @param fileLocation
	 * @return the enclosing Project or null if nothing matches.
	 * @throws URISyntaxException
	 */
	public static IProject extractProject(final String fileLocation) {
		try {
			URI fileURI = new URI(fileLocation);
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			final IFile[] filesForLocation = root.findFilesForLocationURI(fileURI);
			for (IFile file : filesForLocation) {
				if (file.exists()) {
					return file.getProject();
				}
			}
		} catch (URISyntaxException e) {
			Logger.error("Failed to convert given file location into an URI:" + fileLocation, e);
		}
		Logger.warn("Unable to retrieve project from file location:" + fileLocation);
		return null;
	}
	
	/**
	 * Returns the {@link IProject} containing the given file or null if none matches.
	 * @param file
	 * @return the surrounding project or null.
	 */
	public static IProject findProjectFromAbsolutePath(final IPath file) {
		for(IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if(project.getLocation().isPrefixOf(file)) {
				return project;
			}
		}
		return null;
	}

	/**
	 * Returns the {@link IProject} containing the given file or null if none matches.
	 * @param file
	 * @return the surrounding project or null.
	 */
	public static IProject findProjectFromResourceLocation(final IPath file) {
		final IPath relativePath = file.makeRelative();
		for(IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if(new Path(project.getName()).isPrefixOf(relativePath)) {
				return project;
			}
		}
		return null;
	}
}
