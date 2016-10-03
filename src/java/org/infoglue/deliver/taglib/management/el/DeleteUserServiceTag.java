package org.infoglue.deliver.taglib.management.el;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.security.InfoGluePrincipal;


/**
 * This tag helps create create / update / delete users in the cms from the delivery application.
 */

public class DeleteUserServiceTag extends org.infoglue.deliver.taglib.management.DeleteUserServiceTag 
{

	private static final long serialVersionUID = -5185376326332573606L;

	public void setPrincipal(final InfoGluePrincipal principal) throws JspException
	{
		super.setPrincipalObject(principal);
	}

}