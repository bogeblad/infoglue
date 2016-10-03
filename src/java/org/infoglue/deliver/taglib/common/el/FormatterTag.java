package org.infoglue.deliver.taglib.common.el;

import java.util.Locale;

import javax.servlet.jsp.JspException;

public class FormatterTag extends org.infoglue.deliver.taglib.common.FormatterTag
{	
	private static final long serialVersionUID = -4854734248275601362L;

	public void setValue(Object value) throws JspException
	{
		super.setValueObject(value);
	}

	public void setLocale(final Locale locale) throws JspException
	{
		super.setLocaleObject(locale);
	}
}
