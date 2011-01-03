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

package org.infoglue.deliver.taglib.content;

import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;
import org.infoglue.deliver.util.Support;

/**
 * This is a tag which lets you get the categories assigned to a content on a specific category key.
 * 
 * <%@ taglib uri="infoglue" prefix="infoglue" %>
 * 
 * <content:assignedCategory propertyName="Article" categoryKey="Area"/>
 *
 * @author Mattias Bogeblad
 * 
 */

public class AssignedCategoriesTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3257850991142318897L;
	
	private Integer contentId;
	private String propertyName;
	private String categoryKey;
	private Integer languageId;
    private boolean useInheritance = true;
	private boolean useRepositoryInheritance = true;
    private boolean useAttributeLanguageFallback = false; 
    private boolean useStructureInheritance = true;

    public AssignedCategoriesTag()
    {
        super();
    }
    
	/**
	 * Initializes the parameters to make it accessible for the children tags (if any).
	 * 
	 * @return indication of whether to evaluate the body or not.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doStartTag() throws JspException 
	{
		return EVAL_BODY_INCLUDE;
	}
	
    public int doEndTag() throws JspException
    {
	    if(this.languageId == null)
	        this.languageId = getController().getLanguageId();
        
	    List result = null;
	    
        if(contentId != null)
        {
        	result = getController().getAssignedCategories(contentId, categoryKey, languageId, useAttributeLanguageFallback);
        }
        else if(propertyName != null)
        {
        	result = getComponentLogic().getAssignedCategories(propertyName, categoryKey, languageId, useInheritance, useAttributeLanguageFallback, useRepositoryInheritance, useStructureInheritance);
        }
        else
        {
            throw new JspException("You must specify either contentId or propertyName");
        }
        
        produceResult(result);

	    contentId = null;
		propertyName = null;
		categoryKey = null;;
	    useInheritance = true;
	    useAttributeLanguageFallback = true;
	    languageId = null;
	    
        return EVAL_PAGE;
    }

	public void setPropertyName(String propertyName) throws JspException
    {
        this.propertyName = evaluateString("contentAttribute", "propertyName", propertyName);
    }
    
    public void setCategoryKey(String categoryKey) throws JspException
    {
        this.categoryKey = evaluateString("contentAttribute", "categoryKey", categoryKey);
    }
    
    public void setUseInheritance(boolean useInheritance)
    {
        this.useInheritance = useInheritance;
    }
    
    public void setUseRepositoryInheritance(boolean useRepositoryInheritance)
    {
        this.useRepositoryInheritance = useRepositoryInheritance;
    }

    public void setUseStructureInheritance(boolean useStructureInheritance)
    {
        this.useStructureInheritance = useStructureInheritance;
    }

    public void setContentId(final String contentId) throws JspException
    {
        this.contentId = evaluateInteger("contentAttribute", "contentId", contentId);
    }
    
    public void setLanguageId(final String languageId) throws JspException
    {
        this.languageId = evaluateInteger("contentAttribute", "languageId", languageId);
    }

    public void setUseAttributeLanguageFallback(boolean useAttributeLanguageFallback)
    {
        this.useAttributeLanguageFallback = useAttributeLanguageFallback;
    }
}