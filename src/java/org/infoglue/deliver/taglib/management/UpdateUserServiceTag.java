package org.infoglue.deliver.taglib.management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.xml.namespace.QName;

import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.entities.management.UserPropertiesVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.webservices.elements.CreatedEntityBean;
import org.infoglue.cms.webservices.elements.StatusBean;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.util.webservices.DynamicWebservice;


/**
 * This tag helps create create / update / delete users in the cms from the delivery application.
 */

public class UpdateUserServiceTag extends TemplateControllerTag 
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
	private String operationName = "updateUser";
	
	/**
	 * 
	 */
	private InfoGluePrincipal principal;
	
	/**
	 * 
	 */
	private SystemUserVO systemUserVO = new SystemUserVO();
	private boolean isPasswordChangeOperation = false;
	private boolean isPasswordResetOperation = false;
	private String oldPassword = null;
	
	private String[] roleNames = new String[]{};
	private String[] groupNames = new String[]{};
	
	/**
	 * 
	 */
	public UpdateUserServiceTag() 
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
		   	       
		   Map userMap = new HashMap();
		   if(this.systemUserVO.getFirstName() != null)
			   userMap.put("firstName", this.systemUserVO.getFirstName());
		   if(this.systemUserVO.getLastName() != null)
			   userMap.put("lastName", this.systemUserVO.getLastName());
		   if(this.systemUserVO.getEmail() != null)
			   userMap.put("email", this.systemUserVO.getEmail());

		   if(this.systemUserVO.getUserName() != null)
			   userMap.put("userName", this.systemUserVO.getUserName());
		   else
			   userMap.put("userName", getController().getPrincipal().getName());
				   
		   if(this.systemUserVO.getPassword() != null)
			   userMap.put("password", this.systemUserVO.getPassword());
		   if(oldPassword != null)
			   userMap.put("oldPassword", this.oldPassword);
		   
		   userMap.put("isPasswordChangeOperation", isPasswordChangeOperation);
		   userMap.put("isPasswordResetOperation", isPasswordResetOperation);
		   
		   List users = new ArrayList();
		   users.add(userMap);
			   
		   ws.addArgument("users", users);

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

   public void setUserName(String userName)  throws JspException
   {
	   this.systemUserVO.setUserName(evaluateString("remoteUserService", "userName", userName));
   }

   public void setFirstName(String firstName)  throws JspException
   {
	   this.systemUserVO.setFirstName(evaluateString("remoteUserService", "firstName", firstName));
   }

   public void setLastName(String lastName)  throws JspException
   {
	   this.systemUserVO.setLastName(evaluateString("remoteUserService", "lastName", lastName));
   }

   public void setEmail(String email)  throws JspException
   {
	   this.systemUserVO.setEmail(evaluateString("remoteUserService", "email", email));
   }

   public void setPassword(String password)  throws JspException
   {
	   this.systemUserVO.setPassword(evaluateString("remoteUserService", "password", password));
   }

   public void setSystemUserVO(final String systemUserVO) throws JspException
   {
	   this.systemUserVO = (SystemUserVO)this.evaluate("remoteUserService", "systemUserVO", systemUserVO, SystemUserVO.class);
   }

   public void setIsPasswordChangeOperation(final Boolean isPasswordChangeOperation) 
   {
	   this.isPasswordChangeOperation = isPasswordChangeOperation;
   }

   public void setIsPasswordResetOperation(final Boolean isPasswordResetOperation) 
   {
	   this.isPasswordResetOperation = isPasswordResetOperation;
   }

   public void setOldPassword(final String oldPassword) throws JspException
   {
	   this.oldPassword = evaluateString("remoteUserService", "oldPassword", oldPassword);
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