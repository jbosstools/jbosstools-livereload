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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.util.SocketUtil;
import org.jboss.tools.livereload.core.internal.server.jetty.LiveReloadProxyServer;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadLaunchConfiguration;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadServerBehaviour;
import org.jboss.tools.livereload.core.internal.service.EventFilter;
import org.jboss.tools.livereload.core.internal.service.EventService;
import org.jboss.tools.livereload.core.internal.service.ServerLifeCycleListener;
import org.jboss.tools.livereload.core.internal.service.Subscriber;
import org.jboss.tools.livereload.core.internal.util.TimeoutUtils;
import org.jboss.tools.livereload.core.internal.util.TimeoutUtils.TaskMonitor;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.jboss.tools.livereload.internal.AbstractCommonTestCase;
import org.jboss.tools.livereload.internal.WorkbenchUtils;
import org.jboss.tools.livereload.test.previewserver.PreviewServerBehaviour;
import org.jboss.tools.livereload.test.previewserver.PreviewServerFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

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
	
	private String unknownServerLocation;

	private String unknownDocumentLocation;

	private String cssDocumentLocation;

	private String asciidocDocumentLocation;

	private String folderDocumentLocation;

	private LiveReloadServerBehaviour liveReloadServerBehaviour;

	final String hostname = resolveHostname();
	
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
		indexDocumentlocation = "http://" + hostname + ":" + liveReloadServerPort + "/" + project.getName() + "/"
				+ index_html_file.getLocation().makeRelativeTo(project.getLocation()).toString();
		unknownServerLocation = "http://" + hostname + ":12345/index.html";
		unknownDocumentLocation = "http://" + hostname + ":" + liveReloadServerPort + "/unknownProject/index.html";
		cssDocumentLocation = indexDocumentlocation.replace("index.html", "styles.css");
		asciidocDocumentLocation = indexDocumentlocation.replace("index.html", "README.adoc");
		folderDocumentLocation = indexDocumentlocation.replace("index.html", "");
	}

	/**
	 * @throws CoreException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws UnknownHostException 
	 * 
	 */
	private IServer createAndLaunchLiveReloadServer(final String serverName, final boolean injectScript)
			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		final IServer livereloadServer = WSTUtils.createLiveReloadServer(serverName, hostname, liveReloadServerPort,
				injectScript, false);
		// ensure notification delay is set to '0'
		setNotificationDelay(livereloadServer, 0);
		liveReloadServerBehaviour = (LiveReloadServerBehaviour) WSTUtils.findServerBehaviour(livereloadServer);
		assertThat(liveReloadServerBehaviour).isNotNull();
		liveReloadServer = liveReloadServerBehaviour.getServer();
		assertThat(liveReloadServer).isNotNull();
		assertThat(liveReloadServer.canStart(ILaunchManager.RUN_MODE).isOK()).isTrue();
		startServer(liveReloadServer, 60, TimeUnit.SECONDS);
		return livereloadServer;
	}

	private static String resolveHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("Unable to retrieve local host name: " + e.getMessage());
		}
		// never reached
		return null;
	}

	/**
	 * @throws CoreException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * 
	 */
	private IServer createAndLaunchLiveReloadServer(final boolean injectScript)
			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		return createAndLaunchLiveReloadServer("LiveReload Test Server at localhost", injectScript);
	}
	
	private void setNotificationDelay(final IServer livereloadServer, final int seconds) throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		final IServerWorkingCopy livereloadServerWorkingCopy = livereloadServer.createWorkingCopy();
		livereloadServerWorkingCopy.setAttribute(LiveReloadLaunchConfiguration.NOTIFICATION_DELAY, seconds);
		livereloadServerWorkingCopy.save(true, null);
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
	 * @param client
	 * @return
	 * @throws Exception
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private Session connectFrom(final LiveReloadTestSocket client) throws Exception, IOException,
			URISyntaxException, InterruptedException, ExecutionException, TimeoutException {
		final WebSocketClient webSocketClient = new WebSocketClient();
		webSocketClient.start();
		final ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        final URI uri = new URI("ws://" + hostname + ":" + this.liveReloadServerPort
				+ "/livereload");
		final Future<Session> future = webSocketClient.connect(client, uri, upgradeRequest);
		// final Connection session = future.get(5, TimeUnit.SECONDS);
		final Session session = future.get();
		while(!client.isHandshakeComplete()) {
			Thread.sleep(100);
		}
		return session;
	}

	@Test
	public void shouldAcceptWebsocketConnexionWithoutProxy() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(false);
		final LiveReloadTestSocket client = new LiveReloadTestSocket(indexFileLocation);
		// operation
		final Session session = connectFrom(client);
		// verification
		assertThat(session.isOpen()).isTrue();
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
		session.close();
	}

	@Test
	public void shouldNotAcceptWebsocketConnexionWithoutValidUrlInfo() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(false);
		final LiveReloadTestSocket client = new LiveReloadTestSocket("");
		// operation
		final Session session = connectFrom(client);
		// verification
		assertThat(session.isOpen()).isFalse();
		// assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(0);
		session.close();
	}

	@Test
	public void shouldAcceptWebsocketConnexionWithProxy() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(false);
		final LiveReloadTestSocket client = new LiveReloadTestSocket(indexFileLocation);
		// operation
		final Session session = connectFrom(client);
		// verification
		assertThat(session.isOpen()).isTrue();
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
		session.close();
	}
	
	@Test
	public void shouldNotAcceptWebsocketConnexionForUnknownFileLocation() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(false);
		final LiveReloadTestSocket client = new LiveReloadTestSocket(unknownDocumentLocation);
		final Map<Subscriber, List<EventFilter>> eventSubscribers = EventService.getInstance().getSubscribers();
		final int subscribersCounter = eventSubscribers.size();
		// operation
		final Session session = connectFrom(client);
		// verification
		assertThat(session.isOpen()).isTrue();
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(0);
		// unchanged number of subscribers
		assertThat(eventSubscribers.size()).isEqualTo(subscribersCounter);
		session.close();
	}
	
	@Test
	public void shouldAcceptWebsocketEvenIfServerUnknown() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(false);
		final LiveReloadTestSocket client = new LiveReloadTestSocket(unknownServerLocation);
		// operation
		final Session session = connectFrom(client);
		// verification
		assertThat(session.isOpen()).isTrue();
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
		session.close();
	}
	
	@Test
	public void shouldAcceptWebsocketEvenIfServerUnknownAndProjectUnknown() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(false);
		final LiveReloadTestSocket client = new LiveReloadTestSocket(unknownServerLocation.replace(project.getName(), "foobar"));
		// operation
		final Session session = connectFrom(client);
		// verification
		assertThat(session.isOpen()).isTrue();
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
		session.close();
	}
	
	@Test
	public void shouldAcceptHttpConnexionAndReturnHtmlResource() throws Exception {
		createAndLaunchLiveReloadServer(true);
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(indexDocumentlocation);
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
	}

	@Test
	public void shouldAcceptHttpConnexionAndReturnNotFoundResource() throws Exception {
		createAndLaunchLiveReloadServer(true);
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(unknownDocumentLocation);
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	public void shouldAcceptHttpConnexionAndReturnForbiddenResponseWhenRequestingFolder() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true);
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
		createAndLaunchLiveReloadServer(false);
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
		String responseBody = method.getResponseBodyAsString();
		assertThat(responseBody).doesNotContain(scriptContent);

	}

	@Test
	public void shouldInjectLiveReloadScriptInHtmlPageWithSimpleAcceptedTypes() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true);
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
		String responseBody = method.getResponseBodyAsString();
		assertThat(responseBody).contains(scriptContent);
	}
	
	@Test
	// @see https://issues.jboss.org/browse/JBIDE-15317
	public void shouldNotCorruptHtmlContentWhenInjectingScript() throws CoreException, InterruptedException, ExecutionException, TimeoutException, HttpException, IOException {
		// pre-condition
		createAndLaunchLiveReloadServer(true);
		final String scriptContent = new StringBuilder(
				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
				.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
		final IResource chineseHtmlFile = project.findMember("WebContent" + File.separator + "chinese.html");
		final String chineseDocumentlocation = "http://" + hostname + ":" + liveReloadServerPort + "/" + project.getName() + "/"
				+ chineseHtmlFile.getLocation().makeRelativeTo(project.getLocation()).toString();
				assertThat(chineseHtmlFile).isNotNull();
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(chineseDocumentlocation);
		method.addRequestHeader("Accept", "text/html");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = method.getResponseBodyAsString();
		byte[] rawResponseBody = method.getResponseBody();
		assertThat(responseBody).contains(scriptContent);
		LOGGER.debug(responseBody);
		assertThat(responseBody).doesNotContain("??");
		assertThat(responseBody).contains("中文");
		//The Content-Length entity-header field indicates the size of the entity-body,
		//in decimal number of OCTETs, sent to the recipient or, in the case of the HEAD
		//method, the size of the entity-body that would have been sent had the request
		//been a GET.
		assertThat(rawResponseBody.length).isEqualTo(257);
		assertThat(method.getResponseHeader("Content-Length").getValue()).isEqualTo("257");
	}

	@Test
	// @see https://issues.jboss.org/browse/JBIDE-15317
	public void shouldNotCorruptHtmlContentWhenNotInjectingScript() throws CoreException, InterruptedException, ExecutionException, TimeoutException, HttpException, IOException {
		// pre-condition
		createAndLaunchLiveReloadServer(false);
		final IResource chineseHtmlFile = project.findMember("WebContent" + File.separator + "chinese.html");
		final String chineseDocumentlocation = "http://" + hostname + ":" + liveReloadServerPort + "/" + project.getName() + "/"
				+ chineseHtmlFile.getLocation().makeRelativeTo(project.getLocation()).toString();
				assertThat(chineseHtmlFile).isNotNull();
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(chineseDocumentlocation);
		method.addRequestHeader("Accept", "text/html");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = method.getResponseBodyAsString();
		assertThat(responseBody).doesNotContain("<script");
		LOGGER.debug(responseBody);
		assertThat(responseBody).doesNotContain("???");
		assertThat(responseBody).contains("中文");
	}
	
	@Test
	public void shouldInjectLiveReloadScriptInHtmlPageWithSimpleAcceptedTypeAndcharset() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true);
		final String scriptContent = new StringBuilder(
				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
		.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(indexDocumentlocation);
		method.addRequestHeader("Accept", "text/html; charset=UTF-8");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = method.getResponseBodyAsString();
		assertThat(responseBody).contains(scriptContent);
	}

	@Test
	public void shouldInjectLiveReloadScriptInHtmlPageWithMultipleAcceptedTypeAndQualityFactors() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true);
		final String scriptContent = new StringBuilder(
				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
		.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(indexDocumentlocation);
		method.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = method.getResponseBodyAsString();
		assertThat(responseBody).contains(scriptContent);
	}

	@Test
	public void shouldGetLiveReloadScriptWithProxyEnabled() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true);
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod("http://" + hostname + ":" + liveReloadServerPort + "/livereload.js");
		method.addRequestHeader("Accept", "text/javascript");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = method.getResponseBodyAsString();
		assertThat(responseBody).isNotEmpty();
	}

	@Test
	public void shouldNotInjectLiveReloadScriptInCssFile() throws Exception {
		createAndLaunchLiveReloadServer(false);
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
		String responseBody = method.getResponseBodyAsString();
		assertThat(responseBody).doesNotContain(scriptContent);
	}

	@Test
	public void shouldNotInjectLiveReloadScriptInAsciidoctorFile() throws Exception {
		createAndLaunchLiveReloadServer(false);
		final String scriptContent = new StringBuilder(
				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
		.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(asciidocDocumentLocation);
		method.addRequestHeader("Accept", "text/html");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = method.getResponseBodyAsString();
		assertThat(responseBody).doesNotContain(scriptContent);
	}

	@Test
	public void shouldBeNotifiedWhenLocalFileChangedWithProxyEnabled() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true);
		final LiveReloadTestSocket client = new LiveReloadTestSocket(indexDocumentlocation);
		// operation
		final Session session = connectFrom(client);
		// operation : trigger a resource changed event
		WorkbenchUtils.replaceAllOccurrencesOfCode("WebContent/index.html", project, "Hello, World",
				"Hello, LiveReload !");
		Thread.sleep(200);
		// verification: client should have been notified with a reload message
		assertThat(client.getNumberOfReloadNotifications()).isEqualTo(1);
		// end
		session.close();
	}

	@Test
	public void shouldBeNotifiedWhenLocalFileChangedWithProxyEnabledAndUnknownServerLocation() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true);
		final LiveReloadTestSocket client = new LiveReloadTestSocket(unknownServerLocation);
		// operation
		final Session session = connectFrom(client);
		// operation : trigger a resource changed event
		WorkbenchUtils.replaceAllOccurrencesOfCode("WebContent/index.html", project, "Hello, World",
				"Hello, LiveReload !");
		Thread.sleep(200);
		// verification: client should have been notified with a reload message
		assertThat(client.getNumberOfReloadNotifications()).isEqualTo(1);
		assertThat(client.getReceivedNotification().contains(unknownServerLocation));
		// end
		session.close();
	}

	@Test
	public void shouldBeNotifiedWhenRemoteResourceDeployedWithProxyEnabledButNotUsed() throws Exception {
		// pre-condition
		final int httpPreviewPort = createHttpPreviewServer();
		createAndLaunchLiveReloadServer(true);
		final String indexRemoteDocumentlocation = "http://" + hostname + ":" + httpPreviewPort + "/" + project.getName()
				+ "/index.html";
		final LiveReloadTestSocket client = new LiveReloadTestSocket(indexRemoteDocumentlocation);
		// operation: start server and connect to it
		((Server) httpPreviewServer).setServerState(IServer.STATE_STARTED);
		final Session session = connectFrom(client);
		// operation: simulate publish
		((Server) httpPreviewServer).publish(IServer.PUBLISH_AUTO, new NullProgressMonitor());
		Thread.sleep(200);
		// verification: client should have been notified with a reload message
		assertThat(client.getNumberOfReloadNotifications()).isEqualTo(1);
		assertThat(client.getReceivedNotification()).contains("http://" + hostname + ":" + httpPreviewPort);
		// end
		session.close();
	}

	@Test
	public void shouldBeNotifiedWhenRemoteResourceDeployedWithProxyEnabledAndUsedAndNoDelay() throws Exception {
		// pre-condition
		final int httpPreviewPort = createHttpPreviewServer();
		createAndLaunchLiveReloadServer(true);
		final String indexRemoteDocumentlocation = "http://" + hostname + ":" + httpPreviewPort + "/" + project.getName()
				+ "/index.html";
		final LiveReloadTestSocket client = new LiveReloadTestSocket(indexRemoteDocumentlocation);
		// operation: start server and connect to it
		startServer(httpPreviewServer, 30, TimeUnit.SECONDS);
		final Session session = connectFrom(client);
		// operation: simulate HTTP preview server publish
		final long start = System.currentTimeMillis();
		((Server) httpPreviewServer).publish(IServer.PUBLISH_AUTO, new NullProgressMonitor());
		final long end = System.currentTimeMillis();
		// should take less than 1s
		Thread.sleep(200);
		// verification: client should have been notified with a reload message
		assertThat(client.getNumberOfReloadNotifications()).isEqualTo(1);
		assertThat(client.getReceivedNotification()).doesNotContain("http://" + hostname + ":" + this.liveReloadServerPort);
		assertThat(end - start).isLessThan(2000);
		// end
		session.close();
	}

	@Test
	public void shouldBeNotifiedWhenRemoteResourceDeployedWithProxyEnabledAndUsedAnd5sDelay() throws Exception {
		// pre-condition
		final int httpPreviewPort = createHttpPreviewServer();
		final IServer livereloadServer = createAndLaunchLiveReloadServer(true);
		setNotificationDelay(livereloadServer, 5);
		
		final String indexRemoteDocumentlocation = "http://" + hostname + ":" + httpPreviewPort + "/" + project.getName()
				+ "/index.html";
		final LiveReloadTestSocket client = new LiveReloadTestSocket(indexRemoteDocumentlocation);
		// operation: start server and connect to it
		startServer(httpPreviewServer, 30, TimeUnit.SECONDS);
		//httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		final Session session = connectFrom(client);
		// operation: simulate HTTP preview server publish
		final long start = System.currentTimeMillis();
		((Server) httpPreviewServer).publish(IServer.PUBLISH_AUTO, new NullProgressMonitor());
		final long end = System.currentTimeMillis();
		Thread.sleep(1000);
		// verification: client should have been notified with a reload message
		assertThat(client.getNumberOfReloadNotifications()).isEqualTo(1);
		assertThat(client.getReceivedNotification()).doesNotContain("http://" + hostname + ":" + this.liveReloadServerPort);
		assertThat(end - start).isGreaterThan(5000);
		// end
		session.close();
	}

	@Test
	public void shouldNotBeNotifiedWhenConnectionClosed() throws Exception {
		// pre-condition
		createAndLaunchLiveReloadServer(true);
		final LiveReloadTestSocket client = new LiveReloadTestSocket(indexFileLocation);
		// operation
		final Session session = connectFrom(client);
		// operation : trigger a resource changed event
		session.close();
		WorkbenchUtils.replaceAllOccurrencesOfCode("WebContent/index.html", project, "Hello, World",
				"Hello, LiveReload !");
		Thread.sleep(200);
		// verification: client should have been notified with a reload message
		assertThat(client.getNumberOfReloadNotifications()).isEqualTo(0);
		// end
	}

	@Test
	public void shouldNotInjectLiveReloadScriptInUnknownHtmlPage() throws Exception {
		createAndLaunchLiveReloadServer(false);
		// operation
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(unknownDocumentLocation);
		method.addRequestHeader("Accept", "text/css");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	public void shouldAddServerListenerWhenCreatingHttpPreviewServerAfterLiveReloadServerAndProxyModeEnabled()
			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		createAndLaunchLiveReloadServer(true);
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
		createAndLaunchLiveReloadServer(true);
		// verification
		assertThat(
				liveReloadServerBehaviour.getServerLifeCycleListener().getSupervisedServers(
						ServerLifeCycleListener.SERVER_LISTENER)).contains(httpPreviewServer);
	}

	@Test
	public void shouldRemoveServerListenerWhenDeletingServer() throws InterruptedException, CoreException,
			ExecutionException, TimeoutException {
		// pre-condition
		createAndLaunchLiveReloadServer(false);
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
		createAndLaunchLiveReloadServer(true);
		// operation
		((Server) httpPreviewServer).setServerState(IServer.STATE_STARTED);
		// verification
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
	}

	public void shouldAddProxyWhenCreatingAndStartingHttpPreviewServerAndProxyModeEnabled() throws CoreException,
			InterruptedException, ExecutionException, TimeoutException {
		// pre-condition
		createAndLaunchLiveReloadServer(true);
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
		startServer(httpPreviewServer, 30, TimeUnit.SECONDS);
		// operation
		createAndLaunchLiveReloadServer(false);
		// verification
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
	}

	@Test
	public void shouldRemoveProxyWhenDeletingServer() throws InterruptedException, CoreException, ExecutionException,
			TimeoutException {
		// pre-condition
		createAndLaunchLiveReloadServer(true);
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
		createAndLaunchLiveReloadServer(true);
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
		createAndLaunchLiveReloadServer(false);
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
		createAndLaunchLiveReloadServer(false);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation
		final NetworkConnector connector = (NetworkConnector) liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0];
		final int proxyPort = connector.getPort();
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod("http://" + hostname + ":" + proxyPort + "/" + projectName
				+ "/WebContent/index.html");
		method.addRequestHeader("Accept", "text/html");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		String responseBody = method.getResponseBodyAsString();
		// verification
		assertThat(responseBody).contains("Hello, World!");
		assertThat(responseBody).doesNotContain("livereload.js");
	}

	@Test(timeout=60*10000)
	public void shouldForwardRequestOnProxiedServerWithScriptInjectionAndDefaultCharset() throws Exception {
		// pre-condition
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation
		final NetworkConnector connector = (NetworkConnector) liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0];
		final int proxyPort = connector.getPort();
		final HttpClient client = new HttpClient();
		final HttpMethod method = new GetMethod("http://" + hostname + ":" + proxyPort + "/" + projectName
				+ "/WebContent/index.html");
		method.addRequestHeader("Accept", "text/html");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the response body.
		// Read the returned content type.
		assertThat(method.getResponseHeader("Content-Type")).isNotNull();
		assertThat(method.getResponseHeader("Content-Type").getValue()).isEqualTo("text/html; charset=UTF-8");
		String responseBody = method.getResponseBodyAsString();
		// verification
		assertThat(responseBody).contains("Hello, World!");
		assertThat(responseBody).contains("livereload.js").contains(
				Integer.toString(liveReloadServerBehaviour.getLiveReloadServer().getPort()));
	}
	
	@Test(timeout=60*10000)
	public void shouldForwardRequestOnProxiedServerWithScriptInjectionAndCustomCharset() throws Exception {
		// pre-condition
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation
		final NetworkConnector connector = (NetworkConnector) liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0];
		final int proxyPort = connector.getPort();
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod("http://" + hostname + ":" + proxyPort + "/" + projectName
				+ "/WebContent/index.html");
		method.addRequestHeader("Accept", "text/html; charset=ISO-8859-1");
		int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(HttpStatus.SC_OK);
		// Read the returned content type.
		assertThat(method.getResponseHeader("Content-Type").getValue()).isEqualTo("text/html; charset=ISO-8859-1");
		// Read the response body.
		String responseBody = method.getResponseBodyAsString();
		// verification
		assertThat(responseBody).contains("Hello, World!");
		assertThat(responseBody).contains("livereload.js").contains(
				Integer.toString(liveReloadServerBehaviour.getLiveReloadServer().getPort()));
	}
	
	@Test
	public void shouldForwardRequestWithQueryParams() throws IOException, CoreException, InterruptedException, ExecutionException, TimeoutException {
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation: send a request with a query param. The Preview server has a special servlet that will return a 400 error if the 
		// proxy did not forward the query param 
		final NetworkConnector connector = (NetworkConnector) liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0];
		final int proxyPort = connector.getPort();
		final HttpClient client = new HttpClient();
		final HttpMethod method = new GetMethod("http://" + hostname + ":" + proxyPort + "/foo/bar?w00t=true");
		final int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(200);
	}

	@Test
	public void shouldAllowWebSocketConnectionFromProxiedLocation() throws Exception {
		// pre-condition
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation
		final NetworkConnector connector = (NetworkConnector) liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0];
		final int proxyPort = connector.getPort();
		final LiveReloadTestSocket client = new LiveReloadTestSocket("http://" + hostname + ":" + proxyPort + "/"
				+ projectName + "/WebContent/index.html");
		// operation
		final Session session = connectFrom(client);
		// verification
		assertThat(session.isOpen()).isTrue();
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
		session.close();
	}

	@Test
	public void shouldReuseSameProxyPortAfterServerRestart() throws Exception {
		// pre-condition
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation
		final NetworkConnector connector = (NetworkConnector) liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0];
		final int proxyPort = connector.getPort();
		final LiveReloadTestSocket client = new LiveReloadTestSocket("http://" + hostname + ":" + proxyPort + "/"
				+ projectName + "/WebContent/index.html");
		// operation
		final Session session = connectFrom(client);
		// operation: restart the Preview Server
		httpPreviewServer.restart(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		// verification
		assertThat(session.isOpen()).isTrue();
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
		final int newProxyPort = connector.getPort();
		assertThat(proxyPort).isEqualTo(newProxyPort);
		session.close();
	}

	@Test(timeout=30*1000)
	public void shouldReuseSameProxyPortAfterLiveReloadServerRestart() throws Exception {
		// pre-condition
		createHttpPreviewServer();
		//httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		startServer(httpPreviewServer, 30, TimeUnit.SECONDS);
		createAndLaunchLiveReloadServer(true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation
		final NetworkConnector connector = (NetworkConnector) liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0];
		final int proxyPort = connector.getPort();
		final LiveReloadTestSocket client = new LiveReloadTestSocket("http://" + hostname + ":" + proxyPort + "/"
				+ projectName + "/WebContent/index.html");
		// operation
		connectFrom(client);
		// operation: restart the LiveReload Server
		//liveReloadServer.restart(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		WSTUtils.restart(liveReloadServer, 30, TimeUnit.SECONDS);
		// verification
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(0);
		final int newProxyPort = connector.getPort();
		assertThat(proxyPort).isEqualTo(newProxyPort);
	}
	
	@Test
	public void shouldNotStartIfPortAlreadyInUse() throws CoreException, InterruptedException, ExecutionException, TimeoutException {
		// pre-condition: create a first server (no need for script injection)
		final IServer firstServer = createAndLaunchLiveReloadServer("Server 1", false);
		assertThat(firstServer.getServerState()).isEqualTo(IServer.STATE_STARTED);
		// operation: create a second server (no need for script injection
		// either) and attempt to start it on the same port -> should not start
		final IServer secondServer = createAndLaunchLiveReloadServer("Server 2", false);
		// verification
		assertThat(secondServer.getServerState()).isEqualTo(IServer.STATE_STOPPED);
	}
	
	@Test
	public void shouldUseACustomHostName() throws CoreException, InterruptedException, ExecutionException, TimeoutException, UnknownHostException {
		final IServer server = createAndLaunchLiveReloadServer("foo.server", true);
		assertThat(server.getServerState()).isEqualTo(IServer.STATE_STARTED);
		assertThat(server.getHost()).isEqualTo(hostname);
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getHost()).isEqualTo(hostname);
		// operation: create a custom PreviewServer and start it
		createHttpPreviewServer();
		startServer(httpPreviewServer, 30, TimeUnit.SECONDS);
		// verification: the proxy server associated with this custom PreviewServer should have the same hostname as 
		// the livereload server
		final LiveReloadProxyServer proxyServer = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer);
		assertThat(proxyServer).isNotNull();
		assertThat(proxyServer.getProxyHost()).isEqualTo(server.getHost());
	}

	
	@Test
	public void shouldRetrieveCustomLocationResponseHeader() throws URISyntaxException, Exception {
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation: send a request and expect a 302 response with a 'Location' header using the proxy port
		final NetworkConnector connector = (NetworkConnector) liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0];
		final String proxyHost = connector.getHost();
		final int proxyPort = connector.getPort();
		final HttpClient client = new HttpClient();
		final HttpMethod method = new GetMethod("http://" + proxyHost + ":" + proxyPort + "/foo/baz");
		method.setFollowRedirects(false);
		final int status = client.executeMethod(method);
		// verification
		assertThat(status).isEqualTo(302);
		assertThat(method.getResponseHeader("Location").getValue()).isEqualTo("http://" + proxyHost + ":" + proxyPort + "/foo/baz/");
	}
	
	@Test
	public void shouldRestartLiveReloadServerWithProxyWithoutHttpClientConnection() throws InterruptedException, CoreException, ExecutionException, TimeoutException, HttpException, IOException {
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		// operation: restart the server (should be a very fast operation)
		WSTUtils.restart(liveReloadServer, 5, TimeUnit.SECONDS);
		// verification
		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getState()).isEqualTo(org.eclipse.jetty.server.Server.STARTED);
	}

	@Test
	public void shouldRestartLiveReloadServerWithProxyAfterHttpClientConnection() throws InterruptedException, CoreException, ExecutionException, TimeoutException, HttpException, IOException {
		createHttpPreviewServer();
		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		createAndLaunchLiveReloadServer(true);
		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
		assertThat(liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getState()).isEqualTo(org.eclipse.jetty.server.Server.STARTED);
		final NetworkConnector connector = (NetworkConnector) liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0];
		final String proxyHost = connector.getHost();
		final int proxyPort = connector.getPort();
		final HttpClient client = new HttpClient();
		final HttpMethod method = new GetMethod("http://" + proxyHost + ":" + proxyPort + "/"
				+ projectName + "/WebContent/index.html");
		method.setFollowRedirects(false);
		final int status = client.executeMethod(method);
		assertThat(status).isEqualTo(200);
		// operation: restart the server (should be a very fast operation)
		WSTUtils.restart(liveReloadServer, 60, TimeUnit.SECONDS);
		// verification
		assertThat(liveReloadServerBehaviour.getServer().getServerState()).isEqualTo(IServer.STATE_STARTED);
		assertThat(liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getState()).isEqualTo(org.eclipse.jetty.server.Server.STARTED);

	}
}
