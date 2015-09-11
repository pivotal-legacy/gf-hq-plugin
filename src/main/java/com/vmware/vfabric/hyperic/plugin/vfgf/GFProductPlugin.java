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
package com.vmware.vfabric.hyperic.plugin.vfgf;

import java.util.HashMap;
import java.util.Map;

import com.vmware.vfabric.hyperic.plugin.vfgf.cache.MemberCache;
import com.vmware.vfabric.hyperic.plugin.vfgf.detector.GF7ServerDetector;
import com.vmware.vfabric.hyperic.plugin.vfgf.util.JmxManagerFinder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.PluginManager;
import org.hyperic.hq.product.ProductPlugin;

/**
 * The Class GFProductPlugin. 
 * 
 * Storing plugin level caches.
 */
public class GFProductPlugin extends ProductPlugin {

    private static final Log log = LogFactory.getLog(GF7ServerDetector.class);
    
    /** The member mapping. */
    private Map<String, MemberCache> memberCaches = new HashMap<String, MemberCache>();

    private static String jmxUrl = new String();
    
    /**
     * Returns instance if member mapping cache.
     * 
     * @param id Id to identify membercache. Need to do this e.g. using jmx.url
     *           to allow this plugin to support multiple DS envs.
     * @return Member cache
     */
    public MemberCache getMemberCache(String id){
        MemberCache cache = memberCaches.get(id);
        if(cache == null) {
            cache = new MemberCache();
            memberCaches.put(id, cache);
        }
        return cache;
    }


    @Override
    public void init(PluginManager manager) throws PluginException {
        super.init(manager);
    }


    @Override
    public void shutdown() throws PluginException {

        for (MemberCache cache: memberCaches.values()) {
            cache.shutdown();
        }

        super.shutdown();
    }
    
    public static void resetJmxUrl() {
        synchronized (jmxUrl) {
            jmxUrl = "";
        }
    }
    
    public static String getJmxUrl(String locators) {
        if (jmxUrl == null) {
            jmxUrl = new String();
        }
        synchronized (jmxUrl) {
            if (!jmxUrl.isEmpty()) {
                return jmxUrl;
            } else {
                jmxUrl = JmxManagerFinder.getJmxUrl(locators);
                if (jmxUrl != null) {
                    return jmxUrl;
                } else {
                    return "";  //Don't return null
                }
            }
        }
    }
    
}
