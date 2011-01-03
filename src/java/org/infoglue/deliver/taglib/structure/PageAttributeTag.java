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

import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.ComponentLogic;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;
import org.infoglue.deliver.util.Support;

/**
 * This is a TagLib that gets a ContentAttribute from a page meta info content.
 * in a JSP.
 *
 * @author Mattias Bogeblad
 * 
 */

public class PageAttributeTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3257850991142318897L;

    private final static Logger logger = Logger.getLogger(PageAttributeTag.class.getName());

	private Integer siteNodeId;
	private Integer languageId;
	private String attributeName;
	private String mapKeyName;
    private boolean disableEditOnSight 			= false;
	private boolean useRepositoryInheritance 	= true;
    private boolean useStructureInheritance 	= true;
    private boolean useAttributeLanguageFallback = false; 
    private boolean parse						= false;
    private boolean fullBaseUrl					= false;
    
    public PageAttributeTag()
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

	private String getPageAttributeValue(Integer languageId) throws JspException
	{
	    String result = null;
	    
	    try
	    {
		    SiteNodeVO siteNodeVO = getController().getSiteNode();
		    if(siteNodeId != null)
		    {
		    	siteNodeVO = getController().getSiteNode(siteNodeId);
		    }
		    
		    while(siteNodeVO != null && (result == null || result.equals("")))
	        {
	            if(!parse)
	            {
	                result = getController().getContentAttribute(siteNodeVO.getMetaInfoContentId(), languageId, attributeName, disableEditOnSight);
	            }
		        else
		        {
		            result = getController().getParsedContentAttribute(siteNodeVO.getMetaInfoContentId(), languageId, attributeName, disableEditOnSight);
	            }
	            
	            if(!useStructureInheritance)
	            	break;
	            
	            if(siteNodeVO.getParentSiteNodeId() != null)
	            {
	            	siteNodeVO = getController().getSiteNode(siteNodeVO.getParentSiteNodeId());
		        }
	            else if(useRepositoryInheritance)
	            {
	            	Integer parentRepositoryId = getController().getParentRepositoryId(siteNodeVO.getRepositoryId());
				    if(parentRepositoryId != null)
				    {
				    	siteNodeVO = getController().getRepositoryRootSiteNode(parentRepositoryId);
					}
				    else
				    {
		            	siteNodeVO = null;				    	
				    }
	            }
	            else
	            {
	            	siteNodeVO = null;
	            }
	            	
	        }
	    }
	    catch (Exception e) 
	    {
	    	logger.error("An error occurred when getting pageAttributeValue:" + e.getMessage());
	    	throw new JspException(e);
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

        if ( mapKeyName != null )
        {
            disableEditOnSight = true;
        }
        
        result = getPageAttributeValue(this.languageId);
        
        if((result == null || result.trim().equals("")) && useAttributeLanguageFallback)
		{
			try
			{
	            LanguageVO masteLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(this.getController().getDatabase(), this.getController().getSiteNodeId());
			    result = getPageAttributeValue(masteLanguageVO.getLanguageId());
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

	    siteNodeId = null;
	    languageId = null;
	    attributeName = null;;
	    mapKeyName = null;;
	    disableEditOnSight = false;
	    useRepositoryInheritance = true;
	    useStructureInheritance = true;
	    useAttributeLanguageFallback = true;
	    parse = false;
	    fullBaseUrl	= false;

        return EVAL_PAGE;
    }
    
    public void setAttributeName(String attributeName) throws JspException
    {
        this.attributeName = evaluateString("pageAttribute", "attributeName", attributeName);
    }
    
    public void setDisableEditOnSight(boolean disableEditOnSight)
    {
        this.disableEditOnSight = disableEditOnSight;
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
    
    public void setSiteNodeId(final String siteNodeId) throws JspException
    {
        this.siteNodeId = evaluateInteger("pageAttribute", "siteNodeId", siteNodeId);
    }
    
    public void setLanguageId(final String languageId) throws JspException
    {
        this.languageId = evaluateInteger("pageAttribute", "languageId", languageId);
    }

    public void setFullBaseUrl(boolean fullBaseUrl)
    {
        this.fullBaseUrl = fullBaseUrl;
    }

    public void setMapKeyName( String mapKeyName )
    {
        this.mapKeyName = mapKeyName;
    }
    
    public void setUseAttributeLanguageFallback(boolean useAttributeLanguageFallback)
    {
        this.useAttributeLanguageFallback = useAttributeLanguageFallback;
    }

}