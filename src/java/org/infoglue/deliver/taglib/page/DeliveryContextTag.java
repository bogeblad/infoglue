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

package org.infoglue.deliver.taglib.page;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * Tag for setting parameters on the delivery context;
 */

public class DeliveryContextTag extends TemplateControllerTag
{
	private static final long serialVersionUID = 3905242346756059449L;
	
    public DeliveryContextTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		setResultAttribute(getController().getDeliveryContext());
        return EVAL_PAGE;
    }	
	
	public void setUseFullUrl(boolean useFullUrl) throws JspException
	{
	    getController().getDeliveryContext().setUseFullUrl(useFullUrl);
	}

	public void setDisablePageCache(boolean disablePageCache) throws JspException
	{
	    getController().getDeliveryContext().setDisablePageCache(disablePageCache);
	}

	public void setDisableNiceUri(boolean disableNiceUri) throws JspException
	{
	    getController().getDeliveryContext().setDisableNiceUri(disableNiceUri);
	}

	public void setTrimResponse(boolean trimResponse) throws JspException
	{
	    getController().getDeliveryContext().setTrimResponse(trimResponse);
	}

	public void setEvaluateFullPage(boolean evaluateFullPage) throws JspException
	{
	    getController().getDeliveryContext().setEvaluateFullPage(evaluateFullPage);
	}

	public void setValidateOnDates(boolean validateOnDates) throws JspException
	{
	    getController().getDeliveryContext().setValidateOnDates(validateOnDates);
	}
	
	public void setContentType(String contentType) throws JspException
	{
	    getController().getDeliveryContext().setContentType(contentType);
	}

	public void setPageCacheTimeout(String pageCacheTimeout) throws JspException
	{
		String evaluatedPageCacheTimeout = evaluateString("DeliverContextTag", "pageCacheTimeout", pageCacheTimeout);
	    if(evaluatedPageCacheTimeout != null && !evaluatedPageCacheTimeout.equals(""))
	    {
	    	try
	    	{
		    	getController().getDeliveryContext().setPageCacheTimeout(new Integer(evaluatedPageCacheTimeout));	    		
	    	}
	    	catch (Exception e) 
	    	{
	    		throw new JspException("Wrong format on pageCacheTimeout:" + pageCacheTimeout + " should be an integer value");
			}
	    }
	}
	
	public void setSelectiveCacheUpdateNonApplicable(String selectiveCacheUpdateNonApplicable) throws JspException
	{
		if(selectiveCacheUpdateNonApplicable != null && selectiveCacheUpdateNonApplicable.equalsIgnoreCase("true"))
			getController().getDeliveryContext().addUsedContent("selectiveCacheUpdateNonApplicable");
	}

	public void setOperatingMode(String operatingMode) throws JspException
	{
	    getController().getDeliveryContext().setOperatingMode(operatingMode);
	}

}