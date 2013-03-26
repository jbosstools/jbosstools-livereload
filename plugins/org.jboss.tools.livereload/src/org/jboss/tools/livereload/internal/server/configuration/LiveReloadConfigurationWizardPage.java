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

import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadConfigurationWizardModel.PROPERTY_NEW_SERVER_HTTP_PORT;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadConfigurationWizardModel.PROPERTY_NEW_SERVER_NAME;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadConfigurationWizardModel.PROPERTY_NEW_SERVER_WEBSOCKET_PORT;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizardMessages.CREATE_NEW_SERVER;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizardMessages.DESCRIPTION;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizardMessages.HTTP_SERVER_NAME_LABEL;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizardMessages.HTTP_SERVER_PORT_INVALID_VALUE;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizardMessages.HTTP_SERVER_PORT_LABEL;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizardMessages.SERVER_ALREADY_EXISTS;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizardMessages.SERVER_PORTS_DUPLICATE_VALUES;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizardMessages.TITLE;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizardMessages.USE_EXISTING_SERVER;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizardMessages.WEBSOCKET_SERVER_PORT_INVALID_VALUE;
import static org.jboss.tools.livereload.internal.server.configuration.LiveReloadLaunchWizardMessages.WEBSOCKET_SERVER_PORT_LABEL;

import java.net.URL;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.common.ui.databinding.ValueBindingBuilder;
import org.jboss.tools.livereload.internal.util.ImageRepository;
import org.jboss.tools.livereload.internal.util.Logger;
import org.jboss.tools.livereload.internal.util.WSTUtils;

/**
 * The Configuration Page where the user chooses to use an existing LiveReload
 * Server or to create a new one.
 * 
 * @author xcoulon
 * 
 */
public class LiveReloadConfigurationWizardPage extends WizardPage {

	private DataBindingContext dbc = null;

	private final LiveReloadConfigurationWizardModel wizardModel;

	private final List<IServer> existingServers = WSTUtils.retrieveLiveReloadServers();

	/**
	 * Constructor
	 * 
	 * @param wizardModel
	 */
	public LiveReloadConfigurationWizardPage(final LiveReloadConfigurationWizardModel wizardModel) {
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
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).margins(10, 10).applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(container);
		createNewServerCreationPanel(container);
		createExistingServerSelectionPanel(container);
		// connects validation status from the databinding context to wizard page
		WizardPageSupport.create(this, dbc);
	}

	/**
	 * Create a radio button followed by a panel with text widgets to create a
	 * new server.
	 * 
	 * @param parentContainer
	 *            the parent container
	 */
	private void createNewServerCreationPanel(final Composite parentContainer) {
		final Button createNewServerButton = new Button(parentContainer, SWT.RADIO);
		createNewServerButton.setText(CREATE_NEW_SERVER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(createNewServerButton);
		final IObservableValue createNewServerButtonObservable = WidgetProperties.selection().observe(
				createNewServerButton);
		final IObservableValue createNewServerModelObservable = BeanProperties.value(
				LiveReloadConfigurationWizardModel.PROPERTY_CREATE_NEW_SERVER).observe(wizardModel);
		ValueBindingBuilder.bind(createNewServerButtonObservable).to(createNewServerModelObservable).in(dbc);

		// Panel itself
		final Composite newServerCreationPanel = new Composite(parentContainer, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).margins(20, 0).applyTo(newServerCreationPanel);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(newServerCreationPanel);
		// Server name
		final Label serverNameLabel = new Label(newServerCreationPanel, SWT.NONE);
		serverNameLabel.setText(HTTP_SERVER_NAME_LABEL);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(serverNameLabel);
		final Text serverNameText = new Text(newServerCreationPanel, SWT.BORDER);
		wizardModel.setNewServerName(WSTUtils.generateDefaultServerName());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(serverNameText);
		final IObservableValue serverNameTextObservable = WidgetProperties.text(SWT.Modify).observe(serverNameText);
		final IObservableValue serverNameModelObservable = BeanProperties.value(PROPERTY_NEW_SERVER_NAME).observe(
				wizardModel);
		ValueBindingBuilder.bind(serverNameTextObservable).to(serverNameModelObservable).in(dbc);

		// HTTP port
		final Label httpPortLabel = new Label(newServerCreationPanel, SWT.NONE);
		httpPortLabel.setText(HTTP_SERVER_PORT_LABEL);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(httpPortLabel);
		final Text httpPortText = new Text(newServerCreationPanel, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(httpPortText);
		final IObservableValue httpPortTextObservable = WidgetProperties.text(SWT.Modify).observe(httpPortText);
		final IObservableValue httpPortModelObservable = BeanProperties.value(PROPERTY_NEW_SERVER_HTTP_PORT).observe(
				wizardModel);
		ValueBindingBuilder.bind(httpPortTextObservable).converting(new StringToIntegerConverter())
				.to(httpPortModelObservable).converting(new IntegerToStringConverter()).in(dbc);

		// WebSockets port
		final Label websocketPortLabel = new Label(newServerCreationPanel, SWT.NONE);
		websocketPortLabel.setText(WEBSOCKET_SERVER_PORT_LABEL);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(websocketPortLabel);
		final Text websocketPortText = new Text(newServerCreationPanel, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(websocketPortText);
		final IObservableValue websocketPortTextObservable = WidgetProperties.text(SWT.Modify).observe(
				websocketPortText);
		final IObservableValue websocketPortModelObservable = BeanProperties.value(PROPERTY_NEW_SERVER_WEBSOCKET_PORT)
				.observe(wizardModel);
		ValueBindingBuilder.bind(websocketPortTextObservable).converting(new StringToIntegerConverter())
				.to(websocketPortModelObservable).converting(new IntegerToStringConverter()).in(dbc);

		// react to button (de)selection
		createNewServerModelObservable.addValueChangeListener(onValueChanged(newServerCreationPanel));

		// panel validation
		addValidator(new ServerNameValidator(createNewServerModelObservable, serverNameTextObservable));
		addValidator(new ServerHttpPortValidator(createNewServerModelObservable, httpPortTextObservable));
		addValidator(new ServerWebSocketPortValidator(createNewServerModelObservable, websocketPortTextObservable));
		addValidator(new ServerPortsValidator(createNewServerModelObservable, httpPortTextObservable, websocketPortTextObservable));

		// initial state
		if (!wizardModel.isCreateNewServer()) {
			swapWidgetsState(newServerCreationPanel);
		}
	}

	/**
	 * Create a radio button followed by a panel containing the existing
	 * servers.
	 * 
	 * @param parentContainer
	 *            the parent container
	 */
	private void createExistingServerSelectionPanel(final Composite parentContainer) {
		// Panel activation button
		final Button useExistingServerButton = new Button(parentContainer, SWT.RADIO);
		useExistingServerButton.setText(USE_EXISTING_SERVER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(useExistingServerButton);
		final IObservableValue useExistingServerButtonObservable = WidgetProperties.selection().observe(
				useExistingServerButton);

		// Panel itself
		final Composite selectExistingServerPanel = new Composite(parentContainer, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(selectExistingServerPanel);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).margins(20, 0)
				.applyTo(selectExistingServerPanel);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(selectExistingServerPanel);
		// final Label existingServersLabel = new
		// Label(selectExistingServerPanel, SWT.NONE);
		// existingServersLabel.setText(SELECT_SERVER);
		// GridDataFactory.fillDefaults().align(SWT.LEFT,
		// SWT.CENTER).grab(false, false).applyTo(existingServersLabel);

		final Table existingServersTable = new Table(selectExistingServerPanel, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.V_SCROLL | SWT.H_SCROLL);
		final TableViewer existingServersTableViewer = new TableViewer(existingServersTable);
		existingServersTableViewer.setContentProvider(new ArrayContentProvider());
		existingServersTableViewer.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				IServer server = (IServer) cell.getElement();
				cell.setText(server.getName());
				cell.setImage(ImageRepository.LIVE_RELOAD_SERVER_ICON.createImage());
			}
		});
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(300, 350)
				.applyTo(existingServersTable);
		final IObservableValue selectedServerModelObservable = BeanProperties.value(
				LiveReloadConfigurationWizardModel.PROPERTY_SELECTED_SERVER).observe(wizardModel);
		dbc.bindValue(ViewerProperties.singleSelection().observe(existingServersTableViewer),
				selectedServerModelObservable);

		existingServersTableViewer.setInput(existingServers);
		if (!existingServers.isEmpty()) {
			wizardModel.setSelectedServer(existingServers.get(0));
		}
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(selectExistingServerPanel);
		// Add listener to react to enablement state change
		useExistingServerButtonObservable.addValueChangeListener(onValueChanged(selectExistingServerPanel));
		// initial state
		if (wizardModel.isCreateNewServer()) {
			swapWidgetsState(selectExistingServerPanel);
		}
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

	/**
	 * Creates and returns an anonymous implementation of a
	 * {@link IValueChangeListener} for the given {@link Composite}
	 * 
	 * @param container
	 * @return
	 */
	private IValueChangeListener onValueChanged(final Composite container) {
		return new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
				swapWidgetsState(container);
			}
		};
	}

	/**
	 * Switch the {@link Control#isEnabled()} status for all the children
	 * control of the given {@link Composite}. Furthermore, if the controls
	 * become enabled, the fist one gets the focus.
	 * 
	 * @param container
	 */
	private void swapWidgetsState(final Composite container) {
		boolean focusDone = false;
		for (Control control : container.getChildren()) {
			control.setEnabled(!control.isEnabled());
			if (!focusDone && control.isEnabled() && (control instanceof Text || control instanceof Table)) {
				control.setFocus();
				focusDone = true;
			}
		}
	}
	
	/**
	 * Adds the given {@link ValidationStatusProvider} to this Wizard Page's
	 * {@link DataBindingContext}
	 * 
	 * @param validator
	 *            the validator to add.
	 */
	private void addValidator(final ValidationStatusProvider validator) {
		dbc.addValidationStatusProvider(validator);
		ControlDecorationSupport.create(validator, SWT.LEFT | SWT.TOP, null,
				new RequiredControlDecorationUpdater(false));
		
	}
	
	/**
	 * Validator to check that the given Server Name is not empty or null and
	 * does not match an existing server.
	 * 
	 * @author xcoulon
	 * 
	 */
	static class ServerNameValidator extends MultiValidator {

		private final IObservableValue createNewServerModelObservable;
		private final IObservableValue serverNameModelObservable;

		public ServerNameValidator(final IObservableValue createNewServerModelObservable,
				final IObservableValue serverNameModelObservable) {
			this.createNewServerModelObservable = createNewServerModelObservable;
			this.serverNameModelObservable = serverNameModelObservable;
		}

		@Override
		protected IStatus validate() {
			final boolean createNewServer = (Boolean) createNewServerModelObservable.getValue();
			final String serverName = (String) serverNameModelObservable.getValue();
			if (createNewServer && serverName != null && !serverName.isEmpty() && WSTUtils.serverExists(serverName)) {
				return ValidationStatus.error(SERVER_ALREADY_EXISTS);
			}
			return ValidationStatus.ok();
		}
	}

	/**
	 * Validator to check that the HTTP Port of the Server to create is a number.
	 * @author xcoulon
	 *
	 */
	static class ServerHttpPortValidator extends MultiValidator {

		private final IObservableValue createNewServerModelObservable;
		private final IObservableValue httpPortObservable;

		public ServerHttpPortValidator(final IObservableValue createNewServerModelObservable,
				final IObservableValue httpPortObservable) {
			this.createNewServerModelObservable = createNewServerModelObservable;
			this.httpPortObservable = httpPortObservable;
		}

		@Override
		protected IStatus validate() {
			final boolean createNewServer = (Boolean) createNewServerModelObservable.getValue();
			final int httpPortValue = toInt((String)httpPortObservable.getValue());
			if (createNewServer && (httpPortValue < 0 || httpPortValue > 65535)) {
				return ValidationStatus.error(HTTP_SERVER_PORT_INVALID_VALUE);
			}
			return ValidationStatus.ok();
		}
	}

	/**
	 * Validator to check that the WebSocket Port of the Server to create is a number.
	 * @author xcoulon
	 *
	 */
	static class ServerWebSocketPortValidator extends MultiValidator {

		private final IObservableValue createNewServerModelObservable;
		private final IObservableValue websocketPortObservable;

		public ServerWebSocketPortValidator(final IObservableValue createNewServerModelObservable,
				final IObservableValue websocketPortObservable) {
			this.createNewServerModelObservable = createNewServerModelObservable;
			this.websocketPortObservable = websocketPortObservable;
		}

		@Override
		protected IStatus validate() {
			final boolean createNewServer = (Boolean) createNewServerModelObservable.getValue();
			final int websocketPortValue = toInt((String)websocketPortObservable.getValue());
			if (createNewServer && (websocketPortValue < 0 || websocketPortValue > 65535)) {
				return ValidationStatus.error(WEBSOCKET_SERVER_PORT_INVALID_VALUE);
			}
			return ValidationStatus.ok();
		}
	}

	/**
	 * Validator to check that the HTTP Port and the WebSocket Port of the Server to create are different.
	 * @author xcoulon
	 *
	 */
	static class ServerPortsValidator extends MultiValidator {

		private final IObservableValue createNewServerModelObservable;
		private final IObservableValue httpPortObservable;
		private final IObservableValue websocketPortObservable;

		public ServerPortsValidator(final IObservableValue createNewServerModelObservable,
				final IObservableValue httpPortObservable, final IObservableValue websocketPortObservable) {
			this.createNewServerModelObservable = createNewServerModelObservable;
			this.httpPortObservable = httpPortObservable;
			this.websocketPortObservable = websocketPortObservable;
		}

		@Override
		protected IStatus validate() {
			final boolean createNewServer = (Boolean) createNewServerModelObservable.getValue();
			final String httpPortValue = (String) httpPortObservable.getValue();
			final String websocketPortValue = (String) websocketPortObservable.getValue();
			if (createNewServer && httpPortValue.equals(websocketPortValue)) {
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
			return toInt((String) fromObject);
		}

		
	}

	/**
	 * Converter from String to IServer
	 * 
	 * @author xcoulon
	 * 
	 */
	static class StringToServerConverter extends Converter {

		public StringToServerConverter() {
			super(String.class, IServer.class);
		}

		@Override
		public Object convert(Object fromObject) {
			if (fromObject instanceof IServer) {
				try {
					return ((IServer) fromObject).getName();
				} catch (NumberFormatException e) {

				}
			}
			return null;
		}
	}

	/**
	 * Converter from IServer to String
	 * 
	 * @author xcoulon
	 * 
	 */
	static class ServerToStringConverter extends Converter {

		public ServerToStringConverter() {
			super(IServer.class, String.class);
		}

		@Override
		public Object convert(Object fromObject) {
			if (fromObject instanceof String) {
				try {
					// FIXME
					return null;
				} catch (NumberFormatException e) {

				}
			}
			return null;
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
	
	/**
	 * Converts the given value into an integer.
	 * @param value
	 * @return the Int value or <code>-1</code> if the given parameter cannot be converted into an integer
	 */
	private static int toInt(String value) {
		if (value instanceof String) {
			try {
				return new Integer((String) value);
			} catch (NumberFormatException e) {

			}
		}
		return new Integer(-1);
	}
}
