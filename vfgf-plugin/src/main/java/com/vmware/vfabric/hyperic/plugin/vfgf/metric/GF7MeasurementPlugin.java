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

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.Metric;
import org.hyperic.hq.product.MetricNotFoundException;
import org.hyperic.hq.product.MetricUnreachableException;
import org.hyperic.hq.product.MetricValue;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.PluginManager;
import org.hyperic.hq.product.jmx.MxMeasurementPlugin;
import com.vmware.vfabric.hyperic.plugin.vfgf.util.JmxManagerFinder;


public class GF7MeasurementPlugin extends MxMeasurementPlugin {
    /** The Constant log. */
    private static final Log log =
        LogFactory.getLog(GF7MeasurementPlugin.class);

    /*
     * Here's what is going on:
     * 
     *  GF 7 Introduced a moving target jmx manager. By moving target
     *  I mean that the host of it can change therefore the jmx url can
     *  change. To workaround this we track *ONLY* a stable locator member
     *  on the platform level. That locator will identify the current jmx
     *  url. To improve performance we're caching the jmx url. If the url
     *  changes in the middle of collection those metrics will fail but we
     *  should have a usable url next time.
     *  Map is in the format of key -> value where key represents the platform
     *  which shouldn't change because ideally a single agent shouldn't host
     *  more than one DS
     */
    
    @Override
    public void init(PluginManager manager) throws PluginException {
        super.init(manager);

        // measurement plugin may be shared between same
        // resource types. we access it by unique id.
        //jmxUrl = new Hashtable<String, String>();
    }
    
    protected String getJmxUrl(String locators) {
        String url = JmxManagerFinder.getJmxUrl(locators);
        return url;
    }
    
    @Override
    public MetricValue getValue(Metric metric)
        throws PluginException,
               MetricNotFoundException,
               MetricUnreachableException
    {
        Properties props = metric.getProperties();
        String template = metric.toString();
        String locators = props.getProperty("locators");
        if(locators == null) {
            throw new PluginException("Locators not configured");
        }
        String locatorsEncoded = Metric.encode(locators);
        //log.debug("[getValue] template=" + template);
        //log.debug("[getValue] locators=" + locators);
        //log.debug("[getValue] encodedLocators=" + locatorsEncoded);
        String jmxUrl = getJmxUrl(locators);
        if(jmxUrl == null) {
            throw new MetricUnreachableException("[getValue] Unable to find jmx.url from " + locators);
        }
        // Replace locators= with jmx.url
        String newTemplate = StringUtils.replace(template, "locators=" + locatorsEncoded, "jmx.url=" + jmxUrl);
        //log.debug("[getValue] newTemplate=" + newTemplate);
        Metric newMetric = Metric.parse(newTemplate);
        //log.debug("[getValue] newMetric=" + newMetric.toString());
        return super.getValue(newMetric);
    }

}
