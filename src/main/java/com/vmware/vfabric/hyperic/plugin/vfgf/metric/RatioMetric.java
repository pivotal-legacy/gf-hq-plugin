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

public class RatioMetric extends CustomMetric {
    
    public final static String TYPE = "ratio";
    
    public RatioMetric(String alias) {
        super(alias);
    }

    public Double calculate(Double[] values) {
        Double val = new Double(values[0]/values[1]);
        if (val.equals(Double.NaN))
            return new Double(0);
        else
            return val;        
    }

}
