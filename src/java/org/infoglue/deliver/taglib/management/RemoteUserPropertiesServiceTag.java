package org.infoglue.deliver.taglib.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.webservices.elements.RemoteAttachment;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.taglib.content.ContentVersionParameterInterface;
import org.infoglue.deliver.util.webservices.DynamicWebservice;


/**
 * This tag helps create a content in the cms from the delivery application.
 */

public class RemoteUserPropertiesServiceTag extends TemplateControllerTag implements ContentVersionParameterInterface
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -1904980538720103871L;
	
	/**
	 * 
	 */
	private String targetEndpointAddress = CmsPropertyHandler.getWebServicesBaseUrl() + "RemoteUserPropertiesService";
	
	/**
	 * 
	 */
	private String operationName;
	
	/**
	 * 
	 */
	private InfoGluePrincipal principal;
	
	/**
	 * 
	 */
	private Integer languageId;
	private Integer contentTypeDefinitionId;
	private boolean forcePublication = true;

	private Boolean allowHTMLContent = false;
	private Boolean allowExternalLinks = false;
	private Boolean allowDollarSigns = false;
	private Boolean allowAnchorSigns = true;
	private Boolean keepExistingAttributes = false;
	private Boolean keepExistingCategories = true;
	private Boolean updateExistingAssets = true;

	private Map userPropertiesAttributesMap = new HashMap();
	private List digitalAssets = new ArrayList();
	
	/**
	 * 
	 */
	public RemoteUserPropertiesServiceTag() 
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

	/**
	 *
	 */
   public int doEndTag() throws JspException
   {
	   try
	   {
	       if(this.principal == null)
	           this.principal = this.getController().getPrincipal();
	       
		   final DynamicWebservice ws = new DynamicWebservice(principal);
		  
		   ws.setTargetEndpointAddress(targetEndpointAddress);
		   ws.setOperationName(operationName);
		   ws.setReturnType(Boolean.class);
		   	       
		   if(this.languageId == null)
			   ws.addArgument("languageId", this.getController().getLanguageId());
		   else
			   ws.addArgument("languageId", this.languageId);
				   
		   ws.addArgument("contentTypeDefinitionId", this.contentTypeDefinitionId);
		   ws.addArgument("forcePublication", this.forcePublication);
		   ws.addArgument("allowHTMLContent", this.allowHTMLContent);
		   ws.addArgument("allowExternalLinks", this.allowExternalLinks);
		   ws.addArgument("allowDollarSigns", this.allowDollarSigns);
		   ws.addArgument("allowAnchorSigns", this.allowAnchorSigns);
		   ws.addArgument("keepExistingAttributes", this.keepExistingAttributes);
		   ws.addArgument("keepExistingCategories", this.keepExistingCategories);
		   ws.addArgument("updateExistingAssets", this.updateExistingAssets);

		   ws.addArgument("userPropertiesAttributesMap", userPropertiesAttributesMap);
		   ws.addArgument("digitalAssets", digitalAssets);

		   ws.callService();
		   setResultAttribute(ws.getResult());
	   }   
	   catch(Exception e)
	   {
		   e.printStackTrace();
		   throw new JspTagException(e.getMessage());
	   }
	   
	   this.contentTypeDefinitionId = null;
	   this.languageId = null;
	   this.forcePublication = true;
	   this.userPropertiesAttributesMap = new HashMap();
	   this.digitalAssets = new ArrayList();
	   this.principal = null;
	   
       this.allowHTMLContent = false;
       this.allowExternalLinks = false;
       this.allowDollarSigns = false;
       this.allowAnchorSigns = false;
       this.keepExistingAttributes = false;
       this.keepExistingCategories = true;
       this.updateExistingAssets = true;

       return EVAL_PAGE;
   }
   
   /**
    * 
    */
   public void setTargetEndpointAddress(final String targetEndpointAddress) throws JspException
   {
	   this.targetEndpointAddress = evaluateString("remoteContentService", "targetEndpointAddress", targetEndpointAddress);
   }

   /**
    * 
    */
   public void setOperationName(final String operationName) 
   {
	   this.operationName = operationName;
   }

   /**
    * 
    */
   public void setPrincipal(final String principalString) throws JspException
   {
	   this.principal = (InfoGluePrincipal) this.evaluate("remoteContentService", "principal", principalString, InfoGluePrincipal.class);
   }

   public void setPrincipalObject(final InfoGluePrincipal principal) throws JspException
   {
	   this.principal = principal;
   }

   /**
    * 
    */
   public void setLanguageId(final String languageIdString) throws JspException
   {
	   this.languageId = this.evaluateInteger("remoteUserPropertiesService", "languageId", languageIdString);
   }

   /**
    * 
    */
   public void setContentTypeDefinitionId(final String contentTypeDefinitionIdString) throws JspException
   {
	   this.contentTypeDefinitionId = this.evaluateInteger("remoteUserPropertiesService", "contentTypeDefinitionId", contentTypeDefinitionIdString);
   }

   /**
    * 
    */
   public void setForcePublication(final String forcePublication) throws JspException
   {
	   this.forcePublication = (Boolean)this.evaluate("remoteUserPropertiesService", "forcePublication", forcePublication, Boolean.class);
   }

	/**
	 * Adds the content version attribute to the contentVersion Value.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addUserPropertiesAttribute(String name, String value) throws JspException
	{
		this.userPropertiesAttributesMap.put(name, value);
	}

	/**
	 * Adds the content version attribute to the contentVersion Value.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addDigitalAsset(RemoteAttachment remoteAttachment) throws JspException
	{
	    digitalAssets.add(remoteAttachment);
	}

	public void addContentCategory(String contentCategory) throws JspException
	{
		// TODO Auto-generated method stub
	}

	public void addContentVersionAttribute(String name, String value) throws JspException
	{
		this.userPropertiesAttributesMap.put(name, value);
	}

	public void setAllowHTMLContent(final String allowHTMLContent) throws JspException
	{
		this.allowHTMLContent = (Boolean)evaluate("updateContentVersion", "allowHTMLContent", allowHTMLContent, Boolean.class);
	}

	public void setAllowExternalLinks(final String allowExternalLinks) throws JspException
	{
		this.allowExternalLinks = (Boolean)evaluate("updateContentVersion", "allowExternalLinks", allowExternalLinks, Boolean.class);
	}

	public void setAllowDollarSigns(final String allowDollarSigns) throws JspException
	{
		this.allowDollarSigns = (Boolean)evaluate("updateContentVersion", "allowDollarSigns", allowDollarSigns, Boolean.class);
	}

	public void setAllowAnchorSigns(final String allowAnchorSigns) throws JspException
	{
		this.allowAnchorSigns = (Boolean)evaluate("updateContentVersion", "allowAnchorSigns", allowAnchorSigns, Boolean.class);
	}

	public void setKeepExistingAttributes(final String keepExistingAttributes) throws JspException
	{
		this.keepExistingAttributes = (Boolean)evaluate("updateContentVersion", "keepExistingAttributes", keepExistingAttributes, Boolean.class);
	}

	public void setKeepExistingCategories(final String keepExistingCategories) throws JspException
	{
		this.keepExistingCategories = (Boolean)evaluate("updateContentVersion", "keepExistingCategories", keepExistingCategories, Boolean.class);
	}

	public void setUpdateExistingAssets(final String updateExistingAssets) throws JspException
	{
		this.updateExistingAssets = (Boolean)evaluate("updateContentVersion", "updateExistingAssets", updateExistingAssets, Boolean.class);
	}


}