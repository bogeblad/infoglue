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

package org.infoglue.cms.applications.contenttool.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupPropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RolePropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserPropertiesController;
import org.infoglue.cms.entities.management.GroupProperties;
import org.infoglue.cms.entities.management.RoleProperties;
import org.infoglue.cms.entities.management.UserProperties;
import org.infoglue.cms.entities.structure.QualifyerVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.dom.DOMBuilder;

/**
 * This action is the base action for all relation editors.
 */ 

public abstract class ViewRelationEditorAction extends InfoGlueAbstractAction
{
	//protected Integer contentVersionId			= null;
	protected Integer entityId					= null;
	protected String entityName					= null;
	//protected String contentVersionAttributeName= null;
	protected String attributeName				= null;
	protected String updateAction				= null;
	
    protected Integer repositoryId 				= null;
	protected ConstraintExceptionBuffer ceb 	= null;
   	protected String qualifyerXML 				= null;
	protected String relationXML 				= null;
	protected String tree 						= null;	
	protected List repositories 				= null;
	protected String path 						= null;
   	protected List qualifyers 					= null;
	protected String currentAction 				= null;
	protected String changeRepositoryAction 	= null;
	protected String currentEntity 				= null;
	protected String currentEntityIdentifyer	= null;

	protected String callbackMethod				= null;
   
	public void setCallbackMethod(String callbackMethod)
	{
		this.callbackMethod = callbackMethod;
	}

	public String getCallbackMethod()
	{
		return this.callbackMethod;
	}

	public void setEntityId(Integer entityId)
	{
		this.entityId = entityId;
	}

	public Integer getEntityId()
	{
		return this.entityId;
	}

	public String getAttributeName()
	{
		return attributeName;
	}

	public void setAttributeName(String attributeName)
	{
		this.attributeName = attributeName;
	}
	
	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public void setPath(String path)
	{
		this.path = path;
	}
	
	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}

	public void setQualifyerXML(String qualifyerXML)
	{
		this.qualifyerXML = qualifyerXML;
	}
	
	public String getQualifyerXML()
	{
		return this.qualifyerXML;
	}
	
	public String getTree()
	{
		return this.tree;
	}

	public void setTree(String tree)
	{
		this.tree = tree;
	}
	
	public List getQualifyers()
	{
		return this.qualifyers;
	}
	
	public String getCurrentEntity()
	{
		return this.currentEntity;
	}

	public void setCurrentEntity(String currentEntity)
	{
		this.currentEntity = currentEntity;
	}

	public String getCurrentEntityIdentifyer()
	{
		return this.currentEntityIdentifyer;
	}

	public void setCurrentEntityIdentifyer(String currentEntityIdentifyer)
	{
		this.currentEntityIdentifyer = currentEntityIdentifyer;
	}

	public String getCurrentAction()
	{
		return this.currentAction;
	}

	public String getChangeRepositoryAction()
	{
		return this.changeRepositoryAction;
	}

	public List getRepositories()
	{
		return repositories;
	}  
	
	public String getUpdateAction()
	{
		return updateAction;
	}

	public void setUpdateAction(String updateAction)
	{
		this.updateAction = updateAction;
	}

	public String getEntityName()
	{
		return entityName;
	}

	public void setEntityName(String entityName)
	{
		this.entityName = entityName;
	}
	
	public abstract String getQualifyerPath(String entityId);
	
	
	/**
	 * This method initializes the repositories-list and selects the first one if no other is selected.
	 * It also reads the relationXML from the content-version.
	 */ 
	
	protected void initialize() throws Exception
	{
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);

		if(this.repositoryId == null)
		{
			this.repositoryId = (Integer)getHttpSession().getAttribute("repositoryId");
			if(this.repositoryId == null)
				this.repositoryId = RepositoryController.getController().getFirstRepositoryVO().getRepositoryId();		
		}

		//this.relationXML = ContentVersionController.getAttributeValue(getContentVersionId(), getContentVersionAttributeName(), false);
		this.relationXML = getXML();
		
		boolean isFormDefinitionValid = true;
		try
		{
		    if(this.relationXML != null && this.relationXML.length() != 0)
		    {
		        Document document = new DOMBuilder().getDocument(this.relationXML);
				this.qualifyers = parseQualifyersFromXML(this.relationXML);
		    }
		    else
		        isFormDefinitionValid = false;
		}
		catch(Exception e)
		{
			isFormDefinitionValid = false;
		}
	}
	     
	/**
	 * This is the default method which just initializes the view.
	 */     

	public String doExecute() throws Exception
	{
		initialize();

		return "success";
	}
    
	public String doChangeRepository() throws Exception
	{
		initialize();

		return "success";
	}

	/**
	 * This is the default method which just initializes the view.
	 */     

	public String doV3() throws Exception
	{
		initialize();

		return "successV3";
	}
    
	public String doChangeRepositoryV3() throws Exception
	{
		initialize();

		return "successV3";
	}

	/**
	 * Updates the qualifyer in the normal content version stucture.
	 */

	public String doUpdateQualifyer() throws Exception
	{
		updateAttributeValue();
		
		initialize();
		
		return "success";
	}
	
	/**
	 * Updates the qualifyer in the normal content version stucture.
	 */

	public String doUpdateQualifyerV3() throws Exception
	{
		updateAttributeValue();
		
		initialize();
		
		return "successV3";
	}

	/**
	 * Updates the qualifyer in the EntityProperties stucture.
	 */
	public String doUpdateQualifyerInEntityProperties() throws Exception
	{	    
	    if(this.entityName.equalsIgnoreCase(UserProperties.class.getName()))
	        UserPropertiesController.getController().updateAttributeValue(getEntityId(), getAttributeName(), this.qualifyerXML);		
	    else if(this.entityName.equalsIgnoreCase(RoleProperties.class.getName()))
	        RolePropertiesController.getController().updateAttributeValue(getEntityId(), getAttributeName(), this.qualifyerXML);		
	    else if(this.entityName.equalsIgnoreCase(GroupProperties.class.getName()))
	        GroupPropertiesController.getController().updateAttributeValue(getEntityId(), getAttributeName(), this.qualifyerXML);		
	
		initialize();
	
		return "success";
	}
	
	/**
	 * Updates the qualifyer in the EntityProperties stucture.
	 */
	public String doUpdateQualifyerInEntityPropertiesV3() throws Exception
	{	    
	    if(this.entityName.equalsIgnoreCase(UserProperties.class.getName()))
	        UserPropertiesController.getController().updateAttributeValue(getEntityId(), getAttributeName(), this.qualifyerXML);		
	    else if(this.entityName.equalsIgnoreCase(RoleProperties.class.getName()))
	        RolePropertiesController.getController().updateAttributeValue(getEntityId(), getAttributeName(), this.qualifyerXML);		
	    else if(this.entityName.equalsIgnoreCase(GroupProperties.class.getName()))
	        GroupPropertiesController.getController().updateAttributeValue(getEntityId(), getAttributeName(), this.qualifyerXML);		
	
		initialize();
	
		return "successV3";
	}

	
	private List parseQualifyersFromXML(String qualifyerXML)
	{
		List qualifyers = new ArrayList(); 
    	
		try
		{
			Document document = new DOMBuilder().getDocument(qualifyerXML);
			
			String entity = document.getRootElement().attributeValue("entity");
			
			List children = document.getRootElement().elements();
			Iterator i = children.iterator();
			while(i.hasNext())
			{
				Element child = (Element)i.next();
				String id = child.getStringValue();
				
				QualifyerVO qualifyerVO = new QualifyerVO();
				qualifyerVO.setName(this.getCurrentEntityIdentifyer());
				qualifyerVO.setValue(id);    
				qualifyerVO.setPath(this.getQualifyerPath(id));
				//qualifyerVO.setSortOrder(new Integer(i));
				qualifyers.add(qualifyerVO);     	
			}		        	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return qualifyers;
	}

	/**
	 * A method that gets the XML that is the relations
	 */
	
	public String getXML()
	{
		try
		{
	        if(this.entityName.equalsIgnoreCase(UserProperties.class.getName()))
			   	return UserPropertiesController.getController().getAttributeValue(getEntityId(), getAttributeName(), false);
	        if(this.entityName.equalsIgnoreCase(RoleProperties.class.getName()))
			   	return RolePropertiesController.getController().getAttributeValue(getEntityId(), getAttributeName(), false);
	        else if(this.entityName.equalsIgnoreCase(GroupProperties.class.getName()))
	            return GroupPropertiesController.getController().getAttributeValue(getEntityId(), getAttributeName(), false);
			else
				return ContentVersionController.getContentVersionController().getAttributeValue(getEntityId(), getAttributeName(), false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return "";
	}
	 
	/**
	 * This method is the common update method which assumes that the update is in a contentVersion.
	 */
	
	public void updateAttributeValue() throws Exception
	{
		ContentVersionController.getContentVersionController().updateAttributeValue(getEntityId(), getAttributeName(), this.qualifyerXML, this.getInfoGluePrincipal());		
	}



}
