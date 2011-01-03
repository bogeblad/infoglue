package com.frovi.ss.Tree;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeControllerProxy;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;

/**
 * BaseNodeSupplier.java
 * Created on 2002-sep-30 
 * @author Stefan Sik, ss@frovi.com 
 * ss
 */
public abstract class BaseNodeSupplier implements INodeSupplier
{
    private final static Logger logger = Logger.getLogger(BaseNodeSupplier.class.getName());

	private BaseNode rootNode = null;
	
	public boolean hasChildren()
	{
		return true;
	}

	public boolean hasChildren(Integer nodeId) throws SystemException, Exception
	{
		// Base functionallity, typically this method is overridden
		// for performance reasons
		Collection tmp = getChildContainerNodes(nodeId);
		Collection tmp2 = getChildLeafNodes(nodeId);
		return (tmp.size() + tmp2.size()) > 0;
	}


	/**
	 * Sets the rootNode.
	 * @param rootNode The rootNode to set
	 */
	protected void setRootNode(BaseNode rootNode)
	{
		this.rootNode = rootNode;
	}

	/**
	 * Returns the rootNode.
	 * @return BaseNode
	 */
	public BaseNode getRootNode()
	{
		return rootNode;
	}

	/**
	 * Used by the view pages to determine if the current user has sufficient access rights
	 * to perform the action specific by the interception point name.
	 *
	 * @param interceptionPointName THe Name of the interception point to check access rights
	 * @return True is access is allowed, false otherwise
	 */
	public boolean hasAccessTo(String interceptionPointName, InfoGluePrincipal infoGluePrincipal, boolean acceptIfNotDefined)
	{
		logger.info("Checking if " + infoGluePrincipal.getName() + " has access to " + interceptionPointName + ". If not the interception point is defined we return true.");

		try
		{
			return AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, interceptionPointName, acceptIfNotDefined);
		}
		catch (SystemException e)
		{
		    logger.warn("Error checking access rights", e);
			return false;
		}
	}

	
	/**
	 * This method return true if the user logged in has access to the content sent in.
	 */
	
	public boolean getHasUserContentAccess(InfoGluePrincipal infoGluePrincipal, Integer contentId)
	{
        boolean hasUserContentAccess = true;
        
        try
        {
	        Database db = CastorDatabaseService.getDatabase();
			
	        try
	        {
	            beginTransaction(db);
	
			    if(contentId != null)
			    {
					Integer protectedContentId = ContentDeliveryController.getContentDeliveryController().getProtectedContentId(db, contentId);
					if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Read", protectedContentId.toString()))
					{
					    hasUserContentAccess = false;
					}
			    }
	
				commitTransaction(db);	
			}
			catch(Exception e)
			{
				logger.error("An error occurred so we should not complete the transaction:" + e, e);
				rollbackTransaction(db);
			}
        }
        catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
		}
        
		return hasUserContentAccess;
	}

	/**
	 * This method return true if the user logged in has access to the content sent in.
	 */
	
	public boolean getHasUserPageAccess(InfoGluePrincipal infoGluePrincipal, Integer siteNodeId)
	{
        boolean hasUserPageAccess = true;
        
        try
        {
	        Database db = CastorDatabaseService.getDatabase();
			
	        try
	        {
	            beginTransaction(db);
	
	    		Integer protectedSiteNodeVersionId = SiteNodeControllerProxy.getController().getProtectedSiteNodeVersionId(siteNodeId);
	    		if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()))
				{
		    		hasUserPageAccess = false;
				}
	
				commitTransaction(db);	
			}
			catch(Exception e)
			{
				logger.error("An error occurred so we should not complete the transaction:" + e, e);
				rollbackTransaction(db);
			}
        }
        catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
		}
        
		return hasUserPageAccess;
	}

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

}
