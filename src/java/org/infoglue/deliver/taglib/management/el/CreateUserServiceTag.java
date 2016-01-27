package org.infoglue.deliver.taglib.management.el;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.security.InfoGluePrincipal;


/**
 * This tag helps create create / update / delete users in the cms from the delivery application.
 */

public class CreateUserServiceTag extends org.infoglue.deliver.taglib.management.CreateUserServiceTag 
{
	private static final long serialVersionUID = 5298907823899677046L;

	/**
    * 
    */
   public void setPrincipal(final InfoGluePrincipal principalString) throws JspException
   {
	   super.setPrincipalObject(principalString);
   }

   /**
    * 
    */
   public void setSystemUserVO(final SystemUserVO systemUserVO) throws JspException
   {
	   super.setSystemUserVOObject(systemUserVO);
   }


}