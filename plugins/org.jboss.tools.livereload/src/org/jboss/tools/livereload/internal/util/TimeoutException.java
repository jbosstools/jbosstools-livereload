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

package org.jboss.tools.livereload.internal.util;

/**
 * @author xcoulon
 *
 */
public class TimeoutException extends RuntimeException {

	/** serialVersionUID. */
	private static final long serialVersionUID = 6886135443815248089L;

	/**
	 * Public constructor
	 * @param cause
	 */
	public TimeoutException(Exception cause) {
		super(cause);
	}
	
}
