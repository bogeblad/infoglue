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

package org.infoglue.deliver.controllers.kernel.impl.simple;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentCategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentCategory;
import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.SmallestContentVersion;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallishContentImpl;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;


public class ContentDeliveryController extends BaseDeliveryController
{
    private final static Logger logger = Logger.getLogger(ContentDeliveryController.class.getName());

	private URLComposer urlComposer = null; 
	private VisualFormatter formatter = new VisualFormatter();
	
	/**
	 * Private constructor to enforce factory-use
	 */
	
	private ContentDeliveryController()
	{
		urlComposer = URLComposer.getURLComposer(); 
	}
	
	/**
	 * Factory method
	 */
	
	public static ContentDeliveryController getContentDeliveryController()
	{
		return new ContentDeliveryController();
	}
	
	/**
	 * This method returns which mode the delivery-engine is running in.
	 * The mode is important to be able to show working, preview and published data separate.
	 */
	
	private Integer getOperatingMode(DeliveryContext deliveryContext)
	{
		Integer operatingMode = new Integer(0); //Default is working
		try
		{
			operatingMode = new Integer(CmsPropertyHandler.getOperatingMode());
			if(!deliveryContext.getOperatingMode().equals(CmsPropertyHandler.getOperatingMode()))
				operatingMode = new Integer(deliveryContext.getOperatingMode());

			//logger.info("Operating mode is:" + operatingMode);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the operating mode from the propertyFile:" + e.getMessage(), e);
		}
		return operatingMode;
	}
	
	
	/**
	 * This method return a contentVO
	 */
	
	public ContentVO getContentVO(Integer contentId, Database db) throws SystemException, Exception
	{
		if(contentId == null || contentId.intValue() < 1)
			return null;
		
		ContentVO contentVO = null;
		
		contentVO = (ContentVO)getVOWithId(SmallContentImpl.class, contentId, db);
		
		return contentVO;
	}
	
	/**
	 * This method return a contentVO
	 */
	
	public ContentVO getContentVO(Database db, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		if(contentId == null || contentId.intValue() < 1)
			return null;

		deliveryContext.addUsedContent("content_" + contentId);

		ContentVO contentVO = (ContentVO)getVOWithId(SmallContentImpl.class, contentId, db);
				
		return contentVO;
	}
	
	public ContentVO getContentWithPath(Integer repositoryId, String path, InfoGluePrincipal principal, Database db) throws SystemException, Exception 
	{
		ContentVO content = getRootContentVO(repositoryId, db);
		logger.info("content:" + content);
		final String paths[] = path.split("/");
		if(path.equals(""))
			return content;
		
		for(int i=0; i<paths.length; ++i) 
		{
			final String name = paths[i];
			final ContentVO childContent = getChildWithName(content.getId(), name, db);
			if(childContent != null)
				content = childContent;
			else if(childContent == null)
				throw new SystemException("There exists no content with the path [" + path + "].");
		}
		return content;
	}
	
	/**
	 * This method returns the root contentVO for the specified repository.
	 * If the repositoryName is null we fetch the name of the master repository.
	 */
	
	public ContentVO getRootContentVO(Integer repositoryId, Database db) throws SystemException, Exception
	{
		ContentVO contentVO = null;

        String key = "" + repositoryId;
		logger.info("key in getRootContentVO:" + key);
		contentVO = (ContentVO)CacheController.getCachedObject("rootContentCache", key);
		if(contentVO != null)
		{
		    logger.info("There was an cached master root contentVO:" + contentVO.getName());
		}
		else
		{
	        logger.info("Fetching the root contentVO for the repository " + repositoryId);
			OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl c WHERE is_undefined(c.parentContent) AND c.repository = $1");
			oql.bind(repositoryId);
			
	    	QueryResults results = oql.execute(Database.ReadOnly);
			
	    	if (results.hasMore()) 
	        {
	    		contentVO = ((Content)results.next()).getValueObject();
				logger.info("The root contentVO was found:" + contentVO.getName());
	        }

	    	results.close();
			oql.close();

			logger.info("contentVO:" + contentVO);

			CacheController.cacheObject("rootContentCache", key, contentVO);
		}

        return contentVO;	
	}

	/**
	 * 
	 */
	private ContentVO getChildWithName(Integer parentContentId, String name, Database db) throws Exception
	{
		ContentVO contentVO = null;
		
		OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.SmallContentImpl c WHERE c.parentContentId = $1 AND c.name = $2");
    	oql.bind(parentContentId);
    	oql.bind(name);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		if(results.hasMore()) 
        {
			contentVO = ((Content)results.next()).getValueObject();
        }

		results.close();
		oql.close();
		
		return contentVO;
	}

	/**
	 * This method returns that contentVersionVO which matches the parameters sent in and which 
	 * also has the correct state for this delivery-instance.
	 */
	
	public ContentVersionVO getContentVersionVO(Database db, Integer siteNodeId, Integer contentId, Integer languageId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		ContentVersionVO contentVersionVO = null;
		SiteNodeVO siteNodeVO = (SiteNodeVO)getVOWithId(SmallSiteNodeImpl.class, siteNodeId, db);
		String contentVersionKey = "contentVersionVO_" + siteNodeVO.getRepositoryId() + "_" + contentId + "_" + languageId + "_" + useLanguageFallback;
		
		contentVersionVO = (ContentVersionVO)CacheController.getCachedObjectFromAdvancedCache("contentVersionCache", contentVersionKey);
			
		if(contentVersionVO != null)
		{
			//logger.info("There was an cached contentVersionVO:" + contentVersionVO.getContentVersionId());
		}
		else
		{
			contentVersionVO = this.getContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
        	if(contentVersionVO != null)
			{
				CacheController.cacheObjectInAdvancedCache("contentVersionCache", contentVersionKey, contentVersionVO, new String[]{"contentVersion_" + contentVersionVO.getId(), "content_" + contentVersionVO.getContentId()}, true);
			}
    	
        }
		
		if(contentVersionVO != null && deliveryContext != null)
		    deliveryContext.addUsedContentVersion("contentVersion_" + contentVersionVO.getId());
		
		return contentVersionVO;
	}

	
	/**
	 * This method returns that contentVersionVO which matches the parameters sent in and which 
	 * also has the correct state for this delivery-instance.
	 */
	
	public SmallestContentVersionVO getSmallestContentVersionVO(Database db, Integer siteNodeId, Integer contentId, Integer languageId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		SmallestContentVersionVO contentVersionVO = null;
		
		SiteNodeVO siteNodeVO = (SiteNodeVO)getVOWithId(SmallSiteNodeImpl.class, siteNodeId, db);
		String contentVersionKey = "smallestContentVersionVO_" + siteNodeVO.getRepositoryId() + "_" + contentId + "_" + languageId + "_" + useLanguageFallback;
		if(logger.isInfoEnabled())
			logger.info("contentVersionKey:" + contentVersionKey);
		
		contentVersionVO = (SmallestContentVersionVO)CacheController.getCachedObjectFromAdvancedCache("contentVersionCache", contentVersionKey);
		
		if(contentVersionVO != null)
		{
			//logger.info("There was an cached contentVersionVO:" + contentVersionVO.getContentVersionId());
		}
		else
		{
			contentVersionVO = this.getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
        	if(contentVersionVO != null)
			{
				//contentVersionVO = contentVersion.getValueObject();
				
				CacheController.cacheObjectInAdvancedCache("contentVersionCache", contentVersionKey, contentVersionVO, new String[]{"contentVersion_" + contentVersionVO.getId(), "content_" + contentVersionVO.getContentId()}, true);
			}
        }
		
		if(contentVersionVO != null && deliveryContext != null)
		    deliveryContext.addUsedContentVersion("contentVersion_" + contentVersionVO.getId());
		
		return contentVersionVO;
	}

	
	/**
	 * This method returns that contentVersion which matches the parameters sent in and which 
	 * also has the correct state for this delivery-instance.
	 */

	private ContentVersion getContentVersion(Integer siteNodeId, Integer contentId, Integer languageId, Database db, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		if(contentId == null || contentId.intValue() < 1)
			return null;
		
		ContentVersion contentVersion = null;
		
		MediumContentImpl content = (MediumContentImpl)getObjectWithId(MediumContentImpl.class, contentId, db);
		boolean isValidContent = isValidContent(infoGluePrincipal, content, languageId, useLanguageFallback, false, db, deliveryContext);
		if(isValidContent)
		{
			contentVersion = getContentVersion(content, languageId, getOperatingMode(deliveryContext), deliveryContext, db);
			if(contentVersion == null && useLanguageFallback)
			{
				Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(db, siteNodeId).getLanguageId();
				if(languageId != null && !languageId.equals(masterLanguageId))
				{
					contentVersion = getContentVersion(content, masterLanguageId, getOperatingMode(deliveryContext), deliveryContext, db);
				}
				
				//Added fallback to the content repository master language... useful for mixing components between sites
				if(languageId != null && contentVersion == null && useLanguageFallback)
				{
					Integer contentMasterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(db, content.getRepositoryId()).getLanguageId();
					if(languageId != null && !languageId.equals(contentMasterLanguageId) && !masterLanguageId.equals(contentMasterLanguageId))
					{
						contentVersion = getContentVersion(content, contentMasterLanguageId, getOperatingMode(deliveryContext), deliveryContext, db);
					}
				}
			}
			
		}
		
		return contentVersion;
	}
	
	
	/**
	 * This method returns that contentVersion which matches the parameters sent in and which 
	 * also has the correct state for this delivery-instance.
	 */
	
	private SmallestContentVersionVO getSmallestContentVersionVO(Integer siteNodeId, Integer contentId, Integer languageId, Database db, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		if(contentId == null || contentId.intValue() < 1)
			return null;
		
		SmallestContentVersionVO contentVersion = null;
		
		MediumContentImpl content = (MediumContentImpl)getObjectWithId(MediumContentImpl.class, contentId, db);
		boolean isValidContent = isValidContent(infoGluePrincipal, content, languageId, useLanguageFallback, false, db, deliveryContext);
		if(isValidContent)
		{
			contentVersion = getSmallestContentVersionVO(contentId, languageId, getOperatingMode(deliveryContext), deliveryContext, db);
			if(contentVersion == null && useLanguageFallback)
			{
				Integer masterLanguageId = null;
				if(siteNodeId != null)
					masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(db, siteNodeId).getLanguageId();
				else
					masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(db, content.getRepositoryId()).getLanguageId();
				
				if(languageId == null || (languageId != null && !languageId.equals(masterLanguageId)))
				{
					contentVersion = getSmallestContentVersionVO(contentId, masterLanguageId, getOperatingMode(deliveryContext), deliveryContext, db);
				}
				
				//Added fallback to the content repository master language... useful for mixing components between sites
				if(languageId != null && contentVersion == null && useLanguageFallback)
				{
					Integer contentMasterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(db, content.getRepositoryId()).getLanguageId();
					if(languageId != null && !languageId.equals(contentMasterLanguageId) && !masterLanguageId.equals(contentMasterLanguageId))
					{
						contentVersion = getSmallestContentVersionVO(contentId, contentMasterLanguageId, getOperatingMode(deliveryContext), deliveryContext, db);
					}
				}
			}
			
		}
		
		return contentVersion;
	}

	/**
	 * This method returns that contentVersion which matches the parameters sent in and which 
	 * also has the correct state for this delivery-instance.
	 */
	
	private ContentVersionVO getContentVersionVO(Integer siteNodeId, Integer contentId, Integer languageId, Database db, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		if(contentId == null || contentId.intValue() < 1)
			return null;
		
		ContentVersionVO contentVersion = null;
		
		MediumContentImpl content = (MediumContentImpl)getObjectWithId(MediumContentImpl.class, contentId, db);
		boolean isValidContent = isValidContent(infoGluePrincipal, content, languageId, useLanguageFallback, false, db, deliveryContext);
		if(isValidContent)
		{
			contentVersion = getContentVersionVO(contentId, languageId, getOperatingMode(deliveryContext), deliveryContext, db);
			if(contentVersion == null && useLanguageFallback)
			{
				Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(db, siteNodeId).getLanguageId();
				if(languageId != null && !languageId.equals(masterLanguageId))
				{
					contentVersion = getContentVersionVO(contentId, masterLanguageId, getOperatingMode(deliveryContext), deliveryContext, db);
				}
				
				//Added fallback to the content repository master language... useful for mixing components between sites
				if(languageId != null && contentVersion == null && useLanguageFallback)
				{
					Integer contentMasterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(db, content.getRepositoryId()).getLanguageId();
					if(languageId != null && !languageId.equals(contentMasterLanguageId) && !masterLanguageId.equals(contentMasterLanguageId))
					{
						contentVersion = getContentVersionVO(contentId, contentMasterLanguageId, getOperatingMode(deliveryContext), deliveryContext, db);
					}
				}
			}
			
		}
		
		return contentVersion;
	}

	/**
	 * This method returns that contentVersion which matches the parameters sent in and which 
	 * also has the correct state for this delivery-instance.
	 */
	
	public ContentVersionVO getContentVersionVOInState(Integer contentId, Integer languageId, Integer stateId, Database db, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		if(contentId == null || contentId.intValue() < 1)
			return null;
		
		ContentVersionVO contentVersion = null;
		
		MediumContentImpl content = (MediumContentImpl)getObjectWithId(MediumContentImpl.class, contentId, db);
		boolean isValidContent = isValidContent(infoGluePrincipal, content, languageId, false, false, db, deliveryContext);
		if(isValidContent)
		{
			contentVersion = getContentVersionVO(contentId, languageId, stateId, deliveryContext, db);
		}
		
		return contentVersion;
	}

	/**
	 * This method returns that contentVersion which matches the parameters sent in and which 
	 * also has the correct state for this delivery-instance.
	 */
	
	public List getContentVersionVOList(Database db, Integer siteNodeId, Integer contentId, Integer languageId, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		if(contentId == null || contentId.intValue() < 1)
			return null;
		
		List contentVersionVOList = new ArrayList();
		
		boolean useLanguageFallback = false;
		if(languageId == null)
			useLanguageFallback = true;
				
		MediumContentImpl content = (MediumContentImpl)getObjectWithId(MediumContentImpl.class, contentId, db);
		boolean isValidContent = isValidContent(infoGluePrincipal, content, languageId, useLanguageFallback, false, db, deliveryContext);
		if(isValidContent)
		{
			contentVersionVOList = getContentVersionVOList(content, languageId, getOperatingMode(deliveryContext), deliveryContext, db);
		}
		
		return contentVersionVOList;
	}

	/**
	 * This method gets a contentVersion with a state and a language which is active.
	 */

	private ContentVersion getContentVersion(Content content, Integer languageId, Integer operatingMode, DeliveryContext deliveryContext, Database db) throws Exception
    {
	    ContentVersion contentVersion = null;
		
	    String versionKey = "" + content.getId() + "_" + languageId + "_" + operatingMode + "_contentVersionId";
	    //logger.info("versionKey:" + versionKey);
		
		Object object = CacheController.getCachedObjectFromAdvancedCache("contentVersionIdCache", versionKey);
		if(object instanceof NullObject)
		{
			logger.info("There was an cached parentSiteNodeVO but it was null:" + object);
		}
		else if(object != null)
		{
			Integer contentVersionId = (Integer)object;
			contentVersion = (ContentVersion)getObjectWithId(ContentVersionImpl.class, contentVersionId, db);
		    //logger.info("Loaded the version from cache instead of querying it:" + contentVersionId);
		}
		else
		{
			//logger.info("Querying for verson: " + versionKey); 
			
		    OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.contentId = $1 AND cv.language.languageId = $2 AND cv.stateId >= $3 AND cv.isActive = $4 ORDER BY cv.contentVersionId desc");
	    	oql.bind(content.getId());
	    	oql.bind(languageId);
	    	oql.bind(operatingMode);
	    	oql.bind(true);
	
	    	QueryResults results = oql.execute(Database.ReadOnly);

			if (results.hasMore()) 
	        {
	        	contentVersion = (ContentVersion)results.next();
				CacheController.cacheObjectInAdvancedCache("contentVersionIdCache", versionKey, contentVersion.getId(), new String[]{"contentVersion_" + contentVersion.getId(), "content_" + contentVersion.getValueObject().getContentId()}, true);
	        }
			else
			{
				CacheController.cacheObjectInAdvancedCache("contentVersionIdCache", versionKey, new NullObject(), new String[]{"content_" + content.getId()}, true);
			}

			results.close();
			oql.close();
		}
		
		if(contentVersion != null)
		    deliveryContext.addUsedContentVersion("contentVersion_" + contentVersion.getId());

		return contentVersion;
    }

	/**
	 * This method gets a contentVersion with a state and a language which is active.
	 */

	private ContentVersionVO getContentVersionVO(Integer contentId, Integer languageId, Integer operatingMode, DeliveryContext deliveryContext, Database db) throws Exception
    {
		ContentVersionVO contentVersionVO = null;
		
	    String versionKey = "" + contentId + "_" + languageId + "_" + operatingMode + "_contentVersionVO";
		
		Object object = CacheController.getCachedObjectFromAdvancedCache("contentVersionCache", versionKey);
		if(object instanceof NullObject)
		{
			logger.info("There was an cached contentVersionVO but it was null:" + object);
		}
		else if(object != null)
		{
			if(object instanceof SmallestContentVersionVO)
			{
				logger.warn("Object was instanceof SmallestContentVersionVO for key:" + versionKey);
				contentVersionVO = (ContentVersionVO)getVOWithId(SmallContentVersionImpl.class, ((SmallestContentVersionVO)object).getId(), db);
				CacheController.cacheObjectInAdvancedCache("contentVersionCache", versionKey, contentVersionVO, new String[]{"contentVersion_" + contentVersionVO.getId(), "content_" + contentVersionVO.getContentId()}, true);
			}
			else
				contentVersionVO = (ContentVersionVO)object;
		}
		else
		{
		    String smallVersionKey = "" + contentId + "_" + languageId + "_" + operatingMode + "_smallestContentVersionVO";
			Object smallestContentVersionVOCandidate = CacheController.getCachedObjectFromAdvancedCache("contentVersionCache", smallVersionKey);
			if(smallestContentVersionVOCandidate instanceof NullObject)
			{
				//logger.info("There was an cached content version but it was null:" + smallestContentVersionVOCandidate);
			}
			else if(smallestContentVersionVOCandidate != null)
			{
				contentVersionVO = (ContentVersionVO)getVOWithId(SmallContentVersionImpl.class, ((SmallestContentVersionVO)smallestContentVersionVOCandidate).getId(), db);
	        	
				CacheController.cacheObjectInAdvancedCache("contentVersionCache", versionKey, contentVersionVO, new String[]{"contentVersion_" + contentVersionVO.getId(), "content_" + contentVersionVO.getContentId()}, true);
			}
			else
			{
				//logger.info("Querying for verson: " + versionKey); 
				OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl cv WHERE cv.contentId = $1 AND cv.languageId = $2 AND cv.stateId >= $3 AND cv.isActive = $4 ORDER BY cv.contentVersionId desc");
		    	oql.bind(contentId);
		    	oql.bind(languageId);
		    	oql.bind(operatingMode);
		    	oql.bind(true);
		
		    	QueryResults results = oql.execute(Database.ReadOnly);
		    	
				if (results.hasMore()) 
		        {
					ContentVersion contentVersion = (ContentVersion)results.next();
		        	contentVersionVO = contentVersion.getValueObject();
	
					CacheController.cacheObjectInAdvancedCache("contentVersionCache", versionKey, contentVersionVO, new String[]{"contentVersion_" + contentVersionVO.getId(), "content_" + contentVersionVO.getContentId()}, true);
		        }
				else
				{
					CacheController.cacheObjectInAdvancedCache("contentVersionCache", versionKey, new NullObject(), new String[]{"content_" + contentId}, true);
				}
				
				results.close();
				oql.close();
			}
		}
		
		if(contentVersionVO != null)
		    deliveryContext.addUsedContentVersion("contentVersion_" + contentVersionVO.getId());
		
		return contentVersionVO;
    }

	/**
	 * This method gets a contentVersion with a state and a language which is active.
	 */

	private SmallestContentVersionVO getSmallestContentVersionVO(Integer contentId, Integer languageId, Integer operatingMode, DeliveryContext deliveryContext, Database db) throws Exception
    {
		//Timer t = new Timer();

		SmallestContentVersionVO contentVersionVO = null;
		
	    String versionKey = "" + contentId + "_" + languageId + "_" + operatingMode + "_smallestContentVersionVO";
	    //String versionVOKey = "" + contentId + "_" + languageId + "_" + operatingMode + "_contentVersionVO";
	    
		Object object = CacheController.getCachedObjectFromAdvancedCache("contentVersionCache", versionKey);
		if(object instanceof NullObject)
		{
			logger.info("There was an cached SmallestContentVersionVO but it was null:" + object);
		}
		else if(object != null)
		{
			contentVersionVO = (SmallestContentVersionVO)object;
		}
		else
		{
			//logger.info("Querying for verson: " + versionKey); 
		    OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl cv WHERE cv.contentId = $1 AND cv.languageId = $2 AND cv.stateId >= $3 AND cv.isActive = $4 ORDER BY cv.contentVersionId desc");
	    	oql.bind(contentId);
	    	oql.bind(languageId);
	    	oql.bind(operatingMode);
	    	oql.bind(true);
	
	    	QueryResults results = oql.execute(Database.ReadOnly);

			if (results.hasMore()) 
	        {
				SmallestContentVersion contentVersion = (SmallestContentVersion)results.next();
	        	contentVersionVO = contentVersion.getValueObject();

	        	CacheController.cacheObjectInAdvancedCache("contentVersionCache", versionKey, contentVersionVO, new String[]{"contentVersion_" + contentVersionVO.getId(), "content_" + contentVersionVO.getContentId()}, true);
	        }
			else
			{
				CacheController.cacheObjectInAdvancedCache("contentVersionCache", versionKey, new NullObject(), new String[]{"content_" + contentId}, true);
			}

			results.close();
			oql.close();
		}

		if(contentVersionVO != null)
		    deliveryContext.addUsedContentVersion("contentVersion_" + contentVersionVO.getId());

		return contentVersionVO;
    }

	private List getContentVersionVOList(Content content, Integer languageId, Integer operatingMode, DeliveryContext deliveryContext, Database db) throws Exception
    {
	    List contentVersionVOList = new ArrayList();
		
	    OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.contentId = $1 AND cv.language.languageId = $2 AND cv.stateId >= $3 AND cv.isActive = $4 ORDER BY cv.contentVersionId desc");
    	if(languageId == null)
    		oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.contentId = $1 AND cv.stateId >= $2 AND cv.isActive = $3 ORDER BY cv.contentVersionId desc");
    
    	oql.bind(content.getId());
	    
	    if(languageId != null)
	    	oql.bind(languageId);
    	
	    oql.bind(operatingMode);
    	oql.bind(true);

    	QueryResults results = oql.execute(Database.ReadOnly);
		
    	ContentVersion contentVersion;
    	
		while(results.hasMore()) 
        {
        	contentVersion = (ContentVersion)results.next();

        	if(contentVersion != null)
    		    deliveryContext.addUsedContentVersion("contentVersion_" + contentVersion.getId());

    		contentVersionVOList.add(contentVersion.getValueObject());
        }

		results.close();
		oql.close();
				
		return contentVersionVOList;
    }

	
	
	/**
	 * This is the most common way of getting attributes from a content. 
	 * It selects the correct contentVersion depending on the language and then gets the attribute in the xml associated.
	 */

	public String getContentAttribute(Database db, Integer contentId, Integer languageId, String attributeName, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infogluePrincipal, boolean escapeHTML) throws SystemException, Exception
	{	    	        
		return getContentAttribute(db, contentId, languageId, attributeName, siteNodeId, useLanguageFallback, deliveryContext, infogluePrincipal, escapeHTML, false, null);
	}

	/**
	 * This is the most common way of getting attributes from a content. 
	 * It selects the correct contentVersion depending on the language and then gets the attribute in the xml associated.
	 */

	public String getContentAttribute(Database db, Integer contentId, Integer languageId, String attributeName, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infogluePrincipal, boolean escapeHTML, boolean isMetaInfoQuery) throws SystemException, Exception
	{	    	        
		return getContentAttribute(db, contentId, languageId, attributeName, siteNodeId, useLanguageFallback, deliveryContext, infogluePrincipal, escapeHTML, isMetaInfoQuery, null);
	}

	/**
	 * This is the most common way of getting attributes from a content. 
	 * It selects the correct contentVersion depending on the language and then gets the attribute in the xml associated.
	 */

	public String getContentAttribute(Database db, Integer contentId, Integer languageId, String attributeName, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infogluePrincipal, boolean escapeHTML, Set usedContentVersionId) throws SystemException, Exception
	{	
		return getContentAttribute(db, contentId, languageId, attributeName, siteNodeId, useLanguageFallback, deliveryContext, infogluePrincipal, escapeHTML, false, usedContentVersionId);
	}
	
	/**
	 * This method return true if the user logged in has access to the content sent in.
	 */
/*
 	public boolean getHasUserContentAccess(Database db, InfoGluePrincipal infoGluePrincipal, Integer contentId)
	{
		boolean hasUserContentAccess = true;
		
		try 
		{
		    if(contentId != null)
		    {
				logger.info("IsProtected:" + protectedContentId);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Read", protectedContentId.toString()))
				{
				    hasUserContentAccess = false;
				}
		    }
		} 
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get determine if content:" + contentId + " has a localized version:" + e.getMessage());
		}
		
		return hasUserContentAccess;
	}
*/

	public boolean getHasUserContentAccess(Database db, InfoGluePrincipal infoGluePrincipal, Integer contentId)
	{
		String key = "" + infoGluePrincipal.getName() + "_" + contentId + "_HasUserContentAccess";
		logger.info("key:" + key);
		Boolean hasUserContentAccess = (Boolean)CacheController.getCachedObjectFromAdvancedCache("personalAuthorizationCache", key);
		if(hasUserContentAccess != null)
		{
			//System.out.println("Cached");
			return hasUserContentAccess.booleanValue();
		}
		else
		{
			hasUserContentAccess = true;
			//System.out.println("----- not Cached");
			try 
			{
			    if(contentId != null)
			    {
					Integer protectedContentId = ContentDeliveryController.getContentDeliveryController().getProtectedContentId(db, contentId);
					logger.info("IsProtected:" + protectedContentId);
					if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Read", protectedContentId.toString()))
					{
					    hasUserContentAccess = false;
					}
			    }
			} 
			catch(Exception e)
			{
				logger.warn("An error occurred trying to get determine if user was allowed read access to:" + contentId + ":" + e.getMessage());
			}

			CacheController.cacheObjectInAdvancedCache("personalAuthorizationCache", key, new Boolean(hasUserContentAccess));
		}
		
		return hasUserContentAccess;
	}

	/**
	 * This is the most common way of getting attributes from a content. 
	 * It selects the correct contentVersion depending on the language and then gets the attribute in the xml associated.
	 */

	public String getContentAttribute(Database db, Integer contentId, Integer languageId, String attributeName, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infogluePrincipal, boolean escapeHTML, boolean isMetaInfoQuery, Set usedContentVersionId) throws SystemException, Exception
	{	
		if(contentId == null || contentId.intValue() < 1)
			return "";
		
		boolean isTemplateQuery = false;
		if(attributeName.equalsIgnoreCase("Template") || attributeName.equalsIgnoreCase("PreTemplate") || attributeName.equalsIgnoreCase("ComponentLabels"))
			isTemplateQuery = true;
		
		//logger.info("usedContentVersionId:" + usedContentVersionId);
		String enforceRigidContentAccess = CmsPropertyHandler.getEnforceRigidContentAccess();
		if(enforceRigidContentAccess != null && enforceRigidContentAccess.equalsIgnoreCase("true") && !isMetaInfoQuery)
		{
			//System.out.println("Enforcing getHasUserContentAccess for attributeName:" + contentId + ":" + languageId + ":" + attributeName);
			boolean hasUserContentAccess = getHasUserContentAccess(db, infogluePrincipal, contentId);
			if(!hasUserContentAccess)
			{
				return "";
			}
		}
		
		StringBuilder attributeKeySB = new StringBuilder();
		StringBuilder versionKeySB = new StringBuilder();
		
		if(!isMetaInfoQuery && !isTemplateQuery)
			attributeKeySB.append("")
			.append(contentId).append("_")
			.append(languageId).append("_")
			.append(attributeName).append("_")
			.append(siteNodeId).append("_")
			.append(useLanguageFallback).append("_")
			.append(escapeHTML);

		else
			attributeKeySB.append("")
			.append(contentId).append("_")
			.append(languageId).append("_")
			.append(attributeName).append("_")
			.append(useLanguageFallback).append("_")
			.append(escapeHTML);

		if(!isMetaInfoQuery && !isTemplateQuery)
			versionKeySB.append("")
			.append(contentId).append("_")
			.append(languageId).append("_")
			.append(siteNodeId).append("_");

		else
			versionKeySB.append("")
			.append(contentId).append("_")
			.append(languageId).append("_");

		String attributeKey = attributeKeySB.toString();
		String versionKey = versionKeySB.append("_contentVersionId").toString();
		
		String matcher = "";
		String cacheName = "contentAttributeCache" + matcher;
		String contentVersionIdCacheName = "contentVersionIdCache" + matcher;
		
		String attribute = (String)CacheController.getCachedObjectFromAdvancedCache(cacheName, attributeKey);
		Integer contentVersionId = null;
		
	    try
	    {
			if(attribute != null)
			{
				contentVersionId = (Integer)CacheController.getCachedObjectFromAdvancedCache(contentVersionIdCacheName, versionKey);
				//logger.info("There was an cached content attribute:" + attribute);
			}
			else
			{
				ContentVersionVO contentVersionVO = getContentVersionVO(db, siteNodeId, contentId, languageId, useLanguageFallback, deliveryContext, infogluePrincipal);
			   
	        	if (contentVersionVO != null) 
				{
				    attribute = getAttributeValue(db, contentVersionVO, attributeName, escapeHTML);	
					contentVersionId = contentVersionVO.getId();
				}
				else
				{
					attribute = "";
				}
	        	
	        	StringBuilder groupKey1 = new StringBuilder("contentVersion_").append(contentVersionId);
	        	StringBuilder groupKey2 = new StringBuilder("content_").append(contentId);
	        	
	        	CacheController.cacheObjectInAdvancedCache(cacheName, attributeKey, attribute, new String[]{groupKey1.toString(), groupKey2.toString()}, true);
	    		if(contentVersionId != null)
				{
    				CacheController.cacheObjectInAdvancedCache(contentVersionIdCacheName, versionKey, contentVersionId, new String[]{groupKey1.toString(), groupKey2.toString()}, true);
				}
			}
			
			if(deliveryContext != null)
			{
				deliveryContext.addUsedContentVersion("contentVersion_" + contentVersionId);
				if(isMetaInfoQuery && contentVersionId != null)
					deliveryContext.getUsedPageMetaInfoContentVersionIdSet().add(contentVersionId);
				if(attributeName.equals("ComponentStructure"))
					deliveryContext.getUsedPageComponentsMetaInfoContentVersionIdSet().add(contentVersionId);
			}
	
			if(usedContentVersionId != null && contentVersionId != null)
			    usedContentVersionId.add(contentVersionId);
	    }
	    catch(Exception e)
	    {
	        throw e;
	    }
	    
		return (attribute == null) ? "" : attribute;
	}


	/**
	 * This is the most common way of getting attributes from a content. 
	 * It selects the correct contentVersion depending on the language and then gets the attribute in the xml associated.
	 */

	public String getContentAttribute(Database db, ContentVersionVO contentVersionVO, String attributeName, boolean escapeHTML) throws SystemException, Exception
	{
		String attribute = getAttributeValue(db, contentVersionVO, attributeName, escapeHTML);		
		
		return attribute;
	}

	/**
	 * Find all ContentVersionVOs that are related to the provided Category.
	 *
	 * TODO: Right now this method depends on the ContentVersion having an owningContent
	 * TODO: This is potentially bad from a performance standpoint app-wide, so a workaround may
	 * TODO: be to look up each Content for the ContentVersions after we have done everything we
	 * TODO: can to wed down the list alot, so the overhead will not be too much.
	 *
	 * @param categoryId The Category to search on
	 * @param attributeName The attribute of the Category relationship
	 * @param infoGluePrincipal The user making the request
	 * @param siteNodeId The SiteNode that the request is coming from
	 * @param languageId The Language of the request
	 * @param useLanguageFallback True is the search is to use the fallback (default) language for the Repository
	 * @return A List of ContentVersionVOs matching the Category search, that are considered valid
	 * @throws SystemException
	 */
	public List findContentVersionVOsForCategory(Database db, Integer categoryId, String attributeName, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean useLanguageFallback, DeliveryContext deliveryContext) throws SystemException, Exception
	{
	    deliveryContext.addUsedContent("selectiveCacheUpdateNonApplicable");
	    
		List results = findContentCategories(db, categoryId, attributeName);
		List versions = findContentVersionsForCategories(results, db);

		// Weed out irrelevant versions
		for (Iterator iter = versions.iterator(); iter.hasNext();)
		{
			ContentVersion version = (ContentVersion) iter.next();
			if(!isValidContentVersion(version, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, db, deliveryContext))
				iter.remove();
		}

		return toVOList(versions);
	}

	public List getAssignedCategoryVOsForContentVersionId(Database db, Integer contentId, Integer languageId, String categoryKey, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws Exception
	{
		List assignedCategoryVOList = new ArrayList();
		
		ContentVersion contentVersion = getContentVersion(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		
		List assignedContentCategories = ContentCategoryController.getController().findByContentVersionAttribute(categoryKey, contentVersion, db, true);
		//List assignedContentCategories = findContentCategoriesForContentVersionId(db, contentVersionVO.getId(), categoryKey, deliveryContext);
		if((assignedCategoryVOList == null || assignedCategoryVOList.size() == 0) && useLanguageFallback)
		{
			LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(db, siteNodeId);
			contentVersion = getContentVersion(siteNodeId, contentId, masterLanguageVO.getLanguageId(), db, useLanguageFallback, deliveryContext, infoGluePrincipal);
			//assignedContentCategories = findContentCategoriesForContentVersionId(db, contentVersionVO.getId(), categoryKey, deliveryContext);
			assignedContentCategories = ContentCategoryController.getController().findByContentVersionAttribute(categoryKey, contentVersion, db, true);
		}
		
		Iterator assignedContentCategoriesIterator = assignedContentCategories.iterator();
		while(assignedContentCategoriesIterator.hasNext())
		{
			ContentCategory contentCategory = (ContentCategory)assignedContentCategoriesIterator.next();
			assignedCategoryVOList.add(contentCategory.getCategory().getValueObject());
		}
		
		return assignedCategoryVOList;
	}
	
	
	/**
	 * Find all CategoryVOs that are related to the provided content version on a specific category key.
	 *
	 * @param contentVersionId The content version id to search on
	 * @param categoryKey The attribute of the Category relationship
	 * @param infoGluePrincipal The user making the request
	 * @param siteNodeId The SiteNode that the request is coming from
	 * @param languageId The Language of the request
	 * @param useLanguageFallback True is the search is to use the fallback (default) language for the Repository
	 * @return A List of ContentVersionVOs matching the Category search, that are considered valid
	 * @throws SystemException
	 */
	public List findContentCategoryVOsForContentVersionId(Database db, Integer contentVersionId, String categoryKey, DeliveryContext deliveryContext) throws SystemException, Exception
	{
	    List contentCategories = findContentVersionCategories(db, contentVersionId, categoryKey);
		
		return toVOList(contentCategories);
	}

	/**
	 * Find all CategoryVOs that are related to the provided content version on a specific category key.
	 *
	 * @param contentVersionId The content version id to search on
	 * @param categoryKey The attribute of the Category relationship
	 * @param infoGluePrincipal The user making the request
	 * @param siteNodeId The SiteNode that the request is coming from
	 * @param languageId The Language of the request
	 * @param useLanguageFallback True is the search is to use the fallback (default) language for the Repository
	 * @return A List of ContentVersionVOs matching the Category search, that are considered valid
	 * @throws SystemException
	 */
	public List findContentCategoriesForContentVersionId(Database db, Integer contentVersionId, String categoryKey, DeliveryContext deliveryContext) throws SystemException, Exception
	{
	    List contentCategories = findContentVersionCategories(db, contentVersionId, categoryKey);
		
		return contentCategories;
	}

	/**
	 * Find all ContentCategories for the given Category id and attributeName.
	 * @param categoryId The Category to find ContentCategories
	 * @param attributeName The ContentTYpeDefintion attribute name of a ContentCategory relationship.
	 * @return A List of ContentCategoryVOs for the supplied Category id.
	 * @throws SystemException If an error happens
	 */
	private List findContentCategories(Database db, Integer categoryId, String attributeName) throws SystemException, Exception
	{
		StringBuffer oql = new StringBuffer();
		oql.append("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentCategoryImpl c ");
		oql.append("WHERE c.category.categoryId = $1 AND c.attributeName = $2");

		ArrayList params = new ArrayList();
		params.add(categoryId);
		params.add(attributeName);
		return toVOList(executeQuery(db, oql.toString(), params));
	}

	/**
	 * Find all ContentCategories for the given Contentversion id and categoryKey.
	 * @param contentVersionId The contentVersionId to find ContentCategories on
	 * @param categoryKey The ContentTYpeDefintion attribute name of a ContentCategory relationship.
	 * @return A List of ContentCategoryVOs for the supplied content version id.
	 * @throws SystemException If an error happens
	 */
	private List findContentVersionCategories(Database db, Integer contentVersionId, String categoryKey) throws SystemException, Exception
	{
		StringBuffer oql = new StringBuffer();
		oql.append("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentCategoryImpl c ");
		oql.append("WHERE c.contentVersion.contentVersionId = $1 AND c.attributeName = $2");

		ArrayList params = new ArrayList();
		params.add(contentVersionId);
		params.add(categoryKey);
		return executeQuery(db, oql.toString(), params);
	}

	/**
	 * Find content versions that are in the provided list of version ids. However over time this
	 * could get to be a large list, so lets weed it out initially at the database restricted
	 * on the time parameters. That should keep the lists manageable
	 *
	 * @param contentCategories A ContentCategoryVO list used to find related ContentVersions
	 * @param db A Database to execute the query against
	 * @return A List of ContentVersions that were related to the provided ContentCategories and
	 * 			fell withing the publishing time frame
	 * @throws Exception if an error happens
	 */
	private List findContentVersionsForCategories(List contentCategories, Database db) throws Exception
	{
		if(contentCategories.isEmpty())
			return Collections.EMPTY_LIST;

		/*
		StringBuffer oql = new StringBuffer();
		oql.append("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl c ")
				.append("WHERE c.owningContent.publishDateTime <= $1 AND c.owningContent.expireDateTime >= $2 ")
				.append("AND c.contentVersionId IN LIST ").append(toVersionIdList(contentCategories));
        */
		
		StringBuffer oql = new StringBuffer();
		oql.append("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl c ")
				.append("WHERE c.contentVersionId IN LIST ").append(toVersionIdList(contentCategories));

		ArrayList params = new ArrayList();
		//params.add(new Date());
		//params.add(new Date());
		return  executeQuery(db, oql.toString(), params);
	}

	/**
	 * Is this a valid Content item based on defined rules for publican/expiration etc.,
	 * and is it the most recent ContentVersion for this deployment. If not then we retrieved
	 * based on categories attached to an old version.
	 */
	private boolean isValidContentVersion(ContentVersion version, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean useLanguageFallback, Database db, DeliveryContext deliveryContext) throws Exception
	{
		//Content content = version.getOwningContent();
	    Integer contentId = version.getValueObject().getContentId();
	    logger.info("contentId:" + contentId);
	    
	    Content content = (MediumContentImpl)getObjectWithId(MediumContentImpl.class, contentId, db);
	    //Content content = ContentController.getContentController().getContentWithId(contentId, db);
	    
	    SmallestContentVersionVO mostRecentVersion = getSmallestContentVersionVO(db, siteNodeId, content.getContentId(), languageId, useLanguageFallback, deliveryContext, infoGluePrincipal);
		boolean isProperVersion = (mostRecentVersion != null) && (mostRecentVersion.getId().equals(version.getId()));

		boolean isValidContent = isValidContent(infoGluePrincipal, content, languageId, useLanguageFallback, false, db, deliveryContext);

		return isProperVersion && isValidContent;
	}

	/**
	 * Builds and IN list for the query to find all potentially relevant content versions.
	 */
	private String toVersionIdList(List results)
	{
		StringBuffer ids = new StringBuffer("(");
		for(Iterator iter = results.iterator(); iter.hasNext();)
			ids.append(((ContentCategoryVO) iter.next()).getContentVersionId() + (iter.hasNext()? ", " : ""));
		ids.append(")");
		return ids.toString();
	}


	/**
	 * This method returns all the assetsKeys available in a contentVersion.
	 */

	public Collection getAssetKeys(Database db, Integer contentId, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		Collection assetKeys = new ArrayList();
		
		SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		if (contentVersion != null) 
        {
			Collection assets = getDigitalAssetVOList(contentVersion.getId(), db);
        	Iterator keysIterator = assets.iterator();
        	while(keysIterator.hasNext())
        	{
        		DigitalAssetVO asset = (DigitalAssetVO)keysIterator.next();
        		String assetKey = asset.getAssetKey();
            	assetKeys.add(assetKey); 		
        	}
        }
		
		return assetKeys;
	}

	/**
	 * This method returns all the assetsKeys available in a contentVersion.
	 */

	public Collection getAssetIds(Database db, Integer contentId, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		Collection assetIds = new ArrayList();
		
		SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		if (contentVersion != null) 
        {
			Collection assets = getDigitalAssetVOList(contentVersion.getId(), db);
        	Iterator keysIterator = assets.iterator();
        	while(keysIterator.hasNext())
        	{
        		DigitalAssetVO asset = (DigitalAssetVO)keysIterator.next();
        		assetIds.add(asset.getId()); 		
        	}
        }
		
		return assetIds;
	}

	/**
	 * This method returns all the assets available in a contentVersion.
	 */

	public List getAssets(Database db, Integer contentId, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		List digitalAssetVOList = new ArrayList();
		
		SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		if (contentVersion != null) 
        {
			digitalAssetVOList = getDigitalAssetVOList(contentVersion.getId(), db);
        }
		
		return digitalAssetVOList;
	}

	/**
	 * This method returns all the assets available in a contentVersion.
	 */

	public DigitalAssetVO getAsset(Database db, Integer contentId, Integer languageId, String assetKey, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		DigitalAssetVO digitalAssetVO = null;
		
		SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		if (contentVersion != null) 
        {
			Collection assets = getDigitalAssetVOList(contentVersion.getId(), db);
        	Iterator keysIterator = assets.iterator();
        	while(keysIterator.hasNext())
        	{
        		DigitalAssetVO asset = (DigitalAssetVO)keysIterator.next();
        		if(asset.getAssetKey().equalsIgnoreCase(assetKey))
        		{
        			digitalAssetVO = asset;
        			break;
        		}
        	}
        }
		
		return digitalAssetVO;
	}

	
	/**
	 * This method should return a list of those digital assets the contentVersion has.
	 */
	   	
	public List getDigitalAssetVOList(Integer contentVersionId, Database db) throws Exception
    {
		String key = "all_" + contentVersionId;
		String cacheName = "digitalAssetCache";
		List digitalAssetVOList = (List)CacheController.getCachedObject(cacheName, key);
		if(digitalAssetVOList != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached digitalAssetVOList:" + digitalAssetVOList);
			
			return digitalAssetVOList;
		}

		digitalAssetVOList = new ArrayList();
    	
		if(logger.isInfoEnabled())
			logger.info("Making a sql call for assets on " + contentVersionId);

    	OQLQuery oql = db.getOQLQuery("CALL SQL SELECT c.digitalAssetId, c.assetFileName, c.assetKey, c.assetFilePath, c.assetContentType, c.assetFileSize FROM cmDigitalAsset c, cmContentVersionDigitalAsset cvda where cvda.digitalAssetId = c.digitalAssetId AND cvda.contentVersionId = $1 ORDER BY c.digitalAssetId AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl");
    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    		oql = db.getOQLQuery("CALL SQL SELECT c.DigAssetId, c.assetFileName, c.assetKey, c.assetFilePath, c.assetContentType, c.assetFileSize FROM cmDigAsset c, cmContVerDigAsset cvda where cvda.DigAssetId = c.DigAssetId AND cvda.ContVerId = $1 ORDER BY c.DigAssetId AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl");

    	oql.bind(contentVersionId);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		while(results.hasMore()) 
        {
        	SmallDigitalAssetImpl digitalAsset = (SmallDigitalAssetImpl)results.next();
        	digitalAssetVOList.add(digitalAsset.getValueObject());
        }
		
		results.close();
		oql.close();
    	
		if(digitalAssetVOList != null)
			CacheController.cacheObject(cacheName, key, digitalAssetVOList);
		
		return digitalAssetVOList;
    }

	/**
	 * This method is used by the getAssetUrl methods, to locate a digital asset in another
	 * languageversion. It is called in the case where no asset where found in the supplied language.
	 * 
	 * This way an image is only required to exist in one of the language versions, reducing the need for 
	 * many duplicates.
	 *  
	 */
	/*
	private DigitalAsset getLanguageIndependentAsset(Integer contentId, Integer languageId, Integer siteNodeId, Database db, String assetKey, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		DigitalAsset asset = null;
		// TODO: This method should only return a asset url depending on settings on the actual content in the future
		// or possibly a systemwide setting.
		
		// TODO: experimental
		// addition ss - 030422
		// Search digital asset among language versions.
		List langs = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguages(db, siteNodeId);
		Iterator lit = langs.iterator();
		while (lit.hasNext())
		{
			LanguageVO langVO = (LanguageVO) lit.next();
			if (langVO.getLanguageId().compareTo(languageId)!=0)
			{
				ContentVersion contentVersion = getContentVersion(siteNodeId, contentId, langVO.getLanguageId(), db, false, deliveryContext, infoGluePrincipal);
				if (contentVersion != null) 
				{
					DigitalAsset digitalAsset = (assetKey == null) ? getLatestDigitalAsset(contentVersion) :getDigitalAssetWithKey(contentVersion, assetKey); 
					
					if(digitalAsset != null)
					{
						asset = digitalAsset;
						break;
					}
				}									
			}
		}
		return asset;			
	}
	*/

	/**
	 * This method is used by the getAssetUrl methods, to locate a digital asset in another
	 * languageversion. It is called in the case where no asset where found in the supplied language.
	 * 
	 * This way an image is only required to exist in one of the language versions, reducing the need for 
	 * many duplicates.
	 *  
	 */
	private DigitalAssetVO getLanguageIndependentAssetVO(Integer contentId, Integer languageId, Integer siteNodeId, Database db, String assetKey, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		DigitalAssetVO asset = null;
		// TODO: This method should only return a asset url depending on settings on the actual content in the future
		// or possibly a systemwide setting.
		
		// TODO: experimental
		// addition ss - 030422
		// Search digital asset among language versions.
		List langs = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguages(db, siteNodeId);
		Iterator lit = langs.iterator();
		while (lit.hasNext())
		{
			LanguageVO langVO = (LanguageVO) lit.next();
			if (langVO.getLanguageId().compareTo(languageId)!=0)
			{
				SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, langVO.getLanguageId(), db, false, deliveryContext, infoGluePrincipal);
				if (contentVersion != null) 
				{
					DigitalAssetVO digitalAsset = (assetKey == null) ? DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getId(), db) : DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getId(), assetKey, db);
					
					if(digitalAsset != null)
					{
						asset = digitalAsset;
						break;
					}
				}									
			}
		}

		if(asset == null)
		{
			ContentVO contentVO = getContentVO(db, contentId, deliveryContext);
			SiteNodeVO siteNodeVO = SiteNodeController.getSiteNodeVOWithId(siteNodeId, db);

			if(!contentVO.getRepositoryId().equals(siteNodeVO.getRepositoryId()))
			{
				List contentRepositoryLangs = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguagesForRepository(db, contentVO.getRepositoryId());
				Iterator contentRepositoryLangsIterator = contentRepositoryLangs.iterator();
				while (contentRepositoryLangsIterator.hasNext())
				{
					LanguageVO langVO = (LanguageVO) contentRepositoryLangsIterator.next();
					if (langVO.getLanguageId().compareTo(languageId)!=0)
					{
						SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, langVO.getLanguageId(), db, false, deliveryContext, infoGluePrincipal);
						if (contentVersion != null) 
						{
							DigitalAssetVO digitalAsset = (assetKey == null) ? DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getId(), db) : DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getId(), assetKey, db);
							
							if(digitalAsset != null)
							{
								asset = digitalAsset;
								break;
							}
						}									
					}
				}
			}
		}
		
		return asset;			
	}

	private String getLanguageIndependentAssetUrl(Integer contentId, Integer languageId, Integer siteNodeId, Database db, String assetKey, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		String assetUrl = "";
		assetUrl = urlComposer.composeDigitalAssetUrl("", null, "", deliveryContext); 
		
		DigitalAssetVO digitalAssetVO = getLanguageIndependentAssetVO(contentId, languageId, siteNodeId, db, assetKey, deliveryContext, infoGluePrincipal);
		if(digitalAssetVO != null)
		{
			String fileName = digitalAssetVO.getDigitalAssetId() + "_" + digitalAssetVO.getAssetFileName();
			
			int i = 0;
			File masterFile = null;

			String folderName = "" + (digitalAssetVO.getDigitalAssetId().intValue() / 1000);
			logger.info("folderName:" + folderName);
			String filePath = CmsPropertyHandler.getDigitalAssetPath0() + File.separator + folderName;

			while(filePath != null)
			{
				try
				{
					if(masterFile == null)
				        masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAssetVO, fileName, filePath, db);
					else
					    DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(masterFile, fileName, filePath);
				}
				catch(Exception e)
				{
					logger.warn("An file could not be written:" + e.getMessage(), e);
				}
			    
			    i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			    if(filePath != null)
			    	filePath += File.separator + folderName;
			}
			//String filePath = CmsPropertyHandler.getDigitalAssetPath();
			//DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath);
		
			SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getSiteNode(db, siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
				dnsName = siteNode.getRepository().getDnsName();
				
			//assetUrl = dnsName + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + fileName;
			if(deliveryContext.getUseDownloadAction())
				assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, siteNodeId, contentId, languageId, assetKey, deliveryContext, db);
			else
				assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, folderName, fileName, deliveryContext); 
		}
		return assetUrl;	
	}


	private String getLanguageIndependentAssetThumbnailUrl(Integer contentId, Integer languageId, Integer siteNodeId, Database db, String assetKey, int width, int height, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		String assetUrl = "";
		assetUrl = urlComposer.composeDigitalAssetUrl("", null, "", deliveryContext); 
		
		DigitalAssetVO digitalAsset = getLanguageIndependentAssetVO(contentId, languageId, siteNodeId, db, assetKey, deliveryContext, infoGluePrincipal);
		if(digitalAsset != null)
		{
			String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
			String thumbnailFileName = "thumbnail_" + width + "_" + height + "_" + fileName;

			int i = 0;
			File masterFile = null;
			File masterThumbFile = null;
			String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
			logger.info("folderName:" + folderName);
			String filePath = CmsPropertyHandler.getDigitalAssetPath0() + File.separator + folderName;
			while(filePath != null)
			{
			    if(masterFile == null)
			        masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath, db);
				else
				    DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(masterFile, fileName, filePath);

			    if(masterThumbFile == null)
			        masterThumbFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(fileName, thumbnailFileName, filePath, width, height);
			    else
			        DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(fileName, thumbnailFileName, filePath, width, height);
			    /*
			    if(masterFile == null)
			        masterThumbFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(fileName, thumbnailFileName, filePath, width, height);
				else
				    DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(fileName, thumbnailFileName, filePath, width, height);
			    */
			    
			    i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			    if(filePath != null)
			    	filePath += File.separator + folderName;
			}

			//String filePath = CmsPropertyHandler.getDigitalAssetPath();
			//DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(digitalAsset, fileName, thumbnailFileName, filePath, width, height);
			
			SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getSiteNode(db, siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
				dnsName = siteNode.getRepository().getDnsName();
				
			//assetUrl = dnsName + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + thumbnailFileName;
			assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, folderName, thumbnailFileName, deliveryContext); 
		}
		return assetUrl;	
	}


	/**
	 * This method returns the id of the digital asset. 
	 * It selects the correct contentVersion depending on the language and then gets the digitalAsset associated.
	 */

	public Integer getDigitalAssetId(Database db, Integer contentId, Integer languageId, String assetKey, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
	    Integer digitalAssetId = null;
		
	    SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		if (contentVersion != null) 
        {
        	DigitalAssetVO digitalAsset = DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getContentVersionId(), assetKey, db);
			
			if(digitalAsset != null)
			{
			    digitalAssetId = digitalAsset.getId();
			}
        }
            		
		return digitalAssetId;
	}

	/**
	 * This is the basic way of getting an asset-url for a content. 
	 * It selects the correct contentVersion depending on the language and then gets the digitalAsset associated.
	 * If the asset is cached on disk it returns that path imediately it's ok - otherwise it dumps it fresh.
	 */

	public String getAssetUrl(Database db, Integer contentId, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
	    String assetCacheKey = "" + languageId + "_" + contentId + "_" + siteNodeId + "_" + useLanguageFallback;
		logger.info("assetCacheKey:" + assetCacheKey);
		String cacheName = "assetUrlCache";
		String cachedAssetUrl = (String)CacheController.getCachedObject(cacheName, assetCacheKey);
		if(cachedAssetUrl != null)
		{
			logger.info("There was an cached cachedAssetUrl:" + cachedAssetUrl);
			return cachedAssetUrl;
		}
		
		String assetUrl = "";
		
		SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		if (contentVersion != null) 
        {
        	DigitalAssetVO digitalAsset = DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getId(), db);
			
			if(digitalAsset != null)
			{
				String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();

				int i = 0;
				File masterFile = null;
				String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
				logger.info("folderName:" + folderName);
				String filePath = CmsPropertyHandler.getDigitalAssetPath0() + File.separator + folderName;
				while(filePath != null)
				{
					try
					{
					    if(masterFile == null)
					        masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath, db);
					    else
					        DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(masterFile, fileName, filePath);
					}
					catch(Exception e)
					{
						logger.warn("An file could not be written:" + e.getMessage(), e);
					}
					
				    i++;
					filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
				    if(filePath != null)
				    	filePath += File.separator + folderName;
				}

				//String filePath = CmsPropertyHandler.getDigitalAssetPath();
				//DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath);
				
				SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getSiteNode(db, siteNodeId);
				String dnsName = CmsPropertyHandler.getWebServerAddress();
				if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
					dnsName = siteNode.getRepository().getDnsName();

				//assetUrl = dnsName + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + fileName;
				assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, folderName, fileName, deliveryContext); 
			}
			else
			{
				assetUrl = getLanguageIndependentAssetUrl(contentId, languageId, siteNodeId, db, null, deliveryContext, infoGluePrincipal);
			}
        }
            		
        CacheController.cacheObject(cacheName, assetCacheKey, assetUrl);
        
		return assetUrl;
	}

	/**
	 * This is the basic way of getting an asset-url for a content. 
	 * It selects the correct contentVersion depending on the language and then gets the digitalAsset associated.
	 * If the asset is cached on disk it returns that path imediately it's ok - otherwise it dumps it fresh.
	 */

	public String getAssetUrl(Database db, Integer digitalAssetId, Integer siteNodeId, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
	    String assetCacheKey = "" + digitalAssetId + "_" + siteNodeId ;
		logger.info("assetCacheKey:" + assetCacheKey);
		String cacheName = "assetUrlCache";
		String cachedAssetUrl = (String)CacheController.getCachedObject(cacheName, assetCacheKey);
		if(cachedAssetUrl != null)
		{
			logger.info("There was an cached cachedAssetUrl:" + cachedAssetUrl);
			return cachedAssetUrl;
		}
		
		String assetUrl = "";
		
    	DigitalAssetVO digitalAsset = DigitalAssetController.getSmallDigitalAssetVOWithId(digitalAssetId, db);
		if(digitalAsset != null)
		{
			String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();

			int i = 0;
			File masterFile = null;
			String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
			logger.info("folderName:" + folderName);
			String filePath = CmsPropertyHandler.getDigitalAssetPath0() + File.separator + folderName;
			while(filePath != null)
			{
				try
				{
				    if(masterFile == null)
				        masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath, db);
				    else
				        DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(masterFile, fileName, filePath);
				}
				catch(Exception e)
				{
					logger.warn("An file could not be written:" + e.getMessage(), e);
				}
				
			    i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			    if(filePath != null)
			    	filePath += File.separator + folderName;
			}

			SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(null, null, null).getSiteNode(db, siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
				dnsName = siteNode.getRepository().getDnsName();

			assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, folderName, fileName, deliveryContext); 
		}
            		
        CacheController.cacheObject(cacheName, assetCacheKey, assetUrl);
        
		return assetUrl;
	}


	/**
	 * This is the basic way of getting an asset-url for a content. 
	 * It selects the correct contentVersion depending on the language and then gets the digitalAsset associated with the key.
	 * If the asset is cached on disk it returns that path imediately it's ok - otherwise it dumps it fresh.
	 */

	public String getAssetUrl(Database db, Integer contentId, Integer languageId, String assetKey, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		if(contentId == null || contentId.intValue() < 1)
			return "";

	    SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId, db);

	    String assetCacheKey = "" + languageId + "_" + contentId + "_" + siteNodeVO.getRepositoryId() + "_" + assetKey + "_" + useLanguageFallback + "_" + deliveryContext.getUseFullUrl() + "_" + deliveryContext.getUseDownloadAction();
	    
	    if(logger.isInfoEnabled())
	    	logger.info("assetCacheKey:" + assetCacheKey);
	    
	    assetKey = URLDecoder.decode(assetKey, "utf-8");
	    
		String cacheName = "assetUrlCache";
		String cachedAssetUrl = (String)CacheController.getCachedObject(cacheName, assetCacheKey);
		if(cachedAssetUrl != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached cachedAssetUrl:" + cachedAssetUrl);
			
			return cachedAssetUrl;
		}
		
		String assetUrl = "";
		assetUrl = urlComposer.composeDigitalAssetUrl("", null, "", deliveryContext); 
		
		SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		ContentVO contentVO = this.getContentVO(db, contentId, deliveryContext);
		LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(contentVO.getRepositoryId(), db);
		if(logger.isInfoEnabled())
		{
			logger.info("languageId:" + languageId);
			logger.info("masterLanguageVO:" + masterLanguageVO);
		}
		/*
		if(deliveryContext.getUseDownloadAction())
		{
			SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getSiteNode(db, siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
				dnsName = siteNode.getRepository().getDnsName();

			return urlComposer.composeDigitalAssetUrl(dnsName, siteNodeId, contentId, languageId, assetKey, deliveryContext);
		}
		*/
		
		boolean isUnprotectedAsset = getHasUserContentAccess(db, UserControllerProxy.getController().getUser(CmsPropertyHandler.getAnonymousUser()), contentId);
		
		if(!isUnprotectedAsset)
		{
			SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getSiteNode(db, siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
				dnsName = siteNode.getRepository().getDnsName();

			return urlComposer.composeDigitalAssetUrl(dnsName, siteNodeId, contentId, languageId, assetKey, deliveryContext, db);
		}
		else if(contentVersion != null) 
        {
        	DigitalAssetVO digitalAsset = DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getId(), assetKey, db);
			
			if(digitalAsset != null)
			{
				String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();

				int i = 0;
				File masterFile = null;
				String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
				logger.info("folderName:" + folderName);
				String filePath = CmsPropertyHandler.getDigitalAssetPath0() + File.separator + folderName;
				while(filePath != null)
				{
					try
					{
					    if(masterFile == null)
					        masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath, db);	
						else
						    DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(masterFile, fileName, filePath);
					}
					catch(Exception e)
					{
						logger.warn("An file could not be written:" + e.getMessage(), e);
					}
				    
				    i++;
				    filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
				    if(filePath != null)
				    	filePath += File.separator + folderName;
				}

				SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getSiteNode(db, siteNodeId);
				String dnsName = CmsPropertyHandler.getWebServerAddress();
				if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
					dnsName = siteNode.getRepository().getDnsName();
					
				if(deliveryContext.getUseDownloadAction())
					assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, siteNodeId, contentId, languageId, assetKey, deliveryContext, db);
				else
					assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, folderName, fileName, deliveryContext); 
			}
			else if(useLanguageFallback)
			{
				assetUrl = getLanguageIndependentAssetUrl(contentId, languageId, siteNodeId, db, assetKey, deliveryContext, infoGluePrincipal);
			}
		}				
		else if(useLanguageFallback && languageId != null && masterLanguageVO != null && languageId.intValue() != masterLanguageVO.getId().intValue())
		{
	    	contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		    
	    	logger.info("contentVersion:" + contentVersion);
			if(contentVersion != null)
			{
            	DigitalAssetVO digitalAsset = DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getId(), assetKey, db);
				
				if(digitalAsset != null)
				{
					String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
					
					int i = 0;
					File masterFile = null;
					String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
					logger.info("folderName:" + folderName);
					String filePath = CmsPropertyHandler.getDigitalAssetPath0() + File.separator + folderName;
					while(filePath != null)
					{
						try
						{
						    if(masterFile == null)
						        masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath, db);
							else
							    DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(masterFile, fileName, filePath);
						}
						catch(Exception e)
						{
							logger.warn("An file could not be written:" + e.getMessage(), e);
						}
						
						i++;
						filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
					    if(filePath != null)
			    			filePath += File.separator + folderName;
					}

					SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getSiteNode(db, siteNodeId);
					String dnsName = CmsPropertyHandler.getWebServerAddress();
					if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
						dnsName = siteNode.getRepository().getDnsName();
						
					if(deliveryContext.getUseDownloadAction())
						assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, siteNodeId, contentId, languageId, assetKey, deliveryContext, db);
					else
						assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, folderName, fileName, deliveryContext); 
				}
				else if(useLanguageFallback)
				{
					assetUrl = getLanguageIndependentAssetUrl(contentId, languageId, siteNodeId, db, assetKey, deliveryContext, infoGluePrincipal);
				}
			}
		}
			
        CacheController.cacheObject(cacheName, assetCacheKey, assetUrl);
        
        return assetUrl;
	}
	
	/**
	 * This is the basic way of getting an asset-url for a content. 
	 * It selects the correct contentVersion depending on the language and then gets the digitalAsset associated.
	 * If the asset is cached on disk it returns that path imediately it's ok - otherwise it dumps it fresh.
	 */

	public String getAssetFilePath(Database db, Integer digitalAssetId, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		String assetFilePath = "";
		
		Integer contentId = DigitalAssetController.getController().getContentId(digitalAssetId, db);
		boolean hasAccess = getHasUserContentAccess(db, infoGluePrincipal, contentId);
		
		if(hasAccess)
		{
	    	DigitalAssetVO digitalAsset = DigitalAssetController.getSmallDigitalAssetVOWithId(digitalAssetId, db);
			if(digitalAsset != null)
			{
				String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
	
				int i = 0;
				File masterFile = null;
				String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
				logger.info("folderName:" + folderName);
				String filePath = CmsPropertyHandler.getDigitalAssetPath0() + File.separator + folderName;
				while(filePath != null)
				{
					try
					{
					    if(masterFile == null)
					        masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath, db);
					    else
					        DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(masterFile, fileName, filePath);
					}
					catch(Exception e)
					{
						logger.warn("An file could not be written:" + e.getMessage(), e);
					}
					
				    i++;
					filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
				    if(filePath != null)
				    	filePath += File.separator + folderName;
				}
	
				assetFilePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName + File.separator + fileName;
			}
		}
		
		return assetFilePath;
	}
		
	
	/**
	 * This is the basic way of getting an asset-url for a content. 
	 * It selects the correct contentVersion depending on the language and then gets the digitalAsset associated.
	 * If the asset is cached on disk it returns that path imediately it's ok - otherwise it dumps it fresh.
	 */

	public String getAssetThumbnailUrl(Database db, Integer contentId, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, int width, int height, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
	    String assetCacheKey = "" + languageId + "_" + contentId + "_" + siteNodeId + "_" + useLanguageFallback + "_" + width + "_" + height;
		logger.info("assetCacheKey:" + assetCacheKey);
		String cacheName = "assetThumbnailUrlCache";
		String cachedAssetUrl = (String)CacheController.getCachedObject(cacheName, assetCacheKey);
		if(cachedAssetUrl != null)
		{
			logger.info("There was an cached cachedAssetUrl:" + cachedAssetUrl);
			return cachedAssetUrl;
		}
		
		String assetUrl = "";
		
		SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		if (contentVersion != null) 
		{
			//DigitalAsset digitalAsset = getLatestDigitalAsset(contentVersion);
        	DigitalAssetVO digitalAsset = DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getId(), db);
			
			if(digitalAsset != null)
			{
				String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
				String thumbnailFileName = "thumbnail_" + width + "_" + height + "_" + fileName;

				int i = 0;
				File masterFile = null;
				File masterThumbFile = null;
				String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
				logger.info("folderName:" + folderName);
				String filePath = CmsPropertyHandler.getDigitalAssetPath0() + File.separator + folderName;
				while(filePath != null)
				{
					try
					{
					    if(masterFile == null)
					        masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath, db);
						else
						    DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(masterFile, fileName, filePath);
					    
					    if(masterThumbFile == null)
					        masterThumbFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(fileName, thumbnailFileName, filePath, width, height);
					    else
					        DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(fileName, thumbnailFileName, filePath, width, height);
					}
					catch(Exception e)
					{
						logger.warn("An file could not be written:" + e.getMessage(), e);
					}
					
					i++;
					filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
				    if(filePath != null)
				    	filePath += File.separator + folderName;
				}

				//String filePath = CmsPropertyHandler.getDigitalAssetPath();
				//DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath);
				//DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(digitalAsset, fileName, thumbnailFileName, filePath, width, height);
				
				SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getSiteNode(db, siteNodeId);
				String dnsName = CmsPropertyHandler.getWebServerAddress();
				if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
					dnsName = siteNode.getRepository().getDnsName();

				//assetUrl = dnsName + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + thumbnailFileName;
				assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, folderName, thumbnailFileName, deliveryContext); 
			}
			else
			{
				assetUrl = getLanguageIndependentAssetThumbnailUrl(contentId, languageId, siteNodeId, db, null, width, height, deliveryContext, infoGluePrincipal);
			}
		}
		
		CacheController.cacheObject(cacheName, assetCacheKey, assetUrl);
		
		return assetUrl;
	}


	/**
	 * This is the basic way of getting an asset-url for a content. 
	 * It selects the correct contentVersion depending on the language and then gets the digitalAsset associated.
	 * If the asset is cached on disk it returns that path imediately it's ok - otherwise it dumps it fresh.
	 */

	public String getAssetThumbnailUrl(Database db, Integer digitalAssetId, Integer siteNodeId, int width, int height, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
	    String assetCacheKey = "" + digitalAssetId + "_" + siteNodeId + "_" + width + "_" + height;
		logger.info("assetCacheKey:" + assetCacheKey);
		String cacheName = "assetThumbnailUrlCache";
		String cachedAssetUrl = (String)CacheController.getCachedObject(cacheName, assetCacheKey);
		if(cachedAssetUrl != null)
		{
			logger.info("There was an cached cachedAssetUrl:" + cachedAssetUrl);
			return cachedAssetUrl;
		}
		
		String assetUrl = "";
		
    	DigitalAssetVO digitalAsset = DigitalAssetController.getSmallDigitalAssetVOWithId(digitalAssetId, db);
		
		if(digitalAsset != null)
		{
			String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
			String thumbnailFileName = "thumbnail_" + width + "_" + height + "_" + fileName;

			int i = 0;
			File masterFile = null;
			File masterThumbFile = null;
			String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
			logger.info("folderName:" + folderName);
			String filePath = CmsPropertyHandler.getDigitalAssetPath0() + File.separator + folderName;
			while(filePath != null)
			{
				try
				{
				    if(masterFile == null)
				        masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath, db);
					else
					    DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(masterFile, fileName, filePath);
				    
				    if(masterThumbFile == null)
				        masterThumbFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(fileName, thumbnailFileName, filePath, width, height);
				    else
				        DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(fileName, thumbnailFileName, filePath, width, height);
				}
				catch(Exception e)
				{
					logger.warn("An file could not be written:" + e.getMessage(), e);
				}
				
				i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			    if(filePath != null)
			    	filePath += File.separator + folderName;
			}
			
			SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(null, null, null).getSiteNode(db, siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
				dnsName = siteNode.getRepository().getDnsName();

			assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, folderName, thumbnailFileName, deliveryContext); 
		}
		
		CacheController.cacheObject(cacheName, assetCacheKey, assetUrl);
		
		return assetUrl;
	}


	/**
	 * This is the basic way of getting an asset-url for a content. 
	 * It selects the correct contentVersion depending on the language and then gets the digitalAsset associated with the key.
	 * If the asset is cached on disk it returns that path imediately it's ok - otherwise it dumps it fresh.
	 */

	public String getAssetThumbnailUrl(Database db, Integer contentId, Integer languageId, String assetKey, Integer siteNodeId, boolean useLanguageFallback, int width, int height, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
	    String assetCacheKey = "" + languageId + "_" + contentId + "_" + siteNodeId + "_" + assetKey + "_" + useLanguageFallback + "_" + width + "_" + height;
		logger.info("assetCacheKey:" + assetCacheKey);
		String cacheName = "assetThumbnailUrlCache";
		String cachedAssetUrl = (String)CacheController.getCachedObject(cacheName, assetCacheKey);
		if(cachedAssetUrl != null)
		{
			logger.info("There was an cached cachedAssetUrl:" + cachedAssetUrl);
			return cachedAssetUrl;
		}

		String assetUrl = "";
		
		SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		if (contentVersion != null) 
		{
			//DigitalAsset digitalAsset = getDigitalAssetWithKey(contentVersion, assetKey);
        	DigitalAssetVO digitalAsset = DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getId(), assetKey, db);

			if(digitalAsset != null)
			{
				String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
				String thumbnailFileName = "thumbnail_" + width + "_" + height + "_" + fileName;

				int i = 0;
				File masterFile = null;
				File masterThumbFile = null;
				String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
				logger.info("folderName:" + folderName);
				String filePath = CmsPropertyHandler.getDigitalAssetPath0() + File.separator + folderName;
				while(filePath != null)
				{
					try
					{
					    if(masterFile == null)
							masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath, db);
						else
							DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath, db);
					    
					    if(masterThumbFile == null)
					        masterThumbFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(fileName, thumbnailFileName, filePath, width, height);
						else
						    DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(fileName, thumbnailFileName, filePath, width, height);
					}
					catch(Exception e)
					{
						logger.warn("An file could not be written:" + e.getMessage(), e);
					}
					
					i++;
					filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
				    if(filePath != null)
				    	filePath += File.separator + folderName;
				}

				//String filePath = CmsPropertyHandler.getDigitalAssetPath();
				//DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAsset(digitalAsset, fileName, filePath);
				//DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpDigitalAssetThumbnail(digitalAsset, fileName, thumbnailFileName, filePath, width, height);
				
				SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getSiteNode(db, siteNodeId);
				String dnsName = CmsPropertyHandler.getWebServerAddress();
				if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
					dnsName = siteNode.getRepository().getDnsName();
				
				//assetUrl = dnsName + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + thumbnailFileName;
				assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, folderName, thumbnailFileName, deliveryContext); 
			}
			else
			{
				assetUrl = getLanguageIndependentAssetThumbnailUrl(contentId, languageId, siteNodeId, db, assetKey, width, height, deliveryContext, infoGluePrincipal);
			}
			
		}				
		
		CacheController.cacheObject(cacheName, assetCacheKey, assetUrl);
		
		return assetUrl;
	}
	


	/*
	 * getAssetFileSize. Prelimenary, we should rather supply a assetvo to the template. 
	 */
	 
	public Integer getAssetFileSize(Database db, Integer contentId, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{ 
		return getAssetFileSize(db, contentId, languageId, null, siteNodeId, useLanguageFallback, deliveryContext, infoGluePrincipal); 
	}
	
	public Integer getAssetFileSize(Database db, Integer contentId, Integer languageId, String assetKey, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		Integer fileSize = null;
	
		SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		if (contentVersion != null) 
		{
			//DigitalAsset digitalAsset =	(assetKey == null) ? getLatestDigitalAsset(contentVersion) : getDigitalAssetWithKey(contentVersion, assetKey); 
			DigitalAssetVO digitalAsset =	(assetKey == null) ? DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getId(), db) : DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getId(), assetKey, db);
			
			if(digitalAsset == null)
				digitalAsset = getLanguageIndependentAssetVO(contentId, languageId, siteNodeId, db, assetKey, deliveryContext, infoGluePrincipal);
				
			if(digitalAsset != null)
			{
				fileSize = digitalAsset.getAssetFileSize();
			}								
		}				
            
		return fileSize;
	}

	/**
	 * This method deliveres a String containing the URL to the directory resulting from unpacking of a uploaded zip-digitalAsset.
	 * This method is meant to be used for javascript plugins and similar bundles - and the target directory is therefore the infoglueDeliverXXXX/script/extensions
	 */

	public String getScriptExtensionUrls(Database db, Integer contentId, Integer languageId, String assetKey, String fileNames, Boolean autoCreateMarkup, Boolean addToHeader, Boolean addToBodyEnd, Boolean addToBundledIncludes, String bundleName, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		Timer t = new Timer();
		String scriptExtensionUrls = "";
		
		SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		if (contentVersion != null) 
        {
			DigitalAssetVO digitalAsset = null;
			if(assetKey == null)
				digitalAsset = DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getContentVersionId(), db);
			else
				digitalAsset = DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getContentVersionId(), assetKey, db);
			
			if(digitalAsset != null)
			{
				String fileName = digitalAsset.getAssetFileName();
				
				int i = 0;
				File masterFile = null;
				String filePath = CmsPropertyHandler.getDigitalAssetPath0();
				while(filePath != null)
				{
					File unzipDirectory = new File(filePath + File.separator + "extensions" + File.separator + fileName.substring(0, fileName.lastIndexOf(".")));
					unzipDirectory.mkdirs();
					
					if(masterFile == null)
					    masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpAndUnzipDigitalAsset(digitalAsset, fileName, filePath, unzipDirectory, db);
					else
					    DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpAndUnzipDigitalAsset(masterFile, fileName, filePath, unzipDirectory);
					    
					i++;
					filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
				}

				//System.out.println("filePath (Should be base url):" + filePath);
				
				SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getSiteNode(db, siteNodeId);
				String dnsName = CmsPropertyHandler.getWebServerAddress();
				if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
					dnsName = siteNode.getRepository().getDnsName();
					
				String archiveBaseUrl = urlComposer.composeDigitalAssetUrl(dnsName, "extensions" + File.separator + fileName.substring(0, fileName.lastIndexOf(".")), deliveryContext); 
			
				if(logger.isInfoEnabled())
					logger.info("archiveBaseUrl:" + archiveBaseUrl);
				if(logger.isInfoEnabled())
					logger.info("fileNames:" + fileNames);
				if(fileNames == null || fileNames.equals(""))
				{
					scriptExtensionUrls = archiveBaseUrl;
				}
				else
				{
					String[] fileNamesArray = fileNames.split(",");
					for(int j=0; j<fileNamesArray.length; j++)
					{
						String scriptExtensionFileUrl = archiveBaseUrl + "/" + fileNamesArray[j].trim();
						if(autoCreateMarkup || addToHeader)
						{
							if(scriptExtensionFileUrl.toLowerCase().endsWith(".css"))
								scriptExtensionFileUrl = "<link href=\"" + scriptExtensionFileUrl + "\" rel=\"stylesheet\" type=\"text/css\" />";
							else
								scriptExtensionFileUrl = "<script type=\"text/javascript\" src=\"" + scriptExtensionFileUrl + "\"></script>";
						}

						if(!addToHeader && !addToBundledIncludes)
						{
							scriptExtensionUrls = scriptExtensionUrls + scriptExtensionFileUrl + "";
						}
						else
						{
							if(addToBundledIncludes)
							{
								////// TODO - clean all bundles and extensions when assets are updated or similar.. figure out how.
								String extensionSuffix = "extensions" + File.separator + fileName.substring(0, fileName.lastIndexOf(".")) + File.separator + fileNamesArray[j];
								if(logger.isInfoEnabled())
									logger.info("extensionSuffix:" + extensionSuffix);

								if(extensionSuffix.toLowerCase().endsWith(".css"))
									deliveryContext.addCSSExtensionBundleFile(bundleName, extensionSuffix);							
								else
								{
									if(addToBodyEnd)
										deliveryContext.addScriptExtensionBodyBundleFile(bundleName, extensionSuffix);							
									else if(addToHeader)
										deliveryContext.addScriptExtensionHeadBundleFile(bundleName, extensionSuffix);							
								}
							}
							else if(addToHeader)
								deliveryContext.getHtmlHeadItems().add(scriptExtensionFileUrl);
							else if(addToBodyEnd)
								deliveryContext.getHtmlBodyEndItems().add(scriptExtensionFileUrl);
						}
					}					
				}
			}
        }				
		
		if(logger.isInfoEnabled())
			t.printElapsedTime("Getting scriptExtensionUrls took");
		
		return scriptExtensionUrls;
	}

	/**
	 * This method deliveres a String with the URL to the base path of the directory resulting from 
	 * an unpacking of a uploaded zip-digitalAsset.
	 */

	public String getArchiveBaseUrl(Database db, Integer contentId, Integer languageId, String assetKey, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		String archiveBaseUrl = null;
		
		SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		if (contentVersion != null) 
        {
			DigitalAssetVO digitalAsset = DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getContentVersionId(), assetKey, db);
			//DigitalAsset digitalAsset = getDigitalAssetWithKey(contentVersion, assetKey);
			
			if(digitalAsset != null)
			{
				String fileName = digitalAsset.getAssetFileName();
				
				int i = 0;
				File masterFile = null;
				String filePath = CmsPropertyHandler.getDigitalAssetPath0();
				String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
				logger.info("folderName:" + folderName);
				while(filePath != null)
				{
					File unzipDirectory = new File(filePath + File.separator + fileName.substring(0, fileName.lastIndexOf(".")));
					unzipDirectory.mkdirs();
					
					if(masterFile == null)
					    masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpAndUnzipDigitalAsset(digitalAsset, fileName, filePath, unzipDirectory, db);
					else
					    DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpAndUnzipDigitalAsset(masterFile, fileName, filePath, unzipDirectory);
					    
					i++;
					filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
				    if(filePath != null)
				    	filePath += File.separator + folderName;
				}

				//String filePath = CmsPropertyHandler.getDigitalAssetPath();
				//File unzipDirectory = new File(filePath + File.separator + fileName.substring(0, fileName.lastIndexOf(".")));
				//unzipDirectory.mkdirs();
				//DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpAndUnzipDigitalAsset(digitalAsset, fileName, filePath, unzipDirectory);
				
				SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getSiteNode(db, siteNodeId);
				String dnsName = CmsPropertyHandler.getWebServerAddress();
				if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
					dnsName = siteNode.getRepository().getDnsName();
					
				//archiveBaseUrl = dnsName + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + fileName.substring(0, fileName.lastIndexOf("."));
				archiveBaseUrl = urlComposer.composeDigitalAssetUrl(dnsName, folderName, fileName.substring(0, fileName.lastIndexOf(".")), deliveryContext); 
			}
        }				
		
		return archiveBaseUrl;
	}

	/**
	 * This method deliveres a String with the URL to the base path of the directory resulting from 
	 * an unpacking of a uploaded zip-digitalAsset.
	 */

	public String getArchiveBaseUrl(Database db, Integer digitalAssetId, Integer siteNodeId, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		String archiveBaseUrl = null;
		
		DigitalAssetVO digitalAsset = DigitalAssetController.getLatestDigitalAssetVO(digitalAssetId, db);
		if(digitalAsset != null)
		{
			String fileName = digitalAsset.getAssetFileName();
			
			int i = 0;
			File masterFile = null;
			String filePath = CmsPropertyHandler.getDigitalAssetPath0();
			while(filePath != null)
			{
				File unzipDirectory = new File(filePath + File.separator + fileName.substring(0, fileName.lastIndexOf(".")));
				unzipDirectory.mkdirs();
				
				if(masterFile == null)
				    masterFile = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpAndUnzipDigitalAsset(digitalAsset, fileName, filePath, unzipDirectory, db);
				else
				    DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpAndUnzipDigitalAsset(masterFile, fileName, filePath, unzipDirectory);
				    
				i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			}

			SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(null, null, null).getSiteNode(db, siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
				dnsName = siteNode.getRepository().getDnsName();
				
			archiveBaseUrl = urlComposer.composeDigitalAssetUrl(dnsName, null, fileName.substring(0, fileName.lastIndexOf(".")), deliveryContext); 
		}
		
		return archiveBaseUrl;
	}

	public Vector getArchiveEntries(Database db, Integer contentId, Integer languageId, String assetKey, Integer siteNodeId, boolean useLanguageFallback, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		Vector entries = null;
		
		SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(siteNodeId, contentId, languageId, db, useLanguageFallback, deliveryContext, infoGluePrincipal);
		if (contentVersion != null) 
		{
			DigitalAssetVO digitalAsset = DigitalAssetController.getLatestDigitalAssetVO(contentVersion.getContentVersionId(), assetKey, db);
			//DigitalAsset digitalAsset = getDigitalAssetWithKey(contentVersion, assetKey);
			
			if(digitalAsset != null)
			{
				String fileName = digitalAsset.getAssetFileName();

				int i = 0;
				File masterFile = null;
				String filePath = CmsPropertyHandler.getDigitalAssetPath0();
				while(filePath != null)
				{
					File unzipDirectory = new File(filePath + File.separator + fileName.substring(0, fileName.lastIndexOf(".")));
					unzipDirectory.mkdirs();
					
					if(masterFile == null)
					{
					    entries = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpAndGetZipEntries(digitalAsset, fileName, filePath, unzipDirectory, db);
						masterFile = new File(filePath + File.separator + fileName);
					}					
					else
					{
					    entries = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpAndGetZipEntries(masterFile, fileName, filePath, unzipDirectory);
					}
					
					i++;
					filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
				}

				//String filePath = CmsPropertyHandler.getDigitalAssetPath();
				//File unzipDirectory = new File(filePath + File.separator + fileName.substring(0, fileName.lastIndexOf(".")));
				//unzipDirectory.mkdirs();
				//entries = DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpAndGetZipEntries(digitalAsset, fileName, filePath, unzipDirectory);
			}
		}				
		
		return entries;
	}


	
	/**
	 * Returns the digital asset for a contentversion that has a certain key.
	 */
	/*
	private DigitalAsset getDigitalAssetWithKey(ContentVersion contentVersion, String assetKey)
	{
		Collection digitalAssets = contentVersion.getDigitalAssets();
		Iterator iterator = digitalAssets.iterator();
		
		while(iterator.hasNext())
		{  
			DigitalAsset currentDigitalAsset = (DigitalAsset)iterator.next();	
				
			if(currentDigitalAsset != null && currentDigitalAsset.getAssetKey().equalsIgnoreCase(assetKey))
			{
				return currentDigitalAsset;
			}
		}

		return null;
	}
	*/
	
	/**
	 * Returns the latest digital asset for a contentversion.
	 */
	/*
	private DigitalAsset getLatestDigitalAsset(ContentVersion contentVersion)
	{
		Collection digitalAssets = contentVersion.getDigitalAssets();
		Iterator iterator = digitalAssets.iterator();
		
		DigitalAsset digitalAsset = null;
		while(iterator.hasNext())
		{
			DigitalAsset currentDigitalAsset = (DigitalAsset)iterator.next();	
			if(digitalAsset == null || currentDigitalAsset.getDigitalAssetId().intValue() > digitalAsset.getDigitalAssetId().intValue())
				digitalAsset = currentDigitalAsset;
		}
		return digitalAsset;
	}
	*/
	


	/**
	 * This method fetches a value from the xml that is the contentVersions Value. If the 
	 * contentVersioVO is null the contentVersion has not been created yet and no values are present.
	 */
	public String getAttributeValue(Database db, ContentVersionVO contentVersionVO, String key, boolean escapeHTML)
	{
		String value = "";
		if(contentVersionVO != null)
		{
			try
	        {
	        	String xml = contentVersionVO.getVersionValue();
	        	
	        	int startTagIndex = xml.indexOf("<" + key + ">");
	        	int endTagIndex   = xml.indexOf("]]></" + key + ">");
	        	
	        	if(startTagIndex > 0 && startTagIndex < xml.length() && endTagIndex > startTagIndex && endTagIndex <  xml.length())
	        		value = xml.substring(startTagIndex + key.length() + 11, endTagIndex);

	    		if(escapeHTML)
	        	    value = formatter.escapeHTML(value);
	        } 
	        catch(Exception e)
	        {
	        	logger.error("An error occurred so we should not return the attribute value:" + e, e);
	        }
		}

		return value;
	}
	

	/**
	 * This method returns a sorted list of childContents to a content ordered by the given attribute in the direction given.
	 */

	public List getChildContents(Database db, InfoGluePrincipal infoGluePrincipal, Integer contentId, Integer languageId, boolean useLanguageFallback, boolean includeFolders, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		List childContents = new ArrayList();
	
		getChildContents(infoGluePrincipal, childContents, contentId, languageId, useLanguageFallback, 0, false, includeFolders, 1, db, deliveryContext);
	
		return childContents;
	}
		

	/**
	 * This method returns a sorted list of childContents to a content ordered by the given attribute in the direction given.
	 */
	
	public List getSortedChildContents(InfoGluePrincipal infoGluePrincipal, Integer languageId, Integer contentId, Integer siteNodeId, Database db, boolean searchRecursive, Integer maximumNumberOfLevels, String sortAttribute, String sortOrder, boolean useLanguageFallback, boolean includeFolders, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		
		String sortedChildContentsKey = "" + infoGluePrincipal.getName() + "_" + languageId + "_" + contentId + "_" + siteNodeId + "_" + searchRecursive + "_" + maximumNumberOfLevels + "_" + sortAttribute + "_" + sortOrder + "_" + useLanguageFallback + "_" + includeFolders;
		logger.info("sortedChildContentsKey:" + sortedChildContentsKey);
		String cacheName = "sortedChildContentsCache";
		List cachedSortedContentVOList = (List)CacheController.getCachedObject(cacheName, sortedChildContentsKey);
		if(cachedSortedContentVOList != null)
		{
			logger.info("There was an cached content cachedSortedContentVOList:" + cachedSortedContentVOList.size());
			return cachedSortedContentVOList;
		}
		
		List sortedContentVOList = new ArrayList();
		
		List unsortedChildren = getChildContents(infoGluePrincipal, languageId, useLanguageFallback, contentId, siteNodeId, searchRecursive, maximumNumberOfLevels, db, includeFolders, deliveryContext);
		
		List sortedContents   = sortContents(db, unsortedChildren, languageId, siteNodeId, sortAttribute, sortOrder, useLanguageFallback, includeFolders, deliveryContext, infoGluePrincipal);

		Iterator boundContentsIterator = sortedContents.iterator();
		while(boundContentsIterator.hasNext())
		{
			Content content = (Content)boundContentsIterator.next();
			sortedContentVOList.add(content.getValueObject());
		}
		
		CacheController.cacheObject(cacheName, sortedChildContentsKey, sortedContentVOList);
			
		return sortedContentVOList;
	}
	


	/**
	 * This method returns the contentTypeDefinitionVO of the content sent in.
	 */
	
	public ContentTypeDefinitionVO getContentTypeDefinitionVO(Database db, Integer contentId) throws SystemException, Exception
	{
		//Timer t = new Timer();
		ContentTypeDefinitionVO contentTypeDefinitionVO = null;
		
		if(contentId != null && contentId.intValue() > 0)
		{
			SmallContentImpl smallContent = (SmallContentImpl)getObjectWithId(SmallContentImpl.class, contentId, db);
			contentTypeDefinitionVO = (ContentTypeDefinitionVO) getVOWithId(ContentTypeDefinitionImpl.class, smallContent.getContentTypeDefinitionId(), db);
			//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getContentTypeDefinitionVO 1", t.getElapsedTimeNanos() / 1000);
			/*
			Content content = (Content)getObjectWithId(ContentImpl.class, contentId, db);
			contentTypeDefinitionVO = content.getContentTypeDefinition().getValueObject();
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getContentTypeDefinitionVO 2", t.getElapsedTimeNanos() / 1000);
			*/
		}
		
		return contentTypeDefinitionVO;
	}


	/**
	 * This method returns a list of children to the content given. It is mostly used to get all 
	 * children a folder has.
	 */
	
	private List getChildContents(InfoGluePrincipal infoGluePrincipal, Integer languageId, boolean useLanguageFallback, Integer contentId, Integer siteNodeId, boolean searchRecursive, Integer maximumNumberOfLevels, Database db, boolean includeFolders, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		List contents = new ArrayList();
		
		getChildContents(infoGluePrincipal, contents, contentId, languageId, useLanguageFallback, 0, searchRecursive, maximumNumberOfLevels.intValue(), db, includeFolders, deliveryContext);
		
		return contents;
	}
	

	/**
	 * This method recurses into the dept of the content-children and fills the list of contents.
	 */
	
	private void getChildContents(InfoGluePrincipal infoGluePrincipal, List contents, Integer contentId, Integer languageId, boolean useLanguageFallback, int currentLevel, boolean searchRecursive, int maximumNumberOfLevels, Database db, boolean includeFolders, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		/*
		OQLQuery oql = db.getOQLQuery("SELECT contentVersion FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl contentVersion WHERE contentVersion.stateId = $1 AND contentVersion.isActive = $2 AND contentVersion.owningContent.parentContent.contentId = $3");
		oql.bind(getOperatingMode());
		oql.bind(new Boolean(true));
    	oql.bind(contentId);
    	
		QueryResults results = oql.execute(Database.ReadOnly);
		
		while (results.hasMore()) 
		{
			ContentVersion contentVersion = (ContentVersion)results.next();
    		Content content = contentVersion.getOwningContent();
    		
			if(searchRecursive && currentLevel < maximumNumberOfLevels)
				getChildContents(contents, content.getContentId(), currentLevel + 1, searchRecursive, maximumNumberOfLevels, db);
    
			if(isValidContent(content, db))
			{
				if(!contents.contains(content))
					contents.add(content);
			}
		}
		*/
		
		deliveryContext.addUsedContent("selectiveCacheUpdateNonApplicable");

		OQLQuery oql = db.getOQLQuery("SELECT content FROM org.infoglue.cms.entities.content.impl.simple.SmallContentImpl content WHERE content.parentContentId = $1 ORDER BY content.contentId");
		//OQLQuery oql = db.getOQLQuery("SELECT content FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl content WHERE content.parentContent.contentId = $1 ORDER BY content.contentId");
    	oql.bind(contentId);
    	
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		while (results.hasMore()) 
        {
        	Content content = (Content)results.next();
    
        	if(searchRecursive && currentLevel < maximumNumberOfLevels)
        		getChildContents(infoGluePrincipal, contents, content.getContentId(), languageId, useLanguageFallback, currentLevel + 1, searchRecursive, maximumNumberOfLevels, db, includeFolders, deliveryContext);

    		if(isValidContent(infoGluePrincipal, content, languageId, useLanguageFallback, includeFolders, db, deliveryContext))
    		{
   				contents.add(content);
    		}
        }
		
		results.close();
		oql.close();
	}

	
	/** 
	 * This method recurses into the dept of the content-children and fills the list of contents.
	 */
	
	private void getChildContents(InfoGluePrincipal infoGluePrincipal, List contents, Integer contentId, Integer languageId, boolean useLanguageFallback, int currentLevel, boolean searchRecursive, boolean includeFolders, int maximumNumberOfLevels, Database db, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		deliveryContext.addUsedContent("selectiveCacheUpdateNonApplicable");

		OQLQuery oql = db.getOQLQuery( "SELECT content FROM org.infoglue.cms.entities.content.impl.simple.ContentImpl content WHERE content.parentContent.contentId = $1 ORDER BY content.contentId");
		oql.bind(contentId);
    	
		QueryResults results = oql.execute(Database.ReadOnly);
		
		while (results.hasMore()) 
		{
			Content content = (Content)results.next();
    
			if(searchRecursive && currentLevel < maximumNumberOfLevels)
				getChildContents(infoGluePrincipal, contents, content.getContentId(), languageId, useLanguageFallback, currentLevel + 1, searchRecursive, includeFolders, maximumNumberOfLevels, db, deliveryContext);

    		if(content.getIsBranch().booleanValue() && includeFolders && isValidContent(infoGluePrincipal, content, languageId, useLanguageFallback, includeFolders, db, deliveryContext))
    		{
				contents.add(content);
    		}
			else if(isValidContent(infoGluePrincipal, content, languageId, useLanguageFallback, includeFolders, db, deliveryContext))
			{
				contents.add(content);
			}
		}
		
		results.close();
		oql.close();
	}
	
	/**
	 * This method validates that right now is between publishdate and expiredate.
	 */
	
	private boolean isValidOnDates(Date publishDate, Date expireDate, boolean validateOnDates)
	{
		if(!validateOnDates)
			return true;
		
		return isValidOnDates(publishDate, expireDate);
	}		

	/**
	 * This method validates that right now is between publishdate and expiredate.
	 */
	
	private boolean isValidOnDates(Date publishDate, Date expireDate)
	{
		boolean isValid = true;
		Date now = new Date();
		
		if(publishDate.after(now) || expireDate.before(now))
			isValid = false;
		
		return isValid;
	}		

	/**
	 * Returns if a content is between dates and has a content version suitable for this delivery mode.
	 * @throws Exception
	 */
/*	
	public boolean isValidContent(Integer contentId, InfoGluePrincipal infoGluePrincipal, Database db) throws Exception
	{
		boolean isValidContent = false;
		
		Content content = (Content)getObjectWithId(ContentImpl.class, contentId, db); 
		isValidContent = isValidContent(content, db);
    	
		return isValidContent;					
	}
*/
	/**
	 * Returns if a content is between dates and has a content version suitable for this delivery mode.
	 * @throws Exception
	 */
	public boolean isValidContent(Database db, Integer contentId, Integer languageId, boolean useLanguageFallback, boolean includeFolders, InfoGluePrincipal infoGluePrincipal, DeliveryContext deliveryContext) throws Exception
	{
	    boolean isValidContent = false;
		
		Timer t = new Timer();
		Content content = (Content)getObjectWithId(ContentImpl.class, contentId, db); 
		isValidContent = isValidContent(infoGluePrincipal, content, languageId, useLanguageFallback, includeFolders, db, deliveryContext, true, true);
		
		return isValidContent;					
	}
	
	/**
	 * Returns if a content is between dates and has a content version suitable for this delivery mode.
	 * @throws Exception
	 */

	public boolean isValidContent(Database db, Content content, Integer languageId, boolean useLanguageFallback, boolean includeFolders, InfoGluePrincipal infoGluePrincipal, DeliveryContext deliveryContext, boolean checkIfVersionExists, boolean checkAuthorization) throws Exception
	{
	    boolean isValidContent = false;
		
	    Timer t = new Timer();
		//Content content = (Content)getObjectWithId(ContentImpl.class, contentId, db); 
	    //RequestAnalyser.getRequestAnalyser().registerComponentStatistics("isValidContent2 content", t.getElapsedTimeNanos() / 1000);
		isValidContent = isValidContent(infoGluePrincipal, content, languageId, useLanguageFallback, includeFolders, db, deliveryContext, checkIfVersionExists, checkAuthorization);
		//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("isValidContent2", t.getElapsedTimeNanos() / 1000);
		
	    /*
	    Timer t = new Timer();
		Content content = (Content)getObjectWithId(ContentImpl.class, contentId, db); 
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("isValidContent2 content", t.getElapsedTimeNanos() / 1000);
		isValidContent = isValidContent(infoGluePrincipal, content, languageId, useLanguageFallback, includeFolders, db, deliveryContext);
	    RequestAnalyser.getRequestAnalyser().registerComponentStatistics("isValidContent2", t.getElapsedTimeNanos() / 1000);
		*/
	    
		return isValidContent;					
	}

	/**
	 * Returns if a content is between dates and has a content version suitable for this delivery mode.
	 * @throws Exception
	 */
	
	public boolean isValidContent(InfoGluePrincipal infoGluePrincipal, Content content, Integer languageId, boolean useLanguageFallBack, boolean includeFolders, Database db, DeliveryContext deliveryContext) throws Exception
	{
		//Timer t = new Timer();
		
		boolean isValidContent = false;
		if(infoGluePrincipal == null)
		    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.");
		
		if(content.getContentTypeDefinition() != null && content.getContentTypeDefinition().getName().equalsIgnoreCase("Meta info"))
			return true;

		boolean validateOnDates = true;
		String operatingMode = CmsPropertyHandler.getOperatingMode();
		if(!deliveryContext.getOperatingMode().equals(operatingMode))
			operatingMode = deliveryContext.getOperatingMode();

		if(operatingMode.equals("0"))
		{
			validateOnDates = deliveryContext.getValidateOnDates();
		}

		//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("isValidContentPart1", t.getElapsedTimeNanos() / 1000);

		Integer protectedContentId = getProtectedContentId(db, content);
		if(logger.isInfoEnabled())
			logger.info("content:" + content.getName() + ":" + protectedContentId);
		if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Content.Read", protectedContentId.toString()))
		{
		    return false;
		}
			    
		//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("isValidContentPart protectedContentId", t.getElapsedTimeNanos() / 1000);

		if(includeFolders && content.getIsBranch().booleanValue() && isValidOnDates(content.getPublishDateTime(), content.getExpireDateTime(), validateOnDates))
		{
			isValidContent = true; 
		}
		else if(isValidOnDates(content.getPublishDateTime(), content.getExpireDateTime(), validateOnDates))
		{
			//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("isValidContentPart3", t.getElapsedTimeNanos() / 1000);

			//ContentVersion contentVersion = getContentVersion(content, languageId, getOperatingMode(), deliveryContext, db);
			//TODO
		    //ContentVersionVO contentVersion = getContentVersionVO(content.getId(), languageId, getOperatingMode(), deliveryContext, db);
		    SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(content.getId(), languageId, getOperatingMode(deliveryContext), deliveryContext, db);
		    
		    //RequestAnalyser.getRequestAnalyser().registerComponentStatistics("isValidContentPart4.1", t.getElapsedTimeNanos() / 1000);

		    Integer repositoryId = null;
			Repository repository = content.getRepository();
			if(repository == null)
			{
			    if(content instanceof MediumContentImpl)
			        repositoryId = ((MediumContentImpl)content).getRepositoryId();
			    if(content instanceof SmallishContentImpl)
			        repositoryId = ((SmallishContentImpl)content).getRepositoryId();
			    else if(content instanceof SmallContentImpl)
			        repositoryId = ((SmallContentImpl)content).getRepositoryId();
			}
			else
			{
			    repositoryId = repository.getId();
			}
			
			if(contentVersion == null && useLanguageFallBack && repositoryId != null)
			{
				LanguageVO masterLanguage = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(repositoryId, db);
				//TODO
				if(masterLanguage != null && !masterLanguage.getId().equals(languageId))
					contentVersion = getSmallestContentVersionVO(content.getId(), masterLanguage.getId(), getOperatingMode(deliveryContext), deliveryContext, db);
					//contentVersion = getContentVersionVO(content.getId(), masterLanguage.getId(), getOperatingMode(), deliveryContext, db);
					//contentVersion = getContentVersion(content, masterLanguage.getId(), getOperatingMode(), deliveryContext, db);
			}

			//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("isValidContentPart5", t.getElapsedTimeNanos() / 1000);

			if(contentVersion != null)
				isValidContent = true;			
		}
    	
		if(isValidContent && !content.getExpireDateTime().before(new Date()))
		{
		    Date expireDateTimeCandidate = content.getExpireDateTime();
		    if(CacheController.expireDateTime == null || expireDateTimeCandidate.before(CacheController.expireDateTime))
			{
			    CacheController.expireDateTime = expireDateTimeCandidate;
			}
		}
		else if(content.getPublishDateTime().after(new Date())) //If it's a publish date to come we consider it
		{
		    Date publishDateTimeCandidate = content.getPublishDateTime();
		    if(CacheController.publishDateTime == null || publishDateTimeCandidate.after(CacheController.publishDateTime))
			{
			    CacheController.publishDateTime = publishDateTimeCandidate;
			}
		}
	    
		//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("isValidContentPart end", t.getElapsedTimeNanos() / 1000);
		
		return isValidContent;					
	}

	private static Integer metaInfoContentTypeId = null;
	
	public boolean isValidContent(InfoGluePrincipal infoGluePrincipal, Content content, Integer languageId, boolean useLanguageFallBack, boolean includeFolders, Database db, DeliveryContext deliveryContext, boolean checkVersionExists, boolean checkAccessRights) throws Exception
	{
		//Timer t = new Timer();
		
		boolean isValidContent = false;
		if(infoGluePrincipal == null)
		    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.");
		
		if(metaInfoContentTypeId == null)
		{
			ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Meta info", db);
			if(ctdVO != null)
				metaInfoContentTypeId = ctdVO.getId();
		}
		
		if(content.getContentTypeDefinitionId() != null && content.getContentTypeDefinitionId().equals(metaInfoContentTypeId))
			return true;

		boolean validateOnDates = true;
		String operatingMode = CmsPropertyHandler.getOperatingMode();
		if(!deliveryContext.getOperatingMode().equals(operatingMode))
			operatingMode = deliveryContext.getOperatingMode();
		
		if(operatingMode.equals("0"))
		{
			validateOnDates = deliveryContext.getValidateOnDates();
		}

	    if(checkAccessRights)
	    {
			Integer protectedContentId = getProtectedContentId(db, content);
			if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Content.Read", protectedContentId.toString()))
			{
			    return false;
			}
	    }

		if(includeFolders && content.getIsBranch().booleanValue() && isValidOnDates(content.getPublishDateTime(), content.getExpireDateTime(), validateOnDates))
		{
			isValidContent = true; 
		}
		else if(isValidOnDates(content.getPublishDateTime(), content.getExpireDateTime(), validateOnDates))
		{
			if(checkVersionExists)
		    {
				//ContentVersion contentVersion = getContentVersion(content, languageId, getOperatingMode(), deliveryContext, db);
				//TODO
			    //ContentVersionVO contentVersion = getContentVersionVO(content.getId(), languageId, getOperatingMode(), deliveryContext, db);
			    SmallestContentVersionVO contentVersion = getSmallestContentVersionVO(content.getId(), languageId, getOperatingMode(deliveryContext), deliveryContext, db);
			    
			    //RequestAnalyser.getRequestAnalyser().registerComponentStatistics("isValidContentPart4.2", t.getElapsedTimeNanos() / 1000);
	
			    Integer repositoryId = null;
				Repository repository = content.getRepository();
				if(repository == null)
				{
				    if(content instanceof MediumContentImpl)
				        repositoryId = ((MediumContentImpl)content).getRepositoryId();
				    if(content instanceof SmallishContentImpl)
				        repositoryId = ((SmallishContentImpl)content).getRepositoryId();
				    else if(content instanceof SmallContentImpl)
				        repositoryId = ((SmallContentImpl)content).getRepositoryId();
				}
				else
				{
				    repositoryId = repository.getId();
				}
				
				if(contentVersion == null && useLanguageFallBack && repositoryId != null)
				{
					LanguageVO masterLanguage = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(repositoryId, db);
					//TODO
					if(masterLanguage != null && !masterLanguage.getId().equals(languageId))
						contentVersion = getSmallestContentVersionVO(content.getId(), masterLanguage.getId(), getOperatingMode(deliveryContext), deliveryContext, db);
						//contentVersion = getContentVersionVO(content.getId(), masterLanguage.getId(), getOperatingMode(), deliveryContext, db);
						//contentVersion = getContentVersion(content, masterLanguage.getId(), getOperatingMode(), deliveryContext, db);
				}
	
			    if(contentVersion != null)
					isValidContent = true;			
		    }
		    else
		    {
		    	isValidContent = true;
		    }
		    
		}
    	
		if(isValidContent && !content.getExpireDateTime().before(new Date()))
		{
		    Date expireDateTimeCandidate = content.getExpireDateTime();
		    if(CacheController.expireDateTime == null || expireDateTimeCandidate.before(CacheController.expireDateTime))
			{
			    CacheController.expireDateTime = expireDateTimeCandidate;
			}
		}
		else if(content.getPublishDateTime().after(new Date())) //If it's a publish date to come we consider it
		{
		    Date publishDateTimeCandidate = content.getPublishDateTime();
		    if(CacheController.publishDateTime == null || publishDateTimeCandidate.after(CacheController.publishDateTime))
			{
			    CacheController.publishDateTime = publishDateTimeCandidate;
			}
		}
	    
		//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("isValidContentPart end", t.getElapsedTimeNanos() / 1000);
		
		return isValidContent;					
	}
	
	
	/**
	 * This method returns the id of the content that is protected if any. Looks recursive upwards.
	 */
	//static long totalLoadTime = 0;
	
	public Integer getProtectedContentId(Database db, Integer contentId) throws SystemException, Exception
	{
		Integer protectedContentId = null;
		
		//org.infoglue.deliver.util.Timer t = new org.infoglue.deliver.util.Timer();
	    Content content = (Content)getObjectWithId(SmallContentImpl.class, contentId, db);
	    //Content content = (Content)getObjectWithId(ContentImpl.class, contentId, db);
	    	    
    	protectedContentId = getProtectedContentId(db, content);

    	//totalLoadTime = totalLoadTime + t.getElapsedTimeNanos();
    	//System.out.println("totalLoadTime:" + totalLoadTime / 1000000);
		
		return protectedContentId;
	}

	
	/**
	 * This method returns the id of the content that is protected if any. Looks recursive upwards.
	 */
	
	public Integer getProtectedContentId(Database db, Content content)
	{
		Integer protectedContentId = null;
		
		try
		{
			logger.info("content:" + content.getId() + ":" + content.getIsProtected());

			if(content != null && content.getIsProtected() != null)
			{	
				if(content.getIsProtected().intValue() == ContentVO.NO.intValue())
					protectedContentId = null;
				else if(content.getIsProtected().intValue() == ContentVO.YES.intValue())
					protectedContentId = content.getId();
				else if(content.getIsProtected().intValue() == ContentVO.INHERITED.intValue())
				{
					Content parentContent = null; //= content.getParentContent();
					if(content instanceof MediumContentImpl)
					{
						Integer parentContentId = ((MediumContentImpl)content).getParentContentId();
						if(parentContentId != null)
							parentContent = (MediumContentImpl)getObjectWithId(MediumContentImpl.class, parentContentId, db);
					}
					else if(content instanceof SmallContentImpl)
					{
						Integer parentContentId = ((SmallContentImpl)content).getParentContentId();
						if(parentContentId != null)
							parentContent = (SmallContentImpl)getObjectWithId(SmallContentImpl.class, parentContentId, db);
					}
					else if(content instanceof ContentImpl)
					{
						parentContent = content.getParentContent();
					}
					
					if(parentContent != null)
					{
						protectedContentId = getProtectedContentId(db, parentContent); 
					}
				}
			}

		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
				
		return protectedContentId;
	}

	
	/**
	 * This method just sorts the list of qualifyers on sortOrder.
	 */
	
	public List sortContents(Database db, Collection contents, Integer languageId, Integer siteNodeId, String sortAttributeName, String sortOrder, boolean useLanguageFallback, boolean includeFolders, DeliveryContext deliveryContext, InfoGluePrincipal infoGluePrincipal)
	{
		List sortedContents = new ArrayList();

		try
		{		
			Iterator iterator = contents.iterator();
			while(iterator.hasNext())
			{
				Content content = (Content)iterator.next();
				if(includeFolders || content.getIsBranch().booleanValue() == false)
				{
					int index = 0;
					Iterator sortedListIterator = sortedContents.iterator();
					while(sortedListIterator.hasNext())
					{
						Content sortedContent = (Content)sortedListIterator.next();
						
						//Here we sort on name if the name on a container is wanted 
						if(sortAttributeName.equalsIgnoreCase("contentId"))
						{
							Integer id       = content.getContentId();
							Integer sortedId = sortedContent.getContentId();
							if(id != null && sortedId != null && sortOrder.equalsIgnoreCase("asc") && id.compareTo(sortedId) < 0)
					    	{
					    		break;
					    	}
					    	else if(id != null && sortedId != null && sortOrder.equalsIgnoreCase("desc") && id.compareTo(sortedId) > 0)
					    	{
					    		break;
					    	}
						}
						else if(sortAttributeName.equalsIgnoreCase("name"))
						{
							String name       = content.getName();
							String sortedName = sortedContent.getName();
							if(name != null && sortedName != null && sortOrder.equalsIgnoreCase("asc") && name.compareTo(sortedName) < 0)
					    	{
					    		break;
					    	}
					    	else if(name != null && sortedName != null && sortOrder.equalsIgnoreCase("desc") && name.compareTo(sortedName) > 0)
					    	{
					    		break;
					    	}
						}
						//Here we sort on date if the dates on a container is wanted 
						else if(sortAttributeName.equalsIgnoreCase("publishDateTime"))
						{
							Date date       = content.getPublishDateTime();
							Date sortedDate = sortedContent.getPublishDateTime();
							if(date != null && sortedDate != null && sortOrder.equalsIgnoreCase("asc") && date.before(sortedDate))
					    	{
					    		break;
					    	}
					    	else if(date != null && sortedDate != null && sortOrder.equalsIgnoreCase("desc") && date.after(sortedDate))
					    	{
					    		break;
					    	}
						}
						else if(sortAttributeName.equalsIgnoreCase("expireDateTime"))
						{
							Date date       = content.getExpireDateTime();
							Date sortedDate = sortedContent.getExpireDateTime();
							if(date != null && sortedDate != null && sortOrder.equalsIgnoreCase("asc") && date.before(sortedDate))
					    	{
					    		break;
					    	}
					    	else if(date != null && sortedDate != null && sortOrder.equalsIgnoreCase("desc") && date.after(sortedDate))
					    	{
					    		break;
					    	}
						}
						else
						{
							String contentAttribute       = this.getContentAttribute(db, content.getId(), languageId, sortAttributeName, siteNodeId, useLanguageFallback, deliveryContext, infoGluePrincipal, false);
							String sortedContentAttribute = this.getContentAttribute(db, sortedContent.getId(), languageId, sortAttributeName, siteNodeId, useLanguageFallback, deliveryContext, infoGluePrincipal, false);
							if(contentAttribute != null && sortedContentAttribute != null && sortOrder.equalsIgnoreCase("asc") && contentAttribute.compareTo(sortedContentAttribute) < 0)
					    	{
					    		break;
					    	}
					    	else if(contentAttribute != null && sortedContentAttribute != null && sortOrder.equalsIgnoreCase("desc") && contentAttribute.compareTo(sortedContentAttribute) > 0)
					    	{
					    		break;
					    	}
						}				    	
				    	index++;
					}
					sortedContents.add(index, content);
				}			    					
			}
		}
		catch(Exception e)
		{
			logger.warn("The sorting of contents failed:" + e.getMessage(), e);
		}
			
		return sortedContents;
	}
	
	private Category getCastorJDOCategory()
    {
        Enumeration enumeration = Logger.getCurrentCategories();
        while(enumeration.hasMoreElements())
        {
            Category category = (Category)enumeration.nextElement();
            if(category.getName().equalsIgnoreCase("org.exolab.castor.jdo"))
                return category;
        }
        
        return null;
    }
}