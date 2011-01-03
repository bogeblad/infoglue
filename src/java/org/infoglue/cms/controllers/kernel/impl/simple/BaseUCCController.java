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

package org.infoglue.cms.controllers.kernel.impl.simple;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.workflowtool.function.email.UsersAddressProvider;
import org.infoglue.cms.exception.SystemException;

public abstract class BaseUCCController
{
    private final static Logger logger = Logger.getLogger(BaseUCCController.class.getName());

    /**
     * Begins a transaction on the named database
     */
     
    protected void beginTransaction(Database db) throws SystemException
    {
        try
        {
            db.begin();
        }
        catch(Exception e)
        {
			e.printStackTrace();
            throw new SystemException("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage(), e);    
        }
    }
    
 
    
    /**
     * Ends a transaction on the named database
     */
     
    protected void commitTransaction(Database db) throws SystemException
    {
        try
        {
            db.commit();
            db.close();
        }
        catch(Exception e)
        {
			e.printStackTrace();
            throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage(), e);    
        }
    }
 
 
    /**
     * Rollbacks a transaction on the named database if there is an open transaction
     */
     
    protected void rollbackTransaction(Database db) throws SystemException
    {
        try
        {
        	if (db.isActive())
        	{
	            db.rollback();
				db.close();
        	}
        }
        catch(Exception e)
        {
			e.printStackTrace();
            throw new SystemException("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage(), e);    
        }
    }
 
    /**
     * A generic method to convert an enumeration to a Collection.
     */
    protected Collection toCollection(Enumeration enumeration) 
    {
        final Set set = new HashSet();
        while(enumeration.hasMoreElements()) 
        { 
            set.add(enumeration.nextElement());
        }
        return Collections.unmodifiableSet(set);      
    }


}