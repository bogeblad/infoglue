package org.infoglue.deliver.taglib.content.el;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.security.InfoGluePrincipal;


/**
 * This tag helps create a content in the cms from the delivery application.
 */

public class RemoteContentServiceTag extends org.infoglue.deliver.taglib.content.RemoteContentServiceTag 
{
	private static final long serialVersionUID = 3293372154959412700L;

	public void setPrincipal(final InfoGluePrincipal principal) throws JspException
    {
        super.setPrincipalObject(principal);
    }

}