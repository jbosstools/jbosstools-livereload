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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.wst.server.core.IServer;
import org.fest.assertions.Assertions;
import org.jboss.tools.livereload.core.internal.service.EventService;
import org.jboss.tools.livereload.core.internal.service.ServerResourcePublishedEvent;
import org.jboss.tools.livereload.core.internal.service.ServerResourcePublishedFilter;
import org.jboss.tools.livereload.core.internal.service.Subscriber;
import org.jboss.tools.livereload.core.internal.service.WorkspaceResourceChangedEvent;
import org.jboss.tools.livereload.core.internal.service.WorkspaceResourceChangedEventFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xcoulon
 * 
 */
public class EventServiceTestCase {

	private EventService eventService = EventService.getInstance();

	private Subscriber localChangesSubscriber1 = null;
	
	private IProject project1 = mock(IProject.class);

	private Subscriber localChangesSubscriber2 = null;

	private IProject project2 = mock(IProject.class);

	private IServer server1 = mock(IServer.class);

	private Subscriber serverChangesSubscriber1 = null;

	private Subscriber serverChangesSubscriber2 = null;
	
	private IServer server2 = mock(IServer.class);

	@Before
	public void setup() {
		localChangesSubscriber1 = mock(Subscriber.class);
		eventService.subscribe(localChangesSubscriber1, new WorkspaceResourceChangedEventFilter(project1));
		localChangesSubscriber2 = mock(Subscriber.class);
		eventService.subscribe(localChangesSubscriber2, new WorkspaceResourceChangedEventFilter(project2));
		serverChangesSubscriber1 = mock(Subscriber.class);
		eventService.subscribe(serverChangesSubscriber1, new ServerResourcePublishedFilter(server1));
		serverChangesSubscriber2 = mock(Subscriber.class);
		eventService.subscribe(serverChangesSubscriber2, new ServerResourcePublishedFilter(server2));
	}

	@After
	public void tearDown() {
		eventService.resetSubscribers();
	}

	@Test
	public void shouldNotifyLocalSubscribersAfterSingleRelevantFileChanged() {
		// pre-condition
		final IResource indexhtmlFile = mock(IResource.class);
		when(indexhtmlFile.getFileExtension()).thenReturn("html");
		when(indexhtmlFile.getProject()).thenReturn(project1);
		// operation
		final WorkspaceResourceChangedEvent event = new WorkspaceResourceChangedEvent(Arrays.asList(indexhtmlFile));
		eventService.publish(event);
		// validation
		verify(localChangesSubscriber1).inform(event);
		verify(localChangesSubscriber2, never()).inform(event);
		verify(serverChangesSubscriber1, never()).inform(event);
		verify(serverChangesSubscriber2, never()).inform(event);
	}

	@Test
	public void shouldNotifyLocalSubscribersAfterSingleRelevantFileWithUppercaseExtensionChanged() {
		// pre-condition
		final IResource indexhtmlFile = mock(IResource.class);
		when(indexhtmlFile.getFileExtension()).thenReturn("HTML");
		when(indexhtmlFile.getProject()).thenReturn(project1);
		// operation
		final WorkspaceResourceChangedEvent event = new WorkspaceResourceChangedEvent(Arrays.asList(indexhtmlFile));
		eventService.publish(event);
		// validation
		verify(localChangesSubscriber1).inform(event);
		verify(localChangesSubscriber2, never()).inform(event);
		verify(serverChangesSubscriber1, never()).inform(event);
		verify(serverChangesSubscriber2, never()).inform(event);
	}

	@Test
	public void shouldNotNotifyLocalSubscribersAfterSingleIrrelevantFileChange() {
		// pre-condition
		final IResource pomxmlFile = mock(IResource.class);
		when(pomxmlFile.getProject()).thenReturn(project1);
		when(pomxmlFile.getFileExtension()).thenReturn("xml");
		// operation
		final WorkspaceResourceChangedEvent event = new WorkspaceResourceChangedEvent(Arrays.asList(pomxmlFile));
		eventService.publish(event);
		// validation
		verify(localChangesSubscriber1, never()).inform(event);
		verify(localChangesSubscriber2, never()).inform(event);
		verify(serverChangesSubscriber1, never()).inform(event);
		verify(serverChangesSubscriber2, never()).inform(event);
	}

	@Test
	public void shouldNotifyLocalSubscribersAfterMultipleRelevantFileChanged() {
		// pre-condition
		final IResource indexhtmlFile = mock(IResource.class);
		when(indexhtmlFile.getFileExtension()).thenReturn("html");
		when(indexhtmlFile.getProject()).thenReturn(project1);
		final IResource pomxmlFile = mock(IResource.class);
		when(pomxmlFile.getFileExtension()).thenReturn("xml");
		when(pomxmlFile.getProject()).thenReturn(project1);
		// operation
		final WorkspaceResourceChangedEvent event = new WorkspaceResourceChangedEvent(Arrays.asList(indexhtmlFile,
				pomxmlFile));
		eventService.publish(event);
		// validation
		verify(localChangesSubscriber1).inform(event);
		verify(localChangesSubscriber2, never()).inform(event);
		verify(serverChangesSubscriber1, never()).inform(event);
		verify(serverChangesSubscriber2, never()).inform(event);
	}

	@Test
	public void shouldNotNotifyLocalSubscribersAfterMultipleIrrelevantFileChanged() {
		// pre-condition
		final IResource pomxmlFile = mock(IResource.class);
		when(pomxmlFile.getFileExtension()).thenReturn("xml");
		when(pomxmlFile.getProject()).thenReturn(project1);
		final IResource metadataxmlFile = mock(IResource.class);
		when(metadataxmlFile.getFileExtension()).thenReturn("xml");
		when(metadataxmlFile.getProject()).thenReturn(project1);
		// operation
		final WorkspaceResourceChangedEvent event = new WorkspaceResourceChangedEvent(Arrays.asList(pomxmlFile,
				metadataxmlFile));
		eventService.publish(event);
		// validation
		verify(localChangesSubscriber1, never()).inform(event);
		verify(localChangesSubscriber2, never()).inform(event);
		verify(serverChangesSubscriber1, never()).inform(event);
		verify(serverChangesSubscriber2, never()).inform(event);
	}

	@Test
	public void shouldNotifySubscriberAfterServerResourcePublished() {
		// pre-condition
		final IResource indexhtmlFile = mock(IResource.class);
		when(indexhtmlFile.getFileExtension()).thenReturn("html");
		// operation
		final ServerResourcePublishedEvent event = new ServerResourcePublishedEvent(server1);
		eventService.publish(event);
		// validation
		verify(localChangesSubscriber1, never()).inform(event);
		verify(localChangesSubscriber2, never()).inform(event);
		verify(serverChangesSubscriber1).inform(event);
		verify(serverChangesSubscriber2, never()).inform(event);
	}

}
