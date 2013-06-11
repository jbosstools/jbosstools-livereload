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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Generates the 'reload' command that should be sent to the browsers
 * 
 * @author xcoulon
 * 
 */
public class ReloadCommandGenerator {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Generates the LiveReload command(s) for the given files.
	 * 
	 * @param files
	 * @return the Livereload command(s)
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public static String generateReloadCommand(final String location) throws IOException, URISyntaxException {
		return buildRefreshCommand(location, false);
	}

	/**
	 * Generates the LiveReload command(s) for the given files.
	 * 
	 * @param files
	 * @return the Livereload command(s)
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public static List<String> generateReloadCommands(final List<IResource> files) throws IOException, URISyntaxException {
		final Map<String, List<IResource>> dispatchedFiles = dispatch(files);
		final List<String> commands = new ArrayList<String>();
		// if HTML files changed, only generate command for those ones
		if (dispatchedFiles.containsKey("html")) {
			for (IResource file : dispatchedFiles.get("html")) {
				commands.add(buildRefreshCommand(file.getLocation().toOSString(), true));
			}
			return commands;
		}
		// generate command for all changes files if those are only css files
		for (Entry<String, List<IResource>> entry : dispatchedFiles.entrySet()) {
			for (IResource file : entry.getValue()) {
				commands.add(buildRefreshCommand(file.getLocation().toOSString(), true));
			}
		}
		return commands;
	}

	/**
	 * Dispatch the files by their extension
	 * 
	 * @param files
	 * @return a map where files are indexed by their extension (lowercased)
	 */
	private static Map<String, List<IResource>> dispatch(List<IResource> files) {
		final Map<String, List<IResource>> dispatchedFiles = new LinkedHashMap<String, List<IResource>>();
		for (IResource file : files) {
			final String fileExtension = file.getFileExtension() != null ? file.getFileExtension().toLowerCase() : null;
			// skip files without extension
			if (fileExtension == null) {
				continue;
			}
			if (dispatchedFiles.containsKey(fileExtension)) {
				dispatchedFiles.get(fileExtension).add(file);
			} else {
				dispatchedFiles.put(fileExtension, new ArrayList<IResource>(Arrays.asList(file)));
			}
		}
		return dispatchedFiles;
	}

	private static String buildRefreshCommand(final String location, final boolean liveCSS) throws IOException, URISyntaxException {
		final Map<String, Object> reloadArgs = new LinkedHashMap<String, Object>();
		reloadArgs.put("command", "reload");
		reloadArgs.put("path", location);
		reloadArgs.put("liveCSS", liveCSS);
		final StringWriter commandWriter = new StringWriter();
		objectMapper.writeValue(commandWriter, reloadArgs);
		return commandWriter.toString();
	}

}
