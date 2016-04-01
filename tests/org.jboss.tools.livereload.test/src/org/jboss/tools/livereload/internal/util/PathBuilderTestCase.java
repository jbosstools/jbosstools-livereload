/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.jboss.tools.livereload.internal.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.tools.livereload.core.internal.util.PathBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Testing the {@link PathBuilder} class.
 */
@RunWith(Parameterized.class)
public class PathBuilderTestCase {

	@Parameters(name = "[{index}] expect ''{0}'' == ''{1}''")
	public static Object[][] data() {
		return new Object[][] { new Object[] { PathBuilder.from("/").build(), "/" },
				new Object[] { PathBuilder.from("/").path("/").build(), "/" },
				new Object[] { PathBuilder.from("/").path(null).path("/").build(), "/" },
				new Object[] { PathBuilder.from("/").path("/foo").build(), "/foo" },
				new Object[] { PathBuilder.from("/").path("/foo").path("/").build(), "/foo/" },
				new Object[] { PathBuilder.from("/").path("foo/").path("/").build(), "/foo/" }, 
				new Object[] { PathBuilder.from("/").path("/foo/").path("/").build(), "/foo/" }, 
				};
	}

	@Parameter(0)
	public String path;

	@Parameter(1)
	public String expectation;

	@Test
	public void shouldBuildPath() {
		assertThat(path).isEqualTo(expectation);
	}

}
