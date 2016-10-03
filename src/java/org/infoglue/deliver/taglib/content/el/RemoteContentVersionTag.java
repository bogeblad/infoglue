package org.infoglue.deliver.taglib.content.el;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * This tag helps get a ContentVersionVO in the cms from the delivery application.
 */

public class RemoteContentVersionTag extends org.infoglue.deliver.taglib.content.RemoteContentVersionTag
{
	private static final long serialVersionUID = 3377198952596401075L;

	public void setPrincipal(final InfoGluePrincipal principal) throws JspException
    {
        super.setPrincipalObject(principal);
    }

}