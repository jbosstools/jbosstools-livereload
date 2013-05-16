package org.jboss.tools.livereload.internal.server.jetty;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.livereload.internal.service.EventService;
import org.jboss.tools.livereload.internal.service.ServerResourcePublishedEvent;
import org.jboss.tools.livereload.internal.service.ServerResourcePublishedFilter;
import org.jboss.tools.livereload.internal.service.Subscriber;
import org.jboss.tools.livereload.internal.service.WorkspaceResourceChangedEvent;
import org.jboss.tools.livereload.internal.service.WorkspaceResourceChangedEventFilter;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.ProjectUtils;
import org.jboss.tools.livereload.internal.util.WSTUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Wrapper around a WebSocket connection established with a Browser that wants
 * to talk about LiveReload ;-) Also, a subscriber to resource change events
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadWebSocket implements WebSocket.OnTextMessage, Subscriber {

	private static final String helloServerToClientHandShakeMessage = "{\"command\":\"hello\",\"protocols\":[\"http://livereload.com/protocols/official-7\"]}";

	private static final ObjectMapper mapper = new ObjectMapper();

	private final String id;

	private Connection connection;
	
	private String browserLocation = null;

	public LiveReloadWebSocket(final String userAgent, final String address) {
		this.id = new StringBuilder((userAgent != null) ? userAgent : "unknown User-Agent").append(" at ")
				.append((address != null) ? address : "unknown IP Address").toString();
	}

	@Override
	public void onOpen(Connection connection) {
		Logger.debug("Opening connection {}", getId());
		this.connection = connection;
	}

	@Override
	public void onClose(int closeCode, String message) {
		EventService.getInstance().unsubscribe(this);
	}

	public void sendMessage(String data) throws IOException {
		if(!connection.isOpen()) {
			Logger.debug("Removing pending closed connection {}", getId());
			EventService.getInstance().unsubscribe(this);
			return;
		} 
		Logger.debug("Sending message '{}'", data);
		connection.sendMessage(data);
	}

	@Override
	public void onMessage(String data) {
		Logger.debug("Received message '{}'", data);
		try {
			JsonNode rootNode = mapper.readTree(data);
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
				browserLocation = rootNode.path("url").asText();
				if(browserLocation == null) {
					return;
				}
				if(browserLocation.startsWith("file:///")) {
					final IProject project = ProjectUtils.extractProject(browserLocation);
					EventService.getInstance().subscribe(this, new WorkspaceResourceChangedEventFilter(project));
				} 
				//TODO: see what's needed to support https://
				else if(browserLocation.startsWith("http://")) {
					final IServer server = WSTUtils.extractServer(browserLocation);
					EventService.getInstance().subscribe(this, new ServerResourcePublishedFilter(server));
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
				WorkspaceResourceChangedEvent event = (WorkspaceResourceChangedEvent) e;
				List<String> commands = ReloadCommandGenerator.generateReloadCommands(event.getChangedResources());
				for(String command : commands) {
					sendMessage(command);
				}
			} else if(e instanceof ServerResourcePublishedEvent) {
				final String command = ReloadCommandGenerator.generateReloadCommand(browserLocation);
				sendMessage(command);
			} else {
				Logger.warn("Ignoring event " + e);
			}
		} catch (Exception ex) {
			Logger.error("Failed to send reload command to browser", ex);
		}
	}

	private static final ObjectMapper objectMapper = new ObjectMapper();

	String buildRefreshCommand(final IPath path) throws JsonGenerationException, JsonMappingException, IOException,
			URISyntaxException {
		final Map<String, Object> reloadArgs = new LinkedHashMap<String, Object>();
		reloadArgs.put("command", "reload");
		reloadArgs.put("path", path.toOSString());
		reloadArgs.put("liveCSS", true);
		final StringWriter commandWriter = new StringWriter();
		objectMapper.writeValue(commandWriter, reloadArgs);
		return commandWriter.toString();
	}

	@Override
	public String getId() {
		return id + " at " + browserLocation;
	}

}