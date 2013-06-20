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

package org.jboss.tools.livereload.core.internal.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author xcoulon
 *
 */
public class TimeoutUtils {
	
	/**
	 * Verifies that the given monitor is verified within the given time.
	 * @param monitor: some custom implementation
	 * @param duration: the timeout duration
	 * @param unit: the duration unit
	 * @return true if the monitor#verify() method did not return true within the given time
	 */
	public static boolean timeout(final TaskMonitor monitor, final long duration, final TimeUnit unit) throws TimeoutException {
		ExecutorService executor = Executors.newCachedThreadPool();
		Future<?> future = executor.submit(new Runnable() {
			@Override
			public void run() {
				final Long limitTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(duration, unit);
				while(!monitor.isComplete() && System.currentTimeMillis() < limitTime) {
					try {
						TimeUnit.MILLISECONDS.sleep(500);
					} catch (InterruptedException e) {
						throw new TimeoutException(e);
					}
				}
			}
		});
		try { 
			future.get(duration, unit);
			// did not timeout
			return false;
		} catch (Exception e) {
			// handle the timeout
			// handle the interrupts
			// handle other exceptions
			Logger.error("Operation failed to complete within expected time", e);
			return true;
		} finally {
			future.cancel(true); 
		} 
	}

	public static abstract class TaskMonitor {
		
		public abstract boolean isComplete();
	}
}
