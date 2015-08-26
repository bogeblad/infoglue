package org.infoglue.deliver.taglib.management.el;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.taglib.content.ContentVersionParameterInterface;


/**
 * This tag helps create a content in the cms from the delivery application.
 */

public class RemoteUserPropertiesServiceTag extends org.infoglue.deliver.taglib.management.RemoteUserPropertiesServiceTag implements ContentVersionParameterInterface
{
	private static final long serialVersionUID = -1813762283959707180L;

	public void setPrincipal(final InfoGluePrincipal principal) throws JspException
	{
		super.setPrincipalObject(principal);
	}

}