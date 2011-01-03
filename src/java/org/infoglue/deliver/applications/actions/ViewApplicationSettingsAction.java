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

package org.infoglue.deliver.applications.actions;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
import org.infoglue.deliver.controllers.kernel.impl.simple.ComponentLogic;
import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;

/**
 * This is the action that can supply a caller with a lot of information about the delivery-engine.
 *
 * @author Mattias Bogeblad
 */

public class ViewApplicationSettingsAction extends ViewPageAction //WebworkAbstractAction 
{
    private final static Logger logger = Logger.getLogger(ViewApplicationSettingsAction.class.getName());

	//Used to get a list of all available mthods 
	private List templateMethods = new ArrayList();

	//Used to get the navigation title of a page
	private String navigationTitle = null;
	private String sourceId = null;
	private String className = null;

    private static final String ENCODING = "UTF-8"; 

	/**
	 * The constructor for this action - contains nothing right now.
	 */
    
    public ViewApplicationSettingsAction() 
    {
    }
    
    /**
     * This method is the application entry-point. The parameters has been set through the setters
     * and now we just have to render the appropriate output. 
     */
         
    public String doExecute() throws Exception
    {
		return NONE;
    }

    protected String out(String string) throws IOException
    {
		getResponse().setContentType("text/xml; charset=" + ENCODING);
		PrintWriter out = getResponse().getWriter();
		out.println(string);
		return null;
    }
    
    private String q(String s)
    {
        return "\"" + s + "\"";
    }
    
    private String createMethodElement(Method m)
    {
        String args = "";
        Class[] params = m.getParameterTypes();
        for(int i=0; i<params.length; i++)
        {
            if(i!=0) args+=", ";
            args += params[i].getName();
        }
        
        return "<method name=" + q(m.getName()) + " returnType=" + q(m.getReturnType().getName()) + " args=" + q(args) + "/>";
    }
    
	public String doGetClassMethods() throws Exception
	{
	    StringBuffer document = new StringBuffer();
		try 
		{
		    Class cls = null;
		    if(className==null || className.equals("$templateLogic"))
		        cls = BasicTemplateController.class;
		    else if(className.equals("$componentLogic"))
		        cls = ComponentLogic.class;
		    else
		        cls = Class.forName(className);
		    
		    if(cls==null) return out("<methods class=\"null\" package=\"null\"/>");
		    
		    document.append("<methods class=" +  q(cls.getName()) + " package=" + q(cls.getPackage().getName()) + ">");
            Method m[] = cls.getDeclaredMethods();
            for (int i = 0; i < m.length; i++)
            {
            	Method method = m[i];
            	if(Modifier.isPublic(method.getModifiers()))
            	{
            	    document.append(createMethodElement(method));
            	}
            }
		    document.append("</methods>");
        }
        catch (Throwable e) 
        {
            System.err.println(e);
            return out("<methods class=\"null\" package=\"null\"/>");
        }

		return out(document.toString());
	}
    
    
	/**
	 * This command is used to get a list of all available methods on the templateController.
	 * This service is mostly used by the template-editor so it can keep up with changes easily.
	 * @deprecated
	 */
	
	public String doGetTemplateLogicMethods() throws Exception
	{
		try 
		{
            Method m[] = BasicTemplateController.class.getDeclaredMethods();
            for (int i = 0; i < m.length; i++)
            {
            	Method method = m[i];
	            if(!method.getName().startsWith("set"))
	            {
		            StringBuffer sb = new StringBuffer();
		            sb.append(method.getName());
		            sb.append("(");
		            Class[] parameters = method.getParameterTypes();
		            for (int j = 0; j < parameters.length; j++)
	               	{
	   		            if(j != 0)
	   		        		sb.append(", ");
	   		        		
	   		        	sb.append(parameters[j].getName());	
	   		        }
		            sb.append(")");
	               	
	               	String methodString = sb.toString();
	               	int position = 0;
	               	while(position < this.templateMethods.size())
	            	{
	            		String currentString = (String)this.templateMethods.get(position);
		            	if(currentString.compareToIgnoreCase(methodString) > 0)
		            	{
		            		break;
		            	}
		            	position++;
	            	}
	            	
	            	this.templateMethods.add(position, methodString);		
            
	            }
            }
        }
        catch (Throwable e) 
        {
            System.err.println(e);
        }
		return "templateMethods";
	}
	
	/**
	 * This command is used to get the navigationtitle for a sitenode in a certain language.
	 */
	
	public String doGetPageNavigationTitle() throws Exception
	{
	    DatabaseWrapper dbWrapper = new DatabaseWrapper(CastorDatabaseService.getDatabase());
    	//Database db = CastorDatabaseService.getDatabase();
		
		beginTransaction(dbWrapper.getDatabase());

		try
		{
		    Principal principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
			if(principal == null)
			{
				try
				{
				    Map arguments = new HashMap();
				    arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
				    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());
				    
				    principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments);
				}
				catch(Exception e) 
				{
				    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
				}
			}
			
			this.nodeDeliveryController		   		= NodeDeliveryController.getNodeDeliveryController(getSiteNodeId(), getLanguageId(), getContentId());
			this.integrationDeliveryController 		= IntegrationDeliveryController.getIntegrationDeliveryController(getSiteNodeId(), getLanguageId(), getContentId());
			TemplateController templateController 	= getTemplateController(dbWrapper, getSiteNodeId(), getLanguageId(), getContentId(), getRequest(), (InfoGluePrincipal)principal, false);
			this.navigationTitle = templateController.getPageNavTitle(this.getSiteNodeId());

			closeTransaction(dbWrapper.getDatabase());
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(dbWrapper.getDatabase());
			throw new SystemException(e.getMessage());
		}

		return "navigationTitle";
	}
		
	public List getTemplateMethods()
	{
		return templateMethods;
	}

	public String getNavigationTitle()
	{
		return navigationTitle;
	}

	public String getSourceId()
	{
		return this.sourceId;
	}

	public void setSourceId(String sourceId)
	{
		this.sourceId = sourceId;
	}

    public String getClassName()
    {
        return className;
    }
    public void setClassName(String className)
    {
        this.className = className;
    }
}
