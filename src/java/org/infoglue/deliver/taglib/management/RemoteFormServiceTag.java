package org.infoglue.deliver.taglib.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.util.webservices.DynamicWebservice;


/**
 * This tag helps create form entries from the delivery application.
 */

public class RemoteFormServiceTag extends TemplateControllerTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -1904980538720103871L;

	/**
	 * The list of siteNodes that should be stored.
	 */
	private List formEntries = new ArrayList();	
	
	/**
	 * 
	 */
	private String name;
	
	/**
	 * 
	 */
	private String targetEndpointAddress = CmsPropertyHandler.getWebServicesBaseUrl() + "RemoteFormService";
	
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
	public RemoteFormServiceTag() 
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
		   
		   ws.addArgument("formEntries", formEntries);
		   
		   ws.callService();
		   setResultAttribute(ws.getResult());
	   }   
	   catch(Exception e)
	   {
		   e.printStackTrace();
		   throw new JspTagException(e.getMessage());
	   }
	   
	   formEntries.clear();
	   principal = null;
	   
       return EVAL_PAGE;
   }
   
   /**
    * 
    */
   public void setTargetEndpointAddress(final String targetEndpointAddress) throws JspException
   {
	   this.targetEndpointAddress = evaluateString("remoteSiteNodeService", "targetEndpointAddress", targetEndpointAddress);
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
	   this.principal = (InfoGluePrincipal) this.evaluate("remoteSiteNodeService", "principal", principalString, InfoGluePrincipal.class);
   }
   
   /**
    * Add the siteNode the child tag generated to the list of siteNodes that are to be persisted.
    */
   public void addFormEntryMap(final Map formEntryMap) 
   {
	   this.formEntries.add(formEntryMap);
   }

}