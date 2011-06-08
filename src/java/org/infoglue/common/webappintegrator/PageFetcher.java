package org.infoglue.common.webappintegrator;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.deliver.util.HttpHelper;

/**
 *
 * This is a simple text mode application that demonstrates
 * how to use the Jakarta HttpClient API.
 *
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author Ortwin Glück
 */
public class PageFetcher
{
    private final static Logger logger = Logger.getLogger(PageFetcher.class.getName());

	public static void main(String[] args) throws Exception
    {				  
		/*
    	Map<String,String> parameters = new HashMap<String,String>();
    	parameters.put("ct_orig_uri","https://forum.tewss.telia.se");
    	parameters.put("ct_error_uri","http://www.telia.se/privat/security/loginpage.do");
    	parameters.put("SCAUTHMETHOD","basic");
    	parameters.put("user","mattiasbogeblad");
    	parameters.put("password","blader22");
    	
    	String url = "https://access.tewss.telia.se/ssotcwss/login";
    	new PageFetcher().fetchPage(url, "post", new HashMap<String,String>(), new HashMap<String,String>(), parameters, new HashMap<String,String>(), new HashMap<String,String>(), new HashMap<String,String>(), new ArrayList<String>());
		*/
     }
	
	public String fetchPage(String url, String httpMethod, String proxyHost, Integer proxyPort, Map<String,String> cookies, Map<String,String> inputRequestHeaders, Map<String,String> requestParameters, Map<String,String> returnCookies, Map<String,String> returnHeaders, Map<String,String> statusData, List<String> blockedParameters) throws Exception
	{
		logger.info("Fetching page on:" + url);
		
        HttpState initialState = new HttpState();
		for(Entry<String,String> cookie : cookies.entrySet())
		{
			logger.info("Cookie: " + cookie.getKey() + "=" + cookie.getValue());
	        Cookie mycookie = new Cookie(".telia.se", cookie.getKey(), cookie.getValue(), "/", null, false);
	        initialState.addCookie(mycookie);
		}
    
		Credentials creds = null;
        //if (args.length >= 3) {
		//    creds = new UsernamePasswordCredentials(args[1], args[2]);
		//}

        HttpClient client = new HttpClient();

		client.setState(initialState);
        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10000);

        if(proxyHost != null && !proxyHost.equals(""))
        {
	        HostConfiguration config = client.getHostConfiguration();
	        config.setProxy(proxyHost, (proxyPort == null ? 80 : proxyPort));
        }
        
        if (creds != null) 
        {
            client.getState().setCredentials(AuthScope.ANY, creds);
        }

        logger.info("URL TO CALL with "  + httpMethod + ":" + url);
        HttpMethod method = null;
        url = URLDecoder.decode(url, "utf-8");
        logger.info("URL TO CALL with "  + httpMethod + ":" + url);
        url = new URI(url).getEscapedURI();
        logger.info("URL TO CALL with "  + httpMethod + ":" + url);
        if(httpMethod.equalsIgnoreCase("post"))
        	method = new PostMethod(url);
        else
        	method = new GetMethod(url);
        
        for(Entry<String,String> requestParameter : requestParameters.entrySet())
		{
        	logger.info("BlockedParameters:" + blockedParameters);
			if(!blockedParameters.contains(requestParameter.getKey()))
			{
				if(method instanceof PostMethod)
				{
					logger.info("Parameter in post: " + requestParameter.getKey() + "=" + requestParameter.getValue());
					((PostMethod)method).addParameter(requestParameter.getKey(), requestParameter.getValue());
				}
			}
			else
				logger.info("Skipping:" + requestParameter.getKey());
		}
        
        /*
        TODO - we must have somewhere to do this controllable
		for(Entry<String,String> inputRequestHeader : inputRequestHeaders.entrySet())
		{
			logger.info("Header: " + inputRequestHeader.getKey() + "=" + inputRequestHeader.getValue());
			method.addRequestHeader(inputRequestHeader.getKey(), inputRequestHeader.getValue());
		}
		*/
		
        //execute the method
        String responseBody = null;
        
        client.executeMethod(method);            
        responseBody = method.getResponseBodyAsString();
        
        //write out the request headers
        logger.info("*** Request ***");
        logger.info("Request Path: " + method.getPath());
        logger.info("Request Query: " + method.getQueryString());
        Header[] requestHeaders = method.getRequestHeaders();
        for (int i=0; i<requestHeaders.length; i++){
        	logger.info(requestHeaders[i]);
        }

        //write out the response headers
        logger.info("*** Response ***");
        logger.info("Status Line: " + method.getStatusLine());
        Header[] responseHeaders = method.getResponseHeaders();
        for (int i=0; i<responseHeaders.length; i++)
        {
        	returnHeaders.put(responseHeaders[i].getName(), responseHeaders[i].getValue());
        	logger.info(responseHeaders[i]);
        }
        
        Cookie[] returnCookiesArray = client.getState().getCookies();
        // Display the cookies
        logger.info("Present cookies: ");
        for (int i = 0; i < returnCookiesArray.length; i++) 
        {
            logger.info(" - " + returnCookiesArray[i].toExternalForm());
            returnCookies.put(returnCookiesArray[i].getName(), returnCookiesArray[i].getValue());
        }
        
        //write out the response body
        //logger.info("*** Response Body ***");
        //logger.info(responseBody);

        logger.info("****************************");
        logger.info("* Looking for redirect.... *");
        logger.info("****************************");

        String redirectLocation;
        Header locationHeader = method.getResponseHeader("location");
        if (locationHeader != null) 
        {
            redirectLocation = locationHeader.getValue();
            logger.info("redirectLocation:" + redirectLocation);
            
            String fullUrl = addParameters(redirectLocation, requestParameters);
            logger.info("fullUrl:" + fullUrl);
            
            //if(httpMethod.equalsIgnoreCase("post"))
            //	method = new PostMethod(fullUrl);
            //else
            	method = new GetMethod(fullUrl);
            
            for(Entry<String,String> requestParameter : requestParameters.entrySet())
    		{
    			if(requestParameter.getKey().equals("ct_error_uri") || 
    			   requestParameter.getKey().equals("ct_orig_uri") || 
    			   requestParameter.getKey().equals("SCAUTHMETHOD") ||
    			   requestParameter.getKey().equals("user") ||
    			   requestParameter.getKey().equals("password"))
    			{
    				if(method instanceof PostMethod)
    				{
    					logger.info("Parameter: " + requestParameter.getKey() + "=" + requestParameter.getValue());
    					((PostMethod)method).addParameter(requestParameter.getKey(), requestParameter.getValue());
    				}
    			}
    			else
    				logger.info("Skipping:" + requestParameter.getKey());
    		}
            
            client.executeMethod(method);
            responseBody = method.getResponseBodyAsString();
            
            //write out the request headers
            logger.info("*** Request ***");
            logger.info("Request Path: " + method.getPath());
            logger.info("Request Query: " + method.getQueryString());
            requestHeaders = method.getRequestHeaders();
            for (int i=0; i<requestHeaders.length; i++){
            	logger.info(requestHeaders[i]);
            }

            //write out the response headers
            logger.info("*** Response ***");
            logger.info("Status Line: " + method.getStatusLine());
            responseHeaders = method.getResponseHeaders();
            for (int i=0; i<responseHeaders.length; i++)
            {
            	returnHeaders.put(responseHeaders[i].getName(), responseHeaders[i].getValue());
            	logger.info(responseHeaders[i]);
            }
            
            returnCookiesArray = client.getState().getCookies();
            // Display the cookies
            logger.info("Present cookies: ");
            for (int i = 0; i < returnCookiesArray.length; i++) 
            {
                logger.info(" - " + returnCookiesArray[i].toExternalForm());
                returnCookies.put(returnCookiesArray[i].getName(), returnCookiesArray[i].getValue());
            }

            //write out the response body
            logger.info("*** Response Body ***");
            logger.info(responseBody);
        }
        
        //clean up the connection resources
        method.releaseConnection();
        
        return responseBody;
    }

	private String addParameters(String redirectLocation, Map<String, String> requestParameters) throws Exception 
	{
		String params = new HttpHelper().toEncodedString(requestParameters, "utf-8");
		String url = redirectLocation + (redirectLocation.indexOf("?") > -1 ? "&" : "?") + params;
			
		return url;
	}
}
