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

package org.infoglue.deliver.taglib.structure;

import java.util.Locale;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ExtendedSearchController;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.util.Support;
import org.infoglue.deliver.taglib.content.ContentAttributeTag;

/**
 * This is a new tag which get's a label from the current template. 
 * It's really just a variant of content:contentAttribute-tag but with the current component content id.
 */

public class ComponentLabelTag extends ContentAttributeTag
{
    private final static Logger logger = Logger.getLogger(ComponentLabelTag.class.getName());

	private static final long serialVersionUID = 3257850991142318897L;
	
	private String attributeName 					= "ComponentLabels";
	private Integer languageId;
	private String mapKeyName;
    private boolean disableEditOnSight 				= true;
    private boolean useAttributeLanguageFallback 	= true; 
    
    public ComponentLabelTag()
    {
        super();
    }
    
	private String getContentAttributeValue(Integer languageId) throws JspException
	{
		Integer componentContentId = getController().getComponentLogic().getIncludedComponentContentId();
		if(componentContentId == null)
			componentContentId = getController().getComponentLogic().getInfoGlueComponent().getContentId();
		
		String result = getController().getContentAttribute(componentContentId, languageId, attributeName, disableEditOnSight);
		
		return result;
	}
	
    public int doEndTag() throws JspException
    {
	    if(this.languageId == null)
	        this.languageId = getController().getLanguageId();

    	Locale locale = null;
	    try
	    {
	    	locale = getController().getLanguageCode(languageId);
	    }
	    catch (Exception e) 
	    {
	    	logger.warn("Error getting locale:" + e.getMessage());
	    }
	    
	    String result = null;

        result = getContentAttributeValue(this.languageId);
        if ( mapKeyName != null && result != null )
        {
            Map map = Support.convertTextToProperties( result.toString() );
            if ( map != null && !map.isEmpty() )
            {
            	result = (String)map.get( mapKeyName + "_" + locale.getLanguage() );
            	if(result == null)
            		result = (String)map.get( mapKeyName );
            }
        }
        
        if((result == null || result.trim().equals("")) && useAttributeLanguageFallback)
		{
			try
			{
	            LanguageVO masteLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(this.getController().getDatabase(), this.getController().getSiteNodeId());
			    result = getContentAttributeValue(masteLanguageVO.getLanguageId());
		        if ( mapKeyName != null && result != null )
		        {
		            Map map = Support.convertTextToProperties( result.toString() );
		            if ( map != null && !map.isEmpty() )
		            {
		            	result = (String)map.get( mapKeyName + "_" + locale.getLanguage() );
		            	if(result == null)
			            	result = (String)map.get( mapKeyName + "_" + masteLanguageVO.getLanguageCode() );

		            	if(result == null)
		            		result = (String)map.get( mapKeyName );
		            }
		        }
			}
			catch(Exception e)
			{
				throw new JspException("Error getting the master language for this sitenode:" + this.getController().getSiteNodeId());
			}
		}
		
        produceResult( result );

	    attributeName = "ComponentLabels";
	    mapKeyName = null;
	    useAttributeLanguageFallback = true;
	    languageId = null;

        return EVAL_PAGE;
    }

    public void setAttributeName(String attributeName) throws JspException
    {
        this.attributeName = evaluateString("ComponentLabelTag", "attributeName", attributeName);
    }
    
    public void setLanguageId(final String languageId) throws JspException
    {
        this.languageId = evaluateInteger("ComponentLabelTag", "languageId", languageId);
    }

    public void setMapKeyName(String mapKeyName) throws JspException
    {
        this.mapKeyName = evaluateString("ComponentLabelTag", "mapKeyName", mapKeyName);
    }
    
    public void setUseAttributeLanguageFallback(boolean useAttributeLanguageFallback)
    {
        this.useAttributeLanguageFallback = useAttributeLanguageFallback;
    }

}