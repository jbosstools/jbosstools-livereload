package org.jboss.tools.livereload.internal.server.configuration;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Andre Dietisheim
 */
public class LiveReloadWizardDialog extends WizardDialog {

	public LiveReloadWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 450);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		getButton(IDialogConstants.FINISH_ID).setText("Finish");
		return control;
	}

	protected void hideButton(Button button) {
		if (button != null) {
			button.setVisible(false);
			GridDataFactory.fillDefaults().exclude(true).applyTo(button);
		}
	}
}