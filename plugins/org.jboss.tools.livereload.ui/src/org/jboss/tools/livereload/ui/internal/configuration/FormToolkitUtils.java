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

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.jboss.tools.livereload.ui.internal.util.Logger;

/**
 * @author xcoulon
 * 
 */
public class FormToolkitUtils {

	/**
	 * <p>
	 * Creates a set of labels and hyperlinks that open the browser when parsing
	 * the given label which follows the Markdown syntax.
	 * </p>
	 * <p>
	 * For example:
	 * </p>
	 * <p>
	 * Here is a Markdown link to [Warped](http://warpedvisions.org)
	 * </p>
	 * 
	 * @param toolkit
	 * @param composite
	 * @param label
	 * @return
	 */
	public static Form createLabelWithHyperlinks(final FormToolkit toolkit, final Composite parent, final String label) {
		final Form form = toolkit.createForm(parent);
		GridLayout layout = new GridLayout();
		form.getBody().setLayout(layout);
		// parse the given label and extract hyperlinks' label and href
		int position = 0;
		while (position < label.length()) {
			int linkLabelOpenBracketPosition = label.indexOf('[', position);
			if (linkLabelOpenBracketPosition != -1) {
				form.setText(label.substring(position, linkLabelOpenBracketPosition));
				int linkLabelCloseBracketPosition = label.indexOf(']', linkLabelOpenBracketPosition);
				int linkHrefOpenBracketPosition = label.indexOf('(', linkLabelCloseBracketPosition);
				int linkHrefCloseBracketPosition = label.indexOf(')', linkHrefOpenBracketPosition);
				final String linkLabel = label.substring(linkLabelOpenBracketPosition + 1,
						linkLabelCloseBracketPosition);
				final String linkHref = label.substring(linkHrefOpenBracketPosition + 1, linkHrefCloseBracketPosition);
				Hyperlink link = toolkit.createHyperlink(form.getBody(), linkLabel, SWT.WRAP);
				link.addHyperlinkListener(new HyperlinkAdapter() {
					public void linkActivated(HyperlinkEvent event) {
						try {
							IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
							support.createBrowser(
									IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR,
									"org.eclipse.ui.browser", null, null).openURL(new URL(linkHref));
						} catch (Exception e) {
							Logger.error("Failed to Open Browser at URL: " + linkHref, e);
						}
					}
				});
				position = linkHrefCloseBracketPosition + 1;
			} else {
				position = label.length();
			}
		}
		return form;
	}

}
