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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.Wizard;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.WSTUtils;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadConfigurationWizard extends Wizard {

	private final LiveReloadConfigurationWizardModel wizardModel;

	public LiveReloadConfigurationWizard(final IFolder folder) {
		wizardModel = new LiveReloadConfigurationWizardModel(folder);
	}

	@Override
	public void addPages() {
		addPage(new LiveReloadConfigurationWizardPage(wizardModel));
	}

	@Override
	public boolean performFinish() {
		if (wizardModel.isCreateNewServer()) {
			try {
				WSTUtils.createLiveReloadServerWorkingCopy(getConfiguration());
			} catch (CoreException e) {
				Logger.error("Failed to create a new LiveReload Server", e);
				return false;
			}
		}
		return true;
	}

	public ILiveReloadConfiguration getConfiguration() {
		return wizardModel;
	}

}
