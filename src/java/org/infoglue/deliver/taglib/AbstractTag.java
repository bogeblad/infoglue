/* ===============================================================================
*
* Part of the InfoGlue Content Management Platform (www.infoglue.org)
*
* ===============================================================================
*
*  Copyright (C)
*
* This program is free software; you can redistribute it and/or modify it under
* the terms of the GNU General Public License version 2, as published by the
* Free Software Foundation. See the file LICENSE.html for more information.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
* Place, Suite 330 / Boston, MA 02111-1307 / USA.
*
* ===============================================================================
*/
package org.infoglue.deliver.taglib;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;

/**
 * Base class for all tags in the infoglue platform.
 */
public abstract class AbstractTag extends TagSupport 
{
	/**
	 * TODO: remove, use var instead.
	 * 
	 * Indicates (if present) the name of the page context variable to store the result in. 
	 */
	private String id;
	
	/**
	 * Default constructor.
	 */
	protected AbstractTag()
	{
		super();
	}

	/**
	 * TODO: remove, use var instead.
	 * 
	 * Sets the name of the page context variable to store the result in.
	 * 
	 * @param id the id to use.
	 */
    public void setId(String id)
    {
        this.id = id;
    }

	/**
	 * Stores the <code>value</code> in the page context variable indicated by the <code>id</code> attribute.
	 * If the <code>value</code> is null; the page context variable is removed instead.
	 * 
	 * @param value the value to store.
	 */
	protected void setResultAttribute(Object value)
	{
		if(value == null)
		{
			pageContext.removeAttribute(id);
		}
		else
		{
			pageContext.setAttribute(id, value);
		}
	}
	
	/**
	 * Depending on wheter the <code>id</code> attribute is set, does one of the following:
	 * 
	 * - stores the value in a page context variable (if the <code>id</code> attribute is set).
	 * - writes the value to the output stream.
	 * 
	 * @param value the value.
	 * @throws JspTagException if an exception occurs when storing/writing the value.
	 */
	protected void produceResult(Object value) throws JspTagException
	{
	    if(id == null)
	    {
			write((value == null) ? "" : value.toString());
	    }
		else
		{
			setResultAttribute(value);
		}
	}

	/**
	 * Writes the specified text to the response stream.
	 * 
	 * @param text the text to write.
	 * @throws JspException if an I/O error occurs.
	 */
	protected void write(final String text) throws JspTagException
	{
		try 
		{
			pageContext.getOut().write(text);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			throw new JspTagException("IO error: " + e.getMessage());
		}
	}

	/**
	 * Evaluates an expression if present, but does not allow the expression to evaluate to <code>null</null>.
	 * 
	 * @param tagName the name of the tag.
	 * @param attributeName the name of the attribute to evaluate.
	 * @param expression the expression to evaluate.
	 * @param expectedType the expected type of the evaluated expression.
	 * @return the evaluated expression.
	 * @throws JspException if an error occurs while evaluating the expression.
	 */
	protected Object evaluate(String tagName, String attributeName, String expression, Class expectedType) throws JspException
	{
		return ExpressionUtil.evalNotNull(tagName, attributeName, expression, expectedType, this, pageContext);
	}

	/**
	 * Evaluates the expression which must evaluate to an Integer.
	 * 
	 * @param tagName the name of the tag.
	 * @param attributeName the name of the attribute to evaluate.
	 * @param expression the expression to evaluate.
	 * @return the evaluated expression.
	 * @throws JspException if an error occurs while evaluating the expression.
	 */
	protected Integer evaluateInteger(String tagName, String attributeName, String expression) throws JspException
	{
		return (Integer) evaluate(tagName, attributeName, expression, Integer.class);
	}

	/**
	 * Evaluates the expression which must evaluate to a String.
	 * 
	 * @param tagName the name of the tag.
	 * @param attributeName the name of the attribute to evaluate.
	 * @param expression the expression to evaluate.
	 * @return the evaluated expression.
	 * @throws JspException if an error occurs while evaluating the expression.
	 */
	protected String evaluateString(String tagName, String attributeName, String expression) throws JspException
	{
		return (String) evaluate(tagName, attributeName, expression, String.class);
	}

	/**
	 * Evaluates the expression which must evaluate to a Collection.
	 * 
	 * @param tagName the name of the tag.
	 * @param attributeName the name of the attribute to evaluate.
	 * @param expression the expression to evaluate.
	 * @return the evaluated expression.
	 * @throws JspException if an error occurs while evaluating the expression.
	 */
	protected Collection evaluateCollection(String tagName, String attributeName, String expression) throws JspException
	{
		return (Collection) evaluate(tagName, attributeName, expression, Collection.class);
	}

	/**
	 * Evaluates the expression which must evaluate to a List.
	 * 
	 * @param tagName the name of the tag.
	 * @param attributeName the name of the attribute to evaluate.
	 * @param expression the expression to evaluate.
	 * @return the evaluated expression.
	 * @throws JspException if an error occurs while evaluating the expression.
	 */
	protected List evaluateList(String tagName, String attributeName, String expression) throws JspException
	{
		return (List) evaluate(tagName, attributeName, expression, List.class);
	}
}
