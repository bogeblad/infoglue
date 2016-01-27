package org.infoglue.deliver.taglib.structure.el;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * This tag helps create a siteNode in the cms from the delivery application.
 */

public class RemoteSiteNodeServiceTag extends org.infoglue.deliver.taglib.structure.RemoteSiteNodeServiceTag 
{
	private static final long serialVersionUID = -5626384781961591492L;

	public void setPrincipal(final InfoGluePrincipal principal) throws JspException
	{
		super.setPrincipalObject(principal);
	}

}