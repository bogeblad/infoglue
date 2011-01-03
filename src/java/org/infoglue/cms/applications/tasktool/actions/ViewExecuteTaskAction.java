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

package org.infoglue.cms.applications.tasktool.actions;


import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.structuretool.actions.ViewStructureTreeForInlineLinkAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.deliver.util.VelocityTemplateProcessor;

public class ViewExecuteTaskAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewExecuteTaskAction.class.getName());

	private static final long serialVersionUID = 1L;

	private Integer taskContentId = null;
	private Integer contentId = null;

	public ViewExecuteTaskAction()
	{
	}
    
    /**
     * This method which is the default one only serves to show a list 
     * of tasks to the user so he/she can select one to run. 
     */
    
	public String doExecute() throws Exception
	{
		return "success";
	}

	public String doUserInput() throws Exception
	{
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(this.getTaskContentId());
		
		logger.info("Language:" + LanguageController.getController().getMasterLanguage((Integer)getHttpSession().getAttribute("repositoryId")).getId());
		
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestContentVersionVO(contentVO.getId(), LanguageController.getController().getMasterLanguage((Integer)getHttpSession().getAttribute("repositoryId")).getId());

		//TODO - should not do this but find one available version probably
		if(contentVersionVO == null)
			contentVersionVO = ContentVersionController.getContentVersionController().getLatestContentVersionVO(contentVO.getId(), ((LanguageVO)LanguageController.getController().getLanguageVOList().get(0)).getId());
		
		//logger.info("contentVersionVO:" + contentVersionVO);
		
		String userInputHTML = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO.getId(), "UserInputHTML", false);
	
		//logger.info("Found userInputHTML:" + userInputHTML);
			 
		ScriptController scriptController = getScriptController();
		scriptController.setRequest(this.getRequest());
		scriptController.beginTransaction();
		
		Map context = new HashMap();
		context.put("scriptLogic", scriptController);

		StringWriter cacheString = new StringWriter();
		PrintWriter cachedStream = new PrintWriter(cacheString);
		new VelocityTemplateProcessor().renderTemplate(context, cachedStream, userInputHTML);
		//renderTemplate(context, cachedStream, userInputHTML);
		String result = cacheString.toString();

		scriptController.commitTransaction();
				
		getResponse().setContentType("text/html");
		PrintWriter out = getResponse().getWriter();
		out.println(result);
		out.flush();
		out.close();
		
		return NONE;
	}

	/**
	 * This method serves as the invoker of a task/script. It uses the velocity-engine to run logic.
	 * It allways run the script within it's own transaction as of now.
	 * 
	 * @throws Exception
	 */

	public String doExecuteTask() throws Exception
	{
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(this.getTaskContentId());
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestContentVersionVO(contentVO.getId(), LanguageController.getController().getMasterLanguage((Integer)getHttpSession().getAttribute("repositoryId")).getId());
		
		//TODO - should not do this but find one available version probably
		if(contentVersionVO == null)
			contentVersionVO = ContentVersionController.getContentVersionController().getLatestContentVersionVO(contentVO.getId(), ((LanguageVO)LanguageController.getController().getLanguageVOList().get(0)).getId());
		
		String code = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO.getId(), "ScriptCode", false);
		String userOutputHTML = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO.getId(), "UserOutputHTML", false);
		
		ScriptController scriptController = getScriptController();
		scriptController.setRequest(this.getRequest());
		scriptController.setResponse(this.getResponse());
		scriptController.beginTransaction();

		Map context = new HashMap();
		context.put("scriptLogic", scriptController);

		StringWriter cacheString = new StringWriter();
		PrintWriter cachedStream = new PrintWriter(cacheString);
		new VelocityTemplateProcessor().renderTemplate(context, cachedStream, code);
		//renderTemplate(context, cachedStream, code);
		String result = cacheString.toString();
		
		scriptController.commitTransaction();
		
		cacheString = new StringWriter();
		cachedStream = new PrintWriter(cacheString);
		new VelocityTemplateProcessor().renderTemplate(context, cachedStream, userOutputHTML);
		//renderTemplate(context, cachedStream, userOutputHTML);
		result = cacheString.toString();

		getResponse().setContentType("text/html");
		PrintWriter out = getResponse().getWriter();
		out.println(result);
		out.flush();
		out.close();
				
		return NONE;
	}

	/**
	 * This method prepares the script-object which should be supplied to the scripting engine.
	 */
	
	private ScriptController getScriptController() throws Exception
	{
		ScriptController scriptController = new BasicScriptController(this.getInfoGluePrincipal());
		
		return scriptController; 
	}

	/**
	 * This method takes arguments and renders a template given as a string to the specified outputstream.
	 * Improve later - cache for example the engine.
	 */
	/*
	public void renderTemplate(Map params, PrintWriter pw, String templateAsString) throws Exception 
	{
		Velocity.init();

		VelocityContext context = new VelocityContext();
		Iterator i = params.keySet().iterator();
		while(i.hasNext())
		{
			String key = (String)i.next();
			context.put(key, params.get(key));
		}
        
		Reader reader = new StringReader(templateAsString);
		boolean finished = Velocity.evaluate(context, pw, "Generator Error", reader);        
	}
	*/

	/**
	 * This method returns the contents that are of contentTypeDefinition "HTMLTemplate"
	 */
	public List getTasks() throws Exception
	{
		HashMap arguments = new HashMap();
		arguments.put("method", "selectListOnContentTypeName");
		
		List argumentList = new ArrayList();
		HashMap argument = new HashMap();
		argument.put("contentTypeDefinitionName", "TaskDefinition");
		argumentList.add(argument);
		arguments.put("arguments", argumentList);
		
		return ContentControllerProxy.getController().getACContentVOList(this.getInfoGluePrincipal(), arguments);
	}

	/**
	 * This method returns the contents that are of contentTypeDefinition "HTMLTemplate"
	 */
	public List getCustomTasks() throws Exception
	{
		HashMap arguments = new HashMap();
		arguments.put("method", "selectListOnContentTypeName");
		
		List argumentList = new ArrayList();
		HashMap argument = new HashMap();
		argument.put("contentTypeDefinitionName", "TaskDefinition");
		argumentList.add(argument);
		arguments.put("arguments", argumentList);
		
		return ContentControllerProxy.getController().getACContentVOList(this.getInfoGluePrincipal(), arguments);
	}

	public Integer getTaskContentId()
	{
		return taskContentId;
	}

	public void setTaskContentId(Integer taskContentId)
	{
		this.taskContentId = taskContentId;
	}

	public Integer getContentId()
	{
		return contentId;
	}

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}
}
