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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinition;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class ContentPropertysetPopulator extends ContentFunction
{
	private final String PROPERTYSET_PREFIX_ARGUMENT = "propertysetPrefix";
	
	private final String ATTRIBUTE_PREFIX_ARGUMENT = "attributePrefix";
	
	private String propertysetPrefix;
	
	private String attributePrefix;
	
	/**
	 * Default constructor.
	 */
	public ContentPropertysetPopulator() 
	{
		super();
	}

	protected void execute() throws WorkflowException 
	{
		final Map attributeValues = getAttributeValues();
		for(final Iterator i = attributeValues.keySet().iterator(); i.hasNext(); )
		{
			final String name   = i.next().toString();
			final String value  = (String) attributeValues.get(name);
			if(name.startsWith(attributePrefix))
			{
				setPropertySetDataString(propertysetPrefix + name, value);
			}
		}
	}

	/**
	 * TODO: MOVE TO CONTROLLER? 
	 */
	protected final Map getAttributeValues() throws WorkflowException
	{
		final Map values = new HashMap();
		if(getContentVO() != null && getContentVersionVO() != null)
		{
			try
			{
				final ContentTypeDefinitionController ctdController = ContentTypeDefinitionController.getController();
				final ContentTypeDefinition ctd = ctdController.getContentTypeDefinitionWithId(getContentVO().getContentTypeDefinitionId(), getDatabase());
				final Collection contentTypeAttributes = ctdController.getContentTypeAttributes(ctd.getValueObject(), true);
				for(final Iterator i=contentTypeAttributes.iterator(); i.hasNext(); ) 
				{
					final ContentTypeAttribute attribute = (ContentTypeAttribute) i.next();
					values.put(attribute.getName(), ContentVersionController.getContentVersionController().getAttributeValue(getContentVersionVO(), attribute.getName(), false));
				}
			}
			catch(Exception e)
			{
				throwException(e);
			}
		}
		return values;
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
		this.propertysetPrefix = getArgument(PROPERTYSET_PREFIX_ARGUMENT, "");
		this.attributePrefix   = getArgument(ATTRIBUTE_PREFIX_ARGUMENT, "");
	}
}
