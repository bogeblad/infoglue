/* ===============================================================================
*
* Part of the InfoGlue Content Management Platform (www.infoglue.org)
*
* ===============================================================================
*
*  Copyright (C)
* 
* This program is free software; you can redistribute it and/or modify it under
* the terms of the GNU General Public License version 2, as published by the
* Free Software Foundation. See the file LICENSE.html for more information.
* 
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
* Place, Suite 330 / Boston, MA 02111-1307 / USA.
*
* ===============================================================================
*/
package org.infoglue.deliver.portal.om;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.pluto.om.common.Parameter;
import org.apache.pluto.om.common.ParameterSet;

/**
 * Dummy implementation of interface
 * @author Jöran
 * TODO Implement this
 *
 */
public class ParameterSetImpl implements ParameterSet {
    private Map parameters = new HashMap() ;
   
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.ParameterSet#iterator()
     */
    public Iterator iterator() {
        return parameters.values().iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.ParameterSet#get(java.lang.String)
     */
    public Parameter get(String name) {
        return (Parameter)parameters.get(name);
    }
    
    /**
     * Add parameter to the set
     * @param parameter add this parameter
     */
    public void add(Parameter parameter){
        parameters.put(parameter.getName(), parameter);
    }

}
