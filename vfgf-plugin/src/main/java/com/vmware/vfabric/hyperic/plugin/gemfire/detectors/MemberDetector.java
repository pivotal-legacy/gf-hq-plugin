package com.vmware.vfabric.hyperic.plugin.gemfire.detectors;

import com.vmware.vfabric.hyperic.plugin.gemfire.GemFireUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.hyperic.hq.product.AutoServerDetector;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.ServerDetector;
import org.hyperic.hq.product.ServerResource;
import org.hyperic.hq.product.ServiceResource;
import org.hyperic.hq.product.jmx.MxUtil;
import org.hyperic.util.config.ConfigResponse;

public abstract class MemberDetector extends ServerDetector implements AutoServerDetector {

    Log log = getLog();

    public List<ServerResource> getServerResources(ConfigResponse pc) throws PluginException {
        Properties props = getTypeProperties();
        this.log.debug("[getServerResources] props = " + props);
        List<ServerResource> servers = new ArrayList<ServerResource>();
        Map<String, String> names = new HashMap<String, String>();
        try {
            MBeanServerConnection mServer = MxUtil.getMBeanServer(props);
            List<String> members=GemFireUtils.getMembers(mServer);
            log.debug("[getServerResources] members=" + Arrays.asList(members));
            for (String memberID : members) {
                Map<?, ?> memberDetails = GemFireUtils.getMemberDetails(memberID, mServer);
                if (log.isDebugEnabled()) {
                    log.debug("[getServerResources] memberDetails=" + memberDetails);
                }

                if (isValidMember(memberDetails)) {
                    String name = (String) memberDetails.get("gemfire.member.name.string");
                    log.debug("Discovered member " + memberID + " named " + name);
                    if (names.get(name) != null) {
                        log.error("[getServerResources] There is 2 of more '" + getTypeInfo().getName() + "' with the same name '" + name + "'");
                    }
                    names.put(name, name);
                    ObjectName mbean = new ObjectName("GemFire:type=MemberInfoWithStatsMBean");
                    String version = (String) mServer.getAttribute(mbean, "Version");
                    boolean versionOK = version.startsWith(getTypeInfo().getVersion());
                    this.log.debug("Agent version='" + version + " " + (versionOK ? "OK" : "") + " (" + getTypeInfo().getVersion() + ")");
                    if (versionOK) {
                        ServerResource server = createServerResource("");
                        server.setName(getPlatformName() + " " + getTypeInfo().getName() + " " + name);
                        server.setIdentifier("GFDS server.name " + name);
                        ConfigResponse c = new ConfigResponse();
                        c.setValue("member.name", name);
                        setMeasurementConfig(server, c);
                        setProductConfig(server, new ConfigResponse());
                        setCustomProperties(server, getAtributtes(memberDetails));
                        servers.add(server);
                    }
                }
            }

        } catch (Exception ex) {
            throw new PluginException(ex.getMessage(), ex);
        }
        return servers;
    }

    @Override
    protected List discoverServices(ConfigResponse config) throws PluginException {
        log.debug("[discoverServices] config=" + config);
        List services = new ArrayList();
        try {
            MBeanServerConnection mServer = MxUtil.getMBeanServer(config.toProperties());
            String memberID = GemFireUtils.memberNameToMemberID(config.getValue("member.name"), mServer);
            Map memberDetails = GemFireUtils.getMemberDetails(memberID, mServer);
            Map<String, Map> regions = (Map) memberDetails.get("gemfire.member.regions.map");

            if (regions != null) {
                for (Map region : regions.values()) {
                    String name = (String) region.get("gemfire.region.name.string");
                    ServiceResource service = createServiceResource("Region");
                    service.setName(memberDetails.get("gemfire.member.name.string") + " Region " + name);
                    ConfigResponse c = new ConfigResponse();
                    c.setValue("regionID", name);
                    log.debug("[discoverServices] region -> c=" + c);

                    ConfigResponse attr = new ConfigResponse();
                    attr.setValue("name", (String) region.get("gemfire.region.name.string"));
                    attr.setValue("path", (String) region.get("gemfire.region.path.string"));
                    attr.setValue("scope", (String) region.get("gemfire.region.scope.string"));
                    attr.setValue("datapolicy", (String) region.get("gemfire.region.datapolicy.string"));
                    attr.setValue("interestpolicy", (String) region.get("gemfire.region.interestpolicy.string"));
                    attr.setValue("diskattrs", (String) region.get("gemfire.region.diskattrs.string"));

                    setMeasurementConfig(service, c);
                    service.setCustomProperties(attr);
                    services.add(service);
                }
            }

            List<Map> gateways = (List) memberDetails.get("gemfire.member.gatewayhub.gateways.collection");
            if (gateways != null) {
                for (Map gateway : gateways) {
                    String id = (String) gateway.get("gemfire.member.gateway.id.string");
                    ServiceResource service = createServiceResource("Gateway");
                    service.setName(memberDetails.get("gemfire.member.name.string") + " Gateway " + id);

                    ConfigResponse c = new ConfigResponse();
                    c.setValue("gatewayID", id);
                    log.debug("[discoverServices] gateway -> c=" + c);

                    setProductConfig(service, new ConfigResponse());
                    setMeasurementConfig(service, c);
                    services.add(service);
                }
            }

        } catch (Exception ex) {
            throw new PluginException(ex.getMessage(), ex);
        }
        return services;
    }

    abstract boolean isValidMember(Map memberDetails);

    abstract ConfigResponse getAtributtes(Map memberDetails);
}
