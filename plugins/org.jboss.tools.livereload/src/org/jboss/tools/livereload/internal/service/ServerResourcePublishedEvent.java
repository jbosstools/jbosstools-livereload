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

import java.util.EventObject;

import org.eclipse.wst.server.core.IServer;

/**
 * @author xcoulon
 *
 */
public class ServerResourcePublishedEvent extends EventObject {

	/** serialVersionUID  */
	private static final long serialVersionUID = -1080733288604938550L;

	/**
	 * Constructor
	 * @param server the server on which the publication occurred
	 */
	public ServerResourcePublishedEvent(final IServer server) {
		super(server);
	}
	
	public IServer getServer() {
		return (IServer)getSource();
	}
	
	@Override
	public String toString() {
		return ServerResourcePublishedEvent.class.getName() + ": " + getSource(); 
	}


}
