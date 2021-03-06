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

import java.util.ArrayList;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.vmware.vfabric.hyperic.plugin.vfgf.GFMXConstants;
import com.vmware.vfabric.hyperic.plugin.vfgf.util.GFMXUtils;
import com.vmware.vfabric.hyperic.plugin.vfgf.util.GFUtils;
import org.hyperic.util.config.ConfigResponse;

/**
 * The Class CacheServerDetector.
 */
public class ApplicationPeerDetector extends MemberDetector {

    /** The Constant log. */
    private final static Log log =
        LogFactory.getLog(ApplicationPeerDetector.class);

    @Override
    protected boolean hasCorrectRoles(int mask) {
        log.debug(GFUtils.roleMaskToDebugString(mask));
        return ((mask & GFMXConstants.MEMBER_ROLE_APPLICATIONPEER) == GFMXConstants.MEMBER_ROLE_APPLICATIONPEER &&
                (mask & GFMXConstants.MEMBER_ROLE_CACHESERVER) != GFMXConstants.MEMBER_ROLE_CACHESERVER &&
                (mask & GFMXConstants.MEMBER_ROLE_GATEWAYHUB) != GFMXConstants.MEMBER_ROLE_GATEWAYHUB);
    }    

    @Override
    protected StatType[] filterSupportedStats(Object[][] statObjects, ConfigResponse config) {

        ArrayList<StatType> stats = new ArrayList<StatType>();

        for (int i = 0; i < statObjects.length; i++) {
            ObjectName o = (ObjectName)statObjects[i][0];
            String type = (String)statObjects[i][1];
            String name = GFMXUtils.getField(o, "name");
            if(name.equals("distributionStats")) {
                stats.add(new StatType("Distribution Statistics", o));
            } else if(name.equals("FunctionExecution")) {
                stats.add(new StatType("Function Service", o));
            } else if(type.equals("FunctionStatistics")) {
                stats.add(new StatType("Function", o, o.getKeyProperty("name")));               
            }

        }

        return stats.toArray(new StatType[0]);
    }

}
