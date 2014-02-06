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

package org.jboss.tools.livereload.ui.internal.configuration;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadLaunchConfiguration;

/**
 * @author xcoulon
 * 
 */
@SuppressWarnings("restriction")
public class LiveReloadServerConfigurationSection extends ServerEditorSection {

	private Text websocketPortText;
	private Button remoteConnectionsEnablementButton;
	private Button scriptInjectionEnablementButton;
	private Spinner notificationDelaySpinner;
	private ControlDecoration websocketPortDecoration;

	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
				| ExpandableComposite.TITLE_BAR);
		section.setText(LiveReloadServerConfigurationMessages.WEBSOCKET_SERVER_CONFIGURATION_TITLE);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		Composite composite = toolkit.createComposite(section);
		composite.setLayout(new GridLayout(2, false));
		Label explanation = toolkit.createLabel(composite,
				LiveReloadServerConfigurationMessages.WEBSOCKET_SERVER_CONFIGURATION_DESCRIPTION, SWT.WRAP);
		GridData d = new GridData();
		d.horizontalSpan = 2;
		d.grabExcessHorizontalSpace = true;
		explanation.setLayoutData(d);
		
		// Websocket port
		Label websocketPortLabel = toolkit.createLabel(composite,
				LiveReloadServerConfigurationMessages.WEBSOCKET_SERVER_PORT_LABEL);
		websocketPortLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		websocketPortText = toolkit.createText(composite,
				server.getAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, ""));
		d = new GridData();
		d.grabExcessHorizontalSpace = true;
		d.widthHint = 100;
		d.horizontalIndent = 10;
		websocketPortText.setLayoutData(d);
		websocketPortDecoration = new ControlDecoration(websocketPortText, SWT.LEFT | SWT.TOP);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldDecorationRegistry.DEC_ERROR);
		websocketPortDecoration.hide();
		websocketPortDecoration.setImage(fieldDecoration.getImage());
		ModifyListener websocketPortModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetWebSocketPortCommand(server));
			}
		};
		websocketPortText.addModifyListener(websocketPortModifyListener);
		
		// notification delay (in seconds)
		Label notificationDelayLabel = toolkit.createLabel(composite,
				LiveReloadServerConfigurationMessages.SET_NOTIFICATION_DELAY_LABEL);
		notificationDelayLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		notificationDelaySpinner = new Spinner(composite, SWT.BORDER);
		notificationDelaySpinner.setSelection(server.getAttribute(LiveReloadLaunchConfiguration.NOTIFICATION_DELAY,
				LiveReloadLaunchConfiguration.DEFAULT_NOTIFICATION_DELAY));
		notificationDelaySpinner.setMinimum(0);
		notificationDelaySpinner.setMaximum(60);
		notificationDelaySpinner.setToolTipText(LiveReloadServerConfigurationMessages.SET_NOTIFICATION_DELAY_TOOLTIP);
		d = new GridData();
		d.grabExcessHorizontalSpace = true;
		d.widthHint = 20;
		d.horizontalIndent = 10;
		notificationDelaySpinner.setLayoutData(d);
		
		notificationDelaySpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetServerNotificationDelayCommand(server));
			}
		});
				
		// livereload.js script injection enablement
		scriptInjectionEnablementButton = toolkit.createButton(composite,
				LiveReloadServerConfigurationMessages.ENABLE_SCRIPT_INJECTION_LABEL, SWT.CHECK);
		scriptInjectionEnablementButton.setSelection(server.getAttribute(
				LiveReloadLaunchConfiguration.ENABLE_SCRIPT_INJECTION, false));
		scriptInjectionEnablementButton.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		d = new GridData();
		d.grabExcessHorizontalSpace = true;
		d.horizontalSpan = 2;
		scriptInjectionEnablementButton.setLayoutData(d);
		scriptInjectionEnablementButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				execute(new SetScriptInjectionEnablementButtonCommand(server));
			}
		});

		// Remote connections enablement
		remoteConnectionsEnablementButton = toolkit.createButton(composite,
				LiveReloadServerConfigurationMessages.ALLOW_REMOTE_CONNECTIONS_LABEL, SWT.CHECK);
		remoteConnectionsEnablementButton.setSelection(server.getAttribute(
				LiveReloadLaunchConfiguration.ALLOW_REMOTE_CONNECTIONS, false));
		remoteConnectionsEnablementButton.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		d = new GridData();
		d.grabExcessHorizontalSpace = true;
		d.horizontalSpan = 2;
		remoteConnectionsEnablementButton.setLayoutData(d);
		remoteConnectionsEnablementButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				execute(new SetRemoteConnectionsEnablementCommand(server));
			}
		});
		
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
	}

	@Override
	public IStatus[] getSaveStatus() {
		return new IStatus[] { Status.OK_STATUS };
		// return new IStatus[] { new Status(IStatus.ERROR,
		// JBossLiveReloadCoreActivator.PLUGIN_ID, "Data is invalid") };
	}

	public class SetWebSocketPortCommand extends ServerCommand {

		public SetWebSocketPortCommand(IServerWorkingCopy server) {
			super(server, LiveReloadServerConfigurationMessages.WEBSOCKET_SERVER_PORT_COMMAND);
		}

		@Override
		public void execute() {
			server.setAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, websocketPortText.getText());
			validate();
		}

		@Override
		public void undo() {
			final String originalValue = server.getOriginal().getAttribute(
					LiveReloadLaunchConfiguration.WEBSOCKET_PORT, "");
			server.setAttribute(LiveReloadLaunchConfiguration.WEBSOCKET_PORT, originalValue);
			websocketPortText.setText(originalValue);
			validate();
		}

		/**
		 * Shows an error decorator if the value cannot be parsed into an
		 * Integer
		 */
		private void validate() {
			try {
				Integer.parseInt(websocketPortText.getText());
				websocketPortDecoration.hide();
			} catch (NumberFormatException e) {
				websocketPortDecoration.show();
			}

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
			server.setAttribute(LiveReloadLaunchConfiguration.ENABLE_SCRIPT_INJECTION,
					scriptInjectionEnablementButton.getSelection());
		}

		@Override
		public void undo() {
			final boolean originalValue = server.getOriginal().getAttribute(
					LiveReloadLaunchConfiguration.ENABLE_SCRIPT_INJECTION, false);
			server.setAttribute(LiveReloadLaunchConfiguration.ENABLE_SCRIPT_INJECTION, originalValue);
			scriptInjectionEnablementButton.setSelection(originalValue);
		}

	}

	public class SetServerNotificationDelayCommand extends ServerCommand {
		
		public SetServerNotificationDelayCommand(final IServerWorkingCopy server) {
			super(server, LiveReloadServerConfigurationMessages.SET_NOTIFICATION_DELAY_COMMAND);
		}
		
		@Override
		public void execute() {
			server.setAttribute(LiveReloadLaunchConfiguration.NOTIFICATION_DELAY,
					notificationDelaySpinner.getSelection());
		}
		
		@Override
		public void undo() {
			final int originalValue = server.getOriginal().getAttribute(
					LiveReloadLaunchConfiguration.NOTIFICATION_DELAY, 0);
			server.setAttribute(LiveReloadLaunchConfiguration.NOTIFICATION_DELAY, originalValue);
			notificationDelaySpinner.setSelection(originalValue);
		}
		
	}

}
