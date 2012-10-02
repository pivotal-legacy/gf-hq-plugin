package com.vmware.vfabric.hyperic.plugin.gemfire.detectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.hyperic.hq.product.AutoServerDetector;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.jmx.MxServerDetector;
import org.hyperic.hq.product.ServerResource;
import org.hyperic.hq.product.jmx.MxUtil;
import org.hyperic.util.config.ConfigResponse;

public class JMXAgentDetector extends MxServerDetector
        implements AutoServerDetector {

    Log log;

    public JMXAgentDetector() {
        this.log = getLog();
    }

    public List<ServerResource> getServerResources(ConfigResponse pc) throws PluginException {
        Properties props = getTypeProperties();
        this.log.debug("[getServerResources] props = " + props);
        List<ServerResource> servers = new ArrayList<ServerResource>();
        try {
            MBeanServerConnection mServer = MxUtil.getMBeanServer(props);
            ObjectName mbean = new ObjectName("GemFire:type=MemberInfoWithStatsMBean");
            String version = (String) mServer.getAttribute(mbean, "Version");
            String id = (String) mServer.getAttribute(mbean, "Id");
            boolean versionOK = version.startsWith(getTypeInfo().getVersion());
            this.log.debug("Agent version='" + version + " " + (versionOK ? "OK" : "") + " (" + getTypeInfo().getVersion() + ")");
            ServerResource server;
            if (versionOK) {
                server = createServerResource("");
                server.setName(getPlatformName() + " " + getTypeInfo().getName() + " " + id);
                server.setIdentifier(id);
                setProductConfig(server, new ConfigResponse());
                setMeasurementConfig(server, pc);
                servers.add(server);
            }

        } catch (Exception e) {
            throw new PluginException(e.getMessage(), e);
        }
        return servers;
    }
}