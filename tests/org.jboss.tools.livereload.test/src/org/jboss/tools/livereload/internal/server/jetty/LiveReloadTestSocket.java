package org.jboss.tools.livereload.internal.server.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class LiveReloadTestSocket {

	private final static Logger LOGGER = LoggerFactory.getLogger(LiveReloadTestSocket.class);

	private final Properties livereloadMessages;
	private Session session = null;
	private final String location;
	private int reloadNotificationsCounter = 0;
	private String receivedNotification;
	
	private boolean handshakeComplete = false;
	
	public LiveReloadTestSocket(final String location) throws IOException {
		this.location = location;
		livereloadMessages = new Properties();
		final InputStream messagesStream = getClass().getResourceAsStream("messages.properties");
		livereloadMessages.load(messagesStream);
	}
 
	@OnWebSocketClose
	public void onClose(int statusCode, final String reason) {
		LOGGER.debug("Closing connection with status=" + statusCode + " / reason=" + reason);
		if(session != null && session.isOpen()) { 
			session.close();
		}
	}

	@OnWebSocketConnect
    public void onConnect(final Session session) {
    	LOGGER.debug("Opening connection");
		this.session = session;
		sendMessage(livereloadMessages.getProperty("hello_command"));
	}

	@OnWebSocketMessage
	public void onMessage(final String message) {
		LOGGER.debug("Received a message: {}", message);
		if(message.contains("\"command\":\"hello\"")) {
			final String urlCommand = livereloadMessages.getProperty("url_command").replace("{}", location);
			sendMessage(urlCommand);
			this.handshakeComplete = true;
		} else if(message.contains("\"command\":\"reload\"")) {
			LOGGER.info("*** 'reload' command received ***");
			this.reloadNotificationsCounter++;
			receivedNotification = message;
		}
	}

	private void sendMessage(final String message) {
		if (session != null) {
			try {
				LOGGER.debug("Sending message {}", message);
				session.getRemote().sendString(message);
			} catch (IOException e) {
				fail("Failed to send message to server: " + e.getMessage());
			}
		}
	}

	public boolean isHandshakeComplete() {
		return handshakeComplete;
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