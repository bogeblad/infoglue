/*
 * WebWork, Web Application Framework
 *
 * Distributable under Apache license.
 * See terms of license at opensource.org
 */
package org.infoglue.deliver.portal.dispatcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.Timer;

import webwork.action.Action;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;
import webwork.dispatcher.ActionResult;
import webwork.dispatcher.GenericDispatcher;
import webwork.dispatcher.ServletDispatcher;
import webwork.util.ServletValueStack;

/**
 * Main dispatcher servlet. It works in three phases: first propagate all
 * parameters to the command JavaBean. Second, call execute() to let the
 * JavaBean create the result data. Third, delegate to the JSP that corresponds to
 * the result state that was chosen by the JavaBean.
 *
 * The command JavaBeans can be found in a package prefixed with either
 * of the package names in the comma-separated "packages" servlet init parameter.
 *
 * Modified by Raymond Lai (alpha2_valen@yahoo.com) on 1 Nov 2003:
 * modified wrapRequest() to set the character encoding of HttpServletRequest
 * using the parameter "webwork.i18n.encoding" in webwork.properties.
 *
 */
public class DeliveryServletDispatcher extends ServletDispatcher
{
    private String actionExtension = ".action";
    
    private static URLClassLoader classLoader = null;
    
   /**
    * Service a request.
    * The request is first checked to see if it is a multi-part. If it is, then the request
    * is wrapped so WW will be able to work with the multi-part as if it was a normal request.
    * Next, we will process all actions until an action returns a non-action which is usually
    * a view. For each action in a chain, the action's context will be first set and then the
    * action will be instantiated. Next, the previous action if this action isn't the first in
    * the chain will have its attributes copied to the current action.
    *
    * @param   aRequest
    * @param   aResponse
    * @exception   ServletException
    */
   public void service(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException
   {
		try 
		{
			if(classLoader == null)
			{
				Timer t = new Timer();
				//logger.info("Setting our own classloaders - smart for:" + CmsPropertyHandler.getContextRootPath());
				String extensionBasePath = CmsPropertyHandler.getContextRootPath() + "WEB-INF" + File.separator + "libextensions";
				File extensionBaseFile = new File(extensionBasePath);
				extensionBaseFile.mkdirs();
				File[] extensionFiles = extensionBaseFile.listFiles();
				List<URL> urls = new ArrayList<URL>();
	
				for(File extensionFile : extensionFiles)
				{
					if(extensionFile.getName().endsWith(".jar"))
					{
						//ClassLoaderUtil.addFile(extensionFile.getPath());
						//logger.info("extensionFile:" + extensionFile.getPath());	
					
						URL url = extensionFile.toURL();
						urls.add(url);
					}
				}
				
				URL[] urlsArray = new URL[urls.size()];
				int i = 0;
				for(URL url : urls)
				{
					urlsArray[i] = url;
					i++;
				}
				
				classLoader = new URLClassLoader(urlsArray, this.getClass().getClassLoader());
				//t.printElapsedTime("Creating classloader took");
			}

			Thread.currentThread().setContextClassLoader(classLoader);
			//logger.info("ClassLoader in context for thread:" + Thread.currentThread().getId() + ":" + Thread.currentThread().getContextClassLoader().getClass().getName());
		}
		catch (Throwable t) 
		{
			t.printStackTrace();
		}


       //wrap request if needed
       if(CmsPropertyHandler.getApplicationName().equalsIgnoreCase("cms"))
       {
           String servletPath = (String) aRequest.getAttribute("javax.servlet.include.servlet_path");
           if (servletPath == null)
        	   servletPath = aRequest.getServletPath();

           //logger.info("servletPath:" + servletPath);

           super.service(aRequest, aResponse);
           return;
       }
       
       // Get action
       String servletPath = (String) aRequest.getAttribute("javax.servlet.include.servlet_path");
       if (servletPath == null)
    	   servletPath = aRequest.getServletPath();
      
       //logger.info("servletPath:" + servletPath);
      
       String actionName = getActionName(servletPath);
       GenericDispatcher gd = new GenericDispatcher(actionName, false);
       ActionContext context = gd.prepareContext();
       
       //logger.info("actionName:" + actionName);

       InfoGluePrincipal principal = (InfoGluePrincipal)aRequest.getSession().getAttribute("infogluePrincipal");
       if(principal != null)
    	   aRequest.setAttribute("infoglueRemoteUser", principal.getName());

       aRequest.setAttribute("webwork.request_url", aRequest.getRequestURL());

       ServletActionContext.setContext(aRequest, aResponse, getServletContext(), actionName);

       gd.prepareValueStack();
       ActionResult ar = null;
       try 
       {
           gd.executeAction();
           ar = gd.finish();
       } 
       catch (Throwable e) 
       {
    	   	log.warn("Could not execute action:" + e.getMessage());
          	try 
          	{
              	aResponse.sendError(404, "Could not execute action [" + actionName + "]:" + e.getMessage() + getHTMLErrorMessage(e));
          	} 
          	catch (IOException e1) 
          	{
          	}
       }

       if (ar != null && ar.getActionException() != null) 
       {
    	   log.warn("Could not execute action:" + ar.getActionException().getMessage());
    	   //log.error("Could not execute action", ar.getActionException());
    	   try 
    	   {
    		   aResponse.sendError(500, ar.getActionException().getMessage() + getHTMLErrorMessage(ar.getActionException()));
    	   } 
    	   catch (IOException e1) 
    	   {
    	   }
       }

      // check if no view exists
      if (ar != null && ar.getResult() != null && ar.getView() == null && !ar.getResult().equals(Action.NONE)) {
          try 
          {
              aResponse.sendError(404, "No view for result [" + ar.getResult() + "] exists for action [" + actionName + "]");
          } 
          catch (IOException e) 
          {
          }
      }

      if (ar != null && ar.getView() != null && ar.getActionException() == null) 
      {
          String view = ar.getView().toString();
          log.debug("Result:" + view);

          RequestDispatcher dispatcher = null;
          try 
          {
               dispatcher = aRequest.getRequestDispatcher(view);
          } 
          catch (Throwable e) 
          {
              // Ignore
          }

          if (dispatcher == null)
              throw new ServletException("No presentation file with name '" + view + "' found!");

          try 
          {
              // If we're included, then include the view
              // Otherwise do forward
              // This allow the page to, for example, set content type
              if (aRequest.getAttribute("javax.servlet.include.servlet_path") == null) 
              {
                   aRequest.setAttribute("webwork.view_uri", view);
                   aRequest.setAttribute("webwork.request_uri", aRequest.getRequestURI());
                   aRequest.setAttribute("webwork.request_url", aRequest.getRequestURL());
                   //aRequest.setAttribute("webwork.contextPath",aRequest.getContextPath());

                   dispatcher.forward(aRequest, aResponse);
              } 
              else 
              {
                   //aRequest.setAttribute("webwork.request_uri",aRequest.getAttribute("javax.servlet.include.request_uri"));
                   //aRequest.setAttribute("webwork.contextPath",aRequest.getAttribute("javax.servlet.include.context_path"));
                   dispatcher.include(aRequest, aResponse);
              }
          } 
          catch (IOException e) 
          {
        	  e.printStackTrace();
              throw new ServletException(e);
          } 
          catch (Exception e) 
          {
        	  e.printStackTrace();
        	  throw new ServletException(e);
          } 
          finally 
          {
              // Get last action from stack and and store it in request attribute STACK_HEAD
              // It is then popped from the stack.
              aRequest.setAttribute(STACK_HEAD, ServletValueStack.getStack(aRequest).popValue());
          }
      }

      gd.finalizeContext();
   }

   /**
    * Determine action name by extracting last string and removing
    * extension. (/.../.../Foo.action -> Foo)
    */
   private String getActionName(String name)
   {
      // Get action name ("Foo.action" -> "Foo" action)
      int beginIdx = name.lastIndexOf("/");
      int endIdx = name.lastIndexOf(actionExtension);
      return name.substring((beginIdx == -1 ? 0 : beginIdx + 1),
            endIdx == -1 ? name.length() : endIdx);
   }

}
