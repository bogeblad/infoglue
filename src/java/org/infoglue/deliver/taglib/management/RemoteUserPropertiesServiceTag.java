package org.infoglue.deliver.taglib.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.entities.management.UserPropertiesVO;
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
		System.out.println("Adding:" + remoteAttachment.getSize());
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

	/**
	 * Adds the content category to the contentVersion Value.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	/*
	public void addContentCategory(String contentCategory) throws JspException
	{
	    List contentCategories = (List)this.contentVersion.get("contentCategories");
	    if(contentCategories == null)
	    {
	    	contentCategories = new ArrayList();
	        this.contentVersion.put("contentCategories", contentCategories);
	    }

	    contentCategories.add(contentCategory);
	}
	*/

}