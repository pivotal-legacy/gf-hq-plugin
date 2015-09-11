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
package com.vmware.vfabric.hyperic.plugin.vfgf.metric;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.vmware.vfabric.hyperic.plugin.vfgf.GFMXConstants;
import com.vmware.vfabric.hyperic.plugin.vfgf.GFProductPlugin;
import com.vmware.vfabric.hyperic.plugin.vfgf.cache.MemberCache;
import com.vmware.vfabric.hyperic.plugin.vfgf.cache.MemberInfo;
import com.vmware.vfabric.hyperic.plugin.vfgf.mx.GFJmxConnection;
import org.hyperic.hq.product.Metric;
import org.hyperic.hq.product.MetricNotFoundException;
import org.hyperic.hq.product.MetricUnreachableException;
import org.hyperic.hq.product.MetricValue;
import org.hyperic.hq.product.PluginException;

/**
 * 
 *
 */
public class GFApplicationPeerMeasurementPlugin extends GFMeasurementPlugin {

    /** The Constant log. */
    private static final Log log =
        LogFactory.getLog(GFApplicationPeerMeasurementPlugin.class);

    @Override
    public MetricValue getValue(Metric metric) throws PluginException,
    MetricNotFoundException, MetricUnreachableException {

        if(log.isDebugEnabled()) {
            log.debug("Plugin hash id:" + hashCode());
            log.debug("Metric:" + metric.toDebugString());
        }

        Properties mProps = metric.getObjectProperties();

        MemberCache memberCache = ((GFProductPlugin)getProductPlugin())
            .getMemberCache(mProps.getProperty(GFMXConstants.CONF_JMX_URL));

        String workingDirectory = mProps.getProperty(GFMXConstants.ATTR_PWD);
        String host = mProps.getProperty(GFMXConstants.ATTR_HOST);
        String name = mProps.getProperty(GFMXConstants.ATTR_NAME);
        
        boolean needMemberUpdate = false;
        double value = Metric.AVAIL_DOWN;
        Map<String, Object> map;

        GFJmxConnection gf = new GFJmxConnection(mProps);
        
        // switch workingDirectory from null to ""
        MemberInfo member = memberCache.getMember(workingDirectory == null ? "" : workingDirectory, host, name);
        if(member == null) {
            needMemberUpdate = true;
        } else {
            String gfid = member.getGfid();
            try {
                map = gf.getApplicationAttributes(gfid, new String[]{"id"});
                if(map.containsKey("id"))
                    value = Metric.AVAIL_UP;   
            } catch (Exception e) {
                needMemberUpdate = true;
            }
        }

        if(needMemberUpdate)
            memberCache.refresh(mProps);
        
        if(log.isDebugEnabled()) {
            if(member == null) {
                log.debug("[getValue] member is null");                
            } else {
                log.debug("[getValue] for " + member.getGfid() + " is " + value);
            }            
        }

        return new MetricValue(value);          
    }


}
