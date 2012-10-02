package com.vmware.vfabric.hyperic.plugin.gemfire.collectors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.Collector;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.jmx.MxUtil;

import com.vmware.vfabric.hyperic.plugin.gemfire.GemFireLiveData;
import com.vmware.vfabric.hyperic.plugin.gemfire.GemFireUtils;
import com.vmware.vfabric.hyperic.plugin.gemfire.detectors.GemfirePlatformDetector;

public class JMXAgentCollector extends Collector {

    static Log log = LogFactory.getLog(JMXAgentCollector.class);
    String last_signature = "";

    @Override
    protected void init() throws PluginException {
        Properties props = getProperties();
        log.debug("[init] props=" + props);
        super.init();
    }

    @Override
    public void collect() {
        int a = 0;
        int c = 0;
        int g = 0;

        Properties props = getProperties();
        try {
            MBeanServerConnection mServer = MxUtil.getMBeanServer(props);
            ObjectName obj = new ObjectName("GemFire:type=Agent");
            mServer.getAttribute(obj, "version");
            String id=GemFireLiveData.getSystemID(mServer);

            List<String> members=GemFireUtils.getMembers(mServer);
            String signature = Arrays.asList(members).toString();
            if (!signature.equals(last_signature)) {
                last_signature=signature;
                GemfirePlatformDetector.runAutoDiscovery(id);
                GemFireUtils.clearNameCache();
            }

            for (String memberID : members) {
                Map<?, ?> memberDetails = GemFireUtils.getMemberDetails(memberID, mServer);
                if ("true".equalsIgnoreCase(memberDetails.get("gemfire.member.isgateway.boolean").toString())) {
                    ++g;
                } else if ("true".equalsIgnoreCase(memberDetails.get("gemfire.member.isserver.boolean").toString())) {
                    ++c;
                } else {
                    ++a;
                }
            }
            setAvailability(true);
            setValue("n_gateways", new Double(g).doubleValue());
            setValue("n_apps", new Double(a).doubleValue());
            setValue("n_caches", new Double(c).doubleValue());

        } catch (Exception ex) {
            setAvailability(false);
            log.debug(ex, ex);
        }
    }    
}
