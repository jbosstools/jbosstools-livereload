package org.jboss.tools.livereload.internal.util;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;

public class WtpUtils {

	public static IFolder getWebappFolder(IProject project) {
		IVirtualComponent component = ComponentCore.createComponent(project);
		if (component == null) {
			return null;
		}
		IVirtualFolder contentFolder = component.getRootFolder();
		return (IFolder) contentFolder.getUnderlyingFolder();
	}

}