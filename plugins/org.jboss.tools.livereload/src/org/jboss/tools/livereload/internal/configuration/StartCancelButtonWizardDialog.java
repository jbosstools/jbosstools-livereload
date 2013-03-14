package org.jboss.tools.livereload.internal.configuration;

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
public class StartCancelButtonWizardDialog extends WizardDialog {

	public StartCancelButtonWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 400);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		getButton(IDialogConstants.FINISH_ID).setText("Start");
		return control;
	}

	protected void hideButton(Button button) {
		if (button != null) {
			button.setVisible(false);
			GridDataFactory.fillDefaults().exclude(true).applyTo(button);
		}
	}
}