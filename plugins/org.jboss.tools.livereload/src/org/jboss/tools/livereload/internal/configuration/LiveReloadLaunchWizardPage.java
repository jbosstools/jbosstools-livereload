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

import static org.jboss.tools.livereload.internal.configuration.LiveReloadLaunchWizardMessages.DESCRIPTION;
import static org.jboss.tools.livereload.internal.configuration.LiveReloadLaunchWizardMessages.PROXY_SERVER_CHECKBOX;
import static org.jboss.tools.livereload.internal.configuration.LiveReloadLaunchWizardMessages.PROXY_SERVER_DESCRIPTION;
import static org.jboss.tools.livereload.internal.configuration.LiveReloadLaunchWizardMessages.PROXY_SERVER_PORT_DUPLICATE_VALUE;
import static org.jboss.tools.livereload.internal.configuration.LiveReloadLaunchWizardMessages.PROXY_SERVER_PORT_INVALID_VALUE;
import static org.jboss.tools.livereload.internal.configuration.LiveReloadLaunchWizardMessages.PROXY_SERVER_PORT_LABEL;
import static org.jboss.tools.livereload.internal.configuration.LiveReloadLaunchWizardMessages.TITLE;
import static org.jboss.tools.livereload.internal.configuration.LiveReloadLaunchWizardMessages.WEBSOCKET_SERVER_PORT_INVALID_VALUE;
import static org.jboss.tools.livereload.internal.configuration.LiveReloadLaunchWizardMessages.WEBSOCKET_SERVER_PORT_LABEL;

import java.net.URL;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.jboss.tools.common.ui.databinding.ValueBindingBuilder;
import org.jboss.tools.livereload.internal.util.ImageRepository;
import org.jboss.tools.livereload.internal.util.Logger;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadLaunchWizardPage extends WizardPage {

	private final ILiveReloadWebServerConfiguration wizardModel;

	private DataBindingContext dbc = null;

	/**
	 * Constructor
	 * 
	 * @param wizardModel
	 */
	public LiveReloadLaunchWizardPage(final ILiveReloadWebServerConfiguration wizardModel) {
		super("LiveReload Configuration");
		this.wizardModel = wizardModel;
		setTitle(TITLE);
		setDescription(DESCRIPTION);
		setImageDescriptor(ImageRepository.LIVE_RELOAD_SERVER_LAUNCH);
	}

	@Override
	public void createControl(Composite parent) {
		dbc = new DataBindingContext();
		Composite container = new Composite(parent, SWT.NONE);
		setControl(container);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).margins(10, 10).applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(container);

		// Web sockets port
		final Label websocketPortLabel = new Label(container, SWT.NONE);
		websocketPortLabel.setText(WEBSOCKET_SERVER_PORT_LABEL);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(websocketPortLabel);
		final Text websocketPortText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(websocketPortText);
		final IObservableValue websocketPortTextObservable = WidgetProperties.text(SWT.Modify).observe(
				websocketPortText);
		final IObservableValue websocketPortModelObservable = BeanProperties.value(
				LiveReloadLaunchWizardModel.PROPERTY_WEBSOCKET_SERVER_PORT).observe(wizardModel);
		ValueBindingBuilder.bind(websocketPortTextObservable).to(websocketPortModelObservable).in(dbc);

		// Proxy Server enablement
		final Button useProxyServerBtn = new Button(container, SWT.CHECK);
		useProxyServerBtn.setText(PROXY_SERVER_CHECKBOX);
		GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, SWT.CENTER).grab(false, false)
				.applyTo(useProxyServerBtn);
		final IObservableValue useProxyServerModelObservable = BeanProperties.value(
				LiveReloadLaunchWizardModel.PROPERTY_USE_PROXY_SERVER).observe(wizardModel);
		final IObservableValue useProxyServerSelection = WidgetProperties.selection().observe(useProxyServerBtn);
		dbc.bindValue(useProxyServerSelection, useProxyServerModelObservable);

		// Proxy Server port
		final Label proxyPortLabel = new Label(container, SWT.NONE);
		proxyPortLabel.setText(PROXY_SERVER_PORT_LABEL);
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).indent(10, 0).applyTo(proxyPortLabel);
		final Text proxyPortText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(proxyPortText);
		final IObservableValue proxyPortTextObservable = WidgetProperties.text(SWT.Modify).observe(proxyPortText);
		final IObservableValue proxyPortModelObservable = BeanProperties.value(
				LiveReloadLaunchWizardModel.PROPERTY_PROXY_SERVER_PORT).observe(wizardModel);
		ValueBindingBuilder.bind(proxyPortTextObservable).to(proxyPortModelObservable).in(dbc);
		ValueBindingBuilder.bind(WidgetProperties.enabled().observe(proxyPortText))
				.notUpdating(useProxyServerModelObservable).in(dbc);

		// Proxy Server brief description
		final Link proxyServerDescription = new Link(container, SWT.WRAP);
		proxyServerDescription.setText(PROXY_SERVER_DESCRIPTION);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).hint(100, SWT.DEFAULT).indent(00, 10).span(2, 1)
				.applyTo(proxyServerDescription);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(6, 6).applyTo(container);
		proxyServerDescription.addSelectionListener(onBrowse());

		// validation
		final ServerPortsValidator validationStatusProvider = new ServerPortsValidator(websocketPortTextObservable,
				useProxyServerSelection, proxyPortTextObservable);
		dbc.addValidationStatusProvider(validationStatusProvider);

	}

	private SelectionListener onBrowse() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IWebBrowser browser;
				try {
					browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
					browser.openURL(new URL("http://livereload.com"));
				} catch (Exception exception) {
					Logger.error("Failed to open browser", exception);
				}
			}
		};
	}

	static class ServerPortsValidator extends MultiValidator {

		private final IObservableValue websocketPortObservable;
		private final IObservableValue proxyServerPortObservable;
		private final IObservableValue useProxyServerSelection;

		public ServerPortsValidator(final IObservableValue websocketPortObservable,
				final IObservableValue useProxyServerSelection, final IObservableValue proxyServerPortObservable) {
			this.websocketPortObservable = websocketPortObservable;
			this.useProxyServerSelection = useProxyServerSelection;
			this.proxyServerPortObservable = proxyServerPortObservable;
		}

		@Override
		protected IStatus validate() {
			final int websocketPortValue = toInt((String) websocketPortObservable.getValue());
			final boolean useProxyServerValue = (Boolean) useProxyServerSelection.getValue();
			final int proxyServerPortValue = toInt((String) proxyServerPortObservable.getValue());
			if (websocketPortValue == -1) {
				return ValidationStatus.error(WEBSOCKET_SERVER_PORT_INVALID_VALUE);
			}
			if (useProxyServerValue && proxyServerPortValue == -1) {
				return ValidationStatus.error(PROXY_SERVER_PORT_INVALID_VALUE);
			}
			if (useProxyServerValue && proxyServerPortValue == websocketPortValue) {
				return ValidationStatus.error(PROXY_SERVER_PORT_DUPLICATE_VALUE);
			}
			return ValidationStatus.ok();
		}

		private static int toInt(String value) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {

			}
			return -1;
		}

	}
}
