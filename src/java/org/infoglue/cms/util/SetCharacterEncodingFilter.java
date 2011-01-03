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

package org.infoglue.cms.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.management.LanguageVO;


public class SetCharacterEncodingFilter implements Filter 
{
    public final static Logger logger = Logger.getLogger(SetCharacterEncodingFilter.class.getName());

    /**
     * The default character encoding to set for requests that pass through
     * this filter.
     */
    public static String defaultEncoding = null;


    /**
     * The default character encoding to set for requests that pass through
     * this filter.
     */
    protected String encoding = null;


    /**
     * The filter configuration object we are associated with.  If this value
     * is null, this filter instance is not currently configured.
     */
    protected FilterConfig filterConfig = null;


    /**
     * Should a character encoding specified by the client be ignored?
     */
    protected boolean ignore = true;



    /**
     * Take this filter out of service.
     */
    public void destroy() {

        this.encoding = null;
        this.filterConfig = null;

    }


    /**
     * Select and set (if specified) the character encoding to be used to
     * interpret request parameters for this request.
     *
     * @param request The servlet request we are processing
     * @param result The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException 
    {
        // Conditionally select and set the character encoding to be used
    	String referer = ((HttpServletRequest)request).getHeader("referer");
        if(referer != null && referer.length() > 0 && referer.indexOf("ViewPage!renderDecoratedPage.action") > -1)
        {
            try
            {
	            int startIndex = referer.indexOf("&languageId=");
	            if(startIndex > -1)
	            {
	                int endIndex = referer.indexOf("&", startIndex + 12);
	                String languageId = referer.substring(startIndex + 12);
	                if(endIndex != -1)
	                	languageId = referer.substring(startIndex + 12, endIndex);
		            
	                if(languageId != null && !languageId.equals(""))
	                {
		                LanguageVO languageVO = LanguageController.getController().getLanguageVOWithId(new Integer(languageId));
	
			            if(logger.isInfoEnabled())
			            	logger.info("encoding decorated:" + languageVO.getCharset());
	
			            request.setCharacterEncoding(languageVO.getCharset());
	                }
	            }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (ignore || (request.getCharacterEncoding() == null)) 
        {
            String encoding = selectEncoding(request);
            if(logger.isInfoEnabled())
            	logger.info("encoding normal:" + encoding);
            
            if (encoding != null)
                request.setCharacterEncoding(encoding);
        }
    	
        // Pass control on to the next filter
        chain.doFilter(request, response);

    }


    /**
     * Place this filter into service.
     *
     * @param filterConfig The filter configuration object
     */
    public void init(FilterConfig filterConfig) throws ServletException 
    {

		this.filterConfig = filterConfig;
        this.encoding = filterConfig.getInitParameter("encoding");
        if(this.encoding == null || this.encoding.equals("") || this.encoding.indexOf("@") > -1)
        {
            System.out.println("Encoding in web.xml was not set:" + encoding);
        	if(this.encoding.indexOf("@deliverInputCharacterEncoding@") > -1)
        		this.encoding = "ISO-8859-1";
        	else
        		this.encoding = "UTF-8";
        	
            System.out.println("Defaulting to standard.");
        }
        
        defaultEncoding = this.encoding;
        
        String value = filterConfig.getInitParameter("ignore");
        if (value == null)
            this.ignore = true;
        else if (value.equalsIgnoreCase("true"))
            this.ignore = true;
        else if (value.equalsIgnoreCase("yes"))
            this.ignore = true;
        else
            this.ignore = false;
    }


    /**
     * Select an appropriate character encoding to be used, based on the
     * characteristics of the current request and/or filter initialization
     * parameters.  If no character encoding should be set, return
     * <code>null</code>.
     * <p>
     * The default implementation unconditionally returns the value configured
     * by the <strong>encoding</strong> initialization parameter for this
     * filter.
     *
     * @param request The servlet request we are processing
     */
    protected String selectEncoding(ServletRequest request) 
    {
    	String inputCharacterEncoding = CmsPropertyHandler.getInputCharacterEncoding(this.encoding);
    	
    	if(logger.isInfoEnabled())
        	logger.info("inputCharacterEncoding:" + inputCharacterEncoding);
    	
    	if(inputCharacterEncoding != null && !inputCharacterEncoding.equals("") && !inputCharacterEncoding.equalsIgnoreCase("@inputCharacterEncoding@"))
    		return inputCharacterEncoding;
    	else
    		return (this.encoding);
    }

}
