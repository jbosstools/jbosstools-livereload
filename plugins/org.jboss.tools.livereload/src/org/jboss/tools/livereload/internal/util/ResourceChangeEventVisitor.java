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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;

/**
 * "Kinda visitor" to retrieve all resources of type {@link IResource#FILE} in a
 * given {@link IResourceChangeEvent}
 * 
 * @author xcoulon
 * 
 */
public class ResourceChangeEventVisitor {

	/**
	 * Retrieves all affected resources of type {@link IResource#FILE} in the
	 * given {@link IResourceChangeEvent}
	 * 
	 * @param delta
	 *            the
	 * @return
	 */
	public static List<IResource> getAffectedFiles(IResourceChangeEvent event) {
		CallBack callBack = new CallBack();
		callBack.addIfFile(event.getResource());
		visitEventDelta(event.getDelta(), callBack);
		return callBack.getFiles();
	}

	/**
	 * Retrieves the {@link IResource} associated with the given
	 * {@link IResourceDelta} and moves on the children delta.
	 * 
	 * @param delta
	 *            the given {@link IResourceDelta}
	 * @param callBack
	 *            the {@link CallBack} that retains all the resources of type
	 *            {@link IResource#FILE}
	 */
	private static void visitEventDelta(final IResourceDelta delta, final CallBack callBack) {
		callBack.addIfFile(delta.getResource());
		for (IResourceDelta childDelta : delta.getAffectedChildren()) {
			visitEventDelta(childDelta, callBack);
		}
	}

	/**
	 * Nested class that retains all the resources of type {@link IResource#FILE} 
	 * 
	 */
	static class CallBack {

		/** the resources of type {@link IResource#FILE}. */
		private final List<IResource> files = new ArrayList<IResource>();

		/**
		 * Add the given resource to the local list of files if it is not null and of type {@link IResource#FILE}.
		 * @param resource the given resource
		 */
		public void addIfFile(final IResource resource) {
			if (resource != null && resource.getType() == IResource.FILE) {
				getFiles().add(resource);
			}
		}

		/**
		 * @return the files
		 */
		public List<IResource> getFiles() {
			return files;
		}
	}

}
