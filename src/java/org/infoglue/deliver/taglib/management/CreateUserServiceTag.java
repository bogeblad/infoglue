package org.infoglue.deliver.taglib.management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.entities.management.UserPropertiesVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.util.webservices.DynamicWebservice;


/**
 * This tag helps create create / update / delete users in the cms from the delivery application.
 */

public class CreateUserServiceTag extends TemplateControllerTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -1904980538720103871L;

	/**
	 * 
	 */
	private String targetEndpointAddress = CmsPropertyHandler.getWebServicesBaseUrl() + "RemoteUserService";
	
	/**
	 * 
	 */
	private String operationName = "createUser";
	
	/**
	 * 
	 */
	private InfoGluePrincipal principal;
	
	/**
	 * 
	 */
	private SystemUserVO systemUserVO;

	private String[] roleNames = new String[]{};
	private String[] groupNames = new String[]{};
	
	/**
	 * 
	 */
	public CreateUserServiceTag() 
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
		   	       
		   ConstraintExceptionBuffer ceb = this.systemUserVO.validate();
		   ceb.throwIfNotEmpty();
		   
		   ws.addArgument("firstName", this.systemUserVO.getFirstName());
		   ws.addArgument("lastName", this.systemUserVO.getLastName());
		   ws.addArgument("email", this.systemUserVO.getEmail());
		   ws.addArgument("userName", this.systemUserVO.getUserName());
		   ws.addArgument("password", this.systemUserVO.getPassword());
		   
		   ws.addNonSerializedArgument("roleNames", new ArrayList(Arrays.asList(this.roleNames)));
		   ws.addNonSerializedArgument("groupNames", new ArrayList(Arrays.asList(this.groupNames)));
		   
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
	   this.targetEndpointAddress = evaluateString("remoteUserService", "targetEndpointAddress", targetEndpointAddress);
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
	   this.principal = (InfoGluePrincipal) this.evaluate("remoteUserService", "principal", principalString, InfoGluePrincipal.class);
   }

   /**
    * 
    */
   public void setSystemUserVO(final String systemUserVO) throws JspException
   {
	   this.systemUserVO = (SystemUserVO)this.evaluate("remoteUserService", "systemUserVO", systemUserVO, SystemUserVO.class);
   }

   /**
    * 
    */
   public void setRoleNames(final String roleNames)  throws JspException
   {
	   String roleNamesString = evaluateString("remoteUserService", "roleNames", roleNames);
	   if(roleNamesString != null && !roleNamesString.equals(""))
		   this.roleNames = roleNamesString.split(",");
   }

   /**
    * 
    */
   public void setGroupNames(final String groupNames)  throws JspException
   {
	   String groupNamesString = evaluateString("remoteUserService", "groupNames", groupNames);
	   if(groupNamesString != null && !groupNamesString.equals(""))
		   this.groupNames = groupNamesString.split(",");
   }

}