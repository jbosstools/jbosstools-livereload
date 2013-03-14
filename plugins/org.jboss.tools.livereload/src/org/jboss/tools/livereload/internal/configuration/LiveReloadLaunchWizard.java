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

package org.jboss.tools.livereload.internal.configuration;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.wst.server.core.IServer;

/**
 * @author xcoulon
 *
 */
public class LiveReloadLaunchWizard extends Wizard {

	private ILiveReloadWebServerConfiguration wizardModel;

	public LiveReloadLaunchWizard(IServer server) {
		this.wizardModel = new LiveReloadLaunchWizardModel();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		addPage(new LiveReloadLaunchWizardPage(wizardModel));
	}

	public ILiveReloadWebServerConfiguration getConfiguration() {
		return wizardModel;
	}

}
