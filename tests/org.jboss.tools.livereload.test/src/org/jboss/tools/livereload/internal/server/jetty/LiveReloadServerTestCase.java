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

package org.jboss.tools.livereload.internal.server.jetty;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IResource;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.eclipse.wst.server.core.util.SocketUtil;
import org.jboss.tools.livereload.internal.AbstractCommonTestCase;
import org.jboss.tools.livereload.internal.service.EventService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadServerTestCase extends AbstractCommonTestCase {

	private final static Logger LOGGER = LoggerFactory.getLogger(LiveReloadServerTestCase.class);
	
	private LiveReloadServer liveReloadServer = null;
	private int websocketPort = -1;

	@Before
	public void resetEventService() {
		EventService.getInstance().resetSubscribers();
	}

	@Before
	public void startServer() throws IOException {
		websocketPort = SocketUtil.findUnusedPort(50000, 55000);
		liveReloadServer = new LiveReloadServer(websocketPort);
		liveReloadServer.start();
	}

	@After
	public void stopServer() {
		liveReloadServer.stop();
	}

	@Test
	public void shouldAcceptClientConnexion() throws Exception {
		// pre-condition
		final WebSocketClientFactory webSocketClientFactory = new WebSocketClientFactory();
		webSocketClientFactory.setBufferSize(4096);
		webSocketClientFactory.start();
		WebSocketClient webSocketClient = new WebSocketClient(webSocketClientFactory);
		final IResource index_html_file = project.findMember("src" + File.separator + "main" + File.separator + "webapp" + File.separator + "index.html");
		final String location = "file://" + index_html_file.getLocation().toOSString();
		// operation
		final LiveReloadClient client = new LiveReloadClient(location);
		final Future<Connection> future = webSocketClient.open(new URI("ws://localhost:" + websocketPort
				+ "/livereload"), client);
		final Connection connection = future.get(2, TimeUnit.SECONDS);
		Thread.sleep(2000);
		// verification
		assertThat(connection.isOpen()).isTrue();
		assertThat(EventService.getInstance().getSubscribers()).hasSize(1);

	}

	public static class LiveReloadClient implements WebSocket.OnTextMessage {

		private final Properties livereloadMessages;
		private Connection connection = null;
		private final String location;
		
		public LiveReloadClient(final String location) throws IOException {
			this.location = location;
			livereloadMessages = new Properties();
			livereloadMessages.load(Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							LiveReloadServerTestCase.class.getPackage().getName().replaceAll("\\.", File.separator)
									+ File.separator + "messages.properties"));
		}

		@Override
		public void onClose(int arg0, String arg1) {
			LOGGER.debug("Closing connection");
			connection.close();
		}

		@Override
		public void onOpen(Connection connection) {
			LOGGER.debug("Opening connection");
			this.connection = connection;
			sendMessage(livereloadMessages.getProperty("hello_command"));
		}

		@Override
		public void onMessage(String message) {
			final String urlCommand = livereloadMessages.getProperty("url_command").replace("{}", location);
			sendMessage(urlCommand);
		}

		public void sendMessage(final String message) {
			if (connection != null) {
				try {
					LOGGER.debug("Sending message {}", message);
					connection.sendMessage(message);
				} catch (IOException e) {
					fail("Failed to send message to server: " + e.getMessage());
				}
			}
		}
	}

}
