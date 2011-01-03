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

package org.infoglue.deliver.util.forms;

import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.deliver.controllers.kernel.impl.simple.FormDeliveryController;

/**
 * This class is an attempt to give template-coders access to helper methods for form-handling.
 */

public class FormHelper
{
	public FormHelper()
	{
	}

	public ContentTypeAttribute getContentTypeAttribute(String schemaValue, String attributeName)
	{
	    ContentTypeAttribute contentTypeAttribute = null;
	    
	    List attributes = FormDeliveryController.getFormDeliveryController().getContentTypeAttributes(schemaValue);
	    
	    Iterator attributesIterator = attributes.iterator();
	    while(attributesIterator.hasNext())
	    {
	        ContentTypeAttribute tempContentTypeAttribute = (ContentTypeAttribute)attributesIterator.next();
	        if(tempContentTypeAttribute.getName().equalsIgnoreCase(attributeName))
	        {
	            contentTypeAttribute = tempContentTypeAttribute;
	            break;
	        }
	    }
	    
	    return contentTypeAttribute;
	}
}
