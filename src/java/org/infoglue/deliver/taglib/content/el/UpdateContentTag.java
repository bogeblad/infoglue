package org.infoglue.deliver.taglib.content.el;

import java.util.Date;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * This tag helps update a content in the cms from the delivery application.
 */

public class UpdateContentTag extends org.infoglue.deliver.taglib.content.UpdateContentTag
{
	private static final long serialVersionUID = 807857102627991333L;

	public void setPrincipal(final InfoGluePrincipal principal) throws JspException
    {
        super.setPrincipalObject(principal);
    }
	
	public void setExpireDateTime(Date expireDateTime) throws JspException
    {
        super.setExpireDateTimeObject(expireDateTime);
    }

    public void setPublishDateTime(Date publishDateTime) throws JspException
    {
    	super.setPublishDateTimeObject(publishDateTime);
    }
}