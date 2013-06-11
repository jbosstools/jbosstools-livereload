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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * Utility class for IResources
 * @author xcoulon
 *
 */
public class ResourceUtils {
	
	/**
	 * <p>Locates the {@link IResource} associated with the given path, where the first segment is the project name, followed by containers and ending with the file name.<p>
	 * <p>For example: <code>/project/dir_A/dir_B/file.html</code>
	 * @param location
	 * @return the {@link IResource} or null if the matching {@link IResource} does not exist 
	 */
	public static IResource retrieveResource(final String location) {
		// try as a file first
		final Path path = new Path(location);
		final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		if(file != null && file.exists()) {
			return file;
		}
		// try as a folder
		final IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
		if(folder != null && folder.exists()) {
			return folder;
		}
		return null;
	}

}
