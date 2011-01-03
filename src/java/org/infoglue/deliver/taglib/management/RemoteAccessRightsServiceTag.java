package org.infoglue.deliver.taglib.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.xml.namespace.QName;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.webservices.elements.CreatedEntityBean;
import org.infoglue.cms.webservices.elements.StatusBean;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.util.webservices.DynamicWebservice;


/**
 * This tag helps manage system aspects of the cms from the delivery application.
 */

public class RemoteAccessRightsServiceTag extends TemplateControllerTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -1904980538720103871L;

	/**
	 * The list of accessRights that should be stored.
	 */
	private List accessRights = new ArrayList();	
	
	/**
	 * 
	 */
	private String name;
	
	/**
	 * 
	 */
	private String targetEndpointAddress = CmsPropertyHandler.getWebServicesBaseUrl() + "RemoteManagementService";
	
	/**
	 * 
	 */
	private String operationName = "setAccessRights";
	
	/**
	 * 
	 */
	private InfoGluePrincipal principal;
	
	/**
	 * 
	 */
	public RemoteAccessRightsServiceTag() 
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
		   ws.setReturnType(StatusBean.class, new QName("infoglue", "StatusBean"));
		   ws.setReturnType(CreatedEntityBean.class, new QName("infoglue", "CreatedEntityBean"));
		   
		   ws.addArgument("accessRights", accessRights);
		   
		   ws.callService();
		   setResultAttribute(ws.getResult());
	   }   
	   catch(Exception e)
	   {
		   e.printStackTrace();
		   throw new JspTagException(e.getMessage());
	   }
	   
	   this.accessRights = new ArrayList();
	   this.principal = null;
	   //this.name = null;
	   //this.operationName = null;
	   
       return EVAL_PAGE;
   }
   
   /**
    * Add the accessRightMap to be persisted.
    */
   public void addAccessRight(final Map accessRight) 
   {
	   this.accessRights.add(accessRight);
   }

}