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

package org.infoglue.deliver.invokers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.exception.NoBaseTemplateFoundException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
import org.infoglue.deliver.util.VelocityTemplateProcessor;

/**
 * @author Mattias Bogeblad
 *
 * This class delivers a normal html page by using the normal template mechanism introduced in infoglue 1.0
 * Used mostly for simple pages when the new component-based page type is ready.
 */

public class HTMLPageInvoker extends PageInvoker
{
    private final static Logger logger = Logger.getLogger(HTMLPageInvoker.class.getName());

    /**
	 * This method should return an instance of the class that should be used for page editing inside the tools or in working. 
	 * Makes it possible to have an alternative to the ordinary delivery optimized class.
	 */
	
	public PageInvoker getDecoratedPageInvoker(TemplateController templateController) throws SystemException
	{
	    return this;
	}

	/**
	 * This is the method that will render the page. It uses the original template style logic. 
	 */ 

	public void invokePage() throws NoBaseTemplateFoundException, SystemException, Exception
	{
		try
		{  			
			String templateString = getPageTemplateString(); 
			
			Map context = getDefaultContext();

			StringWriter cacheString = new StringWriter();
			PrintWriter cachedStream = new PrintWriter(cacheString);
			new VelocityTemplateProcessor().renderTemplate(context, cachedStream, templateString);
			String pageString = cacheString.toString();
			
			pageString = this.getTemplateController().decoratePage(pageString);
			
			this.setPageString(pageString);
		}
		catch(Exception e)
		{
			//logger.error(e.getMessage(), e);
			throw e;
		}

	}
	
	
	/**
	 * This method fetches the template-string.
	 */
  
	private String getPageTemplateString() throws NoBaseTemplateFoundException, SystemException, Exception
	{
		String template = null;
    	
		ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId()).getBoundContent(this.getTemplateController().getDatabase(), this.getTemplateController().getPrincipal(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), true, "Template", this.getDeliveryContext());		

		if(logger.isDebugEnabled())
			logger.info("contentVO:" + contentVO);

		if(contentVO == null)
			throw new NoBaseTemplateFoundException("There was no template bound to this page which makes it impossible to render.");	
		
		if(logger.isDebugEnabled())
			logger.info("contentVO:" + contentVO.getName());

		template = this.getTemplateController().getContentAttribute(contentVO.getContentId(), this.getTemplateController().getTemplateAttributeName());
		
		if(template == null)
			throw new SystemException("There was no template bound to this page which makes it impossible to render.");	

		return template;
	}

}
