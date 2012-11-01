package com.vmware.vfabric.hyperic.plugin.vfgf.util;

/**
 * Describes the location of a jmx manager. If a jmx manager does not exist then
 * port will be 0.
 * @author darrel
 * 
 */
public class JmxManagerInfo {
    private String host;
    private int port;
    private boolean ssl;
    
    JmxManagerInfo(String host, int port, boolean ssl) {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
    }

    /**
     * The host/address the jmx manager is listening on.
     */
    public final String getHost() {
        return host;
    }
    /**
     * The port the jmx manager is listening on.
     */
    public final int getPort() {
        return port;
    }
    /**
     * True if the jmx manager is using SSL.
     */
    public final boolean getSsl() {
        return ssl;
    }
}
