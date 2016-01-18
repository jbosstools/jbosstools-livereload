/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.livereload.ui.internal.command;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.IServerModule;
import org.jboss.tools.livereload.core.internal.util.WSTUtils;
import org.jboss.tools.livereload.ui.internal.util.Logger;
import org.jboss.tools.livereload.ui.internal.util.Pair;

public class LaunchLiveReloadServerCommandHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final Object target = getTarget(event);
		// skip if no target
		if (target == null) {
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "LiveReload", "Current selection cannot be opened in the Web browser.");
				}
			});
			return null;
		}
		try {
			final boolean shouldEnableScriptInjection = true;
			final boolean shouldAllowRemoteConnections = false;
			final Pair<IServer, Boolean> result = OpenInWebBrowserViaLiveReloadUtils
					.getLiveReloadServer(shouldEnableScriptInjection, shouldAllowRemoteConnections);
			if (result != null) {
				final IServer liveReloadServer = result.left;
				final boolean needsStartOrRestart = result.right;
				if (needsStartOrRestart) {
					final Job startOrRestartJob = WSTUtils.startOrRestartServer(liveReloadServer, 30, TimeUnit.SECONDS);
					startOrRestartJob.addJobChangeListener(new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							if (event.getResult().isOK()) {
								openInWebBrowser(target, liveReloadServer);
							}
						}
					});
					startOrRestartJob.schedule();
				} else {
					openInWebBrowser(target, liveReloadServer);
				}
			}
		} catch (CoreException | TimeoutException | InterruptedException | java.util.concurrent.ExecutionException e) {
			Logger.error("Failed to open current selection in a Web browser", e);
		}

		return null;
	}

	/**
	 * Returns the target element to display in the Web browser, depending on
	 * the current selection or the ative editor. This can be an
	 * {@link IServerModule} or an {@link IFile} or <code>null</code>.
	 * 
	 * @param event
	 *            the {@link ExecutionEvent} that triggered to call to this
	 *            command handler.
	 * @return the target to open in the browser or <code>null</code> if not
	 *         applicable.
	 */
	private Object getTarget(final ExecutionEvent event) {
		final ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		final IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		if (currentSelection instanceof IStructuredSelection) {
			final Object selectedElement = ((IStructuredSelection) currentSelection).getFirstElement();
			if (selectedElement instanceof IServerModule || selectedElement instanceof IFile) {
				return selectedElement;
			}
		} else if (activeEditor instanceof AbstractTextEditor) {
			final AbstractTextEditor textEditor = (AbstractTextEditor) activeEditor;
			final IEditorInput textEditorInput = textEditor.getEditorInput();
			if (textEditorInput instanceof FileEditorInput) {
				return ((FileEditorInput) textEditorInput).getFile();
			}
		}
		return null;
	}

	private void openInWebBrowser(final Object target, final IServer liveReloadServer) {
		if (target instanceof IServerModule) {
			OpenInWebBrowserViaLiveReloadUtils.openInWebBrowser((IServerModule) target);
		} else if (target instanceof IFile) {
			OpenInWebBrowserViaLiveReloadUtils.openInWebBrowser(((IFile) target).getLocation(), liveReloadServer);
		}
	}

}
