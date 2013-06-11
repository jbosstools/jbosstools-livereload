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
 * Event dispatched when a new LiveReload client disconnected.
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadClientDisconnectedEvent extends EventObject {

	/** serialVersionUID */
	private static final long serialVersionUID = -7493387504238052576L;

	public LiveReloadClientDisconnectedEvent(Object source) {
		super(source);
	}

}
