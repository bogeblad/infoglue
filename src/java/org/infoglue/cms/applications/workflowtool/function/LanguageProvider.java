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

import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.management.LanguageVO;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class LanguageProvider extends InfoglueFunction 
{
	/**
	 * 
	 */
	public static final String LANGUAGE_PARAMETER   = "language";

	/**
	 * 
	 */
	public static final String LANGUAGE_PROPERTYSET_KEY  = "languageId";

	/**
	 * 
	 */
	public static final String LANGUAGE_CODE_ARGUMENT = "code";

	/**
	 * 
	 */
	public static final String ARGUMENT_SCOPE_ARGUMENT = "scope";

	/**
	 * 
	 */
	public static final String LANGUAGE_ID_IDENTIFIER = "languageId";

	
	
	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		//try
		//{
			
		LanguageVO languageVO = null;
		
		if(argumentExists(ARGUMENT_SCOPE_ARGUMENT) && getArgument(ARGUMENT_SCOPE_ARGUMENT).equalsIgnoreCase("argument"))
		{
			languageVO = getLanguageWithCode(getArgument(LANGUAGE_CODE_ARGUMENT).toString());
		}
		else if(argumentExists(ARGUMENT_SCOPE_ARGUMENT) && getArgument(ARGUMENT_SCOPE_ARGUMENT).equalsIgnoreCase("parameter"))
		{
			String languageIdString = getParameterStringValue(LANGUAGE_ID_IDENTIFIER, false);
			
			if(argumentExists("parameterName"))
			{
				String parameterNameString = getArgument("parameterName");
				if(parameterNameString != null && !parameterNameString.equals(""))
				{
					String altLanguageIdString = getParameterStringValue(parameterNameString, false);
					if(altLanguageIdString != null && !altLanguageIdString.equals(""))
						languageIdString = altLanguageIdString;
				}
			}
			
			if(languageIdString != null)
				languageVO = getLanguageWithID(languageIdString);
		}

		if(languageVO == null)
		{
			if(propertySetContains(LANGUAGE_PROPERTYSET_KEY))
			{
				languageVO = getLanguageWithID(getPropertySetString(LANGUAGE_PROPERTYSET_KEY));
			}
			if(languageVO == null && parameterExists(LANGUAGE_ID_IDENTIFIER))
			{
				languageVO = getLanguageWithID(getParameter(LANGUAGE_ID_IDENTIFIER).toString());
			}
			if(languageVO == null && argumentExists(LANGUAGE_CODE_ARGUMENT))
			{
				languageVO = getLanguageWithCode(getArgument(LANGUAGE_CODE_ARGUMENT));
			}
	
			if(languageVO == null)
			{
				languageVO = getAnyLanguage();
			}
		}

		populate(languageVO);
		//}
		//catch (Exception e) {
		//	e.printStackTrace();
		//}
	}
	
	/**
	 * 
	 */
	private void populate(final LanguageVO languageVO) throws WorkflowException
	{
		if(languageVO == null && propertySetContains(LANGUAGE_PROPERTYSET_KEY))
		{
			removeFromPropertySet(LANGUAGE_PROPERTYSET_KEY);
		}
		if(languageVO != null) 
		{
			setParameter(LANGUAGE_PARAMETER, languageVO);
			setPropertySetString(LANGUAGE_PROPERTYSET_KEY, languageVO.getId().toString());
		}
	}
	
	/**
	 * 
	 */
	public LanguageVO getAnyLanguage() throws WorkflowException 
	{
		LanguageVO languageVO = null;
		try 
		{
			final List languages = LanguageController.getController().getLanguageVOList(getDatabase());
			if(!languages.isEmpty())
			{
				languageVO = (LanguageVO) languages.get(0);
			}
			throwException("No languages found...");
		} 
		catch (Exception e) 
		{
			throwException("Language.getAnyLanguage() : " + e);
		}
		return languageVO;
	}

	/**
	 * 
	 */
	public LanguageVO getLanguageWithID(final String languageId) throws WorkflowException 
	{
		LanguageVO languageVO = null;
		try 
		{
			languageVO = LanguageController.getController().getLanguageVOWithId(new Integer(languageId), getDatabase());
		} 
		catch (Exception e) 
		{
			throw new WorkflowException("Language.getLanguageWithID() : " + e);
		}
		return languageVO;
	}

	/**
	 * 
	 */
	public LanguageVO getLanguageWithCode(final String code) throws WorkflowException 
	{
		LanguageVO languageVO = null;
		try 
		{
			languageVO = LanguageController.getController().getLanguageVOWithCode(code, getDatabase());
		} 
		catch (Exception e) 
		{
			throwException("Language.getLanguageWithCode() : " + e);
		}
		return languageVO;
	}
}