package org.infoglue.deliver.taglib.content.el;

import java.util.Date;

import javax.servlet.jsp.JspException;

/**
 * This tag helps update a content in the cms from the delivery application.
 */

public class ContentParameterTag extends org.infoglue.deliver.taglib.content.ContentParameterTag
{
	private static final long serialVersionUID = 1932092490515602798L;

	public void setExpireDateTime(Date expireDateTime) throws JspException
    {
        super.setExpireDateTimeObject(expireDateTime);
    }

    public void setPublishDateTime(Date publishDateTime) throws JspException
    {
    	super.setPublishDateTimeObject(publishDateTime);
    }
}