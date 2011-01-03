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

import java.util.Map;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;
import org.infoglue.deliver.util.Support;

/**
 * This is an attempt to make an TagLib for attempts to get a ContentAttribute from a content referenced by a component
 * in a JSP.
 * 
 * <%@ taglib uri="infoglue" prefix="infoglue" %>
 * 
 * <infoglue:component.ContentAttribute propertyName="Article" attributeName="Title"/>
 *
 * @author Mattias Bogeblad
 * 
 * 2005-12-22 Added mapKeyName which extracts a value from a properties.file formated text content. / per.jonsson@it-huset.se
 *
 * 
 */

public class ContentAttributeTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3257850991142318897L;
	
	private ContentVersionVO contentVersionVO;
	private Integer contentId;
	private String propertyName;
	private String attributeName;
	private Integer languageId;
	private String mapKeyName;
    private boolean disableEditOnSight 	= false;
    private boolean useInheritance		= true;
	private boolean useRepositoryInheritance = true;
    private boolean useStructureInheritance = true;
    private boolean useAttributeLanguageFallback = false; 
    private boolean parse				= false;
    private boolean fullBaseUrl			= false;
    
    public ContentAttributeTag()
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

	private String getContentAttributeValue(Integer languageId) throws JspException
	{
	    String result = null;
	    
	    if(contentVersionVO != null)
	    {
	    	if(!parse)
            {
                result = getController().getContentAttribute(contentVersionVO, attributeName, disableEditOnSight);
            }
	        else
	        {
	            result = getController().getParsedContentAttribute(contentVersionVO, attributeName, disableEditOnSight);
            }
	    }
	    else if(contentId != null)
        {
            if(!parse)
            {
                result = getController().getContentAttribute(contentId, languageId, attributeName, disableEditOnSight);
            }
	        else
	        {
	            result = getController().getParsedContentAttribute(contentId, languageId, attributeName, disableEditOnSight);
            }
        }
        else if(propertyName != null)
        {
	        if(!parse)
            {
                result = getComponentLogic().getContentAttribute(propertyName, languageId, attributeName, disableEditOnSight, useInheritance, useRepositoryInheritance, useStructureInheritance);
            }
	        else
            {
	            result = getComponentLogic().getParsedContentAttribute(propertyName, languageId, attributeName, disableEditOnSight, useInheritance, useRepositoryInheritance, useStructureInheritance);
            }
        }
        else
        {
            throw new JspException("You must specify either contentId or propertyName");
        }

	    return result;
	}
	
    public int doEndTag() throws JspException
    {
	    if(this.languageId == null)
	        this.languageId = getController().getLanguageId();

        boolean previousSetting = getController().getDeliveryContext().getUseFullUrl();
        String result = null;
        if(previousSetting != fullBaseUrl)
        {
            getController().getDeliveryContext().setUseFullUrl(fullBaseUrl);
        }
        // Have to force a disable editon sight, not good with renderstuff
        // when converting a attributeto a map.
        // per.jonsson@it-huset.se
        if ( mapKeyName != null )
        {
            disableEditOnSight = true;
        }
        
        result = getContentAttributeValue(this.languageId);
        
        if((result == null || result.trim().equals("")) && useAttributeLanguageFallback)
		{
			try
			{
	            LanguageVO masteLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(this.getController().getDatabase(), this.getController().getSiteNodeId());
			    result = getContentAttributeValue(masteLanguageVO.getLanguageId());
			}
			catch(Exception e)
			{
				throw new JspException("Error getting the master language for this sitenode:" + this.getController().getSiteNodeId());
			}
		}
		
        if ( mapKeyName != null && result != null )
        {
            Map map = Support.convertTextToProperties( result.toString() );
            if ( map != null && !map.isEmpty() )
            {
                result = (String)map.get( mapKeyName );
            }
        }
        produceResult( result );
        //Resetting the full url to the previous state
        getController().getDeliveryContext().setUseFullUrl(previousSetting);

        contentVersionVO = null;
	    contentId = null;
		propertyName = null;
	    attributeName = null;;
	    mapKeyName = null;;
	    disableEditOnSight = false;
	    useInheritance = true;
    	useRepositoryInheritance = true;
        useStructureInheritance = true;
	    useAttributeLanguageFallback = true;
	    parse = false;
	    fullBaseUrl	= false;
	    languageId = null;

        return EVAL_PAGE;
    }

	public void setPropertyName(String propertyName) throws JspException
    {
        this.propertyName = evaluateString("contentAttribute", "propertyName", propertyName);
    }
    
    public void setAttributeName(String attributeName) throws JspException
    {
        this.attributeName = evaluateString("contentAttribute", "attributeName", attributeName);
    }
    
    public void setDisableEditOnSight(boolean disableEditOnSight)
    {
        this.disableEditOnSight = disableEditOnSight;
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

    public void setParse(boolean parse)
    {
        this.parse = parse;
    }
    
    public void setContentId(final String contentId) throws JspException
    {
        this.contentId = evaluateInteger("contentAttribute", "contentId", contentId);
    }
    
    public void setLanguageId(final String languageId) throws JspException
    {
        this.languageId = evaluateInteger("contentAttribute", "languageId", languageId);
    }

	public void setContentVersion(final String contentVersion) throws JspException
	{
		this.contentVersionVO = (ContentVersionVO)evaluate("contentAttribute", "contentVersion", contentVersion, ContentVersionVO.class);
	}

    public void setFullBaseUrl(boolean fullBaseUrl)
    {
        this.fullBaseUrl = fullBaseUrl;
    }

    public void setMapKeyName(String mapKeyName) throws JspException
    {
        this.mapKeyName = evaluateString("contentAttribute", "mapKeyName", mapKeyName);
    }
    
    public void setUseAttributeLanguageFallback(boolean useAttributeLanguageFallback)
    {
        this.useAttributeLanguageFallback = useAttributeLanguageFallback;
    }

}