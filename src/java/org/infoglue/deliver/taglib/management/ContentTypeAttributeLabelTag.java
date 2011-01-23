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
package org.infoglue.deliver.taglib.management;

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

import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeAttributeParameter;
import org.infoglue.cms.entities.management.ContentTypeAttributeParameterValue;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * ContentTypeAttributeLabelTag is an InfoGlue JSP tag that fetches localized
 * labels of attributes or attribute values of content type definitions.
 * 
 * @author Peter.Jaric@its.uu.se
 */
public class ContentTypeAttributeLabelTag extends TemplateControllerTag
{
	/* Tag constants */
	private static final String TAG_NAME = "contentTypeAttributeLabel";
	private static final String PARAM_ATTRIBUTE_VALUE = "attributeValue";
	private static final String PARAM_ATTRIBUTE = "attribute";
	private static final String PARAM_CONTENT_ID = "contentId";
	private static final String PARAM_CONTENT_TYPE_DEFINITION_NAME = "contentTypeDefinitionName";

	/* Exception messages */
	private static final String EXCEPTION_MSG_AT_LEAST_ONE_PARAM = "At least one of " + PARAM_CONTENT_ID + " or " + PARAM_CONTENT_TYPE_DEFINITION_NAME + " must be specified.";
	private static final String EXCEPTION_MSG_COULD_NOT_FIND_CTD = "Could not find ContentTypeDefinition with name ";
	private static final String EXCEPTION_MSG_OTHER_EXCEPTION = "Exception in " + TAG_NAME + ": ";

	private static final String CTD_VALUES_KEY = "values";
	private static final String CTD_LABEL_KEY = "label";
	private static final String CTD_TITLE_KEY = "title";

	/* Tag parameters */
	private String attribute;
	private ContentTypeDefinitionVO contentType;
	private Integer contentId;
	private String attributeValue;

	/**
	 * Default constructor.
	 */
	public ContentTypeAttributeLabelTag()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException
	{
		return EVAL_BODY_INCLUDE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doEndTag() throws JspException
	{
		try
		{
			ContentTypeDefinitionVO ctd;

			// Get the content type definition.
			if (contentType != null)
			{
				// If we have a content type already specified, use that
				ctd = contentType;
			}
			else if (contentId != null)
			{
				// otherwise get it from the content with contentId
				Integer ctdId = ContentController.getContentController().getContentVOWithId(contentId).getContentTypeDefinitionId();
				ctd = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(ctdId);
			}
			else
			{
				// Neither was specified. Error!
				throw new JspTagException(EXCEPTION_MSG_AT_LEAST_ONE_PARAM);
			}

			String label;

			// Get the localized label
			if (attributeValue != null)
			{
				// If an attributeValue name was specified, we should retrieve
				// the localized label of that value
				label = getAttributeValueLabel(getController(), ctd, attribute, attributeValue);
			}
			else
			{
				// Otherwise just get localized label of the attribute
				label = getAttributeLabel(getController(), ctd, attribute);
			}

			// We have a label, give it to the page
			produceResult(label);
		}
		catch (SystemException e)
		{
			throw new JspTagException(EXCEPTION_MSG_OTHER_EXCEPTION + e.getMessage());
		}

		// Make sure everything is reset for the next call to this tag
		resetParameters();

		return EVAL_PAGE;
	}

	public void setContentId(final String contentId) throws JspException
	{
		this.contentId = evaluateInteger(TAG_NAME, PARAM_CONTENT_ID, contentId);
	}

	public void setContentTypeDefinitionName(final String contentTypeDefinitionName) throws JspException
	{
		ContentTypeDefinitionVO contentType;
		String evaluatedName = evaluateString(TAG_NAME,	PARAM_CONTENT_TYPE_DEFINITION_NAME, contentTypeDefinitionName);

		// Try to fetch the content type matching contentTypeDefinitionName
		try
		{
			contentType = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(evaluatedName);
		}
		catch (Exception e)
		{
			contentType = null;
		}

		if (contentType != null)
		{
			// If we got a content type, use it
			this.contentType = contentType;
		}
		else
		{
			// Otherwise throw an exception that tells the caller that the
			// contentTypeDefinitionName was wrong
			throw new JspException(EXCEPTION_MSG_COULD_NOT_FIND_CTD + contentTypeDefinitionName);
		}
	}

	public void setAttribute(final String attribute) throws JspException
	{
		this.attribute = evaluateString(TAG_NAME, PARAM_ATTRIBUTE, attribute);
	}

	public void setAttributeValue(final String attributeValue) throws JspException
	{
		this.attributeValue = evaluateString(TAG_NAME, PARAM_ATTRIBUTE_VALUE, attributeValue);
	}

	private void resetParameters()
	{
		contentId = null;
		contentType = null;
		attribute = null;
		attributeValue = null;
	}

	/**
	 * Returns the localized version of the label of a content type's attribute.
	 * 
	 * @param pc
	 * @param contentVO
	 * @param attributeName
	 * @return
	 * @throws SystemException
	 * @throws SystemException
	 */
	private static String getAttributeLabel(TemplateController pc, ContentTypeDefinitionVO ctd, String attributeName) throws SystemException
	{
		Locale currentLocale = pc.getLocale();
		ContentTypeAttribute attr = getContentTypeDefinitionAttribute(ctd, attributeName);

		if (attr != null)
		{
			return attr.getContentTypeAttribute(CTD_TITLE_KEY).getContentTypeAttributeParameterValue().getLocalizedValue(CTD_LABEL_KEY, currentLocale);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns the localized version of the label of a value of a content type's
	 * attribute.
	 * 
	 * @param pc
	 * @param contentVO
	 * @param attributeName
	 * @return
	 * @throws SystemException
	 */
	private static String getAttributeValueLabel(TemplateController pc,
			ContentTypeDefinitionVO ctd, String attributeName, String attributeValueName)
			throws SystemException
	{
		Locale currentLocale = pc.getLocale();
		ContentTypeAttribute attr = getContentTypeDefinitionAttribute(ctd, attributeName);

		if (attr != null)
		{
			ContentTypeAttributeParameter valuesParameter =
			// This method actually gets a ContentTypeAttributeParameter
			attr.getContentTypeAttribute(CTD_VALUES_KEY);
			
			if (valuesParameter != null)
			{
				ContentTypeAttributeParameterValue value = valuesParameter.getContentTypeAttributeParameterValue(attributeValueName);
				if (value != null)
				{
					return value.getLocalizedValue(CTD_LABEL_KEY, currentLocale);
				}
			}
		}

		return null;
	}

	/**
	 * Returns an attribute from the specified schema.
	 * 
	 * @param contentVO
	 * @param attributeName
	 * @param schema
	 * @return
	 * @throws SystemException
	 */
	@SuppressWarnings("unchecked")
	private static ContentTypeAttribute getContentTypeDefinitionAttribute(ContentTypeDefinitionVO ctd, String attributeName) throws SystemException
	{
		List<ContentTypeAttribute> attrs = ContentTypeDefinitionController.getController().getContentTypeAttributes(ctd, true);
		for (ContentTypeAttribute attr : attrs)
		{
			if (attr.getName().equals(attributeName))
			{
				return attr;
			}
		}

		// The attribute name was not found.
		return null;
	}
}
