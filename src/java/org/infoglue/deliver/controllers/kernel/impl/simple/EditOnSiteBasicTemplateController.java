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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
import org.infoglue.deliver.applications.databeans.WebPage;

/**
 * This is the most basic template controller supplying the templates using it with
 * methods to fetch contents, structure and other suff needed for a site. Mostly this class just acts as a
 * delegator to other more specialized classes.
 */

public class EditOnSiteBasicTemplateController extends BasicTemplateController
{
    private final static Logger logger = Logger.getLogger(EditOnSiteBasicTemplateController.class.getName());

    public EditOnSiteBasicTemplateController(DatabaseWrapper databaseWrapper, InfoGluePrincipal infoGluePrincipal)
    {
        super(databaseWrapper, infoGluePrincipal);
    }
    
	/**
	 * This method adds the neccessairy html to a output for it to be editable.
	 */	
	
	private String decorateTag(Integer contentId, Integer languageId, String attributeName, String attributeValue)
	{
		if(attributeValue != null && !attributeValue.trim().equals(""))
	    {
			ContentTypeAttribute contentTypeAttribute = null;
			
			ContentVO contentVO = this.getContent(contentId);
			ContentTypeDefinitionVO contentTypeDefinitionVO = getContentTypeDefinitionVO(contentId);
			Collection attributes = this.getContentTypeDefinitionAttributes(contentTypeDefinitionVO.getSchemaValue());
			Iterator attributesIterator = attributes.iterator();
			while(attributesIterator.hasNext())
			{
				
				ContentTypeAttribute contentTypeAttributeCandidate = (ContentTypeAttribute)attributesIterator.next();
				if(contentTypeAttributeCandidate.getName().equals(attributeName))
				{
					contentTypeAttribute = contentTypeAttributeCandidate;
					break;
				}
			}
			
			String className = "";
			String enableWYSIWYG = "false";
			String WYSIWYGToolbar = "Default";
			String WYSIWYGExtraConfig = "";
			if(contentTypeAttribute != null)
			{
				className = contentTypeAttribute.getInputType();
				try
				{
					if(contentTypeAttribute.getContentTypeAttribute("enableWYSIWYG") != null && contentTypeAttribute.getContentTypeAttribute("enableWYSIWYG").getContentTypeAttributeParameterValue() != null)
						enableWYSIWYG = contentTypeAttribute.getContentTypeAttribute("enableWYSIWYG").getContentTypeAttributeParameterValue().getLocalizedValue("label", getLocale());
					if(contentTypeAttribute.getContentTypeAttribute("WYSIWYGToolbar") != null && contentTypeAttribute.getContentTypeAttribute("WYSIWYGToolbar").getContentTypeAttributeParameterValue() != null)
						WYSIWYGToolbar = contentTypeAttribute.getContentTypeAttribute("WYSIWYGToolbar").getContentTypeAttributeParameterValue().getLocalizedValue("label", getLocale());
					if(contentTypeAttribute.getContentTypeAttribute("WYSIWYGExtraConfig") != null && contentTypeAttribute.getContentTypeAttribute("WYSIWYGExtraConfig").getContentTypeAttributeParameterValue() != null)
						WYSIWYGExtraConfig = contentTypeAttribute.getContentTypeAttribute("WYSIWYGExtraConfig").getContentTypeAttributeParameterValue().getLocalizedValue("label", getLocale());
				}
				catch (Exception e) 
				{
					logger.warn("Error setting WYSIWYGToolbar or WYSIWYGExtraConfig for attribute:" + contentTypeAttribute.getName() + " - " + e.getMessage());
				}
			}

			String editOnSiteUrl = CmsPropertyHandler.getEditOnSiteUrl();
            StringBuffer requestDelim = new StringBuffer( CmsPropertyHandler.getRequestArgumentDelimiter() );
            StringBuffer setContentItemParametersJavascript = new StringBuffer();
            setContentItemParametersJavascript.append("setContentItemParameters(" );
            setContentItemParametersJavascript.append( this.getSiteNode().getRepositoryId() ).append( "," ).append( contentId ).append( "," ).append( languageId );
            setContentItemParametersJavascript.append( ",'").append( attributeName ).append( "'); setEditUrl('");
            setContentItemParametersJavascript.append( editOnSiteUrl ).append( "?contentId=" ).append( contentId );
            setContentItemParametersJavascript.append( requestDelim ).append( "languageId=").append( languageId );
            setContentItemParametersJavascript.append( requestDelim ).append( "attributeName=" ).append( attributeName );
            setContentItemParametersJavascript.append( requestDelim ).append( "forceWorkingChange=true" );
            setContentItemParametersJavascript.append( "#" + attributeName + "Anchor');" );
            
            StringBuffer decoratedAttributeValue = new StringBuffer();
            decoratedAttributeValue.append("<span class=\"" + className + " attribute" + contentId + "\" id=\"attribute" + contentId + attributeName + "\" oncontextmenu=\"" + setContentItemParametersJavascript + "\">" + attributeValue + "</span>");
            decoratedAttributeValue.append("<script type=\"text/javascript\">" +
            		"editOnSightAttributeNames[\"attribute" + contentId + attributeName + "\"]=\"" + attributeName + "\";" +
            		"editOnSightAttributeNames[\"attribute" + contentId + attributeName + "_type\"]=\"" + className + "\";" +
            		"editOnSightAttributeNames[\"attribute" + contentId + attributeName + "_enableWYSIWYG\"]=\"" + enableWYSIWYG + "\";" +
            		"editOnSightAttributeNames[\"attribute" + contentId + attributeName + "_WYSIWYGToolbar\"]=\"" + WYSIWYGToolbar + "\";" +
            		"editOnSightAttributeNames[\"attribute" + contentId + attributeName + "_WYSIWYGExtraConfig\"]=\"" + WYSIWYGExtraConfig + "\";" +
            		"var element=$(\"#attribute" + contentId + attributeName + "\");" +
            		"element.dblclick(function(){editInline(" + this.getSiteNode().getRepositoryId() + "," + contentId + "," + languageId + ",true);" +
            		"});" +
            		"</script>");
            
            /*
            decoratedAttributeValue.append("<script type=\"text/javascript\"><!-- var element = $(\"#attribute" + contentId + attributeName + "\");");
            decoratedAttributeValue.append("alert('element:' + element); /*element.dblclick(function () { ");
            decoratedAttributeValue.append("	" + setContentItemParametersJavascript + " editInline(" + this.getSiteNode().getRepositoryId() + "); ");
            decoratedAttributeValue.append("});--></script>");
            */
            
            /*
            decoratedAttributeValue.append("<span class=\" " + className + "\" id=\"attribute" + contentId + attributeName + "\" ondblclick=\"\" oncontextmenu=\"setContentItemParameters(" );
            decoratedAttributeValue.append( contentId ).append( "," ).append( languageId );
            decoratedAttributeValue.append( ",'").append( attributeName ).append( "'); setEditUrl('");
            decoratedAttributeValue.append( editOnSiteUrl ).append( "?contentId=" ).append( contentId );
            decoratedAttributeValue.append( requestDelim ).append( "languageId=").append( languageId );
            decoratedAttributeValue.append( requestDelim ).append( "attributeName=" ).append( attributeName );
            decoratedAttributeValue.append( requestDelim ).append( "forceWorkingChange=true" );
            decoratedAttributeValue.append( "#" + attributeName + "Anchor');\">" );
            decoratedAttributeValue.append( attributeValue + "</span>");
			*/
			return decoratedAttributeValue.toString();
	    }
	    else
	    {
	        return "";
	    }
	} 
	
	/**
	 * This method adds the neccessairy html to a template to make it right-clickable.
	 */	
 
	public String decoratePage(String page)
	{
		String decoratedTemplate = page;
		/*
		try
		{
		    String extraHeader 	= FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "preview/editOnSiteHeader.vm"));
		    String extraBody 	= FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "preview/editOnSiteBody.vm"));
		    
			String servletContext = request.getContextPath();
			extraHeader = extraHeader.replaceAll("\\{applicationContext\\}", servletContext);
			//logger.info("extraHeader:" + extraHeader);
			
			StringBuffer modifiedTemplate = new StringBuffer(page);
			
			//Adding stuff in the header
			int indexOfHeadEndTag = modifiedTemplate.indexOf("</head");
			if(indexOfHeadEndTag == -1)
				indexOfHeadEndTag = modifiedTemplate.indexOf("</HEAD");
			
			if(indexOfHeadEndTag > -1)
			{
			    modifiedTemplate = modifiedTemplate.replace(indexOfHeadEndTag, modifiedTemplate.indexOf(">", indexOfHeadEndTag) + 1, extraHeader);
			}
			else
			{
				int indexOfHTMLStartTag = modifiedTemplate.indexOf("<html");
				if(indexOfHTMLStartTag == -1)
					indexOfHTMLStartTag = modifiedTemplate.indexOf("<HTML");
		
				if(indexOfHTMLStartTag > -1)
				{
					modifiedTemplate = modifiedTemplate.insert(modifiedTemplate.indexOf(">", indexOfHTMLStartTag) + 1, "<head>" + extraHeader);
				}
				else
				{
					logger.info("The current template is not a valid document. It does not comply with the simplest standards such as having a correct header.");
				}
			}

			//Adding stuff in the body	
			int indexOfBodyStartTag = modifiedTemplate.indexOf("<body");
			if(indexOfBodyStartTag == -1)
				indexOfBodyStartTag = modifiedTemplate.indexOf("<BODY");
				
			if(indexOfBodyStartTag > -1)
			{
				modifiedTemplate = modifiedTemplate.insert(modifiedTemplate.indexOf(">", indexOfBodyStartTag) + 1, extraBody);
			}
			else
			{
				logger.info("The current template is not a valid document. It does not comply with the simplest standards such as having a correct body.");
			}
			
			decoratedTemplate = modifiedTemplate.toString();
		}
		catch(Exception e)
		{
			logger.warn("An error occurred when deliver tried to decorate your template to enable onSiteEditing. Reason " + e.getMessage(), e);
		}
		*/
		return decoratedTemplate;
	}
	
		
	/**
	 * This method deliveres a String with the content-attribute asked for if it exists in the content
	 * defined in the url-parameter contentId. It decorates the attibute with html so the attribute can be clicked on for
	 * editing.
	 */
	 
	public String getContentAttribute(String attributeName) 
	{
		return decorateTag(this.getContentId(), this.getLanguageId(), attributeName, super.getContentAttribute(attributeName));
	}

	/**
	 * This method deliveres a String with the content-attribute asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getContentAttribute(String contentBindningName, String attributeName) 
	{
		return decorateTag(this.getContentId(), this.getLanguageId(), attributeName, super.getContentAttribute(contentBindningName, attributeName));
	}


    /**
     * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
     * value if OnSiteEdit is on.
     */
	public String getContentAttribute(String contentBindningName, String attributeName, boolean clean) 
	{				
       return  super.getContentAttribute(contentBindningName, attributeName);
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
	 
	public String getContentAttribute(String attributeName, boolean clean) 
	{				
		return super.getContentAttribute(attributeName);
	}

 
    /**
     * This method deliveres a String with the content-attribute asked for a
     * specific content and ensure not to get decorated attributes if EditOnSite is
     * turned on.
     * 
     * @param contentId
     *            the contentId of a content
     * @param attributeName
     *            the attribute name in the content. (ie. Title, Leadin etc)
     * @param clean
     *            true if the content should be decorated in the editonsite
     *            working mode. No decoration is made if content-attribute is
     *            empty.
     * @return the contentAttribute or empty string if none found.
     */
	public String getContentAttribute(Integer contentId, String attributeName, boolean clean)
	{
	    String attributeValue = super.getContentAttribute(contentId, attributeName); 
    
        if( clean == false )
        {
            attributeValue = this.decorateTag( contentId, this.languageId, attributeName, attributeValue );
        }
        return attributeValue;
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
 
	public String getContentAttributeValue(Integer contentId, String attributeName, boolean clean, boolean escapeHTML)
	{
	    String attributeValue = "";

	    if(clean)
	        attributeValue = super.getContentAttributeValue(contentId, this.getLanguageId(), attributeName, escapeHTML);
		else
			return decorateTag(this.getContentId(), this.getLanguageId(), attributeName, super.getContentAttributeValue(contentId, this.getLanguageId(), attributeName, escapeHTML));

		return attributeValue;
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
 
	public String getContentAttribute(Integer contentId, Integer languageId, String attributeName, boolean clean)
	{
		//return super.getContentAttribute(contentId, languageId, attributeName);
	    String attributeValue = "";
	    
	    if(clean)
	    {
	        attributeValue = super.getContentAttribute(contentId, languageId, attributeName);
	    }
	    else
	    {
	        attributeValue = super.getContentAttribute(contentId, languageId, attributeName);
	        attributeValue = decorateTag(contentId, languageId, attributeName, attributeValue);
	    }
	    
	    return attributeValue;
	}
	
	/**
	 * This method deliveres a String with the content-attribute asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getContentAttribute(Integer contentId, String attributeName) 
	{
		if(attributeName.equalsIgnoreCase(this.getTemplateAttributeName()))
			return super.getContentAttribute(contentId, attributeName);
			//return decorateTemplate(super.getContentAttribute(contentId, attributeName));		
		else
			return decorateTag(contentId, this.getLanguageId(), attributeName, super.getContentAttribute(contentId, attributeName));
	}



	/**
	 * This method deliveres a String with the content-attribute asked for after it has been parsed and all special tags have been converted.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getParsedContentAttribute(String attributeName) 
	{
		return decorateTag(this.getContentId(), this.getLanguageId(), attributeName, super.getParsedContentAttribute(attributeName));
	}


	/**
	 * This method deliveres a String with the content-attribute asked for after it has been parsed and all special tags have been converted.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getParsedContentAttribute(String contentBindningName, String attributeName) 
	{
		String attributeValue = "";
		
		try
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(this.getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
			if(contentVO != null)
			{
				attributeValue = getParsedContentAttribute(contentVO.getContentId(), attributeName);
				attributeValue = decorateTag(contentVO.getContentId(), this.getLanguageId(), attributeName, attributeValue);
			}
		}
		catch(Exception e)
		{
			logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on contentBindning " + contentBindningName + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
	 
	public String getParsedContentAttribute(String attributeName, boolean clean) 
	{			
		//return super.getParsedContentAttribute(attributeName);	
		String attributeValue = "";
	    
	    if(clean)
	    {
	        attributeValue = super.getParsedContentAttribute(attributeName);
	    }
	    else
	    {
	        attributeValue = super.getParsedContentAttribute(attributeName);
	        attributeValue = decorateTag(contentId, this.getLanguageId(), attributeName, attributeValue);
	    }
	    
	    return attributeValue;
	}
	
	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
	 
	public String getParsedContentAttribute(String contentBindningName, String attributeName, boolean clean) 
	{		
		//return super.getParsedContentAttribute(contentBindningName, attributeName);		
	    
	    String attributeValue = "";
	    
	    if(clean)
	    {
	        attributeValue = super.getParsedContentAttribute(contentBindningName, attributeName);
	    }
	    else
	    {
	        attributeValue = super.getParsedContentAttribute(contentBindningName, attributeName);
	        attributeValue = decorateTag(contentId, this.getLanguageId(), attributeName, attributeValue);
	    }
	    
	    return attributeValue;
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
	 
	public String getParsedContentAttribute(Integer contentId, String attributeName, boolean clean) 
	{		
		//return super.getParsedContentAttribute(contentId, attributeName);
		
	    String attributeValue = "";
	    
	    if(clean)
	    {
	        attributeValue = super.getParsedContentAttribute(contentId, attributeName);
	    }
	    else
	    {
	        attributeValue = super.getParsedContentAttribute(contentId, attributeName);
	        attributeValue = decorateTag(contentId, this.getLanguageId(), attributeName, attributeValue);
	    }
	    
	    return attributeValue;
		
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
	 
	public String getParsedContentAttribute(Integer contentId, Integer languageId, String attributeName, boolean clean) 
	{		
	    //return super.getParsedContentAttribute(contentId, attributeName);
		
	    String attributeValue = "";
	    
	    if(clean)
	    {
	        attributeValue = super.getParsedContentAttribute(contentId, languageId, attributeName);
	    }
	    else
	    {
	        attributeValue = super.getParsedContentAttribute(contentId, languageId, attributeName);
	        attributeValue = decorateTag(contentId, languageId, attributeName, attributeValue);
	    }
	    
	    return attributeValue;
		
	}

	/**
	 * This method deliveres a String with the content-attribute asked for after it has been parsed and all special tags have been converted.
	 * The attribute is fetched from the specified content.
	 */
	 
	public String getParsedContentAttribute(Integer contentId, String attributeName) 
	{
		return decorateTag(contentId, this.getLanguageId(), attributeName, super.getParsedContentAttribute(contentId, attributeName));
	}


	
	/**
	 * The method returns a list of WebPage-objects that is the bound sitenodes of named binding. 
	 * The method is great for navigation-purposes on any site. 
	 */

	public List getBoundPages(String structureBindningName)
	{
		List boundPages = super.getBoundPages(structureBindningName);
		Iterator i = boundPages.iterator();
		while(i.hasNext())
		{
			WebPage webPage = (WebPage)i.next();
			Integer contentId = super.getContentId(webPage.getSiteNodeId(), META_INFO_BINDING_NAME);
			String navigationTitle = decorateTag(contentId, this.getLanguageId(), NAV_TITLE_ATTRIBUTE_NAME, webPage.getNavigationTitle());
			webPage.setNavigationTitle(navigationTitle);
		}
		
		return boundPages;
	}
	

	/**
	 * The method returns a list of WebPage-objects that is the bound sitenodes of named binding. 
	 * The method is great for navigation-purposes on any site. 
	 */

	public List getBoundPages(Integer siteNodeId, String structureBindningName)
	{
		List boundPages = super.getBoundPages(siteNodeId, structureBindningName);
		Iterator i = boundPages.iterator();
		while(i.hasNext())
		{
			WebPage webPage = (WebPage)i.next();
			Integer contentId = super.getContentId(webPage.getSiteNodeId(), META_INFO_BINDING_NAME);
			String navigationTitle = decorateTag(contentId, this.getLanguageId(), NAV_TITLE_ATTRIBUTE_NAME, webPage.getNavigationTitle());
			webPage.setNavigationTitle(navigationTitle);
		}
		
		return boundPages;
	}

	/**
	 * This method deliveres a String with the Navigation title the page asked for has.
	 * As the siteNode can have multiple bindings the method requires a bindingName 
	 * which refers to the AvailableServiceBinding.name-attribute. The navigation-title is fetched
	 * from the meta-info-content bound to the site node.
	 */
	 
	public String getPageNavTitle(String structureBindningName) 
	{
		Integer siteNodeId = super.getSiteNodeId(structureBindningName);
		Integer contentId = super.getContentId(siteNodeId, META_INFO_BINDING_NAME);
		String navTitle = decorateTag(contentId, this.getLanguageId(), NAV_TITLE_ATTRIBUTE_NAME, super.getPageNavTitle(structureBindningName));
						
		return navTitle;
	}
	
	
	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
/*	 
	public String getAssetUrl(String contentBindningName) 
	{
		return decorateTag(this.getContentId(), this.getLanguageId(), "", super.getAssetUrl(contentBindningName));
	}
*/
	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 */
	/*	 
	 
	public String getAssetUrl(Integer contentId) 
	{
		return decorateTag(this.getContentId(), this.getLanguageId(), "", super.getAssetUrl(contentId));
	}
*/

	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 */
	/*	 
	 
	public String getAssetUrl(Integer contentId, String assetKey) 
	{
		return decorateTag(this.getContentId(), this.getLanguageId(), assetKey, super.getAssetUrl(contentId, assetKey));
	}
*/

	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	/*	 
	 
	public String getAssetUrl(String contentBindningName, int index) 
	{
		return decorateTag(this.getContentId(), this.getLanguageId(), "", super.getAssetUrl(contentBindningName, index));
	}
*/

	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	/*	 
	 
	public String getAssetUrl(String contentBindningName, String assetKey) 
	{
		return decorateTag(this.getContentId(), this.getLanguageId(), assetKey, super.getAssetUrl(contentBindningName, assetKey));
	}
*/	
	
	/**
	 * This method should be much more sophisticated later and include a check to see if there is a 
	 * digital asset uploaded which is more specialized and can be used to act as serverside logic to the template.
	 */
	
	public TemplateController getTemplateController(Integer siteNodeId, Integer languageId, Integer contentId, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		return getTemplateController(siteNodeId, languageId, contentId, this.request, infoGluePrincipal);
	}	
	
	public TemplateController getTemplateController(Integer siteNodeId, Integer languageId, Integer contentId, HttpServletRequest request, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		TemplateController templateController = null;
		templateController = new EditOnSiteBasicTemplateController(this.databaseWrapper, infoGluePrincipal);
		templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
		templateController.setHttpRequest(request);	
		templateController.setBrowserBean(this.browserBean);
		templateController.setDeliveryControllers(this.nodeDeliveryController, null, this.integrationDeliveryController);	
		return templateController;		
	}
	

}