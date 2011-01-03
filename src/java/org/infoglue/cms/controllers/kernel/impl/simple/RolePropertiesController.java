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

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.dom4j.Element;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.GroupProperties;
import org.infoglue.cms.entities.management.GroupPropertiesVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.PropertiesCategoryVO;
import org.infoglue.cms.entities.management.RoleContentTypeDefinition;
import org.infoglue.cms.entities.management.RoleProperties;
import org.infoglue.cms.entities.management.RolePropertiesVO;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.RoleContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.RolePropertiesImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class is the controller for all handling of extranet roles properties.
 */

public class RolePropertiesController extends BaseController
{
    private final static Logger logger = Logger.getLogger(RolePropertiesController.class.getName());

	/**
	 * Factory method
	 */

	public static RolePropertiesController getController()
	{
		return new RolePropertiesController();
	}
	
	
    public RoleProperties getRolePropertiesWithId(Integer rolePropertiesId, Database db) throws SystemException, Bug
    {
		return (RoleProperties) getObjectWithId(RolePropertiesImpl.class, rolePropertiesId, db);
    }
    
    public RolePropertiesVO getRolePropertiesVOWithId(Integer rolePropertiesId) throws SystemException, Bug
    {
		return (RolePropertiesVO) getVOWithId(RolePropertiesImpl.class, rolePropertiesId);
    }
  
    public List getRolePropertiesVOList() throws SystemException, Bug
    {
        return getAllVOObjects(RolePropertiesImpl.class, "rolePropertiesId");
    }

    
	/**
	 * This method created a new RolePropertiesVO in the database.
	 */

	public RolePropertiesVO create(Integer languageId, Integer contentTypeDefinitionId, RolePropertiesVO rolePropertiesVO) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		RoleProperties roleProperties = null;

		beginTransaction(db);
		try
		{
			roleProperties = create(languageId, contentTypeDefinitionId, rolePropertiesVO, db);
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not completes the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
    
		return roleProperties.getValueObject();
	}     

	/**
	 * This method created a new RolePropertiesVO in the database. It also updates the extranetrole
	 * so it recognises the change. 
	 */

	public RoleProperties create(Integer languageId, Integer contentTypeDefinitionId, RolePropertiesVO rolePropertiesVO, Database db) throws ConstraintException, SystemException, Exception
    {
		Language language = LanguageController.getController().getLanguageWithId(languageId, db);
		ContentTypeDefinition contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);

		RoleProperties roleProperties = new RolePropertiesImpl();
		roleProperties.setLanguage((LanguageImpl)language);
		roleProperties.setContentTypeDefinition((ContentTypeDefinition)contentTypeDefinition);
	
		roleProperties.setValueObject(rolePropertiesVO);
		db.create(roleProperties); 
		
		return roleProperties;
	}     
	
	/**
	 * This method updates an extranet role properties.
	 */

	public RolePropertiesVO update(Integer languageId, Integer contentTypeDefinitionId, RolePropertiesVO rolePropertiesVO) throws ConstraintException, SystemException
	{
		RolePropertiesVO realRolePropertiesVO = rolePropertiesVO;
    	
		if(rolePropertiesVO.getId() == null)
		{
			logger.info("Creating the entity because there was no version at all for: " + contentTypeDefinitionId + " " + languageId);
			realRolePropertiesVO = create(languageId, contentTypeDefinitionId, rolePropertiesVO);
		}

		return (RolePropertiesVO) updateEntity(RolePropertiesImpl.class, (BaseEntityVO) realRolePropertiesVO);
	}        

	public RolePropertiesVO update(RolePropertiesVO rolePropertiesVO, String[] extranetUsers) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		RoleProperties roleProperties = null;

		beginTransaction(db);

		try
		{
			//add validation here if needed
			roleProperties = getRolePropertiesWithId(rolePropertiesVO.getRolePropertiesId(), db);       	
			roleProperties.setValueObject(rolePropertiesVO);

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

		return roleProperties.getValueObject();
	}     
	
	/**
	 * This method gets a list of roleProperties for a role
	 * The result is a list of propertiesblobs - each propertyblob is a list of actual properties.
	 */

	public List getRolePropertiesVOList(String roleName, Integer languageId) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		List rolePropertiesVOList = new ArrayList();

		beginTransaction(db);

		try
		{
			List roleProperties = getRolePropertiesList(roleName, languageId, db, true);
			rolePropertiesVOList = toVOList(roleProperties);
			
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

		return rolePropertiesVOList;
	}

	/**
	 * This method gets a list of roleProperties for a role
	 * The result is a list of propertiesblobs - each propertyblob is a list of actual properties.
	 */

	public List getRolePropertiesList(String roleName, Integer languageId, Database db, boolean readOnly) throws ConstraintException, SystemException, Exception
	{
		List rolePropertiesList = new ArrayList();

		OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.RolePropertiesImpl f WHERE f.roleName = $1 AND f.language = $2");
		oql.bind(roleName);
		oql.bind(languageId);

		QueryResults results = null;
		if(readOnly)
		{
		    results = oql.execute(Database.ReadOnly);
		}
		else
		{
		    logger.info("Fetching entity in read/write mode:" + roleName);
		    results = oql.execute();
		}

		while (results.hasMore()) 
		{
			RoleProperties roleProperties = (RoleProperties)results.next();
			rolePropertiesList.add(roleProperties);
		}

		results.close();
		oql.close();

		return rolePropertiesList;
	}
	
	public Set<InfoGlueRole> getRolesByMatchingProperty(String propertyName, String value, Integer languageId, boolean useLanguageFallback, Database db) throws ConstraintException, SystemException, Exception
	{
		Set<InfoGlueRole> roles = new HashSet<InfoGlueRole>();
		
		try
		{
			List<RolePropertiesVO> roleProperties = RolePropertiesController.getController().getRolePropertiesVOList(propertyName, value, db);
			Iterator<RolePropertiesVO> rolePropertiesIterator = roleProperties.iterator();
			while(rolePropertiesIterator.hasNext())
			{
				RolePropertiesVO rolePropertiesVO = rolePropertiesIterator.next();
				if(useLanguageFallback || (!useLanguageFallback && languageId != null && rolePropertiesVO.getLanguageId().equals(languageId)))
				{
					if(rolePropertiesVO.getRoleName() != null)
					{
						try
						{
							InfoGlueRole role = RoleControllerProxy.getController(db).getRole(rolePropertiesVO.getRoleName());
							if(role != null)
								roles.add(role);
						}
						catch (Exception e) 
						{
							logger.warn("No such role or problem getting role " + rolePropertiesVO.getRoleName() + ":" + e.getMessage());
						}
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.warn("Problem in getRoleByMatchingProperty:" + e.getMessage(), e);
		}
		
		return roles;
	}

	/**
	 * This method gets a list of groupProperties where the group property matches a search made
	 */

	public List<RolePropertiesVO> getRolePropertiesVOList(String propertyName, String propertyValue, Database db) throws ConstraintException, SystemException, Exception
	{
		List<RolePropertiesVO> rolePropertiesVOList = new ArrayList<RolePropertiesVO>();

		String FREETEXT_EXPRESSION_VARIABLE  					= "%<" + propertyName + "><![CDATA[%" + propertyValue + "%]]></" + propertyName + ">%";
		String FREETEXT_EXPRESSION_VARIABLE_SQL_SERVER_ESCAPED  = "%<" + propertyName + "><![[]CDATA[[]%{" + propertyValue + "}%]]></" + propertyName + ">%";

		OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.RolePropertiesImpl f WHERE f.value like $1 ORDER BY f.rolePropertiesId");
		if(CmsPropertyHandler.getUseSQLServerDialect())
			oql.bind(FREETEXT_EXPRESSION_VARIABLE_SQL_SERVER_ESCAPED);
		else
			oql.bind(FREETEXT_EXPRESSION_VARIABLE);

		QueryResults results = oql.execute(Database.ReadOnly);

		while (results.hasMore()) 
		{
			RoleProperties roleProperties = (RoleProperties)results.next();
			rolePropertiesVOList.add(roleProperties.getValueObject());
		}
		
		results.close();
		oql.close();

		return rolePropertiesVOList;
	}

    public void delete(RolePropertiesVO rolePropertiesVO) throws ConstraintException, SystemException
    {
    	deleteEntity(RolePropertiesImpl.class, rolePropertiesVO.getRolePropertiesId());
    }        

    
	/**
	 * This method should return a list of those digital assets the contentVersion has.
	 */
	   	
	public List getDigitalAssetVOList(Integer rolePropertiesId) throws SystemException, Bug
    {
    	Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

    	List digitalAssetVOList = new ArrayList();

        beginTransaction(db);

        try
        {
			RoleProperties roleProperties = RolePropertiesController.getController().getRolePropertiesWithId(rolePropertiesId, db); 
			if(roleProperties != null)
			{
				Collection digitalAssets = roleProperties.getDigitalAssets();
				digitalAssetVOList = toVOList(digitalAssets);
			}
			            
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.info("An error occurred when we tried to fetch the list of digitalAssets belonging to this roleProperties:" + e);
            e.printStackTrace();
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    	
		return digitalAssetVOList;
    }

	
	/**
	 * This method deletes the relation to a digital asset - not the asset itself.
	 */
	public void deleteDigitalAssetRelation(Integer rolePropertiesId, DigitalAsset digitalAsset, Database db) throws SystemException, Bug
    {
	    RoleProperties roleProperties = getRolePropertiesWithId(rolePropertiesId, db);
	    roleProperties.getDigitalAssets().remove(digitalAsset);
        digitalAsset.getRoleProperties().remove(roleProperties);
    }


	/**
	 * This method fetches all content types available for this role. 
	 */
	
	public List getContentTypeDefinitionVOList(String roleName) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		List contentTypeDefinitionVOList = new ArrayList();

		beginTransaction(db);

		try
		{
			List roleContentTypeDefinitionList = getRoleContentTypeDefinitionList(roleName, db);
			Iterator contentTypeDefinitionsIterator = roleContentTypeDefinitionList.iterator();
			while(contentTypeDefinitionsIterator.hasNext())
			{
				RoleContentTypeDefinition roleContentTypeDefinition = (RoleContentTypeDefinition)contentTypeDefinitionsIterator.next();
				contentTypeDefinitionVOList.add(roleContentTypeDefinition.getContentTypeDefinition().getValueObject());
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
	 * This method fetches all role content types available for this role within a transaction. 
	 */
	
	public List getRoleContentTypeDefinitionList(String roleName, Database db) throws ConstraintException, SystemException, Exception
	{
		List roleContentTypeDefinitionList = new ArrayList();

		OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.RoleContentTypeDefinitionImpl f WHERE f.roleName = $1");
		oql.bind(roleName);

		QueryResults results = oql.execute();
		this.logger.info("Fetching entity in read/write mode");

		while (results.hasMore()) 
		{
			RoleContentTypeDefinition roleContentTypeDefinition = (RoleContentTypeDefinition)results.next();
			roleContentTypeDefinitionList.add(roleContentTypeDefinition);
		}

		results.close();
		oql.close();

		return roleContentTypeDefinitionList;
	}
	
	/**
	 * This method fetches all content types available for this role. 
	 */

	public void updateContentTypeDefinitions(String roleName, String[] contentTypeDefinitionIds) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		List contentTypeDefinitionVOList = new ArrayList();

		beginTransaction(db);

		try
		{
			List roleContentTypeDefinitionList = this.getRoleContentTypeDefinitionList(roleName, db);
			Iterator contentTypeDefinitionsIterator = roleContentTypeDefinitionList.iterator();
			while(contentTypeDefinitionsIterator.hasNext())
			{
				RoleContentTypeDefinition roleContentTypeDefinition = (RoleContentTypeDefinition)contentTypeDefinitionsIterator.next();
				db.remove(roleContentTypeDefinition);
			}
			
			for(int i=0; i<contentTypeDefinitionIds.length; i++)
			{
				Integer contentTypeDefinitionId = new Integer(contentTypeDefinitionIds[i]);
				ContentTypeDefinition contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);
				RoleContentTypeDefinitionImpl roleContentTypeDefinitionImpl = new RoleContentTypeDefinitionImpl();
				roleContentTypeDefinitionImpl.setRoleName(roleName);
				roleContentTypeDefinitionImpl.setContentTypeDefinition(contentTypeDefinition);
				db.create(roleContentTypeDefinitionImpl);
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
	 * This method fetches a value from the xml that is the roleProperties Value. It then updates that
	 * single value and saves it back to the db.
	 */
	 
	public void updateAttributeValue(Integer rolePropertiesId, String attributeName, String attributeValue) throws SystemException, Bug
	{
		RolePropertiesVO rolePropertiesVO = getRolePropertiesVOWithId(rolePropertiesId);
		
		if(rolePropertiesVO != null)
		{
			try
			{
				logger.info("attributeName:"  + attributeName);
				logger.info("versionValue:"   + rolePropertiesVO.getValue());
				logger.info("attributeValue:" + attributeValue);
				InputSource inputSource = new InputSource(new StringReader(rolePropertiesVO.getValue()));
				
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
				rolePropertiesVO.setValue(sb.toString());
				update(rolePropertiesVO.getLanguageId(), rolePropertiesVO.getContentTypeDefinitionId(), rolePropertiesVO);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * Returns the value of a Role Property
	 */

	public String getAttributeValue(String roleName, Integer languageId, String attributeName) throws SystemException
	{
		String value = "";
		
	    Database db = CastorDatabaseService.getDatabase();

		beginTransaction(db);

		try
		{
		    value = getAttributeValue(roleName, languageId, attributeName, db);
		    
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
	 * Returns the value of a Role Property
	 */

	public String getAttributeValue(String roleName, Integer languageId, String attributeName, Database db) throws SystemException, Exception
	{
		String value = "";
		
	    List roleProperties = this.getRolePropertiesList(roleName, languageId, db, true);
	    Iterator iterator = roleProperties.iterator();
	    RoleProperties roleProperty = null;
	    while(iterator.hasNext())
	    {
	        roleProperty = (RoleProperties)iterator.next();
	        break;
	    }
	    
	    value = this.getAttributeValue(roleProperty.getValue(), attributeName, false);
		    		
		return value;
	}

	
	/**
	 * This method fetches a value from the xml that is the roleProperties Value. 
	 */
	 
	public String getAttributeValue(Integer rolePropertiesId, String attributeName, boolean escapeHTML) throws SystemException, Bug
	{
		String value = "";
		
		RolePropertiesVO rolePropertiesVO = getRolePropertiesVOWithId(rolePropertiesId);
		
		if(rolePropertiesVO != null)
		{	
			value = getAttributeValue(rolePropertiesVO.getValue(), attributeName, escapeHTML);
		}

		return value;
	}

	/**
	 * This method fetches a value from the xml that is the roleProperties Value. 
	 */
	 
	public String getAttributeValue(String xml, String attributeName, boolean escapeHTML) throws SystemException, Bug
	{
		String value = "";
		
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

		return value;
	}
	
	
	/**
	 * Returns the related Contents
	 * @param rolePropertiesId
	 * @return
	 */

	public List getRelatedContents(String roleName, Integer languageId, String attributeName) throws SystemException
	{
		Database db = CastorDatabaseService.getDatabase();

		List relatedContentVOList = new ArrayList();

		beginTransaction(db);

		try
		{
		    List roleProperties = this.getRolePropertiesList(roleName, languageId, db, true);
		    Iterator iterator = roleProperties.iterator();
		    RoleProperties roleProperty = null;
		    while(iterator.hasNext())
		    {
		        roleProperty = (RoleProperties)iterator.next();
		        break;
		    }
		    
		    String xml = this.getAttributeValue(roleProperty.getValue(), attributeName, false);
			List contents = this.getRelatedContentsFromXML(db, xml);

			relatedContentVOList = toVOList(contents);
			
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
	 * Returns the related SiteNodes
	 * @param rolePropertiesId
	 * @return
	 */

	public List getRelatedSiteNodes(String roleName, Integer languageId, String attributeName) throws SystemException
	{
		Database db = CastorDatabaseService.getDatabase();

		List relatedSiteNodeVOList = new ArrayList();

		beginTransaction(db);

		try
		{
		    List roleProperties = this.getRolePropertiesList(roleName, languageId, db, true);
		    Iterator iterator = roleProperties.iterator();
		    RoleProperties roleProperty = null;
		    while(iterator.hasNext())
		    {
		        roleProperty = (RoleProperties)iterator.next();
		        break;
		    }
		    
		    String xml = this.getAttributeValue(roleProperty.getValue(), attributeName, false);
			List siteNodes = this.getRelatedSiteNodesFromXML(db, xml);

			relatedSiteNodeVOList = toVOList(siteNodes);
			
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
			e.printStackTrace();
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
			e.printStackTrace();
		}
		
		return siteNodes;
	}

	
	/**
	 * Returns all current Category relationships for th specified attribute name
	 * @param attribute
	 * @return
	 */
	
	public List getRelatedCategories(String roleName, Integer languageId, String attribute)
	{
	    List relatedCategories = new ArrayList();
	    
		try
		{
		    List rolePropertiesVOList = this.getRolePropertiesVOList(roleName, languageId);
		    Iterator iterator = rolePropertiesVOList.iterator();
		    RolePropertiesVO rolePropertyVO = null;
		    while(iterator.hasNext())
		    {
		        rolePropertyVO = (RolePropertiesVO)iterator.next();
		        break;
		    }

			if(rolePropertyVO != null && rolePropertyVO.getId() != null)
			{
		    	List propertiesCategoryVOList = PropertiesCategoryController.getController().findByPropertiesAttribute(attribute, RoleProperties.class.getName(), rolePropertyVO.getId());
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
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new RolePropertiesVO();
	}

}
 