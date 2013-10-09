package org.jboss.tools.livereload.internal.server.jetty;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.jetty.websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveReloadTestClient implements WebSocket.OnTextMessage {

	private final static Logger LOGGER = LoggerFactory.getLogger(LiveReloadTestClient.class);

	private final Properties livereloadMessages;
	private Connection connection = null;
	private final String location;
	private int reloadNotificationsCounter = 0;
	private String receivedNotification;
	
	public LiveReloadTestClient(final String location) throws IOException {
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
		if(connection != null && connection.isOpen()) { 
			connection.close();
		}
	}

	@Override
	public void onOpen(final Connection connection) {
		LOGGER.debug("Opening connection");
		this.connection = connection;
		sendMessage(livereloadMessages.getProperty("hello_command"));
	}

	@Override
	public void onMessage(final String message) {
		LOGGER.debug("Received a message: {}", message);
		if(message.contains("\"command\":\"hello\"")) {
			final String urlCommand = livereloadMessages.getProperty("url_command").replace("{}", location);
			sendMessage(urlCommand);
		} else if(message.contains("\"command\":\"reload\"")) {
			LOGGER.info("*** 'reload' command received ***");
			this.reloadNotificationsCounter++;
			receivedNotification = message;
		}
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

	/**
	 * @return the reloadNotificationsCounter
	 */
	public int getNumberOfReloadNotifications() {
		return reloadNotificationsCounter;
	}

	public String getReceivedNotification() {
		return receivedNotification;
	}
}