package org.infoglue.deliver.taglib.structure;

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
 * This tag helps create a siteNode in the cms from the delivery application.
 */

public class RemoteSiteNodeServiceTag extends TemplateControllerTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -1904980538720103871L;

	/**
	 * The list of siteNodes that should be stored.
	 */
	private List siteNodes = new ArrayList();	
	
	/**
	 * 
	 */
	private String name;
	
	/**
	 * 
	 */
	private String targetEndpointAddress = CmsPropertyHandler.getWebServicesBaseUrl() + "RemoteSiteNodeService";
	
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
	public RemoteSiteNodeServiceTag() 
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
		   //ws.setReturnType(Boolean.class);
		   
		   ws.addArgument("siteNodes", siteNodes);
		   
		   ws.callService();
		   setResultAttribute(ws.getResult());
	   }   
	   catch(Exception e)
	   {
		   e.printStackTrace();
		   throw new JspTagException(e.getMessage());
	   }
	   
	   siteNodes.clear();
	   this.principal = null;
	   
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
   /*
   public void addSiteNode(final SiteNodeVO siteNodeVO) 
   {
       //Map siteNodeMap = new HashMap();
       //siteNodeMap.put("name", "Kalle" + siteNodeVO.getName());
       //this.siteNodes.add(siteNodeMap);
	   this.siteNodes.add(siteNodeVO);
   }
   */
   
   /**
    * Add the siteNode the child tag generated to the list of siteNodes that are to be persisted.
    */
   public void addSiteNodeMap(final Map siteNodeMap) 
   {
	   this.siteNodes.add(siteNodeMap);
   }

}