/**
 * NOTE: This copyright does *not* cover user programs that use Hyperic
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 *
 *  Copyright (C) [2010-2012], VMware, Inc.
 *  This file is part of Hyperic.
 *
 *  Hyperic is free software; you can redistribute it and/or modify
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

package com.vmware.vfabric.hyperic.plugin.vfgf.detector;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.ServerResource;
import org.hyperic.hq.product.ServiceResource;
import org.hyperic.util.config.ConfigResponse;

import com.vmware.vfabric.hyperic.plugin.vfgf.GFMXConstants;
import com.vmware.vfabric.hyperic.plugin.vfgf.GFVersionInfo;
import com.vmware.vfabric.hyperic.plugin.vfgf.mx.GFJmxConnection;

public class JMXAgentDetector extends MemberDetector {
    /** The Constant log. */
    private final static Log log =
        LogFactory.getLog(JMXAgentDetector.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.hyperic.hq.product.AutoServerDetector#getServerResources(org.hyperic
     * .util.config.ConfigResponse)
     */
    public List<ServerResource> getServerResources(ConfigResponse config) throws PluginException {
        log.debug("[getServerResources] Detecting server resources for: " + getTypeInfo().getFormattedName() + " " + getTypeInfo().getVersion());
        log.debug("[getServerResources] Config used for detection: " + config);    

        List<ServerResource> servers = new ArrayList<ServerResource>();

        // We need to find the jmx agent and use it for discovery
        // first check if there's an agent running in underlying platform
        long pids[] = getAgentPids();
        // we can only discover settings if exactly one jmx agent process is running
        // since only one platform can be created.
        if(pids.length == 0) {
            log.debug("[getServerResources] No Gemfire JMX Agent processes detected");
            return servers;           
        } else if(pids.length > 1) {
            log.info("[getServerResources] Detected " + pids.length + " GF JMX Agent processes. Can continue only with 1 process.");
            return servers;
        }

        // now, try to find settings from running process
        String file = findAgentProperties(pids[0]);
        if (file != null) {
            Properties p = new Properties();
            try {
                p.load(new FileInputStream(file));

                String rmiPort = p.getProperty("rmi-port");
                String rmiBindAddress = p.getProperty("rmi-bind-address");
                log.debug("[getServerResources] rmi port:" + rmiPort);
                log.debug("[getServerResources] rmi address:" + rmiBindAddress);                 
                config.setValue("jmx.url", "service:jmx:rmi:///jndi/rmi://" + rmiBindAddress + ":" + rmiPort + "/jmxconnector");
            } catch (Exception e) {
                log.info("[getServerResources] Can't read Gemfire agent configuration.", e);
                return servers;
            }
        }

        // if jmx url doesn't exist, just bail out with empty server set
        if(config.getValue(GFMXConstants.CONF_JMX_URL) == null) {
            log.debug("[getServerResources] Unable to continue, " + GFMXConstants.CONF_JMX_URL + " is undefined");
            return servers;
        }

        GFJmxConnection gf = new GFJmxConnection(config);

        GFVersionInfo gfVersionInfo = gf.getVersionInfoFromAgent();
        if(gfVersionInfo == null) 
        {
            return servers;
        }
        if(!gfVersionInfo.isGFVersion(getTypeInfo().getVersion())) {
            return servers;
        }
        ServerResource server;
        server = createServerResource("");
        server.setName(getPlatformName() + " " + getTypeInfo().getName());
        setProductConfig(server, new ConfigResponse());
        setMeasurementConfig(server, config);
        servers.add(server);
        return servers;
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.hyperic.hq.product.ServerDetector#discoverServices(org.hyperic.util
     * .config.ConfigResponse)
     */
    @Override
    protected List<ServiceResource> discoverServices(ConfigResponse config) throws PluginException {
        return null;
    }
    
    @Override
    protected boolean hasCorrectRoles(int mask) {
        // Always return true since we're just detecting the agent
        return true;
    }

    @Override
    protected StatType[] filterSupportedStats(Object[][] statObjects, ConfigResponse config) {
        // The agent has no Stats
        return null;
    }
}