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

package org.infoglue.cms.applications.managementtool.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;


/**
  * This is the action-class for UpdateContentTypeDefinition
  * 
  * @author Mattias Bogeblad
  */
public class UpdateContentTypeDefinitionAction extends ViewContentTypeDefinitionAction //WebworkAbstractAction
{
	private final static Logger logger = Logger.getLogger(UpdateContentTypeDefinitionAction.class.getName());

	private ContentTypeDefinitionVO contentTypeDefinitionVO;
	private Integer versionsToKeep;
	private ConstraintExceptionBuffer ceb;
	
	public UpdateContentTypeDefinitionAction()
	{
		this(new ContentTypeDefinitionVO());
	}
	
	public UpdateContentTypeDefinitionAction(ContentTypeDefinitionVO contentTypeDefinitionVO)
	{
		this.contentTypeDefinitionVO = contentTypeDefinitionVO;
		this.ceb = new ConstraintExceptionBuffer();	
	}

       	
	public String doExecute() throws Exception
    {
		logUserActionInfo(getClass(), "doExecute");
		super.initialize(getContentTypeDefinitionId());

    	ceb.add(this.contentTypeDefinitionVO.validate());
    	ceb.throwIfNotEmpty();		
    	
		ContentTypeDefinitionController.getController().update(contentTypeDefinitionVO.getParentId(), contentTypeDefinitionVO);
		
    	Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    ps.setString("contentTypeDefinitionId_" + this.contentTypeDefinitionVO.getId() + "_versionsToKeep", "" + this.versionsToKeep);

		return "success";
	}

	public String doSaveSimple() throws Exception
    {
		logUserActionInfo(getClass(), "doSaveSimple");
		doExecute();
						
		return "simple";
	}
	
	public String doSaveAndExit() throws Exception
    {
		logUserActionInfo(getClass(), "doSaveAndExit");
		doExecute();
						
		return "saveAndExit";
	}
	
	public void setContentTypeDefinitionId(Integer contentTypeDefinitionId) throws Exception
	{
		this.contentTypeDefinitionVO.setContentTypeDefinitionId(contentTypeDefinitionId);	
	}

    public java.lang.Integer getContentTypeDefinitionId()
    {
        return this.contentTypeDefinitionVO.getContentTypeDefinitionId();
    }
        
    public java.lang.String getName()
    {    
    	return this.contentTypeDefinitionVO.getName();
    }
        
    public void setName(java.lang.String name)
    {
       	this.contentTypeDefinitionVO.setName(name);
    }

    public String getSchemaValue()
    {
        return this.contentTypeDefinitionVO.getSchemaValue();
    }
        
    public void setSchemaValue(String schemaValue)
    {
    	VisualFormatter vf = new VisualFormatter();
    	
    	Pattern pattern = Pattern.compile("\".*?\"");
        Matcher matcher = pattern.matcher(schemaValue);
        while (matcher.find())
        {
        	String value = matcher.group();
        	value = value.substring(1,value.length() - 1);
        	
            int indexOfTag = value.indexOf("<");
            if(indexOfTag > -1)
            {
            	String newAttributeValue = vf.escapeHTML(value);
            	if(logger.isInfoEnabled())
            		logger.info("Replacing:" + value + " with " + newAttributeValue);
            	schemaValue = StringUtils.replace(schemaValue, "\"" + value + "\"", "\"" + newAttributeValue + "\"");
            }
        }
    	
      	this.contentTypeDefinitionVO.setSchemaValue(schemaValue);
    }
    
	public Integer getType()
	{
		return this.contentTypeDefinitionVO.getType();
	}

	public void setType(Integer type)
	{
		this.contentTypeDefinitionVO.setType(type);
	}

	public Integer getVersionsToKeep()
	{
		return this.versionsToKeep;
	}

	public void setVersionsToKeep(Integer versionsToKeep)
	{
		this.versionsToKeep = versionsToKeep;
	}

	public void setDetailPageResolverClass(String detailPageResolverClass)
	{
		this.contentTypeDefinitionVO.setDetailPageResolverClass(detailPageResolverClass);
	}

	public void setDetailPageResolverData(String detailPageResolverData)
	{
		this.contentTypeDefinitionVO.setDetailPageResolverData(detailPageResolverData);
	}

	public void setParentContentTypeDefinitionId(Integer parentContentTypeDefinitionId)
	{
		this.contentTypeDefinitionVO.setParentId(parentContentTypeDefinitionId);
	}

}
