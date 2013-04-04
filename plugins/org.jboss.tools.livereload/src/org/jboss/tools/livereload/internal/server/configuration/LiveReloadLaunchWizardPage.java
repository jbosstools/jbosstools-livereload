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

import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadServerConfigurationMessages.DESCRIPTION;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadServerConfigurationMessages.HTTP_PROXY_SERVER_CHECKBOX;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadServerConfigurationMessages.HTTP_PROXY_SERVER_DESCRIPTION;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadServerConfigurationMessages.HTTP_PROXY_SERVER_PORT_INVALID_VALUE;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadServerConfigurationMessages.HTTP_PROXY_SERVER_PORT_LABEL;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadServerConfigurationMessages.SERVER_PORTS_DUPLICATE_VALUES;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadServerConfigurationMessages.TITLE;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadServerConfigurationMessages.WEBSOCKET_SERVER_PORT_INVALID_VALUE;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadServerConfigurationMessages.WEBSOCKET_SERVER_PORT_LABEL;

import java.net.URL;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
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

	private final LiveReloadLaunchWizardModel wizardModel;

	private DataBindingContext dbc = null;

	/**
	 * Constructor
	 * 
	 * @param wizardModel
	 */
	public LiveReloadLaunchWizardPage(final LiveReloadLaunchWizardModel wizardModel) {
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
		ValueBindingBuilder.bind(websocketPortTextObservable).converting(new StringToIntegerConverter())
				.to(websocketPortModelObservable).converting(new IntegerToStringConverter()).in(dbc);

		// Proxy Server enablement
		final Button useProxyServerBtn = new Button(container, SWT.CHECK);
		useProxyServerBtn.setText(HTTP_PROXY_SERVER_CHECKBOX);
		GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, SWT.CENTER).grab(false, false)
				.applyTo(useProxyServerBtn);
		final IObservableValue useProxyServerModelObservable = BeanProperties.value(
				LiveReloadLaunchWizardModel.PROPERTY_USE_HTTP_PROXY_SERVER).observe(wizardModel);
		final IObservableValue useProxyServerSelection = WidgetProperties.selection().observe(useProxyServerBtn);
		dbc.bindValue(useProxyServerSelection, useProxyServerModelObservable);

		// Proxy Server port
		final Label proxyPortLabel = new Label(container, SWT.NONE);
		proxyPortLabel.setText(HTTP_PROXY_SERVER_PORT_LABEL);
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).indent(10, 0).applyTo(proxyPortLabel);
		final Text proxyPortText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(proxyPortText);
		final IObservableValue proxyPortModelObservable = BeanProperties.value(
				LiveReloadLaunchWizardModel.PROPERTY_HTTP_PROXY_SERVER_PORT).observe(wizardModel);
		final IObservableValue proxyPortTextObservable = WidgetProperties.text(SWT.Modify).observe(proxyPortText);
		ValueBindingBuilder.bind(WidgetProperties.enabled().observe(proxyPortText))
				.notUpdating(useProxyServerModelObservable).in(dbc);
		ValueBindingBuilder.bind(proxyPortTextObservable).converting(new StringToIntegerConverter())
				.to(proxyPortModelObservable).converting(new IntegerToStringConverter()).in(dbc);

		// Proxy Server brief description
		final Link proxyServerDescription = new Link(container, SWT.WRAP);
		proxyServerDescription.setText(HTTP_PROXY_SERVER_DESCRIPTION);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).hint(100, SWT.DEFAULT).indent(00, 10).span(2, 1)
				.applyTo(proxyServerDescription);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(6, 6).applyTo(container);
		proxyServerDescription.addSelectionListener(onBrowse());

		// validation
		final ServerPortsValidator validationStatusProvider = new ServerPortsValidator(websocketPortModelObservable,
				useProxyServerModelObservable, proxyPortModelObservable);
		dbc.addValidationStatusProvider(validationStatusProvider);
		ControlDecorationSupport.create(
				validationStatusProvider, SWT.LEFT | SWT.TOP, null, new RequiredControlDecorationUpdater(false));
		WizardPageSupport.create(this, dbc);
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
			final int websocketPortValue = (Integer) websocketPortObservable.getValue();
			final boolean useProxyServerValue = (Boolean) useProxyServerSelection.getValue();
			final int proxyServerPortValue = (Integer) proxyServerPortObservable.getValue();
			if (websocketPortValue == -1) {
				return ValidationStatus.error(WEBSOCKET_SERVER_PORT_INVALID_VALUE);
			}
			if (useProxyServerValue && proxyServerPortValue == -1) {
				return ValidationStatus.error(HTTP_PROXY_SERVER_PORT_INVALID_VALUE);
			}
			if (useProxyServerValue && proxyServerPortValue == websocketPortValue) {
				return ValidationStatus.error(SERVER_PORTS_DUPLICATE_VALUES);
			}
			return ValidationStatus.ok();
		}

	}

	/**
	 * Converter from String to Integer
	 * 
	 * @author xcoulon
	 * 
	 */
	static class StringToIntegerConverter extends Converter {

		public StringToIntegerConverter() {
			super(String.class, Integer.class);
		}

		@Override
		public Object convert(Object fromObject) {
			if (fromObject instanceof String) {
				try {
					return new Integer((String) fromObject);
				} catch (NumberFormatException e) {

				}
			}
			return new Integer(-1);
		}
	}

	/**
	 * Converter from Integer to String
	 * 
	 * @author xcoulon
	 * 
	 */
	static class IntegerToStringConverter extends Converter {

		public IntegerToStringConverter() {
			super(Integer.class, String.class);
		}

		@Override
		public Object convert(Object fromObject) {
			if (fromObject instanceof Integer) {
				return fromObject.toString();
			}
			return "";
		}

	}
}
