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

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.apache.pluto.om.common.SecurityRoleRef;
import org.apache.pluto.om.common.SecurityRoleRefSet;

/**
 * Dummy implementation of interface
 * @author Jöran
 * TODO Implement this
 *
 */
public class SecurityRoleRefSetImpl implements SecurityRoleRefSet {
    private Vector container = new Vector();
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.SecurityRoleRefSet#get(java.lang.String)
     */
    public SecurityRoleRef get(String name) {
        for(Iterator iter = container.iterator(); iter.hasNext();){
            Object o = iter.next();
            if(o instanceof SecurityRoleRef){
                SecurityRoleRef ref = (SecurityRoleRef)o;
                if(ref.getRoleName().equals(name)){
                    return ref;
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#size()
     */
    public int size() {
        return container.size();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    public void clear() {
        container.clear();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        return container.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray() {
        return container.toArray();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(Object arg0) {
        return container.add(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object arg0) {
        return container.contains(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object arg0) {
        return container.remove(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection arg0) {
        return container.addAll(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection arg0) {
        return container.containsAll(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection arg0) {
        return container.removeAll(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection arg0) {
        return container.retainAll(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator() {
        return container.iterator();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    public Object[] toArray(Object[] arg0) {
        return container.toArray(arg0);
    }

}
