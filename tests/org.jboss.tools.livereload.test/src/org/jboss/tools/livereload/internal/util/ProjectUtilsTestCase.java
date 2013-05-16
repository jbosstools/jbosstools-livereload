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

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.jboss.tools.livereload.internal.AbstractCommonTestCase;
import org.junit.Test;

/**
 * @author xcoulon
 *
 */
public class ProjectUtilsTestCase extends AbstractCommonTestCase {

	@Test
	public void shouldExtractProjectFromAbsoluteLocation() {
		// pre-condition
		final IResource index_html_file = project.findMember("src" + File.separator + "main" + File.separator + "webapp" + File.separator + "index.html");
		final String location = "file://" + index_html_file.getLocation().toOSString();
		// operation
		IProject project = ProjectUtils.extractProject(location);
		// verification
		assertThat(project).isNotNull().isEqualTo(project);
	}
	
	@Test
	public void shouldNotExtractProjectFromAbsoluteLocation() {
		// pre-condition
		final String location = "file:///path/to/workspace/project/path/to/file";
		// operation
		IProject project = ProjectUtils.extractProject(location);
		// verification
		assertThat(project).isNull();
	}
}
