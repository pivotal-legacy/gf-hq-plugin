/**
 * Copyright (C) [2010-2015], Pivotal Software, Inc.
 *
 *  This is free software; you can redistribute it and/or modify
 *  it under the terms version 2 of the GNU General Public License as
 *  published by the Free Software Foundation. This program is distributed
 *  in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE. See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *
 */
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
