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

package org.jboss.tools.livereload.internal.server.wst;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.util.SocketUtil;
import org.jboss.tools.livereload.internal.AbstractCommonTestCase;
import org.jboss.tools.livereload.internal.util.WSTTestUtils;
import org.jboss.tools.livereload.internal.util.WSTUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xcoulon
 *
 */
public class LiveReloadServerBehaviourTestCase extends AbstractCommonTestCase {
	
	private IServer liveReloadServer;
	private int websocketPort;

	@Before
	public void createServer() throws CoreException {
		websocketPort = SocketUtil.findUnusedPort(50000, 65000);
		liveReloadServer = WSTTestUtils.createLiveReloadServer(LiveReloadServerBehaviourTestCase.class.getName(), websocketPort);
		assertThat(liveReloadServer).isNotNull();
	}
	
	@After
	public void destroyServer() throws CoreException {
		liveReloadServer.delete();
	}
	
	@Test(timeout=10000)
	public void shouldStartLiveReloadServer() throws InterruptedException, IOException {
		// pre-condition
		// calling the Server#canStart(int) creates the underlying ServerBehaviour
		assertThat(liveReloadServer.canStart(ILaunchManager.RUN_MODE).isOK()).isTrue();
		LiveReloadServerBehaviour liveReloadServerBehaviour = WSTUtils.findServerBehaviour(liveReloadServer.getId());
		assertThat(liveReloadServerBehaviour).isNotNull();
		assertThat(SocketUtil.isPortInUse(websocketPort)).isFalse();
		// operation
		liveReloadServerBehaviour.startServer();
		// verification
		// give a few millis to the server to actually start
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod("http://localhost:" + websocketPort + "/livereload.js");
		final int statusCode = client.executeMethod(method);
		assertThat(statusCode).isEqualTo(200);
	}
	
}
