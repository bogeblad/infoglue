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


package org.infoglue.cms.applications.cmstool.actions;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.common.actions.SimpleXmlServiceAction;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallishContentImpl;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
import org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl;
import org.infoglue.cms.entities.management.impl.simple.InterceptorImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallAvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallSystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.controllers.kernel.impl.simple.DigitalAssetDeliveryController;
import org.infoglue.deliver.util.CacheController;


/**
 * This is the action that takes care of all incoming update-calls. This action is
 * called by either the system or by replication-program and the class the distibutes the 
 * update-call to all the listeners which have registered earlier.
 *
 * @author Mattias Bogeblad
 */

public class UpdateCacheAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(UpdateCacheAction.class.getName());

	private static final long serialVersionUID = -1669612689042389758L;
	
	private String className = null;
	private String objectId = null;
	private String objectName = null;
	private String typeId = null;

	private String repositoryName = null;
	private Integer languageId    = null;
	private Integer siteNodeId    = null;
	
	private static boolean cachingInProgress = false;
	
	/**
	 * The constructor for this action - contains nothing right now.
	 */
    
    public UpdateCacheAction() 
    {
	
    }
    
    /**
     * This method will just reply to a testcall. 
     */
         
    public String doTest() throws Exception
    {
        this.getResponse().getWriter().println("test ok - cache action available");
        return NONE;
    }

    /**
     * This method is the application entry-point. The parameters has been set through the setters
     * and now we just have to render the appropriate output. 
     */
         
    public String doExecute() throws Exception
    {
		try
		{  
			CacheController.clearCaches(className, objectId, null);
			
			logger.info("Updating className with id:" + className + ":" + objectId);
			
			if(className != null)
			{
				Class types = Class.forName(className);
				Object[] ids = {new Integer(objectId)};
				
				CacheController.clearCache(types, ids);
				
				if(Class.forName(className).getName().equals(ContentImpl.class.getName()))
				{
				    logger.info("We clear all small contents as well " + objectId);
					Class typesExtra = SmallContentImpl.class;
					Object[] idsExtra = {new Integer(objectId)};
					CacheController.clearCache(typesExtra, idsExtra);

				    logger.info("We clear all smallish contents as well " + objectId);
					Class typesExtraSmallish = SmallishContentImpl.class;
					Object[] idsExtraSmallish = {new Integer(objectId)};
					CacheController.clearCache(typesExtraSmallish, idsExtraSmallish);

					logger.info("We clear all medium contents as well " + objectId);
					Class typesExtraMedium = MediumContentImpl.class;
					Object[] idsExtraMedium = {new Integer(objectId)};
					CacheController.clearCache(typesExtraMedium, idsExtraMedium);
				}
				if(Class.forName(className).getName().equals(ContentVersionImpl.class.getName()))
				{
				    logger.info("We clear all small contents as well " + objectId);
					Class typesExtra = SmallContentVersionImpl.class;
					Object[] idsExtra = {new Integer(objectId)};
					CacheController.clearCache(typesExtra, idsExtra);

					logger.info("We clear all small contents as well " + objectId);
					Class typesExtraSmallest = SmallestContentVersionImpl.class;
					Object[] idsExtraSmallest = {new Integer(objectId)};
					CacheController.clearCache(typesExtraSmallest, idsExtraSmallest);
				}
				else if(Class.forName(className).getName().equals(AvailableServiceBindingImpl.class.getName()))
				{
				    Class typesExtra = SmallAvailableServiceBindingImpl.class;
					Object[] idsExtra = {new Integer(objectId)};
					CacheController.clearCache(typesExtra, idsExtra);
				}
				else if(Class.forName(className).getName().equals(SiteNodeImpl.class.getName()))
				{
				    Class typesExtra = SmallSiteNodeImpl.class;
					Object[] idsExtra = {new Integer(objectId)};
					CacheController.clearCache(typesExtra, idsExtra);
				}
				else if(Class.forName(className).getName().equals(SiteNodeVersionImpl.class.getName()))
				{
				    Class typesExtra = SmallSiteNodeVersionImpl.class;
					Object[] idsExtra = {new Integer(objectId)};
					CacheController.clearCache(typesExtra, idsExtra);
				}
				else if(Class.forName(className).getName().equals(DigitalAssetImpl.class.getName()))
				{
					CacheController.clearCache("digitalAssetCache");
					Class typesExtra = SmallDigitalAssetImpl.class;
					Object[] idsExtra = {new Integer(objectId)};
					CacheController.clearCache(typesExtra, idsExtra);

					Class typesExtraMedium = MediumDigitalAssetImpl.class;
					Object[] idsExtraMedium = {new Integer(objectId)};
					CacheController.clearCache(typesExtraMedium, idsExtraMedium);
				}
				else if(Class.forName(className).getName().equals(SystemUserImpl.class.getName()))
				{
				    Class typesExtra = SmallSystemUserImpl.class;
					Object[] idsExtra = {objectId};
					CacheController.clearCache(typesExtra, idsExtra);
				}
				else if(Class.forName(className).getName().equals(RoleImpl.class.getName()))
				{
				    Class typesExtra = SmallRoleImpl.class;
					Object[] idsExtra = {objectId};
					CacheController.clearCache(typesExtra, idsExtra);
				}
				else if(Class.forName(className).getName().equals(GroupImpl.class.getName()))
				{
				    Class typesExtra = SmallGroupImpl.class;
					Object[] idsExtra = {objectId};
					CacheController.clearCache(typesExtra, idsExtra);
				}

				CacheController.clearCache("workflowCache");
				CacheController.clearCache("myActiveWorkflows");
				CacheController.clearCache("workflowNameCache");
				
				if(className.equals("ServerNodeProperties"))
				{
			        CacheController.clearServerNodeProperty(true);
			        CacheController.clearCastorCaches();
			        CacheController.clearCaches(null, null, null);
			        CacheController.clearFileCaches("pageCache");
				}
				if(className.indexOf("AccessRight") > 0 || className.indexOf("SystemUser") > 0 || className.indexOf("Role") > 0  || className.indexOf("Group") > 0 || className.indexOf("Intercept") > 0)
				{
					CacheController.clearCache("personalAuthorizationCache");
				}
				if(className.indexOf("ContentTypeDefinition") > 0)
				{
					CacheController.clearCache("contentTypeDefinitionCache");
				}
				if(className.indexOf("ServiceBinding") > 0 || className.indexOf("Qualifyer") > 0 || className.indexOf("SiteNodeVersion") > 0 || className.indexOf("ContentVersion") > 0 || className.indexOf("Content") > 0 || className.indexOf("AccessRight") > 0 || className.indexOf("SystemUser") > 0 || className.indexOf("Role") > 0  || className.indexOf("Group") > 0)
				{
					CacheController.clearCache("boundContentCache");
				}
				if(className.indexOf("SystemUser") > 0 || className.indexOf("Role") > 0  || className.indexOf("Group") > 0)
				{
					CacheController.clearCache("principalCache");
					CacheController.clearCache("principalPropertyValueCache");
					CacheController.clearCache("userCache");
				}
				if(className.indexOf("Intercept") > 0)
				{
					CacheController.clearCache("interceptorsCache");
					CacheController.clearCache("interceptionPointCache");
				}
				if(className.indexOf("AvailableServiceBinding") > 0)
				{
					CacheController.clearCache("availableServiceBindingCache");					
				}
				if(className.indexOf("Content") > 0)
				{
					CacheController.clearCache("childContentCache");					
				}
				if(className.indexOf("SiteNode") > 0 || className.indexOf("Content") > 0)
				{
					CacheController.clearCache("NavigationCache");					
				}
				if(className.indexOf("DigitalAsset") > 0 || className.indexOf("ContentVersion") > 0)
				{
					CacheController.clearCache("digitalAssetCache");					
				}
				if(className.indexOf("Language") > -1)
				{
					CacheController.clearCache("languageCache");
				}
				if(className.indexOf("Repository") > -1 || className.indexOf("Language") > -1)
				{
					CacheController.clearCache("repositoryLanguageListCache");
					CacheController.clearCache("masterLanguageCache");
				}
				if(className.indexOf("Repository") > -1)
				{
					CacheController.clearCache("repositoryCache");
					CacheController.clearCache("parentRepository");
					CacheController.clearCache("masterRepository");
				}
			}
		}
		catch(Exception e)
		{
		    logger.error(e.getMessage(), e);
		}
                
        return "success";
    }
    

	/**
	 * This method is for letting users update cache manually. 
	 */
         
	public String doInput() throws Exception
	{
		return "input";
	}
    

	/**
	 * Setters and getters for all things sent to the page in the request
	 */
	        
    public void setClassName(String className)
    {
	    this.className = className;
    }
        
    public void setObjectId(String objectId)
    {
	    this.objectId = objectId;
    }

    public void setObjectName(String objectName)
    {
	    this.objectName = objectName;
    }

    public void setTypeId(String typeId)
    {
	    this.typeId = typeId;
    }
    
}
