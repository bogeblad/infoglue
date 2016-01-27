package org.infoglue.deliver.taglib.content.el;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * This tag helps update a content in the cms from the delivery application.
 */

public abstract class InfoGlueWebServiceTag extends org.infoglue.deliver.taglib.content.InfoGlueWebServiceTag
{
	private static final long serialVersionUID = 5968084942278503060L;

	public void setPrincipal(final InfoGluePrincipal principal) throws JspException
    {
        super.setPrincipalObject(principal);
    }
    
}