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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.UserContentTypeDefinition;
import org.infoglue.cms.entities.management.UserProperties;
import org.infoglue.cms.entities.management.UserPropertiesVO;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.UserContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.UserPropertiesImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class is the controller for all handling of extranet roles properties.
 */

public class UserPropertiesController extends BaseController
{
    private final static Logger logger = Logger.getLogger(UserPropertiesController.class.getName());

	/**
	 * Factory method
	 */

	public static UserPropertiesController getController()
	{
		return new UserPropertiesController();
	}
	
	
    public UserProperties getUserPropertiesWithId(Integer userPropertiesId, Database db) throws SystemException, Bug
    {
		return (UserProperties) getObjectWithId(UserPropertiesImpl.class, userPropertiesId, db);
    }
    
    public UserPropertiesVO getUserPropertiesVOWithId(Integer userPropertiesId) throws SystemException, Bug
    {
		return (UserPropertiesVO) getVOWithId(UserPropertiesImpl.class, userPropertiesId);
    }
  
    public List getUserPropertiesVOList() throws SystemException, Bug
    {
        return getAllVOObjects(UserPropertiesImpl.class, "userPropertiesId");
    }

    /**
	 * This method created a new UserPropertiesVO in the database.
	 */

	public UserPropertiesVO create(Integer languageId, Integer contentTypeDefinitionId, UserPropertiesVO userPropertiesVO) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		UserProperties userProperties = null;

		beginTransaction(db);
		try
		{
			userProperties = create(languageId, contentTypeDefinitionId, userPropertiesVO, db);
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not completes the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
    
		return userProperties.getValueObject();
	}     

	/**
	 * This method created a new UserPropertiesVO in the database. It also updates the extranetgroup
	 * so it recognises the change. 
	 */

	public UserProperties create(Integer languageId, Integer contentTypeDefinitionId, UserPropertiesVO userPropertiesVO, Database db) throws ConstraintException, SystemException, Exception
    {
		Language language = LanguageController.getController().getLanguageWithId(languageId, db);
		ContentTypeDefinition contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);

		UserProperties userProperties = new UserPropertiesImpl();
		userProperties.setLanguage((LanguageImpl)language);
		userProperties.setContentTypeDefinition((ContentTypeDefinition)contentTypeDefinition);
	
		userProperties.setValueObject(userPropertiesVO);

		db.create(userProperties); 
		
		return userProperties;
	}     
	
	/**
	 * This method updates an extranet role properties.
	 */

	public UserPropertiesVO update(Integer languageId, Integer contentTypeDefinitionId, UserPropertiesVO userPropertiesVO) throws ConstraintException, SystemException, Exception
	{
		UserPropertiesVO realUserPropertiesVO = userPropertiesVO;
    	
		if(userPropertiesVO.getId() == null)
		{
			InfoGluePrincipal infoGluePrincipal = UserControllerProxy.getController().getUser(userPropertiesVO.getUserName());
			List userPropertiesVOList = UserPropertiesController.getController().getUserPropertiesVOList(infoGluePrincipal.getName(), languageId);
			if(userPropertiesVOList != null && userPropertiesVOList.size() > 0)
			{
				realUserPropertiesVO = (UserPropertiesVO)userPropertiesVOList.get(0);
				realUserPropertiesVO.setValue(userPropertiesVO.getValue());	
			}
			else
			{
				logger.info("Creating the entity because there was no version at all for: " + contentTypeDefinitionId + " " + languageId);
				realUserPropertiesVO = create(languageId, contentTypeDefinitionId, userPropertiesVO);
			}
		}

		return (UserPropertiesVO) updateEntity(UserPropertiesImpl.class, (BaseEntityVO) realUserPropertiesVO);
	}        

	public UserPropertiesVO update(UserPropertiesVO userPropertiesVO, String[] users) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		UserProperties userProperties = null;

		beginTransaction(db);

		try
		{
			//add validation here if needed
			userProperties = getUserPropertiesWithId(userPropertiesVO.getUserPropertiesId(), db);       	
			userProperties.setValueObject(userPropertiesVO);

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

		return userProperties.getValueObject();
	}     
	


    public void delete(UserPropertiesVO userPropertiesVO) throws ConstraintException, SystemException
    {
    	deleteEntity(UserPropertiesImpl.class, userPropertiesVO.getUserPropertiesId());
    }        

	/**
	 * This method deletes the relation to a digital asset - not the asset itself.
	 */
	public void deleteDigitalAssetRelation(Integer userPropertiesId, DigitalAsset digitalAsset, Database db) throws SystemException, Bug
    {
	    UserProperties userProperties = getUserPropertiesWithId(userPropertiesId, db);
	    
		userProperties.getDigitalAssets().remove(digitalAsset);
        digitalAsset.getUserProperties().remove(userProperties);
    }

	/**
	 * This method gets a list of roleProperties for a role
	 * The result is a list of propertiesblobs - each propertyblob is a list of actual properties.
	 */

	public List getUserPropertiesVOList(Database db, String userName, Integer languageId) throws ConstraintException, SystemException, Exception
	{
		List userPropertiesVOList = new ArrayList();

		List userProperties = getUserPropertiesList(userName, languageId, db, true);
		userPropertiesVOList = toVOList(userProperties);
			
		return userPropertiesVOList;
	}

	/**
	 * This method gets a list of roleProperties for a role
	 * The result is a list of propertiesblobs - each propertyblob is a list of actual properties.
	 */

	public List getUserPropertiesVOList(String userName, Integer languageId) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		List userPropertiesVOList = new ArrayList();

		beginTransaction(db);

		try
		{
			List userProperties = getUserPropertiesList(userName, languageId, db, true);
			userPropertiesVOList = toVOList(userProperties);
			
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

		return userPropertiesVOList;
	}

	/**
	 * This method gets a list of userProperties for a user
	 * The result is a list of propertiesblobs - each propertyblob is a list of actual properties.
	 */

	public List getUserPropertiesList(String userName, Integer languageId, Database db, boolean readOnly) throws ConstraintException, SystemException, Exception
	{
		List userPropertiesList = new ArrayList();

		OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.UserPropertiesImpl f WHERE f.userName = $1 AND f.language = $2");
		oql.bind(userName);
		oql.bind(languageId);

		QueryResults results = null;
		if(readOnly)
		{
		    results = oql.execute(Database.ReadOnly);
		}
		else
		{
		    logger.info("Fetching entity in read/write mode:" + userName);
		    results = oql.execute();
		}
		
		while (results.hasMore()) 
		{
			UserProperties userProperties = (UserProperties)results.next();
			userPropertiesList.add(userProperties);
		}

		results.close();
		oql.close();

		return userPropertiesList;
	}
	

	/**
	 * This method fetches all content types available for this user. 
	 */
	
	public List getContentTypeDefinitionVOList(String userName) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		List contentTypeDefinitionVOList = new ArrayList();

		beginTransaction(db);

		try
		{
			List userContentTypeDefinitionList = getUserContentTypeDefinitionList(userName, db);
			Iterator contentTypeDefinitionsIterator = userContentTypeDefinitionList.iterator();
			while(contentTypeDefinitionsIterator.hasNext())
			{
				UserContentTypeDefinition userContentTypeDefinition = (UserContentTypeDefinition)contentTypeDefinitionsIterator.next();
				contentTypeDefinitionVOList.add(userContentTypeDefinition.getContentTypeDefinition().getValueObject());
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
	 * This method fetches all user content types available for this user within a transaction. 
	 */
	
	public List getUserContentTypeDefinitionList(String userName, Database db) throws ConstraintException, SystemException, Exception
	{
		List userContentTypeDefinitionList = new ArrayList();
		
		OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.UserContentTypeDefinitionImpl f WHERE f.userName = $1");
		oql.bind(userName);

		QueryResults results = oql.execute();
		logger.info("Fetching entity in read/write mode:" + userName);

		while (results.hasMore()) 
		{
			UserContentTypeDefinition userContentTypeDefinition = (UserContentTypeDefinition)results.next();
			userContentTypeDefinitionList.add(userContentTypeDefinition);
		}

		results.close();
		oql.close();

		return userContentTypeDefinitionList;
	}
	
	/**
	 * This method fetches all content types available for this role. 
	 */

	public void updateContentTypeDefinitions(String userName, String[] contentTypeDefinitionIds) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		List contentTypeDefinitionVOList = new ArrayList();

		beginTransaction(db);

		try
		{
			List userContentTypeDefinitionList = this.getUserContentTypeDefinitionList(userName, db);
			Iterator contentTypeDefinitionsIterator = userContentTypeDefinitionList.iterator();
			while(contentTypeDefinitionsIterator.hasNext())
			{
				UserContentTypeDefinition userContentTypeDefinition = (UserContentTypeDefinition)contentTypeDefinitionsIterator.next();
				db.remove(userContentTypeDefinition);
			}
			
			for(int i=0; i<contentTypeDefinitionIds.length; i++)
			{
				Integer contentTypeDefinitionId = new Integer(contentTypeDefinitionIds[i]);
				ContentTypeDefinition contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);
				UserContentTypeDefinitionImpl userContentTypeDefinitionImpl = new UserContentTypeDefinitionImpl();
				userContentTypeDefinitionImpl.setUserName(userName);
				userContentTypeDefinitionImpl.setContentTypeDefinition(contentTypeDefinition);
				db.create(userContentTypeDefinitionImpl);
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
	 * This method fetches a value from the xml that is the userProperties Value. It then updates that
	 * single value and saves it back to the db.
	 */
	 
	public void updateAttributeValue(Integer userPropertiesId, String attributeName, String attributeValue) throws SystemException, Bug
	{
		UserPropertiesVO userPropertiesVO = getUserPropertiesVOWithId(userPropertiesId);
		
		if(userPropertiesVO != null)
		{
			try
			{
				logger.info("attributeName:"  + attributeName);
				logger.info("versionValue:"   + userPropertiesVO.getValue());
				logger.info("attributeValue:" + attributeValue);
				InputSource inputSource = new InputSource(new StringReader(userPropertiesVO.getValue()));
				
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
				userPropertiesVO.setValue(sb.toString());
				update(userPropertiesVO.getLanguageId(), userPropertiesVO.getContentTypeDefinitionId(), userPropertiesVO);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * This method fetches a value from the xml that is the userProperties Value. 
	 */
	 
	public String getAttributeValue(Integer userPropertiesId, String attributeName, boolean escapeHTML) throws SystemException, Bug
	{
		String value = "";
		
		UserPropertiesVO userPropertiesVO = getUserPropertiesVOWithId(userPropertiesId);
		
		if(userPropertiesVO != null)
		{	
			try
			{
				logger.info("attributeName:" + attributeName);
				logger.info("VersionValue:"  + userPropertiesVO.getValue());
				InputSource inputSource = new InputSource(new StringReader(userPropertiesVO.getValue()));
				
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
							logger.info("Getting value: " + value);
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
		}
		//logger.info("value:" + value);	
		return value;
	}
	
	/**
	 * This method should return a list of those digital assets the contentVersion has.
	 */
	   	
	public List getDigitalAssetVOList(Integer userPropertiesId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

    	List digitalAssetVOList = new ArrayList();

        beginTransaction(db);

        try
        {
			UserProperties userProperties = UserPropertiesController.getController().getUserPropertiesWithId(userPropertiesId, db); 
			if(userProperties != null)
			{
				Collection digitalAssets = userProperties.getDigitalAssets();
				digitalAssetVOList = toVOList(digitalAssets);
			}
			            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.info("An error occurred when we tried to fetch the list of digitalAssets belonging to this userProperties:" + e);
            e.printStackTrace();
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    	
		return digitalAssetVOList;
    }

	
	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new UserPropertiesVO();
	}



}
 