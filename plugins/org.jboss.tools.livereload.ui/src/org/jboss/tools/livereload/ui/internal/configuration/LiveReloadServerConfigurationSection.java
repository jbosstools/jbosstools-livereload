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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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

}
