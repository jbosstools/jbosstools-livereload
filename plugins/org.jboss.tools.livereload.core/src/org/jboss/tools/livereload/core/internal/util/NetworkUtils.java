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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Network Utility Class
 * 
 * @author xcoulon
 * 
 */
public class NetworkUtils {

	/**
	 * Private constructor for the utility class
	 */
	private NetworkUtils() {
	}

	/**
	 * Retrieves all {@link InetAddress} for the running VM, indexed by their name.
	 * @return all {@link InetAddress} for the running VM.
	 * @throws SocketException
	 */
	public static Map<String, InetAddress> retrieveNetworkInterfaces() throws SocketException {
		final Map<String, InetAddress> namedAddresses = new HashMap<String, InetAddress>();
		final Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			final Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
			for (InetAddress inetAddress : Collections.list(inetAddresses)) {
				if((inetAddress instanceof Inet6Address) || inetAddress.isAnyLocalAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isMulticastAddress()
						) {
					continue;
				}
				namedAddresses.put(netint.getDisplayName(), inetAddress);
			}
		}
		return namedAddresses;
	}
}
