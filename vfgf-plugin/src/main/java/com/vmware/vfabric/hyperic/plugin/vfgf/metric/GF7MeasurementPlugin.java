package com.vmware.vfabric.hyperic.plugin.vfgf.metric;

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.Metric;
import org.hyperic.hq.product.MetricNotFoundException;
import org.hyperic.hq.product.MetricUnreachableException;
import org.hyperic.hq.product.MetricValue;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.jmx.MxMeasurementPlugin;
import org.hyperic.hq.product.jmx.MxUtil;

import com.vmware.vfabric.hyperic.plugin.vfgf.GFProductPlugin;
import com.vmware.vfabric.hyperic.plugin.vfgf.detector.GF7PlatformDetector;


public class GF7MeasurementPlugin extends MxMeasurementPlugin {
    /** The Constant log. */
    private static final Log log =
        LogFactory.getLog(GF7MeasurementPlugin.class);

    private static String PROP_LOCATORS = "locators";
    private static String JMX_USERNAME = "jmx.username";
    private static String JMX_PASSWORD = "jmx.password";
    
    private static String last_signature = new String();
    
    @Override
    public MetricValue getValue(Metric metric)
        throws PluginException,
               MetricNotFoundException,
               MetricUnreachableException
    {
        Properties props = metric.getProperties();
        String template = metric.toString();
        String locators = props.getProperty(PROP_LOCATORS);
        String jmxUsername = props.getProperty(JMX_USERNAME);
        String jmxPassword = props.getProperty(JMX_PASSWORD);

        if(locators == null) {
            throw new MetricUnreachableException("Locators not configured");
        }

        if(jmxUsername == null) {
            jmxUsername = "";
        }
        
        if(jmxPassword == null) {
            jmxPassword = "";
        }
        String locatorsEncoded = Metric.encode(locators);  // Need to be encoded
        String jmxUrl = GFProductPlugin.getJmxUrl(locators);

        if(jmxUrl.isEmpty()) {
            throw new MetricUnreachableException("Unable to find jmx.url from " + locators);
        }
        // Replace locators= with jmx.url
        String jmxConfig = "jmx.url=" + jmxUrl + ",jmx.username=" + jmxUsername + ",jmx.password=" + jmxPassword;
        log.debug("[getValue] jmxConfig=" + jmxConfig);
        String newTemplate = StringUtils.replace(template, "locators=" + locatorsEncoded, jmxConfig);
        Metric newMetric = Metric.parse(newTemplate);
        
        // See HHQ-2341. HQ doesn't run autoserverdetectors on remote platforms so this is a hack
        // Only run this for availability to avoid bogging down agent 
        if(newMetric.isAvail()) {
            Set<ObjectName> names = new HashSet<ObjectName>();
            String objName = "GemFire:type=Member,member=*";

            JMXConnector connector = null;
            MBeanServerConnection mServer;
            try {
                connector = MxUtil.getMBeanConnector(newMetric.getProperties());
                mServer = connector.getMBeanServerConnection();
                names = mServer.queryNames(new ObjectName(objName), null);
            } catch (MalformedObjectNameException e) {
                log.debug("[getServerResources] " + e.getMessage(), e);    
            } catch (IOException e) {
                log.debug("[getServerResources] " + e.getMessage(), e);
            }

            String signature = Arrays.asList(names).toString();
            if(!signature.equals(last_signature)) {
                last_signature=signature;
                log.debug("[getValue] Membership change detected. Forcing new auto discovery. Old=" + last_signature +
                    ", new=" +signature);
                GF7PlatformDetector.runAutoDiscovery(locators);
            }
        }
        MetricValue val;
        try {
            val =  super.getValue(newMetric);
        } catch (Exception e) {
            log.debug("[getValue] " + e.getMessage(), e);
            GFProductPlugin.resetJmxUrl();
            try {
                MxUtil.getMBeanConnector(metric.getProperties()).close();
            } catch (MalformedURLException e1) {
                throw new MetricUnreachableException(e1.getMessage(),e1);
            } catch (IOException e1) {
                throw new MetricUnreachableException(e1.getMessage(),e);
            }
            throw new MetricUnreachableException(e.getMessage(),e);
        }
        return val;
    }

}
