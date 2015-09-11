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
package com.vmware.vfabric.hyperic.plugin.vfgf.detector;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.agent.AgentCommand;
import org.hyperic.hq.agent.AgentRemoteValue;
import org.hyperic.hq.agent.server.AgentDaemon;
import org.hyperic.hq.autoinventory.ScanConfigurationCore;
import org.hyperic.hq.autoinventory.agent.AICommandsAPI;
import org.hyperic.hq.product.PlatformDetector;
import org.hyperic.hq.product.PlatformResource;
import org.hyperic.hq.product.PluginException;
import org.hyperic.util.config.ConfigResponse;

public class GF7PlatformDetector extends PlatformDetector {

    private static Log log = LogFactory.getLog(GF7PlatformDetector.class);
    private static Map<String, ConfigResponse> configs = new HashMap();

    @Override
    public PlatformResource getPlatformResource(ConfigResponse config) throws PluginException {
        log.debug("[getPlatformResource] config=" + config);
        try {
            String id = config.getValue("locators");
            configs.put(id, config);
        } catch (Exception e) {
            throw new PluginException(e.getMessage(), e);
        }
        log.debug("[getPlatformResource] configs=" + configs);
        PlatformResource res = super.getPlatformResource(config);
        return res;
    }

    public static void runAutoDiscovery(String id) {
        log.debug("[runAutoDiscovery] Starting auto discovery for id=" + id);
        log.debug("[runAutoDiscovery] configs=" + configs);
        
        try {
            ScanConfigurationCore scanConfig = new ScanConfigurationCore();
            ConfigResponse c = configs.get(id);
            if (c != null) {
                scanConfig.setConfigResponse(c);
                AgentRemoteValue configARV = new AgentRemoteValue();
                scanConfig.toAgentRemoteValue(AICommandsAPI.PROP_SCANCONFIG, configARV);
                AgentCommand ac = new AgentCommand(1, 1, "autoinv:startScan", configARV);
                AgentDaemon.getMainInstance().getCommandDispatcher().processRequest(ac, null, null);
                log.info("[runAutoDiscovery] id=" + id + " << OK");
            } else {
                log.debug("[runAutoDiscovery] Config not found for id=" + id);
            }
        } catch (Exception ex) {
            log.error("[runAutoDiscovery] id=" + id + " " + ex, ex);
        }
    }
}