package org.infoglue.deliver.taglib.management.el;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * This tag helps create form entries from the delivery application.
 */

public class RemoteFormServiceTag extends org.infoglue.deliver.taglib.management.RemoteFormServiceTag 
{
	private static final long serialVersionUID = 5061935149685966877L;

	public void setPrincipal(final InfoGluePrincipal principal) throws JspException
	{
		super.setPrincipalObject(principal);
	}
}