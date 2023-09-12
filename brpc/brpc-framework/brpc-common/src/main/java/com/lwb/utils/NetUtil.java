package com.lwb.utils;

import com.lwb.exceptions.NetWorkException;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@Slf4j
public class NetUtil {
    public static String getIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // 过滤掉非回环接口和虚拟接口
                if (iface.isLoopback() || iface.isVirtual() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 过滤掉IPV6地址和回环地址
                    if (addr instanceof Inet6Address || addr.isLoopbackAddress()) {
                        continue;
                    }
                    String ipAddress = addr.getHostAddress();
                    if(log.isDebugEnabled()) {
                        log.debug("局域网IP地址：{}", ipAddress);
                    }
                    return ipAddress;
                }
            }
            throw new NetWorkException();
        }catch (SocketException e) {
            log.error("获取局域网IP时发生异常", e);
            throw new NetWorkException(e);
        }
    }

    public static void main(String[] args) {
        String ip = NetUtil.getIp();
        System.out.println("ip = " + ip);
    }
}
