package com.xirpc.common;

import com.xicp.util.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: URL
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
public class URL implements Serializable {

    private static final long serialVersionUID = -1985165475234910535L;

    private final String protocol;

    private final String host;

    private final int port;

    private final String path;

    private final Map<String, String> parameters;


    protected URL() {
        this.protocol = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
    }

    public URL(String protocol, String host, int port, String path) {
        this(protocol, host, port, path, (Map<String, String>) null);
    }

    public URL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this.protocol = protocol;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        this.path = path;
        if (parameters == null) {
            parameters = new HashMap<>();
        } else {
            parameters = new HashMap<>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public URL setProtocol(String protocol) {
        return new URL(protocol, host, port, path, getParameters());
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public static URL valueOf(String url) {
        url = url.replaceAll("\\$_\\$", "/");
        url = url.replaceAll("%2F", "/");
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = url.substring(0, url.indexOf("://"));
        url = url.replace(protocol + "://", "");
        String host = url.substring(0, url.indexOf(":"));
        url = url.replace(host + ":", "");
        String ps = url.substring(0, url.indexOf("/"));
        int port = ps == null? 0 : ps.equals("null")? 0 : Integer.valueOf(ps);
        url = url.replace(ps + "/", "");
        String path = url.substring(0, url.contains(":")? url.lastIndexOf(":") : url.length());
        URL u = new URL(protocol, host, port, path);
        return u;
    }

    @Override
    public String toString() {
        String url = protocol + "://" + host + ":" + port + "/" + path;
        url = url.replaceAll("/", "%2F");
        return url;
    }

    public static void main(String[] args) {
        String url = "xicp://192.168.0.0:20880/XiRpc/provider/com.experience.api.HiXiRpc";
        System.out.println(url);
        url = url.replaceAll("/", "%2F");
        System.out.println(url);
        url = url.replaceAll("%2F", "/");
        System.out.println(url);
    }
}
