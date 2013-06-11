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
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.util.SocketUtil;
import org.jboss.tools.livereload.internal.AbstractCommonTestCase;
import org.jboss.tools.livereload.internal.WorkbenchUtils;
import org.jboss.tools.livereload.internal.server.wst.LiveReloadServerBehaviour;
import org.jboss.tools.livereload.internal.service.EventService;
import org.jboss.tools.livereload.internal.service.ServerLifeCycleListener;
import org.jboss.tools.livereload.internal.util.TimeoutUtils;
import org.jboss.tools.livereload.internal.util.TimeoutUtils.TaskMonitor;
import org.jboss.tools.livereload.internal.util.WSTTestUtils;
import org.jboss.tools.livereload.internal.util.WSTUtils;
import org.jboss.tools.livereload.test.previewserver.PreviewServerBehaviour;
import org.jboss.tools.livereload.test.previewserver.PreviewServerFactory;
import org.jboss.tools.livereload.test.previewserver.ServerStarterUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xcoulon
 * 
 */
@SuppressWarnings("restriction")
public class LiveReloadServerTestCase extends AbstractCommonTestCase {

	/** LiveReload server to test. */
	private IServer liveReloadServer = null;

	/** Optional HTTP Preview Server. */
	private IServer httpPreviewServer = null;

	private int liveReloadServerPort = -1;

	/** File location prefixed with file:// scheme. */
	private String indexFileLocation;

	/** File location prefixed with http:// scheme. */
	private String indexDocumentlocation;

	private String unknowDocumentLocation;

	private String cssDocumentLocation;

	private String folderDocumentLocation;

	private LiveReloadServerBehaviour liveReloadServerBehaviour;

	@Before
	public void setup() throws IOException, CoreException {
		// remove all servers
		for (final IServer server : ServerCore.getServers()) {
			server.stop(true);
			TaskMonitor monitor = new TaskMonitor() {
				@Override
				public boolean isComplete() {
					return !(server.canStop().isOK());
				}
			};
			TimeoutUtils.timeout(monitor, 2, TimeUnit.SECONDS);
			server.delete();
		}
		//
		EventService.getInstance().resetSubscribers();
		liveReloadServerPort = SocketUtil.findUnusedPort(50000, 55000);
		final IResource index_html_file = project.findMember("WebContent" + File.separator + "index.html");
		indexFileLocation = "file://" + index_html_file.getLocation().toOSString();
		indexDocumentlocation = "http://localhost:" + liveReloadServerPort + "/" + project.getName() + "/"
				+ index_html_file.getLocation().makeRelativeTo(project.getLocation()).toOSString();
		unknowDocumentLocation = indexDocumentlocation.replace("index.html", "unknown.html");
		cssDocumentLocation = indexDocumentlocation.replace("index.html", "/styles.css");
		folderDocumentLocation = indexDocumentlocation.replace("index.html", "");
	}

	@After
	public void destroyServer() throws CoreException {
		if (liveReloadServer != null) {
			liveReloadServer.delete();
		}
	}

	/**
	 * @throws CoreException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * 
	 */
	private void createAndLaunchLiveReloadServer(final boolean enableProxyServer, final boolean injectScript)
			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		final String serverId = WSTTestUtils.createLiveReloadServer(liveReloadServerPort, enableProxyServer,
				injectScript);
		liveReloadServerBehaviour = (LiveReloadServerBehaviour) WSTUtils.findServerBehaviour(serverId);
		assertThat(liveReloadServerBehaviour).isNotNull();
		liveReloadServer = liveReloadServerBehaviour.getServer();
		assertThat(liveReloadServer).isNotNull();
		assertThat(liveReloadServer.canStart(ILaunchManager.RUN_MODE).isOK()).isTrue();

		assertThat(SocketUtil.isPortInUse(liveReloadServerPort)).isFalse();
		ServerStarterUtil.startServer(liveReloadServer, 5, TimeUnit.SECONDS);
	}

	/**
	 * Creates an HTTP Preview Server but does not start it.
	 * 
	 * @return
	 * 
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	private int createHttpPreviewServer() throws InterruptedException, CoreException {
		this.httpPreviewServer = PreviewServerFactory.createServer(project);
		return httpPreviewServer.getAttribute(PreviewServerBehaviour.PORT, -1);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private WebSocketClient createWebSocketClient() throws Exception {
		final WebSocketClientFactory webSocketClientFactory = new WebSocketClientFactory();
		webSocketClientFactory.setBufferSize(4096);
		webSocketClientFactory.start();
		WebSocketClient webSocketClient = new WebSocketClient(webSocketClientFactory);
		return webSocketClient;
	}

	/**
	 * @param client
	 * @return
	 * @throws Exception
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private Connection connectFrom(final LiveReloadTestClient client) throws Exception, IOException,
			URISyntaxException, InterruptedException, ExecutionException, TimeoutException {
		final WebSocketClient webSocketClient = createWebSocketClient();
		final Future<Connection> future = webSocketClient.open(new URI("ws://localhost:" + liveReloadServerPort
				+ "/livereload"), client);
		// final Connection connection = future.get(5, TimeUnit.SECONDS);
		final Connection connection = future.get();
		Thread.sleep(200);
		return connection;
	}

	@After
	public void stopServers() throws CoreException {
		if (liveReloadServerBehaviour != null) {
			liveReloadServerBehaviour.stop(true);
		}
		if (liveReloadServer != null) {
			liveReloadServer.delete();
		}
		if (httpPreviewServer != null) {
			httpPreviewServer.stop(true);
			httpPreviewServer.delete();
		}
	}

	@Test
	public void shouldAcceptWebsocketConnexionWithoutProxy() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(false, false);
		final LiveReloadTestClient client = new LiveReloadTestClient(indexFileLocation);
		// operation
		final Connection connection = connectFrom(client);
		// verification
		assertThat(connection.isOpen()).isTrue();
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
		connection.close();
	}

	@Test
	public void shouldNotAcceptWebsocketConnexionWithoutValidUrlInfo() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(false, false);
		final LiveReloadTestClient client = new LiveReloadTestClient("");
		// operation
		final Connection connection = connectFrom(client);
		// verification
		assertThat(connection.isOpen()).isFalse();
		// assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(0);
		connection.close();
	}

	@Test
	public void shouldAcceptWebsocketConnexionWithProxy() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true, false);
		final LiveReloadTestClient client = new LiveReloadTestClient(indexFileLocation);
		// operation
		final Connection connection = connectFrom(client);
		// verification
		assertThat(connection.isOpen()).isTrue();
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
		connection.close();
	}

	@Test
	public void shouldNotAcceptHttpConnexion() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(false, false);
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(indexDocumentlocation);
		client.executeMethod(method);
		// verification: should have time'd out
	}

	@Test
	public void shouldAcceptHttpConnexionAndReturnHtmlResource() throws Exception {
		createAndLaunchLiveReloadServer(true, true);
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(indexDocumentlocation);
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
	}

	@Test
	public void shouldAcceptHttpConnexionAndReturnNotFoundResource() throws Exception {
		createAndLaunchLiveReloadServer(true, true);
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(unknowDocumentLocation);
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	public void shouldAcceptHttpConnexionAndReturnForbiddenResponseWhenRequestingFolder() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true, true);
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(folderDocumentLocation);
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_FORBIDDEN);
	}

	@Test
	public void shouldNotInjectLiveReloadScriptInHtmlPage() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true, false);
		final String scriptContent = new StringBuilder(
				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
				.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(indexDocumentlocation);
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = new String(method.getResponseBody());
		assertThat(responseBody).doesNotContain(scriptContent);

	}

	@Test
	public void shouldInjectLiveReloadScriptInHtmlPage() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true, true);
		final String scriptContent = new StringBuilder(
				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
				.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(indexDocumentlocation);
		method.addRequestHeader("Accept", "text/html");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = new String(method.getResponseBody());
		assertThat(responseBody).contains(scriptContent);
	}

	@Test
	public void shouldGetLiveReloadScriptWithProxyDisabled() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(false, false);
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod("http://localhost:" + liveReloadServerPort + "/livereload.js");
		method.addRequestHeader("Accept", "text/javascript");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = new String(method.getResponseBody());
		assertThat(responseBody).isNotEmpty();
	}

	@Test
	public void shouldGetLiveReloadScriptWithProxyEnabled() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true, true);
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod("http://localhost:" + liveReloadServerPort + "/livereload.js");
		method.addRequestHeader("Accept", "text/javascript");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = new String(method.getResponseBody());
		assertThat(responseBody).isNotEmpty();
	}

	@Test
	public void shouldNotInjectLiveReloadScriptInCssPage() throws Exception {
		createAndLaunchLiveReloadServer(true, false);
		final String scriptContent = new StringBuilder(
				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
				.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(cssDocumentLocation);
		method.addRequestHeader("Accept", "text/css");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = new String(method.getResponseBody());
		assertThat(responseBody).doesNotContain(scriptContent);
	}

	@Test
	public void shouldBeNotifiedWhenLocalFileChangedWithProxyEnabled() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true, true);
		final LiveReloadTestClient client = new LiveReloadTestClient(indexDocumentlocation);
		// operation
		final Connection connection = connectFrom(client);
		// operation : trigger a resource changed event
		WorkbenchUtils.replaceAllOccurrencesOfCode("WebContent/index.html", project, "Hello, World",
				"Hello, LiveReload !");
		Thread.sleep(200);
		// verification: client should have been notified with a reload message
		assertThat(client.isNotificationReceived()).isEqualTo(true);
		// end
		connection.close();
	}

	@Test
	public void shouldBeNotifiedWhenLocalFileChangedWithProxyDisabled() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true, true);
		final LiveReloadTestClient client = new LiveReloadTestClient(indexFileLocation);
		// operation
		final Connection connection = connectFrom(client);
		// operation : trigger a resource changed event
		WorkbenchUtils.replaceAllOccurrencesOfCode("WebContent/index.html", project, "Hello, World",
				"Hello, LiveReload !");
		Thread.sleep(200);
		// verification: client should have been notified with a reload message
		assertThat(client.isNotificationReceived()).isEqualTo(true);
		assertThat(client.getReceivedNotification()).contains(
				"\"path\":\"" + indexFileLocation.substring("file://".length()) + "\"");
		// end
		connection.close();
	}

	@Test
	public void shouldBeNotifiedWhenRemoteResourceDeployedWithProxyDisabled() throws Exception {
		// pre-condition
		final int httpPreviewPort = createHttpPreviewServer();
		createAndLaunchLiveReloadServer(false, false);
		final String indexRemoteDocumentlocation = "http://localhost:" + httpPreviewPort + "/" + project.getName()
				+ "/index.html";
		final LiveReloadTestClient client = new LiveReloadTestClient(indexRemoteDocumentlocation);
		// operation: client-server handshake
		final Connection connection = connectFrom(client);
		// operation: simulate HTTP preview server startup: notify listeners
		((Server) httpPreviewServer).setServerState(IServer.STATE_STARTED);
		((Server) httpPreviewServer).publish(IServer.PUBLISH_AUTO, new NullProgressMonitor());
		Thread.sleep(200);
		// verification: client should have been notified with a reload message
		assertThat(client.isNotificationReceived()).isEqualTo(true);
		// end
		connection.close();
	}

	@Test
	public void shouldBeNotifiedWhenRemoteResourceDeployedWithProxyEnabledButNotUsed() throws Exception {
		// pre-condition
		final int httpPreviewPort = createHttpPreviewServer();
		createAndLaunchLiveReloadServer(true, true);
		final String indexRemoteDocumentlocation = "http://localhost:" + httpPreviewPort + "/" + project.getName()
				+ "/index.html";
		final LiveReloadTestClient client = new LiveReloadTestClient(indexRemoteDocumentlocation);
		// operation
		final Connection connection = connectFrom(client);
		// operation: simulate HTTP preview server startup: notify listeners
		((Server) httpPreviewServer).setServerState(IServer.STATE_STARTED);
		((Server) httpPreviewServer).publish(IServer.PUBLISH_AUTO, new NullProgressMonitor());
		Thread.sleep(200);
		// verification: client should have been notified with a reload message
		assertThat(client.isNotificationReceived()).isEqualTo(true);
		assertThat(client.getReceivedNotification()).contains("http://localhost:" + httpPreviewPort);
		// end
		connection.close();
	}

	@Test
	public void shouldBeNotifiedWhenRemoteResourceDeployedWithProxyEnabledAndUsed() throws Exception {
		// pre-condition
		final int httpPreviewPort = createHttpPreviewServer();
		createAndLaunchLiveReloadServer(true, true);
		final String indexRemoteDocumentlocation = "http://localhost:" + httpPreviewPort + "/" + project.getName()
				+ "/index.html";
		final LiveReloadTestClient client = new LiveReloadTestClient(indexRemoteDocumentlocation);
		// operation
		final Connection connection = connectFrom(client);
		// operation: simulate HTTP preview server startup: notify listeners
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		((Server) httpPreviewServer).publish(IServer.PUBLISH_AUTO, new NullProgressMonitor());
		Thread.sleep(200);
		// verification: client should have been notified with a reload message
		assertThat(client.isNotificationReceived()).isEqualTo(true);
		assertThat(client.getReceivedNotification()).doesNotContain("http://localhost:" + this.liveReloadServerPort);
		// end
		connection.close();

	}

	@Test
	public void shouldNotBeNotifiedWhenConnectionClosed() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true, true);
		final LiveReloadTestClient client = new LiveReloadTestClient(indexFileLocation);
		// operation
		final Connection connection = connectFrom(client);
		// operation : trigger a resource changed event
		connection.close();
		WorkbenchUtils.replaceAllOccurrencesOfCode("WebContent/index.html", project, "Hello, World",
				"Hello, LiveReload !");
		Thread.sleep(200);
		// verification: client should have been notified with a reload message
		assertThat(client.isNotificationReceived()).isEqualTo(false);
		// end
	}

	@Test
	public void shouldNotInjectLiveReloadScriptInUnknownHtmlPage() throws Exception {
		createAndLaunchLiveReloadServer(true, false);
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(unknowDocumentLocation);
		method.addRequestHeader("Accept", "text/css");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	public void shouldAddServerListenerWhenCreatingHttpPreviewServerAfterLiveReloadServerAndProxyModeEnabled()
			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		createAndLaunchLiveReloadServer(true, true);
		// operation
		createHttpPreviewServer();
		// verification
		assertThat(
				liveReloadServerBehaviour.getServerLifeCycleListener().getSupervisedServers(
						ServerLifeCycleListener.SERVER_LISTENER)).contains(httpPreviewServer);
	}

	@Test
	public void shouldAddServerListenerWhenCreatingLiveReloadServerAfterHttpPreviewServerAndProxyModeEnabled()
			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		createHttpPreviewServer();
		// operation
		createAndLaunchLiveReloadServer(true, true);
		// verification
		assertThat(
				liveReloadServerBehaviour.getServerLifeCycleListener().getSupervisedServers(
						ServerLifeCycleListener.SERVER_LISTENER)).contains(httpPreviewServer);
	}

	@Test
	public void shouldAddServerListenerWhenCreatingHttpPreviewServerAfterLiveReloadServerAndProxyModeDisabled()
			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		createAndLaunchLiveReloadServer(false, false);
		// operation
		createHttpPreviewServer();
		// verification
		assertThat(
				liveReloadServerBehaviour.getServerLifeCycleListener().getSupervisedServers(
						ServerLifeCycleListener.SERVER_LISTENER)).contains(httpPreviewServer);
	}

	@Test
	public void shouldAddServerListenerWhenCreatingLiveReloadServerAfterHttpPreviewServerAndProxyModeDisabled()
			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		createHttpPreviewServer();
		// operation
		createAndLaunchLiveReloadServer(false, false);
		// verification
		assertThat(
				liveReloadServerBehaviour.getServerLifeCycleListener().getSupervisedServers(
						ServerLifeCycleListener.SERVER_LISTENER)).contains(httpPreviewServer);
	}

	@Test
	public void shouldRemoveServerListenerWhenDeletingServer() throws InterruptedException, CoreException,
			ExecutionException, TimeoutException {
		// pre-condition
		createAndLaunchLiveReloadServer(false, false);
		createHttpPreviewServer();
		assertThat(
				liveReloadServerBehaviour.getServerLifeCycleListener().getSupervisedServers(
						ServerLifeCycleListener.SERVER_LISTENER)).contains(httpPreviewServer);
		// operation
		httpPreviewServer.delete();
		// verification
		assertThat(
				liveReloadServerBehaviour.getServerLifeCycleListener().getSupervisedServers(
						ServerLifeCycleListener.SERVER_LISTENER)).isEmpty();
	}

	@Test
	public void shouldAddProxyWhenStartingHttpPreviewServerAndProxyModeEnabled() throws CoreException,
			InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		createHttpPreviewServer();
		createAndLaunchLiveReloadServer(true, true);
		// operation
		((Server) httpPreviewServer).setServerState(IServer.STATE_STARTED);
		// verification
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
	}

	public void shouldAddProxyWhenCreatingAndStartingHttpPreviewServerAndProxyModeEnabled() throws CoreException,
			InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		createAndLaunchLiveReloadServer(true, true);
		// operation
		createHttpPreviewServer();
		((Server) httpPreviewServer).setServerState(IServer.STATE_STARTED);
		// verification
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
	}

	@Test
	public void shouldAddProxyWhenCreatingLiveReloadServerAndHttpPreviewServerStartedAndProxyModeEnabled()
			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		createHttpPreviewServer();
		((Server) httpPreviewServer).setServerState(IServer.STATE_STARTED);
		// operation
		createAndLaunchLiveReloadServer(true, true);
		// verification
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
	}

	@Test
	public void shouldNotAddProxyWhenStartingHttpPreviewServerAndProxyModeDisabled() throws CoreException,
			InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		createAndLaunchLiveReloadServer(false, false);
		// operation
		createHttpPreviewServer();
		((Server) httpPreviewServer).setServerState(IServer.STATE_STARTED);
		// verification
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).isEmpty();
		;
	}

	@Test
	public void shouldAddProxyWhenCreatingLiveReloadServerAndHttpPreviewServerStartedAndProxyModeDisabled()
			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		createHttpPreviewServer();
		((Server) httpPreviewServer).setServerState(IServer.STATE_STARTED);
		// operation
		createAndLaunchLiveReloadServer(false, false);
		// verification
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).isEmpty();
	}

	@Test
	public void shouldRemoveProxyWhenDeletingServer() throws InterruptedException, CoreException, ExecutionException,
			TimeoutException {
		// pre-condition
		createAndLaunchLiveReloadServer(true, true);
		createHttpPreviewServer();
		// operation
		httpPreviewServer.delete();
		// verification
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).isEmpty();
	}

	@Test
	public void shouldRemoveProxyWhenStoppingServer() throws InterruptedException, CoreException, ExecutionException,
			TimeoutException {
		// pre-condition
		createAndLaunchLiveReloadServer(true, true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).isEmpty();
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		// operation
		httpPreviewServer.stop(true);
		// give it a little time to actually stop (in a separate thread)
		TimeoutUtils.timeout(new TaskMonitor() {
			@Override
			public boolean isComplete() {
				return !(httpPreviewServer.canStop().isOK());
			}
		}, 5, TimeUnit.SECONDS);
		TimeoutUtils.timeout(new TaskMonitor() {
			@Override
			public boolean isComplete() {
				return liveReloadServerBehaviour.getProxyServers().keySet().isEmpty();
			}
		}, 5, TimeUnit.SECONDS);
		// verification
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).isEmpty();
	}

	@Test
	public void shouldStopLiveReloadServerIfRunning() throws CoreException, InterruptedException, ExecutionException,
			TimeoutException {
		// precondition
		createAndLaunchLiveReloadServer(false, false);
		assertThat(liveReloadServer.canStop().isOK()).isTrue();
		// operation
		liveReloadServer.stop(true);
		// give it a little time to actually stop (in a separate thread)
		TaskMonitor monitor = new TaskMonitor() {
			@Override
			public boolean isComplete() {
				return !(liveReloadServer.canStop().isOK());
			}
		};
		TimeoutUtils.timeout(monitor, 2, TimeUnit.SECONDS);
		// verification
		assertThat(liveReloadServer.canStop().isOK()).isFalse();
	}

	@Test
	public void shouldForwardRequestOnProxiedServerWithoutScriptInjection() throws Exception {
		// pre-condition
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true, false);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation
		int proxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0].getPort();
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod("http://localhost:" + proxyPort + "/" + projectName
				+ "/WebContent/index.html");
		method.addRequestHeader("Accept", "text/html");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = new String(method.getResponseBody());
		// verification
		assertThat(responseBody).contains("Hello, World!");
		assertThat(responseBody).doesNotContain("livereload.js");
	}

	@Test
	public void shouldForwardRequestOnProxiedServerWithScriptInjection() throws Exception {
		// pre-condition
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true, true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation
		int proxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0].getPort();
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod("http://localhost:" + proxyPort + "/" + projectName
				+ "/WebContent/index.html");
		method.addRequestHeader("Accept", "text/html");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = new String(method.getResponseBody());
		// verification
		assertThat(responseBody).contains("Hello, World!");
		assertThat(responseBody).contains("livereload.js").contains(
				Integer.toString(liveReloadServerBehaviour.getLiveReloadServer().getPort()));
	}

	@Test
	public void shouldAllowWebSocketConnectionFromProxiedLocation() throws Exception {
		// pre-condition
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true, true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation
		int proxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0].getPort();
		final LiveReloadTestClient client = new LiveReloadTestClient("http://localhost:" + proxyPort + "/"
				+ projectName + "/WebContent/index.html");
		// operation
		final Connection connection = connectFrom(client);
		// verification
		assertThat(connection.isOpen()).isTrue();
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
		connection.close();
	}

	@Test
	public void shouldReuseSameProxyPortAfterServerRestart() throws Exception {
		// pre-condition
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true, true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation
		int proxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0].getPort();
		final LiveReloadTestClient client = new LiveReloadTestClient("http://localhost:" + proxyPort + "/"
				+ projectName + "/WebContent/index.html");
		// operation
		final Connection connection = connectFrom(client);
		// operation: restart the Preview Server
		httpPreviewServer.restart(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		// verification
		assertThat(connection.isOpen()).isTrue();
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
		int newProxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0]
				.getPort();
		assertThat(proxyPort).isEqualTo(newProxyPort);
		connection.close();
	}

	@Test
	public void shouldReuseSameProxyPortAfterLiveReloadServerRestart() throws Exception {
		// pre-condition
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true, true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation
		int proxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0].getPort();
		final LiveReloadTestClient client = new LiveReloadTestClient("http://localhost:" + proxyPort + "/"
				+ projectName + "/WebContent/index.html");
		// operation
		final Connection connection = connectFrom(client);
		// operation: restart the LiveReload Server
		liveReloadServer.restart(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		// verification
		assertThat(connection.isOpen()).isTrue();
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
		int newProxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0]
				.getPort();
		assertThat(proxyPort).isEqualTo(newProxyPort);
		connection.close();
	}

}
