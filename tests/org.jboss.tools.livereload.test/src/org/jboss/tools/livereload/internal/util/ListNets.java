package org.jboss.tools.livereload.internal.util;

import static java.lang.System.out;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class ListNets {

    public static void main(String args[]) throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets))
            displayInterfaceInformation(netint);
    }

    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        out.printf("Display name: %s\n", netint.getDisplayName());
        out.printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            out.printf("InetAddress: %s (loopback	: %s / anylocal: %s / linklocal: %s / multicast: %s)\n ", inetAddress, inetAddress.isLoopbackAddress(), inetAddress.isAnyLocalAddress(), inetAddress.isLinkLocalAddress(), inetAddress.isMulticastAddress());
        }
        out.printf("\n");
     }
}  