package com.xirpc.common;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @description: UrlUtil
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
public class UrlUtil {

    public static InetSocketAddress urlToInetSocketAddress(URL url) {
        if (url == null ){
            return null;
        }
        String host = url.getHost();
        int port = url.getPort();
        return new InetSocketAddress(host, port);
    }

    public static URL strToUrl(String protocol, String host, int port, String provider) throws Exception {
        URL url = new URL(protocol, host, port, provider);
        return url;
    }

    public static void main(String[] args) throws Exception {
        //URL url = strToUrl("xirpc", "localhost", 20880, "com.xirpc.config.ServiceBean");

        //URL url = URL.valueOf("xirpc://localhost:20880/com.xirpc.config.ServiceBean");
        URL url = URL.valueOf("xirpc:%2F%2F192.168.56.1:20880%2Fcom.experience.api.HiXiRpc:0");
        System.out.println(url.toString());
        System.out.println(url.getHost());
        System.out.println(url.getPort());
        System.out.println(url.getPath());
    }

    public static Set<URL> strToUrls(List<String> children) {
        Set<URL> urls = new HashSet<>();
        if (children == null) {
            return urls;
        }
        for (String urlStr : children) {
            URL url = URL.valueOf(urlStr);
            urls.add(url);
        }
        return urls;
    }

    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5}$");
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    public static String ANYHOST_VALUE = "0.0.0.0";

    public static String LOCALHOST_KEY = "localhost";

    public static String LOCALHOST_VALUE = "127.0.0.1";

    public static boolean isInvalidLocalHost(String host) {
        return host == null
                || host.length() == 0
                || host.equalsIgnoreCase(LOCALHOST_KEY)
                || host.equals(ANYHOST_VALUE)
                || (LOCAL_IP_PATTERN.matcher(host).matches());
    }
}
