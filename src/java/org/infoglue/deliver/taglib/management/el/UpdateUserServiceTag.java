package org.infoglue.deliver.taglib.management.el;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.security.InfoGluePrincipal;


/**
 * This tag helps create create / update / delete users in the cms from the delivery application.
 */

public class UpdateUserServiceTag extends org.infoglue.deliver.taglib.management.UpdateUserServiceTag 
{
	private static final long serialVersionUID = 486141666760139614L;

	public void setPrincipal(final InfoGluePrincipal principal) throws JspException
	{
		super.setPrincipalObject(principal);
	}

	public void setSystemUserVO(final SystemUserVO systemUserVO) throws JspException
	{
		super.setSystemUserVOObject(systemUserVO);
	}

}