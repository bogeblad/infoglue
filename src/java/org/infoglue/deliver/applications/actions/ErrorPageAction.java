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

import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.controllers.kernel.impl.simple.RepositoryDeliveryController;
import org.infoglue.deliver.util.CacheController;


/**
 * This is an error page action. Used to send out the right error codes and the right html
 *
 * @author Mattias Bogeblad
 */

public class ErrorPageAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(ErrorPageAction.class.getName());

    private int responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    
  	private String getErrorUrl(Integer repositoryId) throws Exception 
  	{
  		String errorUrl = CmsPropertyHandler.getErrorUrl();
  		
  		String isErrorPage = getRequest().getParameter("isErrorPage");
  		
  		if(isErrorPage == null || isErrorPage.equals(""))
  		{
			String repositoryErrorUrl = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(repositoryId, "errorUrl");
			if(repositoryErrorUrl != null && !repositoryErrorUrl.equals(""))
			{
				errorUrl = repositoryErrorUrl;
			}
			
			if(errorUrl != null)
				errorUrl = errorUrl + (errorUrl.indexOf("?") > -1 ? "&" : "?") + "isErrorPage=true";
  		}
		
		return errorUrl;
  	}
	
    
    private Set<RepositoryVO> getRepositoryId(HttpServletRequest request) throws ServletException, SystemException, Exception 
    {
        String serverName = request.getServerName();
        String portNumber = new Integer(request.getServerPort()).toString();
        String repositoryName = request.getParameter("repositoryName");
        
        String repCacheKey = "" + serverName + "_" + portNumber + "_" + repositoryName;
        Set<RepositoryVO> repositoryVOList = (Set<RepositoryVO>)CacheController.getCachedObject("NavigationCache", repCacheKey);
        if (repositoryVOList != null) 
        {
            return repositoryVOList;
        }
        
        Set<RepositoryVO> repositories = RepositoryDeliveryController.getRepositoryDeliveryController().getRepositoryVOListFromServerName(serverName, portNumber, repositoryName, request.getRequestURI());
        
        CacheController.cacheObject("NavigationCache", repCacheKey, repositories);

        return repositories;
    }
    
    /**
     * This is the excecute method - it will send the right error codes and also show the right error message.
     */
    
    public String doExecute() throws Exception
    {
    	try
    	{
	        String responseCodeAttribute = (String)this.getRequest().getAttribute("responseCode");
	        if(responseCodeAttribute != null)
	            responseCode = Integer.parseInt(responseCodeAttribute);
	        
	        String responseCodeParameter = (String)this.getRequest().getParameter("responseCode");
	        if(responseCodeParameter != null)
	            responseCode = Integer.parseInt(responseCodeParameter);

	        String requestURI = this.getRequest().getServerName() + this.getRequest().getRequestURI();
	        
	        String errorUrlAttribute = (String)this.getRequest().getAttribute("errorUrl");
	        String errorUrlParameter = (String)this.getRequest().getParameter("errorUrl");
	        
	        Exception e = (Exception)this.getRequest().getAttribute("error");
	        if(e != null)
	        {
	            setError(e, e.getCause());
	        }
	                
	        this.getResponse().setContentType("text/html; charset=UTF-8");
	        this.getResponse().setStatus(responseCode);
	
	        String errorUrl = CmsPropertyHandler.getErrorUrl();
	        if(errorUrlAttribute != null && !errorUrlAttribute.equals(""))
	        {
	        	errorUrl = errorUrlAttribute;
	        }
	        else if(errorUrlParameter != null && !errorUrlParameter.equals(""))
	        {
	        	errorUrl = errorUrlParameter;
	        }

	        Set<RepositoryVO> repositoryVOList = getRepositoryId(this.getRequest());
	        if(repositoryVOList != null && repositoryVOList.size() > 0)
	        {
	        	RepositoryVO repositoryVO = (RepositoryVO)repositoryVOList.toArray()[0];
	        	String localErrorUrl = getErrorUrl(repositoryVO.getId());
	        	if(localErrorUrl != null)
	        		errorUrl = localErrorUrl;
	        }
	        
	        if(errorUrl == null || errorUrl.indexOf("@errorUrl@") > -1)
	        {
	            logger.error("No valid error url was defined:" + errorUrl + ". You should fix this. Defaulting to /error.jsp");
		       	errorUrl = "/error.jsp";
	        }
	        
	        if(errorUrl != null && errorUrl.indexOf("@errorUrl@") == -1)
	        {
	            if(errorUrl.indexOf("http") > -1)
	                this.getResponse().sendRedirect(errorUrl);
	            else
	            {
	            	try
	            	{
		                RequestDispatcher dispatch = this.getRequest().getRequestDispatcher(errorUrl);
		                this.getRequest().setAttribute("error", e);
		                //dispatch.forward(this.getRequest(), this.getResponse());
		                dispatch.include(this.getRequest(), this.getResponse());
	            	}
	            	catch(Exception e2)
	            	{
	            		e2.printStackTrace();
	                    return SUCCESS;            		
	            	}
	            }
	            
	            return NONE;
	        }
	        else
	        {
	            logger.error("No valid error url was defined:" + errorUrl + ". You should fix this.");
	        	return SUCCESS;
	        }
	    }
    	catch(Throwable t)
    	{
    		logger.error("Error executing ErrorPage action:" + t.getMessage());
    		if(logger.isDebugEnabled())
        		logger.debug("Error executing ErrorPage action:" + t.getMessage(), t);
    			
    		return SUCCESS;
    	}
    }

    /**
     * This is the busy method - it will send the right error codes and also show the right error message.
     */
    
    public String doBusy() throws Exception
    {
    	String responseCodeAttribute = (String)this.getRequest().getAttribute("responseCode");
        if(responseCodeAttribute != null)
            responseCode = Integer.parseInt(responseCodeAttribute);
        
        String responseCodeParameter = (String)this.getRequest().getParameter("responseCode");
        if(responseCodeParameter != null)
            responseCode = Integer.parseInt(responseCodeParameter);

        Exception e = (Exception)this.getRequest().getAttribute("error");
        if(e != null)
        {
            setError(e, e.getCause());
        }
                
        this.getResponse().setContentType("text/html; charset=UTF-8");
        this.getResponse().setStatus(responseCode);

        String errorUrl = CmsPropertyHandler.getErrorBusyUrl();
        if(errorUrl != null && errorUrl.indexOf("@errorBusyUrl@") == -1)
        {
            if(errorUrl.indexOf("http") > -1)
                this.getResponse().sendRedirect(errorUrl);
            else
            {
                RequestDispatcher dispatch = this.getRequest().getRequestDispatcher(errorUrl);
                this.getRequest().setAttribute("error", e);
                //dispatch.forward(this.getRequest(), this.getResponse());
                dispatch.include(this.getRequest(), this.getResponse());
            }
            
            return NONE;
        }
        else
            return SUCCESS;
    }

    public int getResponseCode()
    {
        return responseCode;
    }
}
