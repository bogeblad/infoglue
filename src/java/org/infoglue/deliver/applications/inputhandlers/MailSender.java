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
 
 
package org.infoglue.deliver.applications.inputhandlers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.mail.MailServiceFactory;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
import org.infoglue.deliver.util.VelocityTemplateProcessor;

/**
 * This is the first implementation of the InfoGlueInputHandler which emails a recipent the feedback.
 * This class needs a couple of parameters from the form-content. They are:
 * 
 * MailSender.fromAddress = The address from where the mail should originate
 * MailSender.toAddress   = The recipient of the feedback on the site.
 * MailSender.subject     = The subject line we want in the emails.
 * MailSender.template    = The template is as all other templates a way to present info and in this case to create an email body. Check out the examples.
 */

public class MailSender implements InfoGlueInputHandler
{
	
	/**
	 * This is the method that is invoked by the calling action.
	 */ 
	
	public void processInput(DatabaseWrapper databaseWrapper, Integer siteNodeId, Integer languageId, Integer contentId, Integer formContentId, HashMap parameters, HttpServletRequest request, InfoGluePrincipal infogluePrincipal) throws Exception
	{
		String template     = ContentDeliveryController.getContentDeliveryController().getContentAttribute(databaseWrapper.getDatabase(), formContentId, languageId, "MailSender_template", siteNodeId, true, DeliveryContext.getDeliveryContext(), infogluePrincipal, false);
		String fromAddress 	= ContentDeliveryController.getContentDeliveryController().getContentAttribute(databaseWrapper.getDatabase(), formContentId, languageId, "MailSender_fromAddress", siteNodeId, true, DeliveryContext.getDeliveryContext(), infogluePrincipal, false);
		String toAddress   	= ContentDeliveryController.getContentDeliveryController().getContentAttribute(databaseWrapper.getDatabase(), formContentId, languageId, "MailSender_toAddress", siteNodeId, true, DeliveryContext.getDeliveryContext(), infogluePrincipal, false);
		String subject 		= ContentDeliveryController.getContentDeliveryController().getContentAttribute(databaseWrapper.getDatabase(), formContentId, languageId, "MailSender_subject", siteNodeId, true, DeliveryContext.getDeliveryContext(), infogluePrincipal, false);
		String body         = renderMailBody(databaseWrapper, siteNodeId, languageId, contentId, template, parameters, request, infogluePrincipal);
		
		//MailServiceFactory.getService().send(fromAddress, toAddress, subject, body);
		MailServiceFactory.getService().send(fromAddress, toAddress, subject, body, "text/html", "UTF-8");
	}
	
	
	
	/**
	 * This method creates a mail from a velocity-template.
	 */
	
	private String renderMailBody(DatabaseWrapper databaseWrapper, Integer siteNodeId, Integer languageId, Integer contentId, String template, HashMap parameters, HttpServletRequest request, InfoGluePrincipal infogluePrincipal) throws Exception
	{
		parameters.put("templateLogic", getTemplateController(databaseWrapper, siteNodeId, languageId, contentId, request, infogluePrincipal));
		
		StringWriter tempString = new StringWriter();
		PrintWriter pw = new PrintWriter(tempString);
		new VelocityTemplateProcessor().renderTemplate(parameters, pw, template);
		return tempString.toString();
	}
	
	
	   	/**
	 * This method should be much more sophisticated later and include a check to see if there is a 
	 * digital asset uploaded which is more specialized and can be used to act as serverside logic to the template.
	 * The method also consideres wheter or not to invoke the preview-version with administrative functioality or the 
	 * normal site-delivery version.
	 */
	
	public TemplateController getTemplateController(DatabaseWrapper databaseWrapper, Integer siteNodeId, Integer languageId, Integer contentId, HttpServletRequest request, InfoGluePrincipal infogluePrincipal) throws Exception
	{
    	NodeDeliveryController nodeDeliveryController				= NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
		IntegrationDeliveryController integrationDeliveryController	= IntegrationDeliveryController.getIntegrationDeliveryController(siteNodeId, languageId, contentId);
		
		TemplateController templateController = new BasicTemplateController(databaseWrapper, infogluePrincipal);
		templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
		templateController.setHttpRequest(request);	
		templateController.setDeliveryControllers(nodeDeliveryController, null, integrationDeliveryController);	
		return templateController;		
	}

}
