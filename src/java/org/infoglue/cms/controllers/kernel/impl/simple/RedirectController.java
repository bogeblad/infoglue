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

package org.infoglue.cms.controllers.kernel.impl.simple;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.Redirect;
import org.infoglue.cms.entities.management.RedirectVO;
import org.infoglue.cms.entities.management.impl.simple.RedirectImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.CacheController;


/**
 * @author Mattias Bogeblad
 */

public class RedirectController extends BaseController
{
    private final static Logger logger = Logger.getLogger(RedirectController.class.getName());

	/**
	 * Factory method
	 */

	public static RedirectController getController()
	{
		return new RedirectController();
	}

    public RedirectVO getRedirectVOWithId(Integer redirectId) throws SystemException, Bug
    {
		return (RedirectVO) getVOWithId(RedirectImpl.class, redirectId);
    }

    public Redirect getRedirectWithId(Integer redirectId, Database db) throws SystemException, Bug
    {
		return (Redirect) getObjectWithId(RedirectImpl.class, redirectId, db);
    }

    public List getRedirectVOList() throws SystemException, Bug
    {
		List redirectVOList = getAllVOObjects(RedirectImpl.class, "redirectId");

		return redirectVOList;
    }

    public List getRedirectVOList(Database db) throws SystemException, Bug
    {
		List redirectVOList = getAllVOObjects(RedirectImpl.class, "redirectId", db);

		return redirectVOList;
    }

    public RedirectVO create(RedirectVO redirectVO) throws ConstraintException, SystemException
    {
        Redirect redirect = new RedirectImpl();
        redirect.setValueObject(redirectVO);
        redirect = (Redirect) createEntity(redirect);
        return redirect.getValueObject();
    }

    public void delete(RedirectVO redirectVO) throws ConstraintException, SystemException
    {
    	deleteEntity(RedirectImpl.class, redirectVO.getRedirectId());
    }

    public RedirectVO update(RedirectVO redirectVO) throws ConstraintException, SystemException
    {
    	return (RedirectVO) updateEntity(RedirectImpl.class, redirectVO);
    }
    
    /**
     * This method checks if there is a redirect that should be used instead.
     * @param requestURI
     * @throws Exception
     */
    
    public String getRedirectUrl(HttpServletRequest request) throws Exception
    {
        try
        {
            String requestURL = request.getRequestURL().toString();
            int indexOfProtocol = requestURL.indexOf("://");
            int indexFirstSlash = requestURL.indexOf("/", indexOfProtocol + 3);
            String base = requestURL.substring(0, indexFirstSlash);

            logger.info("base:" + base);

            String requestURI = base + getContextURI(request);
            logger.info("requestURI:" + requestURI);
            logger.info("full requestURI:" + requestURI);
            
            Collection cachedRedirects = (Collection)CacheController.getCachedObject("redirectCache", "allRedirects");
            if(cachedRedirects == null)
            {
                cachedRedirects = RedirectController.getController().getRedirectVOList();
                CacheController.cacheObject("redirectCache", "allRedirects", cachedRedirects);
            }
            
			if(logger.isInfoEnabled())
	        	logger.info("requestURI before decoding:" + requestURI);
            
			requestURI = URLDecoder.decode(requestURI, CmsPropertyHandler.getURIEncoding());
			if(logger.isInfoEnabled())
	        	logger.info("requestURI after decoding:" + requestURI);
			
			String fromEncoding = CmsPropertyHandler.getURIEncoding();
			String toEncoding = "utf-8";
			String testRequestURI = new String(requestURI.getBytes(fromEncoding), toEncoding);
			if(testRequestURI.indexOf((char)65533) == -1)
				requestURI = testRequestURI;

			if(logger.isInfoEnabled())
	        	logger.info("requestURI after redecoding:" + requestURI);
			//System.out.println("requestURI:" + requestURI);
                        
            Iterator redirectsIterator = cachedRedirects.iterator();
            while(redirectsIterator.hasNext())
            {
                RedirectVO redirect = (RedirectVO)redirectsIterator.next(); 
                //System.out.println("url:" + redirect.getUrl());
                if(logger.isInfoEnabled())
                	logger.info("url:" + redirect.getUrl());
                boolean matches = false;
                if(redirect.getUrl().startsWith(".*"))
                {
                   if(requestURI.indexOf(redirect.getUrl().substring(2)) > -1)
                       matches = true;
                }
                else if(requestURI.startsWith(redirect.getUrl()))
                {
                    matches = true;
                }
                
                //if(requestURI.startsWith(redirect.getUrl()))
                if(matches)
                {
                    logger.info("redirectUrl:" + redirect.getRedirectUrl());
                    String remainingURI = requestURI.replaceAll(redirect.getUrl(), "");
                    logger.info("remainingURI:" + remainingURI);
                    return redirect.getRedirectUrl() + remainingURI + (request.getQueryString() != null && request.getQueryString().length() > 0 ? "?" + request.getQueryString() : "");
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new SystemException("An error occurred when looking for page:" + e.getMessage());
        }
        
        return null;
    }
    
    private String getContextRelativeURI(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && requestURI.length() > 0) {
            requestURI = requestURI.substring(contextPath.length(), requestURI.length());
        }
        if (requestURI.length() == 0)
            return "/";
        return requestURI;
    }

    private String getContextURI(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.length() == 0)
            return "/";
        return requestURI;
    }


	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new ContentTypeDefinitionVO();
	}
}
