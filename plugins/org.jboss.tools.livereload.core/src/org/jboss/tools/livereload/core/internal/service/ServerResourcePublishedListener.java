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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.util.PublishAdapter;
import org.jboss.tools.livereload.core.internal.util.Logger;

/**
 * Notified when a server finished publishing content (although this
 * implementation is not notified of the detail of the published resources)
 * 
 * @author xcoulon
 * 
 */
public class ServerResourcePublishedListener extends PublishAdapter {

	@Override
	public void publishFinished(IServer server, IStatus status) {
		Logger.trace("Received notification after publish on server '{}' (started={}) finished with status {}", server.getName(), (server.getServerState() == IServer.STATE_STARTED),
				status.getSeverity());
		if (server.getServerState() == IServer.STATE_STARTED && status.isOK()) {
			EventService.getInstance().publish(new ServerResourcePublishedEvent(server));
		} else {
			Logger.debug("Ignoring this publish notification..");
		}

	}

}
