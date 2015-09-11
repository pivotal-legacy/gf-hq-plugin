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
package com.vmware.vfabric.hyperic.plugin.vfgf.util;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * The Class GFMXUtils.
 */
public class GFMXUtils {

    /** The Constant EMPTY_ARGS. */
    public final static Object[] EMPTY_ARGS = {}; 

    /** The Constant EMPTY_DEF. */
    public final static String[] EMPTY_DEF = {};

    public final static String KEY_ID = "id";

    public static String getId(ObjectName obj){
        return obj.getKeyProperty(KEY_ID);
    }

    public static String getField(ObjectName obj, String fName){
        return obj.getKeyProperty(fName);
    }


    public static ObjectName combine(ObjectName obj,String key, String value){
        StringBuilder b = new StringBuilder();
        b.append(obj.getCanonicalName());
        b.append(',');
        b.append(key);
        b.append('=');
        b.append(value);
        try {
            return new ObjectName(b.toString());
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

}
