package org.jboss.tools.livereload.core.internal.server.jetty;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EventObject;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.core.internal.service.EventService;
import org.jboss.tools.livereload.core.internal.service.LiveReloadClientConnectedEvent;
import org.jboss.tools.livereload.core.internal.service.LiveReloadClientDisconnectedEvent;
import org.jboss.tools.livereload.core.internal.service.ServerResourcePublishedEvent;
import org.jboss.tools.livereload.core.internal.service.ServerResourcePublishedFilter;
import org.jboss.tools.livereload.core.internal.service.Subscriber;
import org.jboss.tools.livereload.core.internal.service.WorkspaceResourceChangedEvent;
import org.jboss.tools.livereload.core.internal.service.WorkspaceResourceChangedEventFallbackFilter;
import org.jboss.tools.livereload.core.internal.service.WorkspaceResourceChangedEventFilter;
import org.jboss.tools.livereload.core.internal.util.Logger;
import org.jboss.tools.livereload.core.internal.util.ProjectUtils;
import org.jboss.tools.livereload.core.internal.util.ReloadCommandGenerator;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Wrapper around a WebSocket connection established with a Browser that wants
 * to talk about LiveReload ;-) Also, a subscriber to workspace resource change events and server publish events
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadWebSocket implements WebSocket.OnTextMessage, Subscriber {

	private static final String helloServerToClientHandShakeMessage = "{\"command\":\"hello\",\"protocols\":[\"http://livereload.com/protocols/official-7\"]}";

	private static final ObjectMapper mapper = new ObjectMapper();

	private static EventService eventService = EventService.getInstance();

	/** The client id (based on its user-agent header and IP Address).*/
	private final String clientId;

	/** The client address. */
	@SuppressWarnings("unused")
	private final String clientAddress;
	
	/** The underlying jetty/websocket connection. */
	private Connection connection;
	
	/** The location that the browser sends in an 'info' message to the server. */
	private String browserLocation = null;

	/**
	 * Indicates if the incoming events should be notified to the browser in
	 * fallback mode, ie, just sending a 'reload' command with the given
	 * browserlocation.
	 */
	private boolean fallbackMode = false;
	
	/** Unique ID to identify the server-side Websocket in logs.*/
	private final UUID id;

	/**
	 * Constructor
	 * @param userAgent the browser's user-agent sent in the HTTP request header
	 * @param clientAddress the browser's IP Address
	 */
	public LiveReloadWebSocket(final String userAgent, final String clientAddress) {
		this.id = UUID.randomUUID();
		this.clientId = new StringBuilder((userAgent != null) ? userAgent : "unknown User-Agent").append(" at ")
				.append((clientAddress != null) ? clientAddress : "unknown IP Address").toString();
		this.clientAddress = clientAddress;
	}

	@Override
	public void onOpen(Connection connection) {
		Logger.debug("Opening connection {} -> {}", id, getId());
		this.connection = connection;
	}

	@Override
	public void onClose(int closeCode, String message) {
		Logger.debug("LiveReload client connection closed (" + id + ") with code " + closeCode + " and message "
				+ message);
		eventService.unsubscribe(this);
		if (connection != null) {
			eventService.publish(new LiveReloadClientDisconnectedEvent(connection));
		}

	}

	public void sendMessage(String data) throws IOException {
		if (!connection.isOpen()) {
			Logger.debug("Removing pending closed connection {}", getId());
			eventService.unsubscribe(this);
			return;
		}
		Logger.debug("Sending message from websocket#{}: '{}'", id, data);
		connection.sendMessage(data);
	}

	@Override
	public void onMessage(String data) {
		Logger.debug("Received message on socket #{}: '{}'", id, data);
		try {
			final JsonNode rootNode = mapper.readTree(data);
			final String commandValue = rootNode.path("command").asText();
			final String HELLO_COMMAND = "hello";
			final String INFO_COMMAND = "info";
			final String URL_COMMAND = "url";
			//final String HELLO_PROTOCOLS = "http://livereload.com/protocols/connection-check-1";
			if (HELLO_COMMAND.equals(commandValue)) {
				//final String protocols = rootNode.path("protocols").get(0).asText();
				sendMessage(helloServerToClientHandShakeMessage);
			} else if(INFO_COMMAND.equals(commandValue) || URL_COMMAND.equals(commandValue)) {
				// TODO: process info message to know if this socket is concerned
				// with some changes
				//eg: '{"command":"info","plugins":{"less":{"disable":false,"version":"1.0"}},"url":"file:///Users/xcoulon/git/html5eap6/src/main/webapp/index.html"}'
				this.browserLocation = rootNode.path("url").asText();
				this.fallbackMode = false;
				if(browserLocation == null) {
					return;
				}
				if(browserLocation.startsWith("file:///")) {
					final IProject project = ProjectUtils.extractProject(browserLocation);
					eventService.subscribe(this, new WorkspaceResourceChangedEventFilter(project));
					eventService.publish(new LiveReloadClientConnectedEvent(project));
				} 
				// register with a ServerResourcePublishedFilter unless the target server is the LiveReload server,
				// in which case, the 
				else if(browserLocation.startsWith("http://")) {
					final IServer server = WSTUtils.extractServer(browserLocation);
					if(server != null && WSTUtils.isLiveReloadServer(server)) {
						final IProject project = ProjectUtils.findProjectFromResourceLocation(new Path(new URL(browserLocation).getFile()));
						eventService.subscribe(this, new WorkspaceResourceChangedEventFilter(project));
						eventService.publish(new LiveReloadClientConnectedEvent(project));
					} else if(server != null) {
						eventService.subscribe(this, new ServerResourcePublishedFilter(server));
						eventService.publish(new LiveReloadClientConnectedEvent(server));
					} else {
						this.fallbackMode = true;
						eventService.subscribe(this, new WorkspaceResourceChangedEventFallbackFilter());
						eventService.publish(new LiveReloadClientConnectedEvent(browserLocation));
						Logger.info("Falling back to file changes notification for browser location: "
								+ browserLocation);
					
					}
				}
				// close connection from this client
				else {
					connection.close();
				}
				
			}
		} catch (IOException e) {
			Logger.error("Failed to reply to LivreReload client hand-shake", e);
		}
	}

	@Override
	public void inform(EventObject e) {
		try {
			if (e instanceof WorkspaceResourceChangedEvent) {
				if (fallbackMode) {
					final String command = ReloadCommandGenerator.generateReloadCommand(browserLocation);
					sendMessage(command);
				} else {
					WorkspaceResourceChangedEvent event = (WorkspaceResourceChangedEvent) e;
					List<String> commands = ReloadCommandGenerator.generateReloadCommands(event.getChangedResources());
					for (String command : commands) {
						sendMessage(command);
					}
				}
			} else if (e instanceof ServerResourcePublishedEvent) {
				final String command = ReloadCommandGenerator.generateReloadCommand(browserLocation);
				sendMessage(command);
			} else {
				Logger.debug("Ignoring event " + e);
			}
		} catch (URISyntaxException ex) {
			Logger.error("Failed to generate reload command to send to browser", ex);
		} catch (IOException ex) {
			Logger.error("Failed to send reload command to browser from socket #" + id, ex);
		}
	}

	/**
	 * Called when by the {@link LiveReloadWebSocketServlet} when this later is destroyed. 
	 * This is the moment when this websocket should close the connection and unsubscribe to the events.
	 */
	public void destroy() {
		if(connection != null && connection.isOpen()) {
			connection.close();
		}
		eventService.unsubscribe(this);
		
	}
	@Override
	public String getId() {
		return clientId + " at " + browserLocation;
	}

}