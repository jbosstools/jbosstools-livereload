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

package org.jboss.tools.livereload.internal.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

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
		return null;
	}
}
