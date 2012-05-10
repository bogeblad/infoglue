package org.infoglue.deliver.taglib.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.taglib.AbstractTag;
import org.infoglue.deliver.util.webservices.DynamicWebservice;


/**
 * 
 */
public class RemoteWorkflowServiceTag extends AbstractTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -1904980538720103871L;

	private static final String WORKFLOW_NAME_PARAMETER = "workflowName";
	
	private static final String LANGUAGE_ID_PARAMETER = "languageId";

	private static final String INPUTS_PARAMETER = "inputs";
	
	private String targetEndpointAddress = CmsPropertyHandler.getWebServicesBaseUrl() + "RemoteWorkflowService";
	
	private String operationName;
	
	private String principalName;
	
	private String workflowName;
	
	private Integer languageId;
	
	private Map inputs = new HashMap();
	
	public RemoteWorkflowServiceTag() 
	{
		super();
	}

   public int doEndTag() throws JspException
   {
	   try
	   {
		   final InfoGluePrincipal principal = UserControllerProxy.getController().getUser(principalName);
		   
		   final DynamicWebservice ws = new DynamicWebservice(principal);
		  
		   ws.setTargetEndpointAddress(targetEndpointAddress);
		   ws.setOperationName(operationName);
		   ws.setReturnType(Boolean.class);
		   
		   ws.addArgument(LANGUAGE_ID_PARAMETER,   languageId);
		   ws.addArgument(WORKFLOW_NAME_PARAMETER, workflowName);
		   ws.addArgument(INPUTS_PARAMETER,        inputs);
		   
		   ws.callService();
		   setResultAttribute(ws.getResult());
	   }   
	   catch(Exception e)
	   {
		   e.printStackTrace();
		   throw new JspTagException(e.getMessage());
	   }
       return EVAL_PAGE;
   }
   
   /**
    * 
    */
   public void setTargetEndpointAddress(final String targetEndpointAddress) throws JspException
   {
	   this.targetEndpointAddress = evaluateString("remoteWorkflowService", "targetEndpointAddress", targetEndpointAddress);
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
   public void setPrincipalName(final String principalName) 
   {
	   this.principalName = principalName;
   }

   /**
    * 
    */
   public void setWorkflowName(final String workflowName) 
   {
	   this.workflowName = workflowName;
   }

   /**
    * 
    */
   public void setLanguageId(final String languageId) throws JspException
   {
	   this.languageId = evaluateInteger("remoteWorkflowService", "languageId", languageId);
   }

   /**
    * 
    */
   public void setInputs(final String inputs) throws JspException
   {
	   this.inputs = (Map) evaluate("remoteWorkflowService", "inputs", inputs, Map.class);
   }
}