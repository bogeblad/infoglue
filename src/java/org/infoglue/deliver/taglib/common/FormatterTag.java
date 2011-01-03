package org.infoglue.deliver.taglib.common;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class FormatterTag extends TemplateControllerTag
{	
	private static final long serialVersionUID 	= -7785046611337302799L;
	private final static Logger logger 			= Logger.getLogger(FormatterTag.class.getName());
	
	private VisualFormatter formatter 			= new VisualFormatter();
	
	private Object value 						= null;
	private String pattern 						= null;
	private Locale locale 						= null;
	private String type 						= null;
	
	public FormatterTag()
	{
		super();
	}
	
	public int doEndTag() throws JspException
	{
		String resultValue = null;
		
		if(value == null)
		{
			resultValue = "";
		}
		else if(type != null && type.equalsIgnoreCase("fileSize"))
		{
			resultValue = formatter.formatFileSize(value);
		}
		else
		{
			try
			{
				if(locale == null)
					locale = this.getController().getLocale();
			}
			catch (Exception e) 
			{
				logger.warn("Problem getting default locale");
			}
			
			if(value instanceof String)
			{
				resultValue = (String)value;
			}
			if(value instanceof Date)
			{
				if(pattern == null || pattern.equals(""))
					resultValue = "yyyy-MM-dd";

				resultValue = formatter.formatDate((Date)value, locale, pattern);
			}
			if(value instanceof Calendar)
			{
				if(pattern == null || pattern.equals(""))
					pattern = "yyyy-MM-dd";

				resultValue = formatter.formatDate(((Calendar)value).getTime(), locale, pattern);
			}
			if(value instanceof Float || value instanceof Double || value instanceof Long || value instanceof Integer)
			{
				if(pattern == null || pattern.equals(""))
					resultValue = NumberFormat.getNumberInstance(locale).format(value);
				else
				{
					NumberFormat formatter = new DecimalFormat(pattern);
					resultValue = formatter.format(value);
				}
			}
		}
		
		produceResult(resultValue);
		
		value 			= null;
		pattern 		= null;
		locale 			= null;
		type 			= null;
		
		return EVAL_PAGE;
	}
		
	public void setValue(String value) throws JspException
	{
		this.value = evaluate("formatterTag", "value", value, Object.class);
	}

	public void setPattern(String pattern) throws JspException
	{
		this.pattern = evaluateString("formatterTag", "pattern", pattern);
	}

	public void setLocale(String locale) throws JspException
	{
		this.locale = (Locale)evaluate("formatterTag", "locale", locale, Locale.class);
	}

	public void setType(String type) throws JspException
	{
		this.type = evaluateString("formatterTag", "type", type);;
	}

}
