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

package org.jboss.tools.livereload.internal.server.wst.configuration;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.tools.livereload.internal.server.wst.LiveReloadLaunchConfiguration;

/**
 * @author xcoulon
 * 
 */
@SuppressWarnings("restriction")
public class LiveReloadProxyServerConfigurationSection extends ServerEditorSection {

	private Button proxyEnablementButton;
	private Button remoteConnectionsEnablementButton;
	private Button scriptInjectionEnablementButton;

	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
				| ExpandableComposite.TITLE_BAR);
		section.setText(LiveReloadServerConfigurationMessages.PROXY_SERVER_CONFIGURATION_TITLE);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);

		composite.setLayout(new GridLayout(2, false));
		Label explanation = toolkit.createLabel(composite,
				LiveReloadServerConfigurationMessages.PROXY_CONFIGURATION_DESCRIPTION, SWT.WRAP);
		GridData d = new GridData();
		d.horizontalSpan = 2;
		d.grabExcessHorizontalSpace = true;
		explanation.setLayoutData(d);

		// Proxy Server enablement
		proxyEnablementButton = toolkit.createButton(composite,
				LiveReloadServerConfigurationMessages.ENABLE_PROXY_SERVER_LABEL, SWT.CHECK);
		proxyEnablementButton.setSelection(server.getAttribute(LiveReloadLaunchConfiguration.ENABLE_PROXY_SERVER, false));
		proxyEnablementButton.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		d = new GridData();
		d.grabExcessHorizontalSpace = true;
		d.horizontalSpan = 2;
		proxyEnablementButton.setLayoutData(d);
		proxyEnablementButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				execute(new SetProxyServerEnablementCommand(server));

			}
		});
		// Remote connections enablement
		remoteConnectionsEnablementButton = toolkit.createButton(composite,
				LiveReloadServerConfigurationMessages.ALLOW_REMOTE_CONNECTIONS_LABEL, SWT.CHECK);
		remoteConnectionsEnablementButton.setSelection(server.getAttribute(LiveReloadLaunchConfiguration.ALLOW_REMOTE_CONNECTIONS, false));
		remoteConnectionsEnablementButton.setEnabled(proxyEnablementButton.getSelection());
		remoteConnectionsEnablementButton.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		d = new GridData();
		d.grabExcessHorizontalSpace = true;
		d.horizontalIndent = 20;
		d.horizontalSpan = 2;
		remoteConnectionsEnablementButton.setLayoutData(d);
		remoteConnectionsEnablementButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				execute(new SetRemoteConnectionsEnablementCommand(server));
			}
		});
		// livereload.js script injection enablement
		scriptInjectionEnablementButton = toolkit.createButton(composite,
				LiveReloadServerConfigurationMessages.ENABLE_SCRIPT_INJECTION_LABEL, SWT.CHECK);
		scriptInjectionEnablementButton.setSelection(server.getAttribute(LiveReloadLaunchConfiguration.ENABLE_SCRIPT_INJECTION, false));
		scriptInjectionEnablementButton.setEnabled(proxyEnablementButton.getSelection());
		scriptInjectionEnablementButton.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		d = new GridData();
		d.grabExcessHorizontalSpace = true;
		d.horizontalIndent = 20;
		d.horizontalSpan = 2;
		scriptInjectionEnablementButton.setLayoutData(d);
		scriptInjectionEnablementButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				execute(new SetScriptInjectionEnablementButtonCommand(server));
			}
		});
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
		
		
		
	}

	@Override
	public IStatus[] getSaveStatus() {
		return new IStatus[] { Status.OK_STATUS };
		// return new IStatus[] { new Status(IStatus.ERROR,
		// LiveReloadActivator.PLUGIN_ID, "Data is invalid") };
	}

	public class SetProxyServerEnablementCommand extends ServerCommand {

		public SetProxyServerEnablementCommand(final IServerWorkingCopy server) {
			super(server, LiveReloadServerConfigurationMessages.ENABLE_PROXY_SERVER_COMMAND);
		}

		@Override
		public void execute() {
			server.setAttribute(LiveReloadLaunchConfiguration.ENABLE_PROXY_SERVER, proxyEnablementButton.getSelection());
			remoteConnectionsEnablementButton.setEnabled(proxyEnablementButton.getSelection());
			scriptInjectionEnablementButton.setEnabled(proxyEnablementButton.getSelection());
		}

		@Override
		public void undo() {
			final boolean originalValue = server.getOriginal().getAttribute(
					LiveReloadLaunchConfiguration.ENABLE_PROXY_SERVER, false);
			server.setAttribute(LiveReloadLaunchConfiguration.ENABLE_PROXY_SERVER, originalValue);
			proxyEnablementButton.setSelection(originalValue);
		}

	}

	public class SetRemoteConnectionsEnablementCommand extends ServerCommand {

		public SetRemoteConnectionsEnablementCommand(final IServerWorkingCopy server) {
			super(server, LiveReloadServerConfigurationMessages.ALLOW_REMOTE_CONNECTIONS_COMMAND);
		}

		@Override
		public void execute() {
			server.setAttribute(LiveReloadLaunchConfiguration.ALLOW_REMOTE_CONNECTIONS,
					remoteConnectionsEnablementButton.getSelection());
		}

		@Override
		public void undo() {
			final boolean originalValue = server.getOriginal().getAttribute(
					LiveReloadLaunchConfiguration.ALLOW_REMOTE_CONNECTIONS, false);
			server.setAttribute(LiveReloadLaunchConfiguration.ALLOW_REMOTE_CONNECTIONS, originalValue);
			remoteConnectionsEnablementButton.setSelection(originalValue);
		}

	}

	public class SetScriptInjectionEnablementButtonCommand extends ServerCommand {

		public SetScriptInjectionEnablementButtonCommand(final IServerWorkingCopy server) {
			super(server, LiveReloadServerConfigurationMessages.ENABLE_SCRIPT_INJECTION_COMMAND);
		}

		@Override
		public void execute() {
			server.setAttribute(LiveReloadLaunchConfiguration.ENABLE_SCRIPT_INJECTION, scriptInjectionEnablementButton.getSelection());
		}

		@Override
		public void undo() {
			final boolean originalValue = server.getOriginal().getAttribute(
					LiveReloadLaunchConfiguration.ENABLE_SCRIPT_INJECTION, false);
			server.setAttribute(LiveReloadLaunchConfiguration.ENABLE_SCRIPT_INJECTION, originalValue);
			scriptInjectionEnablementButton.setSelection(originalValue);
		}

	}

}
