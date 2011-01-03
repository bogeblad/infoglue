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
package org.infoglue.cms.applications.workflowtool.function;

import org.apache.log4j.Logger;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.StringManager;
import org.infoglue.cms.util.StringManagerFactory;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public abstract class ErrorPopulator extends InfoglueFunction 
{
    private final static Logger logger = Logger.getLogger(ErrorPopulator.class.getName());

	/**
	 * 
	 */
	private static final String PACKAGE = "org.infoglue.cms.entities";
	
	/**
	 * 
	 */
	private StringManager stringManager; 
	
	/**
	 * 
	 */
	protected ErrorPopulator()
	{
		super();
	}
	
	/**
	 * 
	 */
	protected final void execute() throws WorkflowException
	{
		clean();
		populate();
	}
	
	/**
	 * 
	 */
	protected abstract void clean() throws WorkflowException;

	/**
	 * 
	 */
	protected abstract void populate() throws WorkflowException;
	
	/**
	 * 
	 */
	protected final void clean(final String errorPrefix) throws WorkflowException
	{
		removeFromPropertySet(errorPrefix, true);
	}

	protected void populate(final ConstraintExceptionBuffer ceb, String languageCode) throws WorkflowException 
	{
		for(ConstraintException e = ceb.toConstraintException(); e != null; e = e.getChainedException())
		{
			populateError(e, languageCode);
		}
	}
	
	/**
	 * 
	 */
	private void populateError(final ConstraintException e, String languageCode) throws WorkflowException
	{
		setPropertySetString(getErrorKey(e, languageCode), getStringManager().getString(e.getErrorCode()));
	}
	
	/**
	 * 
	 */
	private String getErrorKey(final ConstraintException e, String languageCode) 
	{
		// The field name has the form:
		//   Content.<name> 
		// or
		//   ContentVersion.<name>
		// 
		// convert this to:
		//   content_<name> 
		// or
		//   contentversion_<name>
		// to better fit into the workflow framework.
		
		final String fieldName = e.getFieldName();
		final int index = fieldName.indexOf('.');
		if(index == -1) // play it safe 
		{
			return ERROR_PROPERTYSET_PREFIX + e.getFieldName();
		}
		final String before = fieldName.substring(0, index).toLowerCase();
		final String after  = fieldName.substring(index + 1);
		String key    = ERROR_PROPERTYSET_PREFIX + before + "_" + after;
		logger.debug("error field name converted from [" + fieldName  + "] to [" + before + "_" + after + "].");
		
		if(languageCode != null && !languageCode.equals(""))
			key = languageCode + "_" + key;
		
		return key;
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
		stringManager = StringManagerFactory.getPresentationStringManager(PACKAGE, getLocale()); 
	}
	
	/**
	 * 
	 */
	protected final StringManager getStringManager()
	{
	    return stringManager;
	}
}
