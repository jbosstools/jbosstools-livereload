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

import org.eclipse.core.resources.IResource;
import org.jboss.tools.livereload.internal.AbstractCommonTestCase;
import org.junit.Test;

/**
 * @author xcoulon
 *
 */
public class ResourceUtilsTestCase extends AbstractCommonTestCase {
	
	@Test
	public void shouldRetrieveProjectFileFromContextLocation() {
		// precondition
		final String location = project.getName() + "/WebContent/index.html";
		// operation
		final IResource resource = ResourceUtils.retrieveResource(location);
		// verification
		assertThat(resource).isNotNull();
	}

	@Test
	public void shouldNotRetrieveProjectFolderFromContextLocation() {
		// precondition
		final String location = project.getName() + "/WebContent/";
		// operation
		final IResource resource = ResourceUtils.retrieveResource(location);
		// verification
		assertThat(resource).isNotNull();
	}
	
	@Test
	public void shouldNotRetrieveProjectResourceFromDummyLocation() {
		// precondition
		final String location = project.getName() + "/src/main/webapp/dummy.html";
		// operation
		final IResource resource = ResourceUtils.retrieveResource(location);
		// verification
		assertThat(resource).isNull();
	}

}
