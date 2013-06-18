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

package org.jboss.tools.livereload.ui.internal.command;

import org.jboss.tools.common.databinding.ObservablePojo;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;

/**
 * Simple {@link ObservablePojo} that handles the state of the 2 checkboxes that
 * will be prompted to the user in {@link CreateLiveReloadServerMessageDialog}
 * and {@link ConfigureLiveReloadServerMessageDialog}
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadServerConfigurationDialogModel extends ObservableUIPojo {

	public static final String PROPERTY_SCRIPT_INJECTION_ENABLED = "scriptInjectionEnabled";

	public static final String PROPERTY_REMOTE_CONNECTIONS_ALLOWED = "remoteConnectionsAllowed";

	/** Script injection enabled (default to true). */
	private boolean scriptInjectionEnabled = true;

	/** Remote connections allowed (default to false). */
	private boolean remoteConnectionsAllowed = false;

	/**
	 * Constructor with default values
	 */
	public LiveReloadServerConfigurationDialogModel() {
		super();
	}
	
	/**
	 * Constructor with specific values
	 * 
	 * @param scriptInjectionEnabled
	 * @param remoteConnectionsAllowed
	 */
	public LiveReloadServerConfigurationDialogModel(final boolean scriptInjectionEnabled,
			final boolean remoteConnectionsAllowed) {
		super();
		this.scriptInjectionEnabled = scriptInjectionEnabled;
		this.remoteConnectionsAllowed = remoteConnectionsAllowed;
	}

	/**
	 * @return the scriptInjectionEnabled
	 */
	public boolean isScriptInjectionEnabled() {
		return scriptInjectionEnabled;
	}

	/**
	 * @param scriptInjectionEnabled
	 *            the scriptInjectionEnabled to set
	 */
	public void setScriptInjectionEnabled(boolean enableScriptInjection) {
		firePropertyChange(PROPERTY_SCRIPT_INJECTION_ENABLED, this.scriptInjectionEnabled,
				this.scriptInjectionEnabled = enableScriptInjection);
	}

	/**
	 * @return the remoteConnectionsAllowed
	 */
	public boolean isRemoteConnectionsAllowed() {
		return remoteConnectionsAllowed;
	}

	/**
	 * @param remoteConnectionsAllowed
	 *            the remoteConnectionsAllowed to set
	 */
	public void setRemoteConnectionsAllowed(boolean allowRemoteConnections) {
		firePropertyChange(PROPERTY_REMOTE_CONNECTIONS_ALLOWED, this.remoteConnectionsAllowed,
				this.remoteConnectionsAllowed = allowRemoteConnections);
	}

}
