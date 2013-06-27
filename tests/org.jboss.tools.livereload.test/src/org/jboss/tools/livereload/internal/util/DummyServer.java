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
import java.net.ServerSocket;

/**
 * A dummy server that will open a socket, just to make sure no one else (like LiveReload) can use it..
 * @author xcoulon
 *
 */
public class DummyServer {

	public static void main(String[] args) throws IOException {
		DummyServer server = new DummyServer(Integer.parseInt(args[0]));
		server.start();
	}

	private final int portNumber; 
	
	/**
	 * 
	 */
	public DummyServer(final int portNumber) {
		this.portNumber = portNumber;
	}

	private void start() throws IOException {
		ServerSocket serverSocket = new ServerSocket(portNumber);
		serverSocket.accept();
		
	}
	
}
