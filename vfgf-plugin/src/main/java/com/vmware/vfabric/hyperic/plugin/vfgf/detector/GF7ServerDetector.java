package com.vmware.vfabric.hyperic.plugin.vfgf.detector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.ServerResource;
import org.hyperic.hq.product.jmx.MxQuery;
import org.hyperic.hq.product.jmx.MxServerDetector;
import org.hyperic.hq.product.jmx.MxUtil;
import org.hyperic.util.config.ConfigResponse;

import com.vmware.vfabric.hyperic.plugin.vfgf.GFProductPlugin;

public class GF7ServerDetector
    extends MxServerDetector {
    
    private static final Log log = LogFactory.getLog(GF7ServerDetector.class);
    
    private static String PROP_LOCATORS = "locators";
    
    @Override    
    public List<ServerResource> getServerResources(ConfigResponse platformConfig) throws PluginException {
        List<ServerResource> servers = new ArrayList<ServerResource>();
        String locators = platformConfig.getValue(PROP_LOCATORS);

        if (locators == null || locators.isEmpty()) {
            log.debug("[getServerResources] No Locators configured.");
            return servers;
        }
        
        String url = GFProductPlugin.getJmxUrl(locators);
        if (url.isEmpty()) {
            return servers;
        }
        platformConfig.setValue("jmx.url", url);
        log.debug("[getServerResources] platformConfig=" + platformConfig);
        log.debug("[getServerResources] url="+url);
        
        JMXConnector connector = null;
        MBeanServerConnection mServer;
    
        try {
            connector = MxUtil.getMBeanConnector(platformConfig.toProperties());
            mServer = connector.getMBeanServerConnection();
        } catch (IOException e) {
            GFProductPlugin.resetJmxUrl();
            throw new PluginException(e.getMessage(), e);
        } catch (Exception e) {
            MxUtil.close(connector);
            throw new PluginException(e.getMessage(), e);
        }

        String objName = getTypeProperty(MxQuery.PROP_OBJECT_NAME);
        log.debug("[getServerResources] objName="+objName);

        Set<ObjectName> names = new HashSet<ObjectName>();
        try {
            names = mServer.queryNames(new ObjectName(objName), null);
        } catch (MalformedObjectNameException e) {
            log.debug("[getServerResources] " + e.getMessage(), e);    
        } catch (IOException e) {
            log.debug("[getServerResources] " + e.getMessage(), e);
        }
        getLog().debug("[getServerResources] discovered " + names.size() + " servers");

        for (Iterator<ObjectName> it = names.iterator(); it.hasNext();) {
            ObjectName name = (ObjectName)it.next();
            ServerResource server = createServerResource(name.toString());
            
            ConfigResponse config = new ConfigResponse();
            ConfigResponse cprops = new ConfigResponse();
            ConfigResponse controlConfig = new ConfigResponse();
            
            config.setValue("member", name.getKeyProperty("member"));
            String port = name.getKeyProperty("port");
            log.debug("[getServerResources] port=" + port);
            if (port != null) {
                config.setValue("port", port);
            }
            //config.setValue("jmx.url", "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxconnector");
            config.setValue("locators", locators);
            server.setName(getPlatformName() + " " + getTypeInfo().getName() + " " + name.getKeyProperty("member"));
            server.setControlConfig(controlConfig);
            setProductConfig(server, config);
            setMeasurementConfig(server, new ConfigResponse());
            server.setCustomProperties(cprops);

            servers.add(server);
        }
        return servers;
    }
    
    @Override
    protected List discoverServices(ConfigResponse serverConfig)
        throws PluginException {
        String locators = serverConfig.getValue(PROP_LOCATORS);
        
        if (locators == null || locators.isEmpty()) {
            throw new PluginException("[getServerResources] No Locators found");
        }
        
        String url = GFProductPlugin.getJmxUrl(locators);
        if (url.isEmpty()) {
            throw new PluginException("Unable to determine jmx url from locators");
        }
        serverConfig.setValue("jmx.url", url);
        return super.discoverServices(serverConfig);
    }
}
