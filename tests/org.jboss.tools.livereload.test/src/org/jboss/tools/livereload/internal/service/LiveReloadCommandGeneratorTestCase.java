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

package org.jboss.tools.livereload.internal.service;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.jboss.tools.livereload.core.internal.service.WorkspaceResourceChangedEvent;
import org.jboss.tools.livereload.core.internal.util.ReloadCommandGenerator;
import org.junit.Test;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadCommandGeneratorTestCase {

	@Test
	public void shouldGenerateCommandForHtmlFileChangeOnly() throws IOException, URISyntaxException {
		// pre-condition
		final IResource indexhtmlFile = mock(IResource.class, RETURNS_DEEP_STUBS);
		when(indexhtmlFile.getFileExtension()).thenReturn("html");
		when(indexhtmlFile.getLocation().toOSString()).thenReturn("file://index.html");
		WorkspaceResourceChangedEvent event = new WorkspaceResourceChangedEvent(Arrays.asList(indexhtmlFile));
		// operation
		final List<String> commands = ReloadCommandGenerator.generateReloadCommands(event.getChangedResources());
		// verification
		assertThat(commands).hasSize(1);
		assertThat(commands.get(0)).isEqualTo(
				"{\"command\":\"reload\",\"path\":\"file://index.html\",\"liveCSS\":true}");
	}

	@Test
	public void shouldGenerateCommandForHtmlFileAfterMultipleChanges() throws IOException, URISyntaxException {
		// pre-condition
		final IResource indexhtmlFile = mock(IResource.class, RETURNS_DEEP_STUBS);
		when(indexhtmlFile.getFileExtension()).thenReturn("html");
		when(indexhtmlFile.getLocation().toOSString()).thenReturn("file://index.html");
		final IResource cssFile = mock(IResource.class, RETURNS_DEEP_STUBS);
		when(cssFile.getFileExtension()).thenReturn("css");
		when(cssFile.getLocation().toOSString()).thenReturn("file://styles.css");
		WorkspaceResourceChangedEvent event = new WorkspaceResourceChangedEvent(Arrays.asList(indexhtmlFile, cssFile));
		// operation
		final List<String> commands = ReloadCommandGenerator.generateReloadCommands(event.getChangedResources());
		// verification
		assertThat(commands).hasSize(1);
		assertThat(commands.get(0)).isEqualTo(
				"{\"command\":\"reload\",\"path\":\"file://index.html\",\"liveCSS\":true}");
	}

	@Test
	public void shouldGenerateCommandForHtmlFileAfterOtherChanges() throws IOException, URISyntaxException {
		// pre-condition
		final IResource pngFile = mock(IResource.class, RETURNS_DEEP_STUBS);
		when(pngFile.getFileExtension()).thenReturn("png");
		when(pngFile.getLocation().toOSString()).thenReturn("file://image.png");
		final IResource jpgFile = mock(IResource.class, RETURNS_DEEP_STUBS);
		when(jpgFile.getFileExtension()).thenReturn("jpg");
		when(jpgFile.getLocation().toOSString()).thenReturn("file://image.jpg");
		WorkspaceResourceChangedEvent event = new WorkspaceResourceChangedEvent(Arrays.asList(pngFile, jpgFile));
		// operation
		final List<String> commands = ReloadCommandGenerator.generateReloadCommands(event.getChangedResources());
		// verification
		assertThat(commands).hasSize(2);
		assertThat(commands.get(0))
				.isEqualTo("{\"command\":\"reload\",\"path\":\"file://image.png\",\"liveCSS\":true}");
		assertThat(commands.get(1))
				.isEqualTo("{\"command\":\"reload\",\"path\":\"file://image.jpg\",\"liveCSS\":true}");
	}

	@Test
	public void shouldGenerateCommandForCssFileChangeOnly() throws IOException, URISyntaxException {
		// pre-condition
		final IResource cssFile1 = mock(IResource.class, RETURNS_DEEP_STUBS);
		when(cssFile1.getFileExtension()).thenReturn("css");
		when(cssFile1.getLocation().toOSString()).thenReturn("file://styles.css");
		final IResource cssFile2 = mock(IResource.class, RETURNS_DEEP_STUBS);
		when(cssFile2.getFileExtension()).thenReturn("css");
		when(cssFile2.getLocation().toOSString()).thenReturn("file://otherstyles.css");
		WorkspaceResourceChangedEvent event = new WorkspaceResourceChangedEvent(Arrays.asList(cssFile1, cssFile2));
		// operation
		final List<String> commands = ReloadCommandGenerator.generateReloadCommands(event.getChangedResources());
		// verification
		assertThat(commands).hasSize(2);
		assertThat(commands.get(0)).isEqualTo(
				"{\"command\":\"reload\",\"path\":\"file://styles.css\",\"liveCSS\":true}");
		assertThat(commands.get(1)).isEqualTo(
				"{\"command\":\"reload\",\"path\":\"file://otherstyles.css\",\"liveCSS\":true}");
	}

	@Test
	public void shouldGenerateCommandsForMultipleCssFileChanges() throws IOException, URISyntaxException {
		// pre-condition
		final IResource cssFile = mock(IResource.class, RETURNS_DEEP_STUBS);
		when(cssFile.getFileExtension()).thenReturn("css");
		when(cssFile.getLocation().toOSString()).thenReturn("file://styles.css");
		WorkspaceResourceChangedEvent event = new WorkspaceResourceChangedEvent(Arrays.asList(cssFile));
		// operation
		final List<String> commands = ReloadCommandGenerator.generateReloadCommands(event.getChangedResources());
		// verification
		assertThat(commands).hasSize(1);
		assertThat(commands.get(0)).isEqualTo(
				"{\"command\":\"reload\",\"path\":\"file://styles.css\",\"liveCSS\":true}");
	}

	@Test
	public void shouldGenerateCommandForJsFileOnly() throws IOException, URISyntaxException {
		// pre-condition
		final IResource jsFile = mock(IResource.class, RETURNS_DEEP_STUBS);
		when(jsFile.getFileExtension()).thenReturn("js");
		when(jsFile.getLocation().toOSString()).thenReturn("file://application.js");
		WorkspaceResourceChangedEvent event = new WorkspaceResourceChangedEvent(Arrays.asList(jsFile));
		// operation
		final List<String> commands = ReloadCommandGenerator.generateReloadCommands(event.getChangedResources());
		// verification
		assertThat(commands).hasSize(1);
		assertThat(commands.get(0)).isEqualTo(
				"{\"command\":\"reload\",\"path\":\"file://application.js\",\"liveCSS\":true}");
	}

	@Test
	public void shouldGenerateCommandForHtmlServerResourceAfterSingleChange() throws IOException, URISyntaxException {
		// pre-condition
		final String location = "http://localhost:8080/index.html";
		// operation
		final String command = ReloadCommandGenerator.generateReloadCommand(location);
		// verification
		assertThat(command).isEqualTo(
				"{\"command\":\"reload\",\"path\":\"" + location + "\",\"liveCSS\":false}");

	}

}
