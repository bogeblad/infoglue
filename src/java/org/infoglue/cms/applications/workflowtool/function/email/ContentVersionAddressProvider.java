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
package org.infoglue.cms.applications.workflowtool.function.email;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.workflowtool.function.ContentFunction;
import org.infoglue.cms.applications.workflowtool.function.defaultvalue.StringPopulator;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVersionVO;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class ContentVersionAddressProvider extends AddressProvider 
{
    private final static Logger logger = Logger.getLogger(ContentVersionAddressProvider.class.getName());

	/**
	 * The name of the attribute argument.
	 */
	private static final String ATTRIBUTE_ARGUMENT = "attribute";
	
	/**
	 * To name of the attribute containing the email.
	 */
	private String attributeName;
	
	/**
	 * 
	 */
	private ContentVersionVO contentVersionVO;
	
	/**
	 * Default constructor.
	 */
	public ContentVersionAddressProvider() 
	{
		super();
	}

	/**
	 * Add all recipients. Note that empty email-addresses will be discarded
	 * if the <code>required</code> attribute is <code>false</code>.
	 */
	protected void populate() throws WorkflowException
	{
		logger.debug("Creating email from the [" + attributeName + "] attribute.");
		addRecipient(getAttribute());
	}
	
	/**
	 * 
	 */
	private String getAttribute() throws WorkflowException
	{
		String value = "";
		try 
		{
			value = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, attributeName, false);
		}
		catch(Exception e)
		{
			throwException(e);
		}
		return value;
	}
	
	/**
	 * Method used for initializing the function; will be called before <code>execute</code> is called.
	 * <p><strong>Note</strong>! You must call <code>super.initialize()</code> first.</p>
	 * 
	 * @throws WorkflowException if an error occurs during the initialization.
	 */
	protected void initialize() throws WorkflowException 
	{
		super.initialize();
		attributeName    = getArgument(ATTRIBUTE_ARGUMENT);
		contentVersionVO = (ContentVersionVO) getParameter(ContentFunction.CONTENT_VERSION_PARAMETER); 
	}
}
