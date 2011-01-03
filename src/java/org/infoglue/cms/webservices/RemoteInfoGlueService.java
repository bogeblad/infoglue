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

package org.infoglue.cms.webservices;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ChangeNotificationController;


/**
 * This class is responsible for letting an external application call InfoGlue
 * API:s remotely. It handles api:s to manage contents and associated entities.
 * 
 * @author Mattias Bogeblad
 */

public class RemoteInfoGlueService 
{
    private final static Logger logger = Logger.getLogger(RemoteInfoGlueService.class.getName());

    public void updateCaches()
    {
		try
	    {
	    	ChangeNotificationController.notifyListeners();
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
    }

    protected HttpServletRequest getRequest() 
    {
    	HttpServletRequest request = (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
    	return request;
	}

    public String cleanAttributeValue(String attributeValue, boolean allowHTML, boolean allowExternalLinks, boolean allowDollarSign, boolean allowAnchorSigns) 
    {
    	if(!allowExternalLinks)
        {
    		attributeValue = attributeValue.replaceAll("http://", "");
    		attributeValue = attributeValue.replaceAll("https://", "");
    		attributeValue = attributeValue.replaceAll("ftp://", "");
        }

    	if(!allowDollarSign)
    		attributeValue = attributeValue.replaceAll("\\$", "");
        
        if(!allowAnchorSigns)
        	attributeValue = attributeValue.replaceAll("#", "");
        
        if(!allowHTML)
        {
        	attributeValue = attributeValue.replaceAll("</*[^>]+>", "");
        }
        else
        {
        	attributeValue = attributeValue.replaceAll("<%/*[^%>]+%>", "");
        }
        
        attributeValue = attributeValue.replaceAll("templateLogic.getPageUrl", "\\$templateLogic.getPageUrl");
        attributeValue = attributeValue.replaceAll("templateLogic.getInlineAssetUrl", "\\$templateLogic.getInlineAssetUrl");

        return attributeValue;
    }

	/*************************************************** 
	 * Transaction specifik operations
	 ***************************************************/

	/**
	 * Creates a new database and starts a transaction
	 * @return A reference to a castor database with a new transaction
	 * @throws SystemException if a database error occurs.
	 */
	protected static Database beginTransaction() throws SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		beginTransaction(db);
		return db;
	}

    /**
     * Begins a transaction on the named database
     */
         
    protected static void beginTransaction(Database db) throws SystemException
    {
        try
        {
            //logger.info("Opening a new Transaction in cms...");
            db.begin();
        }
        catch(Exception e)
        {
			throw new SystemException("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage(), e);    
        }
    }
       
    /**
     * Ends a transaction on the named database
     */
     
    protected static void commitTransaction(Database db) throws SystemException
    {
        try
        {
            //logger.info("Closing a transaction in cms...");

            db.commit();
		    db.close();
        }
        catch(Exception e)
        {
			throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage(), e);    
        }
    }
 
 
    /**
     * Rollbacks a transaction on the named database
     */
     
    protected static void rollbackTransaction(Database db) throws SystemException
    {
        try
        {
            //logger.info("rollbackTransaction a transaction in cms...");
            
            if (db != null && db.isActive())
        	{
                db.rollback();
				db.close();
        	}
        }
        catch(Exception e)
        {
            logger.warn("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage());
        }
    }

    /**
     * Rollbacks a transaction on the named database
     */
     
    protected static void closeDatabase(Database db) throws SystemException
    {
        try
        {
            if (db != null)
        	{
				db.close();
        	}
        }
        catch(Exception e)
        {
            logger.warn("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage());
        }
    }
}
