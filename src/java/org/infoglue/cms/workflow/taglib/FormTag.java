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

package org.infoglue.cms.workflow.taglib;

import java.text.MessageFormat;

import javax.servlet.jsp.JspException;

/**
 * This class implements the &lt;iw:form&gt; tag, which presents an &lt;form ... /&gt; element 
 * with all the attributes and hidden fields properly initialized 
 * as required by the workflow framework.  
 */
public class FormTag extends WorkflowTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -558848421886366918L;

	/**
	 * The key used for finding the form action in the request.
	 */
	private static final String ACTION_ADDRESS_PARAMETER = "returnAddress";

	/**
	 * The key used for finding the form action in the request.
	 */
	private static final String FINISHED_ADDRESS_PARAMETER = "finalReturnAddress";

	/**
	 * The template for the start tag of the form element.
	 */
	private static final String FORM_START = "<form name=\"form\" id=\"form\" method=\"post\" action=\"{0}\">";

	/**
	 * The template for the hiddens fields needed by all workflows (action and workflow id).
	 */
	private static final String HIDDEN = "<div><input id=\"{0}\" name=\"{0}\" type=\"hidden\" value=\"{1}\"/></div>";

	/**
	 * The template for the end tag of the form element.
	 */
	private static final String FORM_END = "</form>";

	/**
	 * Default constructor.
	 */
	public FormTag() 
	{
		super();
	}

	/**
	 * Process the start tag. Writes the start tag of the form element and the required hidden fields
	 * to the output stream.
	 * 
	 * @return indication of whether to evaluate the body or not.
	 * @throws JspException if an I/O error occurs when writing to the output stream.
	 */
	public int doStartTag() throws JspException 
	{
		write(MessageFormat.format(FORM_START, new Object[] { getActionAddress() }));
		write(MessageFormat.format(HIDDEN, new Object[] { ACTION_ID_PARAMETER, getActionID() }));
		write(MessageFormat.format(HIDDEN, new Object[] { FINISHED_ADDRESS_PARAMETER, getFinalReturnAddress() }));
		write(MessageFormat.format(HIDDEN, new Object[] { WORKFLOW_ID_PARAMETER,  getWorkflowID() }));
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Process the end tag. Writes the end tag of the form element to the output stream.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an I/O error occurs when writing to the output stream.
	 */
	public int doEndTag() throws JspException 
	{
		write(FORM_END);
		return EVAL_PAGE;
	}

	/**
	 * Returns the action to use in the form.
	 * 
	 * @return the action to use in the form.
	 */
	private String getActionAddress() 
	{
		return pageContext.getRequest().getParameter(ACTION_ADDRESS_PARAMETER);
	}

	/**
	 * Returns the action to use in the form.
	 * 
	 * @return the action to use in the form.
	 */
	private String getFinalReturnAddress() 
	{
		return pageContext.getRequest().getParameter(FINISHED_ADDRESS_PARAMETER);
	}

}
