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

/**
 * @author xcoulon
 *
 */
public class LiveReloadClientRefreshedEvent extends EventObject {

	/** serialVersionUID. */
	private static final long serialVersionUID = 4845626286256994123L;

	/**
	 * Constructor
	 * @param clientID id of the client to refresh
	 */
	public LiveReloadClientRefreshedEvent(String clientId) {
		super(clientId);
	}

}
