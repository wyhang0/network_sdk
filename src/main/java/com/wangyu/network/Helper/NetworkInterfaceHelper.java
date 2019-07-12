package com.wangyu.network.Helper;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetworkInterfaceHelper {
    public static List<InetAddress> getBroadcastAddresses(String networkInterfaceName) {
        List<InetAddress> addresses = new ArrayList<>();
        try {
            Enumeration<?> netInterfaces = (Enumeration<?>) NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) netInterfaces.nextElement();
                if (!netInterface.isLoopback() && netInterface.isUp()
                        && (networkInterfaceName == null || networkInterfaceName == "" || netInterface.getName().contains(networkInterfaceName))) {
                    List<InterfaceAddress> interfaceAddresses = netInterface.getInterfaceAddresses();
                    for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                        //只有 IPv4 网络具有广播地址，因此对于 IPv6 网络将返回 null。
                        if (interfaceAddress.getBroadcast() != null) {
                            addresses.add(interfaceAddress.getBroadcast());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addresses;
    }
}
