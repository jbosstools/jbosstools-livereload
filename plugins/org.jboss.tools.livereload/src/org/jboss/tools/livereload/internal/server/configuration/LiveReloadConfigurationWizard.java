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

package org.jboss.tools.livereload.internal.server.configuration;

import org.eclipse.jface.wizard.Wizard;

/**
 * @author xcoulon
 *
 */
public class LiveReloadConfigurationWizard extends Wizard {
	
	private final LiveReloadConfigurationWizardModel wizardModel = new LiveReloadConfigurationWizardModel();

	@Override
	public void addPages() {
		addPage(new LiveReloadConfigurationWizardPage(wizardModel));
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public ILiveReloadConfiguration getConfiguration() {
		return wizardModel;
	}

}
