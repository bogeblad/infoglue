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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.dom4j.Element;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.GroupContentTypeDefinition;
import org.infoglue.cms.entities.management.GroupProperties;
import org.infoglue.cms.entities.management.GroupPropertiesVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.PropertiesCategory;
import org.infoglue.cms.entities.management.PropertiesCategoryVO;
import org.infoglue.cms.entities.management.impl.simple.GroupContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupPropertiesImpl;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class is the controller for all handling of extranet groups properties.
 */

public class GroupPropertiesController extends BaseController
{
    private final static Logger logger = Logger.getLogger(GroupPropertiesController.class.getName());

	/**
	 * Factory method
	 */

	public static GroupPropertiesController getController()
	{
		return new GroupPropertiesController();
	}
	
	
    public GroupProperties getGroupPropertiesWithId(Integer groupPropertiesId, Database db) throws SystemException, Bug
    {
		return (GroupProperties) getObjectWithId(GroupPropertiesImpl.class, groupPropertiesId, db);
    }
    
    public GroupPropertiesVO getGroupPropertiesVOWithId(Integer groupPropertiesId) throws SystemException, Bug
    {
		return (GroupPropertiesVO) getVOWithId(GroupPropertiesImpl.class, groupPropertiesId);
    }
  
    public List getGroupPropertiesVOList() throws SystemException, Bug
    {
        return getAllVOObjects(GroupPropertiesImpl.class, "groupPropertiesId");
    }

    
	/**
	 * This method created a new GroupPropertiesVO in the database.
	 */

	public GroupPropertiesVO create(Integer languageId, Integer contentTypeDefinitionId, GroupPropertiesVO groupPropertiesVO) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		GroupProperties groupProperties = null;

		beginTransaction(db);
		try
		{
			groupProperties = create(languageId, contentTypeDefinitionId, groupPropertiesVO, db);
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not completes the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
    
		return groupProperties.getValueObject();
	}     

	/**
	 * This method created a new GroupPropertiesVO in the database. It also updates the extranetgroup
	 * so it recognises the change. 
	 */

	public GroupProperties create(Integer languageId, Integer contentTypeDefinitionId, GroupPropertiesVO groupPropertiesVO, Database db) throws ConstraintException, SystemException, Exception
    {
		Language language = LanguageController.getController().getLanguageWithId(languageId, db);
		ContentTypeDefinition contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);

		GroupProperties groupProperties = new GroupPropertiesImpl();
		groupProperties.setLanguage((LanguageImpl)language);
		groupProperties.setContentTypeDefinition((ContentTypeDefinition)contentTypeDefinition);
	
		groupProperties.setValueObject(groupPropertiesVO);
		db.create(groupProperties); 
		
		return groupProperties;
	}     
	
	/**
	 * This method updates an extranet group properties.
	 */

	public GroupPropertiesVO update(Integer languageId, Integer contentTypeDefinitionId, GroupPropertiesVO groupPropertiesVO) throws ConstraintException, SystemException
	{
		GroupPropertiesVO realGroupPropertiesVO = groupPropertiesVO;
    	
		if(groupPropertiesVO.getId() == null)
		{
			logger.info("Creating the entity because there was no version at all for: " + contentTypeDefinitionId + " " + languageId);
			realGroupPropertiesVO = create(languageId, contentTypeDefinitionId, groupPropertiesVO);
		}

		return (GroupPropertiesVO) updateEntity(GroupPropertiesImpl.class, (BaseEntityVO) realGroupPropertiesVO);
	}        

	public GroupPropertiesVO update(GroupPropertiesVO groupPropertiesVO, String[] extranetUsers) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		GroupProperties groupProperties = null;

		beginTransaction(db);

		try
		{
			//add validation here if needed
			groupProperties = getGroupPropertiesWithId(groupPropertiesVO.getGroupPropertiesId(), db);       	
			groupProperties.setValueObject(groupPropertiesVO);

			//If any of the validations or setMethods reported an error, we throw them up now before create.
			ceb.throwIfNotEmpty();
            
			commitTransaction(db);
		}
		catch(ConstraintException ce)
		{
			logger.warn("An error occurred so we should not complete the transaction:" + ce, ce);
			rollbackTransaction(db);
			throw ce;
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

		return groupProperties.getValueObject();
	}     

	
	/**
	 * This method gets a list of groupProperties for a group
	 * The result is a list of propertiesblobs - each propertyblob is a list of actual properties.
	 */

	public List getGroupPropertiesVOList(String groupName, Integer languageId) throws ConstraintException, SystemException
	{
	    List groupPropertiesVOList = new ArrayList();
	    
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		beginTransaction(db);

		try
		{
			groupPropertiesVOList = getGroupPropertiesVOList(groupName, languageId, db);
			
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return groupPropertiesVOList;
	}

	
	/**
	 * This method gets a list of groupProperties for a group
	 * The result is a list of propertiesblobs - each propertyblob is a list of actual properties.
	 */
	public static AtomicBoolean inCacheProgress = new AtomicBoolean(false);
	public List getGroupPropertiesVOList(String groupName, Integer languageId, Database db) throws ConstraintException, Exception
	{
	    List groupPropertiesVOList = new ArrayList();
	    
		String cacheKey = "" + groupName + "_" + languageId;
		logger.info("cacheKey:" + cacheKey);
		
		Object groupPropertiesVOListCandidate = CacheController.getCachedObject("groupPropertiesCache", cacheKey);
		Boolean cached = (Boolean)CacheController.getCachedObject("groupPropertiesCache", "preCacheDone");
		if(groupPropertiesVOListCandidate != null && groupPropertiesVOListCandidate instanceof NullObject)
		{
			logger.info("NullObject found:" + cacheKey);
			return groupPropertiesVOList;
		}
		else if(groupPropertiesVOListCandidate != null)
		{
			logger.info("groupPropertiesVOListCandidate found:" + cacheKey);
			groupPropertiesVOList =  (List)groupPropertiesVOListCandidate;
		}
		else if(cached != null && cached.booleanValue() == true)
		{
			return groupPropertiesVOList;
		}
		else
		{
			class PreCacheGroupPropertiesTask implements Runnable 
			{
				PreCacheGroupPropertiesTask() { }
		        
		        public void run() 
		        {
					try
					{
						Database db = CastorDatabaseService.getDatabase();
						try 
						{
							beginTransaction(db);

							preCacheAllGroupProperties(db);
							commitTransaction(db);
						} 
						catch (Exception e) 
						{
							logger.error("Error precaching all group properties: " + e.getMessage(), e);
							rollbackTransaction(db);
						}
					}
					catch (Exception e) 
					{
						logger.error("Could not start PreCacheTask:" + e.getMessage(), e);
					}
					finally
					{
						inCacheProgress.set(false);
					}
		        }
		    }
			if(inCacheProgress.compareAndSet(false, true))
			{
				Thread thread = new Thread(new PreCacheGroupPropertiesTask());
			    thread.start();
			}

			logger.info("No groupPropertiesVOListCandidate found:" + cacheKey);
			List groupPropertiesList = getGroupPropertiesList(groupName, languageId, db, true);
			if(groupPropertiesList != null)
			{
			    groupPropertiesVOList = toVOList(groupPropertiesList);
			    logger.info("Caching:" + cacheKey + "=" + groupPropertiesVOList);
		    	CacheController.cacheObject("groupPropertiesCache", cacheKey, groupPropertiesVOList);
			}
			else
			{
				logger.info("Caching nullobject:" + cacheKey);
				CacheController.cacheObject("groupPropertiesCache", cacheKey, new NullObject());
			}
		}
		/*
		groupPropertiesVOList = (List)CacheController.getCachedObject("groupPropertiesCache", cacheKey);
		if(groupPropertiesVOList != null)
		{
			logger.info("There was an cached groupPropertiesVOList:" + groupPropertiesVOList.size());
		}
		else
		{
			//System.out.println("Reading hard group properties...");
			List groupPropertiesList = getGroupPropertiesList(groupName, languageId, db, true);
			if(groupPropertiesList != null)
			{
			    groupPropertiesVOList = toVOList(groupPropertiesList);
		    	CacheController.cacheObject("groupPropertiesCache", cacheKey, groupPropertiesVOList);
			}

		}
		*/
		
		return groupPropertiesVOList;
	}


	/**
	 * This method gets a list of groupProperties for a group
	 * The result is a list of propertiesblobs - each propertyblob is a list of actual properties.
	 */

	public List getGroupPropertiesList(String groupName, Integer languageId, Database db, boolean readOnly) throws ConstraintException, SystemException, Exception
	{
		List groupPropertiesList = new ArrayList();

		try
		{
			RequestAnalyser.getRequestAnalyser().incApproximateNumberOfDatabaseQueries();

			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.GroupPropertiesImpl f WHERE f.groupName = $1 AND f.language = $2 ORDER BY f.groupPropertiesId");
			oql.bind(groupName);
			oql.bind(languageId);
	
			if(logger.isInfoEnabled())
			{
				logger.info("groupName:" + groupName);
				logger.info("languageId:" + languageId);
			}
	
			QueryResults results;
			if(readOnly)
			{
				logger.info("Fetching groupPropertiesList in readonly mode");
			    results = oql.execute(Database.ReadOnly);
			}
			else
			{
				logger.info("Fetching groupPropertiesList in read/write mode");
			    results = oql.execute();
			}   
	
			while (results.hasMore()) 
			{
				GroupProperties groupProperties = (GroupProperties)results.next();
				logger.info("Found one:" + groupProperties);
				groupPropertiesList.add(groupProperties);
			}
			
			logger.info("In total:" + groupPropertiesList.size());
	
			results.close();
			oql.close();
		}
		catch(Exception e)
        {
			logger.warn("Error getting groupPropertiesList. Message: " + e.getMessage() + ". Not retrying...");
			throw e;
        }
		finally
		{
			RequestAnalyser.getRequestAnalyser().decApproximateNumberOfDatabaseQueries();
		}
		
		return groupPropertiesList;
	}

	public Set<InfoGlueGroup> getGroupsByMatchingProperty(String propertyName, String value, Integer languageId, boolean useLanguageFallback, Database db) throws ConstraintException, SystemException, Exception
	{
		Set<InfoGlueGroup> groups = new HashSet<InfoGlueGroup>();
		
		try
		{
			List<GroupPropertiesVO> groupProperties = GroupPropertiesController.getController().getGroupPropertiesVOList(propertyName, value, db);
			Iterator<GroupPropertiesVO> groupPropertiesIterator = groupProperties.iterator();
			while(groupPropertiesIterator.hasNext())
			{
				GroupPropertiesVO groupPropertiesVO = groupPropertiesIterator.next();
				if(useLanguageFallback || (!useLanguageFallback && languageId != null && groupPropertiesVO.getLanguageId().equals(languageId)))
				{
					if(groupPropertiesVO.getGroupName() != null)
					{
						try
						{
							InfoGlueGroup group = GroupControllerProxy.getController(db).getGroup(groupPropertiesVO.getGroupName());
							if(group != null)
								groups.add(group);
						}
						catch (Exception e) 
						{
							logger.warn("No such group or problem getting group " + groupPropertiesVO.getGroupName() + ":" + e.getMessage());
						}
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.warn("Problem in getGroupByMatchingProperty:" + e.getMessage(), e);
		}
		
		return groups;
	}

	/**
	 * This method gets a list of groupProperties where the group property matches a search made
	 */

	public List<GroupPropertiesVO> getGroupPropertiesVOList(String propertyName, String propertyValue, Database db) throws ConstraintException, SystemException, Exception
	{
		List<GroupPropertiesVO> groupPropertiesVOList = new ArrayList<GroupPropertiesVO>();

		String FREETEXT_EXPRESSION_VARIABLE  					= "%<" + propertyName + "><![CDATA[%" + propertyValue + "%]]></" + propertyName + ">%";
		String FREETEXT_EXPRESSION_VARIABLE_SQL_SERVER_ESCAPED  = "%<" + propertyName + "><![[]CDATA[[]%{" + propertyValue + "}%]]></" + propertyName + ">%";

		OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.GroupPropertiesImpl f WHERE f.value like $1 ORDER BY f.groupPropertiesId");
		if(CmsPropertyHandler.getUseSQLServerDialect())
			oql.bind(FREETEXT_EXPRESSION_VARIABLE_SQL_SERVER_ESCAPED);
		else
			oql.bind(FREETEXT_EXPRESSION_VARIABLE);

		QueryResults results = oql.execute(Database.ReadOnly);

		while (results.hasMore()) 
		{
			GroupProperties groupProperties = (GroupProperties)results.next();
			groupPropertiesVOList.add(groupProperties.getValueObject());
		}
		
		results.close();
		oql.close();

		return groupPropertiesVOList;
	}

    public void delete(GroupPropertiesVO groupPropertiesVO) throws ConstraintException, SystemException
    {
    	deleteEntity(GroupPropertiesImpl.class, groupPropertiesVO.getGroupPropertiesId());
    }        

    
	/**
	 * This method should return a list of those digital assets the contentVersion has.
	 */
	   	
	public List getDigitalAssetVOList(Integer groupPropertiesId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

    	List digitalAssetVOList = new ArrayList();

        beginTransaction(db);

        try
        {
			GroupProperties groupProperties = GroupPropertiesController.getController().getGroupPropertiesWithId(groupPropertiesId, db); 
			if(groupProperties != null)
			{
				Collection digitalAssets = groupProperties.getDigitalAssets();
				digitalAssetVOList = toVOList(digitalAssets);
			}
			            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred when we tried to fetch the list of digitalAssets belonging to this groupProperties:" + e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    	
		return digitalAssetVOList;
    }

	
	/**
	 * This method deletes the relation to a digital asset - not the asset itself.
	 */
	public void deleteDigitalAssetRelation(Integer groupPropertiesId, DigitalAsset digitalAsset, Database db) throws SystemException, Bug
    {
	    GroupProperties groupProperties = getGroupPropertiesWithId(groupPropertiesId, db);
	    groupProperties.getDigitalAssets().remove(digitalAsset);
	    digitalAsset.getGroupProperties().remove(groupProperties);
    }


	/**
	 * This method fetches all content types available for this group. 
	 */
	
	public List getContentTypeDefinitionVOList(String groupName) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		List contentTypeDefinitionVOList = new ArrayList();

		beginTransaction(db);

		try
		{
			List groupContentTypeDefinitionList = getGroupContentTypeDefinitionList(groupName, db);
			Iterator contentTypeDefinitionsIterator = groupContentTypeDefinitionList.iterator();
			while(contentTypeDefinitionsIterator.hasNext())
			{
				GroupContentTypeDefinition groupContentTypeDefinition = (GroupContentTypeDefinition)contentTypeDefinitionsIterator.next();
				contentTypeDefinitionVOList.add(groupContentTypeDefinition.getContentTypeDefinition().getValueObject());
			}
	
			ceb.throwIfNotEmpty();
    
			commitTransaction(db);
		}
		catch(ConstraintException ce)
		{
			logger.warn("An error occurred so we should not complete the transaction:" + ce, ce);
			rollbackTransaction(db);
			throw ce;
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

		return contentTypeDefinitionVOList;
	}

	/**
	 * This method fetches all group content types available for this group within a transaction. 
	 */
	
	public List getGroupContentTypeDefinitionList(String groupName, Database db) throws ConstraintException, SystemException, Exception
	{
		List groupContentTypeDefinitionList = new ArrayList();

		OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.GroupContentTypeDefinitionImpl f WHERE f.groupName = $1");
		oql.bind(groupName);

		QueryResults results = oql.execute();
		this.logger.info("Fetching groupContentTypeDefinitionList in read/write mode");

		while (results.hasMore()) 
		{
			GroupContentTypeDefinition groupContentTypeDefinition = (GroupContentTypeDefinition)results.next();
			groupContentTypeDefinitionList.add(groupContentTypeDefinition);
		}

		results.close();
		oql.close();

		return groupContentTypeDefinitionList;
	}
	
	/**
	 * This method fetches all content types available for this group. 
	 */

	public void updateContentTypeDefinitions(String groupName, String[] contentTypeDefinitionIds) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		List contentTypeDefinitionVOList = new ArrayList();

		beginTransaction(db);

		try
		{
			List groupContentTypeDefinitionList = this.getGroupContentTypeDefinitionList(groupName, db);
			Iterator contentTypeDefinitionsIterator = groupContentTypeDefinitionList.iterator();
			while(contentTypeDefinitionsIterator.hasNext())
			{
				GroupContentTypeDefinition groupContentTypeDefinition = (GroupContentTypeDefinition)contentTypeDefinitionsIterator.next();
				db.remove(groupContentTypeDefinition);
			}
			
			for(int i=0; i<contentTypeDefinitionIds.length; i++)
			{
				Integer contentTypeDefinitionId = new Integer(contentTypeDefinitionIds[i]);
				ContentTypeDefinition contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);
				GroupContentTypeDefinitionImpl groupContentTypeDefinitionImpl = new GroupContentTypeDefinitionImpl();
				groupContentTypeDefinitionImpl.setGroupName(groupName);
				groupContentTypeDefinitionImpl.setContentTypeDefinition(contentTypeDefinition);
				db.create(groupContentTypeDefinitionImpl);
			}
			
			ceb.throwIfNotEmpty();

			commitTransaction(db);
		}
		catch(ConstraintException ce)
		{
			logger.warn("An error occurred so we should not complete the transaction:" + ce, ce);
			rollbackTransaction(db);
			throw ce;
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}
	
	
	/**
	 * This method fetches a value from the xml that is the groupProperties Value. It then updates that
	 * single value and saves it back to the db.
	 */
	 
	public void updateAttributeValue(Integer groupPropertiesId, String attributeName, String attributeValue) throws SystemException, Bug
	{
		GroupPropertiesVO groupPropertiesVO = getGroupPropertiesVOWithId(groupPropertiesId);
		
		if(groupPropertiesVO != null)
		{
			try
			{
				logger.info("attributeName:"  + attributeName);
				logger.info("versionValue:"   + groupPropertiesVO.getValue());
				logger.info("attributeValue:" + attributeValue);
				InputSource inputSource = new InputSource(new StringReader(groupPropertiesVO.getValue()));
				
				DOMParser parser = new DOMParser();
				parser.parse(inputSource);
				Document document = parser.getDocument();
				
				NodeList nl = document.getDocumentElement().getChildNodes();
				Node attributesNode = nl.item(0);
				
				boolean existed = false;
				nl = attributesNode.getChildNodes();
				for(int i=0; i<nl.getLength(); i++)
				{
					Node n = nl.item(i);
					if(n.getNodeName().equalsIgnoreCase(attributeName))
					{
						if(n.getFirstChild() != null && n.getFirstChild().getNodeValue() != null)
						{
							n.getFirstChild().setNodeValue(attributeValue);
							existed = true;
							break;
						}
						else
						{
							CDATASection cdata = document.createCDATASection(attributeValue);
							n.appendChild(cdata);
							existed = true;
							break;
						}
					}
				}
				
				if(existed == false)
				{
					org.w3c.dom.Element attributeElement = document.createElement(attributeName);
					attributesNode.appendChild(attributeElement);
					CDATASection cdata = document.createCDATASection(attributeValue);
					attributeElement.appendChild(cdata);
				}
				
				StringBuffer sb = new StringBuffer();
				org.infoglue.cms.util.XMLHelper.serializeDom(document.getDocumentElement(), sb);
				logger.info("sb:" + sb);
				groupPropertiesVO.setValue(sb.toString());
				update(groupPropertiesVO.getLanguageId(), groupPropertiesVO.getContentTypeDefinitionId(), groupPropertiesVO);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * Returns the value of a Group Property
	 */

	public String getAttributeValue(String groupName, Integer languageId, String attributeName) throws SystemException
	{
		String value = "";
		
	    Database db = CastorDatabaseService.getDatabase();

		beginTransaction(db);

		try
		{
		    value = getAttributeValue(groupName, languageId, attributeName, db);
			
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return value;
	}


	/**
	 * Returns the value of a Group Property
	 */

	public String getAttributeValue(String groupName, Integer languageId, String attributeName, Database db) throws SystemException, Exception
	{
		String value = "";
		
		logger.info("groupName: " + groupName);
		logger.info("languageId: " + languageId);
		logger.info("attributeName: " + attributeName);
	    List groupPropertiesVO = this.getGroupPropertiesVOList(groupName, languageId, db);
		logger.info("groupPropertiesVO: " + groupPropertiesVO);
	    Iterator iterator = groupPropertiesVO.iterator();
		GroupPropertiesVO groupPropertyVO = null;
		while(iterator.hasNext())
		{
	        groupPropertyVO = (GroupPropertiesVO)iterator.next();
			logger.info("groupPropertyVO: " + groupPropertyVO.getId() + ":" + groupPropertyVO.getValue());
	        break;
	    }

		logger.info("groupPropertyVO: " + groupPropertyVO);
		if(groupPropertyVO != null)
		{	
			value = this.getAttributeValue(groupPropertyVO.getValue(), attributeName, false);
			logger.info("value: " + value);
		}
		
		return value;
	}

	/**
	 * This method fetches a value from the xml that is the groupProperties Value. 
	 */
	 
	public String getAttributeValue(Integer groupPropertiesId, String attributeName, boolean escapeHTML) throws SystemException, Bug
	{
		String value = "";
		
		GroupPropertiesVO groupPropertiesVO = getGroupPropertiesVOWithId(groupPropertiesId);
		
		if(groupPropertiesVO != null)
		{	
			value = getAttributeValue(groupPropertiesVO.getValue(), attributeName, escapeHTML);
		}

		return value;
	}

	
	/**
	 * This method fetches a value from the xml that is the groupProperties Value. 
	 */
	 
	public String getAttributeValue(String xml, String attributeName, boolean escapeHTML) throws SystemException, Bug
	{
		String value = "";
		int startTagIndex = xml.indexOf("<" + attributeName + ">");
		int endTagIndex   = xml.indexOf("]]></" + attributeName + ">");

		if(startTagIndex > 0 && startTagIndex < xml.length() && endTagIndex > startTagIndex && endTagIndex <  xml.length())
		{
			value = xml.substring(startTagIndex + attributeName.length() + 11, endTagIndex);
			if(escapeHTML)
				value = new VisualFormatter().escapeHTML(value);
		}
		/*
		try
		{
			InputSource inputSource = new InputSource(new StringReader(xml));
			
			DOMParser parser = new DOMParser();
			parser.parse(inputSource);
			Document document = parser.getDocument();
			
			NodeList nl = document.getDocumentElement().getChildNodes();
			Node n = nl.item(0);
			
			nl = n.getChildNodes();
			for(int i=0; i<nl.getLength(); i++)
			{
				n = nl.item(i);
				if(n.getNodeName().equalsIgnoreCase(attributeName))
				{
					if(n.getFirstChild() != null && n.getFirstChild().getNodeValue() != null)
					{
						value = n.getFirstChild().getNodeValue();
						if(value != null && escapeHTML)
							value = new VisualFormatter().escapeHTML(value);

						break;
					}
				}
			}		        	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		*/
		return value;
	}

	/**
	 * Returns an attribute value from the ContentVersionVO
	 *
	 * @param contentVersionVO The version on which to find the value
	 * @param attributeName THe name of the attribute whose value is wanted
	 * @param escapeHTML A boolean indicating if the result should be escaped
	 * @return The String vlaue of the attribute, or blank if it doe snot exist.
	 */
	public String getAttributeValue(ContentVersionVO contentVersionVO, String attributeName, boolean escapeHTML)
	{
		String xml = contentVersionVO.getVersionValue();

		String value = "";
		int startTagIndex = xml.indexOf("<" + attributeName + ">");
		int endTagIndex   = xml.indexOf("]]></" + attributeName + ">");

		if(startTagIndex > 0 && startTagIndex < xml.length() && endTagIndex > startTagIndex && endTagIndex <  xml.length())
		{
			value = xml.substring(startTagIndex + attributeName.length() + 11, endTagIndex);
			if(escapeHTML)
				value = new VisualFormatter().escapeHTML(value);
		}		

		return value;
	}

	
	/**
	 * Returns the related Contents
	 * @param groupPropertiesId
	 * @return
	 */

	public List getRelatedContents(String groupName, Integer languageId, String attributeName) throws SystemException
	{
		Database db = CastorDatabaseService.getDatabase();

		List relatedContentVOList = new ArrayList();

		beginTransaction(db);

		try
		{
			List relatedContents = getRelatedContents(groupName, languageId, attributeName, db);
			if(relatedContents != null)
			    relatedContentVOList = toVOList(relatedContents);
			
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return relatedContentVOList;
	}

	/**
	 * Returns the related Contents
	 * @param groupPropertiesId
	 * @return
	 */

	public List getReadOnlyRelatedContents(String groupName, Integer languageId, String attributeName, Database db) throws SystemException, Exception
	{
		List relatedContentList = new ArrayList();

		List groupPropertiesVO = this.getGroupPropertiesVOList(groupName, languageId, db);
	    Iterator iterator = groupPropertiesVO.iterator();
	    GroupPropertiesVO groupPropertyVO = null;
	    while(iterator.hasNext())
	    {
	        groupPropertyVO = (GroupPropertiesVO)iterator.next();
	        break;
	    }

		if(groupPropertyVO != null)
		{
			String xml = this.getAttributeValue(groupPropertyVO.getValue(), attributeName, false);
			relatedContentList = this.getReadOnlyRelatedContentsFromXML(db, xml);
		}
		
		return relatedContentList;
	}

	public List<ContentVO> getReadOnlyRelatedContentVOList(String groupName, Integer languageId, String attributeName, Database db) throws SystemException, Exception
	{
		List<ContentVO> relatedContentList = new ArrayList<ContentVO>();

		List groupPropertiesVO = this.getGroupPropertiesVOList(groupName, languageId, db);
	    Iterator iterator = groupPropertiesVO.iterator();
	    GroupPropertiesVO groupPropertyVO = null;
	    while(iterator.hasNext())
	    {
	        groupPropertyVO = (GroupPropertiesVO)iterator.next();
	        break;
	    }

		if(groupPropertyVO != null)
		{
			String xml = this.getAttributeValue(groupPropertyVO.getValue(), attributeName, false);
			relatedContentList = this.getRelatedContentVOListFromXML(db, xml);
		}
		
		return relatedContentList;
	}

	/**
	 * Returns the related Contents
	 * @param groupPropertiesId
	 * @return
	 */

	public List getRelatedContents(String groupName, Integer languageId, String attributeName, Database db) throws SystemException, Exception
	{
		List relatedContentList = new ArrayList();

		List groupPropertiesVO = this.getGroupPropertiesVOList(groupName, languageId, db);
	    Iterator iterator = groupPropertiesVO.iterator();
	    GroupPropertiesVO groupPropertyVO = null;
	    while(iterator.hasNext())
	    {
	        groupPropertyVO = (GroupPropertiesVO)iterator.next();
	        break;
	    }

		if(groupPropertyVO != null)
		{
			String xml = this.getAttributeValue(groupPropertyVO.getValue(), attributeName, false);
			relatedContentList = this.getRelatedContentsFromXML(db, xml);
		}
		
		return relatedContentList;
	}

	/**
	 * Returns the related SiteNodes
	 * @param groupPropertiesId
	 * @return
	 */

	public List getRelatedSiteNodes(String groupName, Integer languageId, String attributeName) throws SystemException
	{
		Database db = CastorDatabaseService.getDatabase();

		List relatedSiteNodeVOList = new ArrayList();

		beginTransaction(db);

		try
		{
		    List relatedSiteNodes = getRelatedSiteNodes(groupName, languageId, attributeName, db);
		    if(relatedSiteNodes != null)
		        relatedSiteNodeVOList = toVOList(relatedSiteNodes);
			
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return relatedSiteNodeVOList;
	}


	/**
	 * Returns the related SiteNodes
	 * @param groupPropertiesId
	 * @return
	 */

	public List getRelatedSiteNodes(String groupName, Integer languageId, String attributeName, Database db) throws SystemException, Exception
	{
		List relatedSiteNodeList = new ArrayList();

		List groupProperties = this.getGroupPropertiesVOList(groupName, languageId, db);
	    Iterator iterator = groupProperties.iterator();
	    GroupPropertiesVO groupPropertyVO = null;
	    while(iterator.hasNext())
	    {
	        groupPropertyVO = (GroupPropertiesVO)iterator.next();
	        break;
	    }
	    
		if(groupPropertyVO != null)
		{
			String xml = this.getAttributeValue(groupPropertyVO.getValue(), attributeName, false);
			relatedSiteNodeList = this.getRelatedSiteNodesFromXML(db, xml);
		}

		return relatedSiteNodeList;
	}

	/**
	 * Returns the related SiteNodes
	 * @param groupPropertiesId
	 * @return
	 */

	public List getReadOnlyRelatedSiteNodes(String groupName, Integer languageId, String attributeName, Database db) throws SystemException, Exception
	{
		List relatedSiteNodeList = new ArrayList();

		List groupProperties = this.getGroupPropertiesVOList(groupName, languageId, db);
	    Iterator iterator = groupProperties.iterator();
	    GroupPropertiesVO groupPropertyVO = null;
	    while(iterator.hasNext())
	    {
	        groupPropertyVO = (GroupPropertiesVO)iterator.next();
	        break;
	    }
	    
		if(groupPropertyVO != null)
		{
			String xml = this.getAttributeValue(groupPropertyVO.getValue(), attributeName, false);
			relatedSiteNodeList = this.getReadOnlyRelatedSiteNodesFromXML(db, xml);
		}

		return relatedSiteNodeList;
	}

	public void preCacheAllGroupProperties(Database db) throws SystemException, Exception
	{
		Boolean cached = (Boolean)CacheController.getCachedObject("groupPropertiesCache", "preCacheDone");
		if(cached != null && cached)
		{
			return;
		}
		
		Timer t = new Timer();
		
		logger.warn("Starting preCache");
		List<LanguageVO> languageVOList = LanguageController.getController().getLanguageVOList(db);
		for(LanguageVO languageVO : languageVOList)
		{
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.GroupPropertiesImpl f WHERE f.language = $1 ORDER BY f.groupName");
			oql.bind(languageVO.getId());
	
			QueryResults results;
		    results = oql.execute(Database.ReadOnly);
	
		    String groupName = "";
		    List groupPropertyValues = new ArrayList();
			while (results.hasMore()) 
			{
				GroupProperties groupProperties = (GroupProperties)results.next();
				if(!groupProperties.getGroupName().equals(groupName))
				{
					String cacheKey = "" + groupName + "_" + languageVO.getId();
					
					logger.info("Caching for " + cacheKey + ":" + groupPropertyValues.size());
					CacheController.cacheObject("groupPropertiesCache", cacheKey, groupPropertyValues);
					
					groupPropertyValues = new ArrayList();
					groupName = groupProperties.getGroupName();
				}
				groupPropertyValues.add(groupProperties.getValueObject());
			}
			if(groupName != null)
			{
				String cacheKey = "" + groupName + "_" + languageVO.getId();
				CacheController.cacheObject("groupPropertiesCache", cacheKey, groupPropertyValues);
			}
			
			results.close();
			oql.close();
		}

		CacheController.cacheObject("groupPropertiesCache", "preCacheDone", new Boolean(true));
		logger.warn("Precaching done in " + t.getElapsedTime());
	}

	
	public List<SiteNodeVO> getReadOnlyRelatedSiteNodeVOList(String groupName, Integer languageId, String attributeName, Database db) throws SystemException, Exception
	{
		List<SiteNodeVO> relatedSiteNodeList = new ArrayList<SiteNodeVO>();

		List groupProperties = this.getGroupPropertiesVOList(groupName, languageId, db);
	    Iterator iterator = groupProperties.iterator();
	    GroupPropertiesVO groupPropertyVO = null;
	    while(iterator.hasNext())
	    {
	        groupPropertyVO = (GroupPropertiesVO)iterator.next();
	        break;
	    }
	    
		if(groupPropertyVO != null)
		{
			String xml = this.getAttributeValue(groupPropertyVO.getValue(), attributeName, false);
			relatedSiteNodeList = this.getReadOnlyRelatedSiteNodeVOListFromXML(db, xml);
		}

		return relatedSiteNodeList;
	}

	/**
	 * Parses contents from an XML within a transaction
	 * @param qualifyerXML
	 * @return
	 */

	private List getRelatedContentsFromXML(Database db, String qualifyerXML)
	{
		List contents = new ArrayList(); 
    	
		if(qualifyerXML == null || qualifyerXML.length() == 0)
			return contents;
		
		try
		{
			org.dom4j.Document document = new DOMBuilder().getDocument(qualifyerXML);
			
			String entity = document.getRootElement().attributeValue("entity");
			
			List children = document.getRootElement().elements();
			Iterator i = children.iterator();
			while(i.hasNext())
			{
				Element child = (Element)i.next();
				String id = child.getStringValue();
				
				Content content = ContentController.getContentController().getContentWithId(new Integer(id), db);
				contents.add(content);     	
			}		        	
		}
		catch(Exception e)
		{
			logger.warn("An error getting related contents:" + e.getMessage(), e);
		}
		
		return contents;
	}

	private List<ContentVO> getRelatedContentVOListFromXML(Database db, String qualifyerXML)
	{
		List<ContentVO> contents = new ArrayList<ContentVO>(); 
    	
		if(qualifyerXML == null || qualifyerXML.length() == 0)
			return contents;
		
		try
		{
			org.dom4j.Document document = new DOMBuilder().getDocument(qualifyerXML);
			
			String entity = document.getRootElement().attributeValue("entity");
			
			List children = document.getRootElement().elements();
			Iterator i = children.iterator();
			while(i.hasNext())
			{
				Element child = (Element)i.next();
				String id = child.getStringValue();
				
				ContentVO content = ContentController.getContentController().getContentVOWithId(new Integer(id), db);
				contents.add(content);     	
			}		        	
		}
		catch(Exception e)
		{
			logger.warn("An error getting related contents:" + e.getMessage());
		}
		
		return contents;
	}

	/**
	 * Parses contents from an XML within a transaction
	 * @param qualifyerXML
	 * @return
	 */

	private List getReadOnlyRelatedContentsFromXML(Database db, String qualifyerXML)
	{
		List contents = new ArrayList(); 
    	
		if(qualifyerXML == null || qualifyerXML.length() == 0)
			return contents;
		
		try
		{
			org.dom4j.Document document = new DOMBuilder().getDocument(qualifyerXML);
			
			String entity = document.getRootElement().attributeValue("entity");
			
			List children = document.getRootElement().elements();
			Iterator i = children.iterator();
			while(i.hasNext())
			{
				Element child = (Element)i.next();
				String id = child.getStringValue();
				
				Content content = ContentController.getContentController().getReadOnlyContentWithId(new Integer(id), db);
				contents.add(content);     	
			}		        	
		}
		catch(Exception e)
		{
			logger.warn("An error getting related contents:" + e.getMessage());
			logger.info("An error getting related contents:" + e.getMessage(), e);
		}
		
		return contents;
	}

	/**
	 * Parses siteNodes from an XML within a transaction
	 * @param qualifyerXML
	 * @return
	 */

	private List getRelatedSiteNodesFromXML(Database db, String qualifyerXML)
	{
		List siteNodes = new ArrayList(); 
    	
		if(qualifyerXML == null || qualifyerXML.length() == 0)
			return siteNodes;
		
		try
		{
			org.dom4j.Document document = new DOMBuilder().getDocument(qualifyerXML);
			
			String entity = document.getRootElement().attributeValue("entity");
			
			List children = document.getRootElement().elements();
			Iterator i = children.iterator();
			while(i.hasNext())
			{
				Element child = (Element)i.next();
				String id = child.getStringValue();
				
				SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(new Integer(id), db);
				siteNodes.add(siteNode);     	
			}		        	
		}
		catch(Exception e)
		{
			logger.warn("An error getting related site nodes:" + e.getMessage(), e);
		}
		
		return siteNodes;
	}

	/**
	 * Parses siteNodes from an XML within a transaction
	 * @param qualifyerXML
	 * @return
	 */

	private List getReadOnlyRelatedSiteNodesFromXML(Database db, String qualifyerXML)
	{
		List siteNodes = new ArrayList(); 
    	
		if(qualifyerXML == null || qualifyerXML.length() == 0)
			return siteNodes;
		
		try
		{
			org.dom4j.Document document = new DOMBuilder().getDocument(qualifyerXML);
			
			String entity = document.getRootElement().attributeValue("entity");
			
			List children = document.getRootElement().elements();
			Iterator i = children.iterator();
			while(i.hasNext())
			{
				Element child = (Element)i.next();
				String id = child.getStringValue();
				
				SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(new Integer(id), db, true);
				siteNodes.add(siteNode);     	
			}		        	
		}
		catch(Exception e)
		{
			logger.warn("An error getting related pages:" + e.getMessage());
			logger.info("An error getting related pages:" + e.getMessage(), e);
		}
		
		return siteNodes;
	}
	
	private List<SiteNodeVO> getReadOnlyRelatedSiteNodeVOListFromXML(Database db, String qualifyerXML)
	{
		List<SiteNodeVO> siteNodes = new ArrayList<SiteNodeVO>(); 
    	
		if(qualifyerXML == null || qualifyerXML.length() == 0)
			return siteNodes;
		
		try
		{
			org.dom4j.Document document = new DOMBuilder().getDocument(qualifyerXML);
			
			String entity = document.getRootElement().attributeValue("entity");
			
			List children = document.getRootElement().elements();
			Iterator i = children.iterator();
			while(i.hasNext())
			{
				Element child = (Element)i.next();
				String id = child.getStringValue();
				
				SiteNodeVO siteNode = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(id), db);
				siteNodes.add(siteNode);     	
			}		        	
		}
		catch(Exception e)
		{
			logger.warn("An error getting related pages:" + e.getMessage());
			logger.info("An error getting related pages:" + e.getMessage(), e);
		}
		
		return siteNodes;
	}

	/**
	 * Returns all current Category relationships for th specified attribute name
	 * @param attribute
	 * @return
	 */
	
	public List getRelatedCategories(String groupName, Integer languageId, String attribute)
	{
	    List relatedCategories = new ArrayList();
	    
		try
		{
		    List groupPropertiesVOList = this.getGroupPropertiesVOList(groupName, languageId);
		    Iterator iterator = groupPropertiesVOList.iterator();
		    GroupPropertiesVO groupPropertyVO = null;
		    while(iterator.hasNext())
		    {
		        groupPropertyVO = (GroupPropertiesVO)iterator.next();
		        break;
		    }

			if(groupPropertyVO != null && groupPropertyVO.getId() != null)
			{
		    	List propertiesCategoryVOList = PropertiesCategoryController.getController().findByPropertiesAttribute(attribute, GroupProperties.class.getName(), groupPropertyVO.getId());
		    	Iterator propertiesCategoryVOListIterator = propertiesCategoryVOList.iterator();
		    	while(propertiesCategoryVOListIterator.hasNext())
		    	{
		    	    PropertiesCategoryVO propertiesCategoryVO = (PropertiesCategoryVO)propertiesCategoryVOListIterator.next();
		    	    relatedCategories.add(propertiesCategoryVO.getCategory());
		    	}
			}
		}
		catch(Exception e)
		{
			logger.warn("We could not fetch the list of defined category keys: " + e.getMessage(), e);
		}

		return relatedCategories;
	}
	
	
	/**
	 * Returns all current Category relationships for th specified attribute name
	 * @param attribute
	 * @return
	 */
	
	public List<CategoryVO> getRelatedCategoriesVOList(String groupName, Integer languageId, String attribute, Database db) throws SystemException, Exception
	{
	    List<CategoryVO> relatedCategoriesVOList = new ArrayList<CategoryVO>();
	    
	    
		String cacheKey = "" + groupName + "_" + languageId + "_" + attribute;
		logger.info("cacheKey:" + cacheKey);
		relatedCategoriesVOList = (List<CategoryVO>)CacheController.getCachedObject("relatedCategoriesCache", cacheKey);
		if(relatedCategoriesVOList != null)
		{
			logger.info("There was an cached groupPropertiesVOList:" + relatedCategoriesVOList.size());
		}
		else
		{
			relatedCategoriesVOList = getRelatedCategoryVOList(groupName, languageId, attribute, db);
	    	if(relatedCategoriesVOList != null)
	    		CacheController.cacheObject("relatedCategoriesCache", cacheKey, relatedCategoriesVOList);
			/*
			List relatedCategories = getRelatedCategoriesReadOnly(groupName, languageId, attribute, db);
			if(relatedCategories != null)
			{
			    relatedCategoriesVOList = toVOList(relatedCategories);
		    	CacheController.cacheObject("relatedCategoriesCache", cacheKey, relatedCategoriesVOList);
			}
			*/
		}

		return relatedCategoriesVOList;
	}

	/**
	 * Returns all current Category relationships for th specified attribute name
	 * @param attribute
	 * @return
	 */
	
	public List getRelatedCategories(String groupName, Integer languageId, String attribute, Database db)
	{
	    List relatedCategories = new ArrayList();
	    
		try
		{
		    List groupPropertiesVOList = this.getGroupPropertiesVOList(groupName, languageId, db);
		    Iterator iterator = groupPropertiesVOList.iterator();
		    GroupPropertiesVO groupPropertyVO = null;
		    while(iterator.hasNext())
		    {
		        groupPropertyVO = (GroupPropertiesVO)iterator.next();
		        break;
		    }

			if(groupPropertyVO != null && groupPropertyVO.getId() != null)
			{
		    	List propertiesCategoryList = PropertiesCategoryController.getController().findByPropertiesAttribute(attribute, GroupProperties.class.getName(), groupPropertyVO.getId(), db);

		    	Iterator propertiesCategoryListIterator = propertiesCategoryList.iterator();
		    	while(propertiesCategoryListIterator.hasNext())
		    	{
		    	    PropertiesCategory propertiesCategory = (PropertiesCategory)propertiesCategoryListIterator.next();
		    	    relatedCategories.add(propertiesCategory.getCategory());
		    	}
			}
		}
		catch(Exception e)
		{
			logger.warn("We could not fetch the list of defined category keys: " + e.getMessage(), e);
		}

		return relatedCategories;
	}

	/**
	 * Returns all current Category relationships for th specified attribute name
	 * @param attribute
	 * @return
	 */
	
	public List getRelatedCategoriesReadOnly(String groupName, Integer languageId, String attribute, Database db)
	{
	    List relatedCategories = new ArrayList();
	    
		try
		{
		    List groupPropertiesVOList = this.getGroupPropertiesVOList(groupName, languageId, db);
		    Iterator iterator = groupPropertiesVOList.iterator();
		    GroupPropertiesVO groupPropertyVO = null;
		    while(iterator.hasNext())
		    {
		        groupPropertyVO = (GroupPropertiesVO)iterator.next();
		        break;
		    }

			if(groupPropertyVO != null && groupPropertyVO.getId() != null)
			{
		    	List propertiesCategoryList = PropertiesCategoryController.getController().findByPropertiesAttributeReadOnly(attribute, GroupProperties.class.getName(), groupPropertyVO.getId(), db);

		    	Iterator propertiesCategoryListIterator = propertiesCategoryList.iterator();
		    	while(propertiesCategoryListIterator.hasNext())
		    	{
		    	    PropertiesCategory propertiesCategory = (PropertiesCategory)propertiesCategoryListIterator.next();
		    	    relatedCategories.add(propertiesCategory.getCategory());
		    	}
			}
		}
		catch(Exception e)
		{
			logger.warn("We could not fetch the list of defined category keys: " + e.getMessage(), e);
		}

		return relatedCategories;
	}

	/**
	 * Returns all current Category relationships for th specified attribute name
	 * @param attribute
	 * @return
	 */
	public static AtomicBoolean preCacheInProgress = new AtomicBoolean(false);
	
	public List<CategoryVO> getRelatedCategoryVOList(String groupName, Integer languageId, String attribute, Database db)
	{
		Timer t = new Timer();
	    List<CategoryVO> relatedCategories = new ArrayList<CategoryVO>();
	    
		try
		{
		    List groupPropertiesVOList = this.getGroupPropertiesVOList(groupName, languageId, db);
		    Iterator iterator = groupPropertiesVOList.iterator();
		    GroupPropertiesVO groupPropertyVO = null;
		    while(iterator.hasNext())
		    {
		        groupPropertyVO = (GroupPropertiesVO)iterator.next();
		        break;
		    }
		  
			if(groupPropertyVO != null && groupPropertyVO.getId() != null)
			{
				if(CacheController.getCacheSize("propertiesCategoryCache") == 0 && preCacheInProgress.compareAndSet(false, true))
				{
					try
					{
						PropertiesCategoryController.getController().preCacheAllPropertiesCategoryVOList();
					}
					finally
					{
						preCacheInProgress.set(false);
					}
					logger.warn("preCacheAllPropertiesCategoryVOList took: " + t.getElapsedTime());
				}

				String key = "categoryVOList_" + attribute + "_" + GroupProperties.class.getName() + "_" + groupPropertyVO.getId();
				List<CategoryVO> categoryVOList = (List<CategoryVO>)CacheController.getCachedObject("propertiesCategoryCache", key);
				if(categoryVOList != null)
				{
					//System.out.println("Returning cached result:" + categoryVOList.size());
					relatedCategories = categoryVOList;
				}
				else
				{
					if(CacheController.getCachedObject("propertiesCategoryCache", "allValuesCached") != null)
					{
						//System.out.println("Skipping as there is no such property in the full precache..");
					}
					else
					{
						logger.warn("Reading the hard way for:" + key);
	
				    	List propertiesCategoryList = PropertiesCategoryController.getController().findByPropertiesAttributeReadOnly(attribute, GroupProperties.class.getName(), groupPropertyVO.getId(), db);
					
				    	Iterator propertiesCategoryListIterator = propertiesCategoryList.iterator();
				    	while(propertiesCategoryListIterator.hasNext())
				    	{
				    	    PropertiesCategory propertiesCategory = (PropertiesCategory)propertiesCategoryListIterator.next();
				    	    relatedCategories.add(propertiesCategory.getCategory().getValueObject());
				    	}
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("We could not fetch the list of defined category keys: " + e.getMessage(), e);
		}

		return relatedCategories;
	}

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new GroupPropertiesVO();
	}

}
 