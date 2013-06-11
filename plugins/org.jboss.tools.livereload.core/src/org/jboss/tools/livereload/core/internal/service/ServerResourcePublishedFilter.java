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

package org.jboss.tools.livereload.core.internal.service;

import java.util.EventObject;

import org.eclipse.wst.server.core.IServer;

/**
 * @author xcoulon
 * 
 */
public class ServerResourcePublishedFilter implements EventFilter {

	/**
	 * The {@link IServer} for which this filter should allow events. Events
	 * related to files in other servers should be discarded by this filter.
	 */
	private final IServer server;
	
	/**
	 * Default constructor.
	 * @param serverthe server that this filter accepts.
	 */
	public ServerResourcePublishedFilter(final IServer server) {
		this.server = server;
	}
	
	//FIXME: should keep info about server host/port at least, in case the host runs several app servers at the same time.
	@Override
	public boolean accept(EventObject e) {
		if (e instanceof ServerResourcePublishedEvent) {
			return ((ServerResourcePublishedEvent)e).getServer().equals(server);
		}
		return false;
	}

}
