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
package org.infoglue.deliver.taglib.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.log4j.Logger;
import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * This class implements the &lt;common:urlBuilder&gt; tag, which creates an url
 * from a base url (user supplied or taken from the request), 
 * a query string (user supplied ot taken from the reuest) and
 * any number of parameters specified using nested &lt;common:parameter&gt; tags.
 */
public class URLTag extends TemplateControllerTag 
{

    private final static Logger logger = Logger.getLogger(URLTag.class.getName());

    /**
	 * 
	 */
	private static final long serialVersionUID = 4433903132736259601L;

	/**
	 * The universal version identifier.
	 */

	/**
	 * The base url to use when constructing the url.
	 */
	private String baseURL;
	
	/**
	 * The query to use when constructing the url.
	 */
	private String query;
	
	/**
	 * The parameter names to exclude from the quertyString. Commasseperated string.
	 */
	private String excludedQueryStringParameters;
	
	/**
	 * The parameter that controls if the full servername etc should be included in the base url.
	 */
	private boolean fullBaseUrl = false;
	
	/**
	 * The parameters to use when constructing the url.
	 */
	private List parameters; // type: <String>, format: <name>=<value>
	
	/**
	 * The names of all parameters added.
	 */
	private Map parameterNames; // <type>: <String>-><String>
	
	/**
	 * Tells the tag if the nice uri option should be disabled if enabled.
	 */
	
	private boolean disableNiceURI = false;
	
	/**
	 * Tells if parameter should be allowed even though there are parameters with that name
	 */
	private boolean allowMultipleArguments = false;

	/**
	 * Tells if parameter should be allowed even though there are parameters with that name
	 */
	private boolean includeCurrentQueryString = true;

	/**
	 * Default constructor.
	 */
	public URLTag()
	{
		super();
	}

	/**
	 * Initializes the parameters to make it accessible for the children tags (if any).
	 * 
	 * @return indication of whether to evaluate the body or not.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doStartTag() throws JspException 
	{
		return EVAL_BODY_INCLUDE;
	}
	
	/**
	 * Generates the url and either sets the result attribute or writes the url
	 * to the output stream. 
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		addQueryParameters();
		produceResult(generateURL());
		
		this.baseURL = null;
		this.query = null;
		this.excludedQueryStringParameters = null;
		this.fullBaseUrl = false;
		this.parameters = null;
		this.parameterNames = null;
		this.disableNiceURI = false;
		this.allowMultipleArguments = false;
		this.includeCurrentQueryString = true;
		
		return EVAL_PAGE;
    }

	/**
	 * Returns the parameters to use when constructing the url.
	 * 
	 * @return the parameters to use when constructing the url.
	 */
	private List getParameters()
	{
		if(parameters == null)
		{
			parameters = new ArrayList();
		}
		return parameters;
	}
	
	/**
	 * Returns the name of all parameters that has been added.
	 * 
	 * @return the name of all parameters that has been added.
	 */
	private Map getParameterNames()
	{
		if(parameterNames == null)
		{
			parameterNames = new HashMap();
		}
		return parameterNames;
	}
	
	/**
	 * 
	 */
	protected final void addParameter(final String name, final String value)
	{
        getParameters().add(name + "=" + value);
        getParameterNames().put(name, name);
	}
	
	/**
	 * Returns the url attribute if present; otherwise the url is taken from the request.
	 * 
	 * @return the url attribute if present; otherwise the url is taken from the request.
	 */
	private String getBaseURL()
	{
	    String newBaseUrl = "";
	    try
	    {
    	    logger.info("fullBaseUrl:" + fullBaseUrl);
	        if(this.fullBaseUrl)
	        {
	            int indexOfProtocol = getRequest().getRequestURL().indexOf("://");
	            int indexFirstSlash = getRequest().getRequestURL().indexOf("/", indexOfProtocol + 3);
	            String base = null;
	            if(indexFirstSlash > -1)
	                base = getRequest().getRequestURL().substring(0, indexFirstSlash);
	            else
	                base = getRequest().getRequestURL().substring(0);
	            
	            String currentPageUrl = this.getController().getCurrentPageUrl();
	            
	            if(currentPageUrl != null)
	            {
			        int cidIndex = currentPageUrl.indexOf("cid");
			        if(excludedQueryStringParameters != null && (excludedQueryStringParameters.indexOf("contentId") > -1 || excludedQueryStringParameters.indexOf("cid") > -1) && cidIndex > -1)
			        {
			        	int lastIndexOf = currentPageUrl.lastIndexOf("/", cidIndex);
			        	int nextIndexOf = currentPageUrl.indexOf("/", cidIndex);
	
			        	currentPageUrl = currentPageUrl.substring(0, lastIndexOf);
			        	if(nextIndexOf > -1)
			        		currentPageUrl = currentPageUrl + currentPageUrl.substring(nextIndexOf);
			        }
	
		            currentPageUrl = currentPageUrl.split("\\?")[0];
		            newBaseUrl = (baseURL == null) ? base + currentPageUrl : baseURL;	        
	            }
	            else
	            {
	            	logger.warn("How can this happen:" + this.getController().getOriginalFullURL());
	            	newBaseUrl = (baseURL == null) ? base : baseURL;
	            }
	        }
		    else
		    {
		        String currentPageUrl = this.getController().getCurrentPageUrl().toString();
		        
		        int cidIndex = currentPageUrl.indexOf("cid");
		        if(excludedQueryStringParameters != null && (excludedQueryStringParameters.indexOf("contentId") > -1 || excludedQueryStringParameters.indexOf("cid") > -1) && cidIndex > -1)
		        {
		        	int lastIndexOf = currentPageUrl.lastIndexOf("/", cidIndex);
		        	int nextIndexOf = currentPageUrl.indexOf("/", cidIndex);

		        	currentPageUrl = currentPageUrl.substring(0, lastIndexOf);
		        	if(nextIndexOf > -1)
		        		currentPageUrl = currentPageUrl + currentPageUrl.substring(nextIndexOf);
		        }
		        	
		        currentPageUrl = currentPageUrl.split("\\?")[0];
	            newBaseUrl = (baseURL == null) ? currentPageUrl : baseURL;	        
		    }
	    }
	    catch(Exception e)
	    {
	        logger.warn("Error getting url:" + e.getMessage(), e);
	        newBaseUrl = (baseURL == null) ? getRequest().getRequestURL().toString() : baseURL;
	    }
	    logger.info("newBaseUrl:" + newBaseUrl);
	    return newBaseUrl;
	}
	
	/**
	 * Returns the query attribute if present; otherwise the query is taken from the request.
	 * 
	 * @return the query attribute if present; otherwise the query is taken from the request.
	 */
	private String getQuery()
	{
		String q = null;
		if(includeCurrentQueryString)
			q = (query == null) ? getRequest().getQueryString() : query;
		else
			q = query;
		
		if(q != null && (q.startsWith("?") || q.startsWith("&")))
		{
			return q.substring(1);
		}
		return q;
	}
	
	/**
	 * Returns the (http) request object.
	 * 
	 * @return the (http) request object.
	 */
	private final HttpServletRequest getRequest()
	{
		return (HttpServletRequest) pageContext.getRequest();
	}
	
	/**
	 * Adds the parameter from the query string to the parameters to use
	 * when constructing the url. If a parameter present in the query already
	 * exists in the parameters, the query parameter will be skipped.
	 * 
	 * @throws JspException if the format of the query string is illegal.
	 */
	private void addQueryParameters() throws JspException
	{
		if(getQuery() != null)
		{
			for(final StringTokenizer st = new StringTokenizer(getQuery(), "&"); st.hasMoreTokens(); )
			{
				final String token = st.nextToken();
				final StringTokenizer parameter = new StringTokenizer(token, "=");
				if(parameter.countTokens() == 0 || parameter.countTokens() > 2)
				{
					throw new JspTagException("Illegal query parameter [" + token + "].");
				}
				final String name  = parameter.nextToken();
				final String value = parameter.hasMoreTokens() ? parameter.nextToken() : "";
				if(allowMultipleArguments || !getParameterNames().containsKey(name))
				{
				    if(excludedQueryStringParameters == null || excludedQueryStringParameters.indexOf(name) == -1)
				        addParameter(name, value);
				}
			}
		}
	}
	
	/**
	 * Generates the url string.
	 * 
	 * @return the url.
	 */
	public String generateURL() 
	{
		if(!getParameters().isEmpty()) 
		{
			StringBuffer sb = new StringBuffer();
			for(Iterator i = getParameters().iterator(); i.hasNext(); ) 
			{
				String parameter = i.next().toString();
				sb.append(parameter + (i.hasNext() ? "&" : ""));
			}
			if(getBaseURL().indexOf("?") > -1)
			    return getBaseURL() + (sb.toString().length() > 0 ? "&" + sb.toString() : "");
			else
			    return getBaseURL() + (sb.toString().length() > 0 ? "?" + sb.toString() : "");
		}
		return getBaseURL();
	}

	/**
	 * Sets the base url attribute.
	 * 
	 * @param baseURL the base url to use.
	 * @throws JspException if an error occurs while evaluating base url parameter.
	 */
	public void setBaseURL(final String baseURL) throws JspException
	{
		this.baseURL = evaluateString("url", "baseURL", baseURL);
	}

	/**
	 * Sets the query attribute.
	 * 
	 * @param query the query to use.
	 * @throws JspException if an error occurs while evaluating query parameter.
	 */
	public void setQuery(final String query) throws JspException
	{
		this.query = evaluateString("url", "query", query);
	}
	
    public void setExcludedQueryStringParameters(String excludedQueryStringParameters) throws JspException
    {
        this.excludedQueryStringParameters = evaluateString("url", "excludedQueryStringParameters", excludedQueryStringParameters);
    }

    public void setFullBaseUrl(boolean fullBaseUrl)
    {
        this.fullBaseUrl = fullBaseUrl;
    }
    
    public void setDisableNiceURI(boolean disableNiceURI)
    {
        this.disableNiceURI = disableNiceURI;
    }

    public void setAllowMultipleArguments(boolean allowMultipleArguments)
    {
        this.allowMultipleArguments = allowMultipleArguments;
    }
    
    public void setIncludeCurrentQueryString(boolean includeCurrentQueryString)
    {
        this.includeCurrentQueryString = includeCurrentQueryString;
    }
    
}
