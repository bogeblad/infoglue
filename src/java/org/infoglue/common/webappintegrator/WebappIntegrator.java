package org.infoglue.common.webappintegrator;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebappIntegrator 
{
    private final static Logger logger = Logger.getLogger(WebappIntegrator.class.getName());

	//Proxy-part
	private String proxyHost = null;
	private Integer proxyPort = null;
	
	private String urlToIntegrate = null;
	private Connection.Method method = Connection.Method.GET;
	private String referrer = null;
	private String userAgent = null;
	private Integer timeout = null;
	private Map<String,String> requestProperties = new HashMap();
	private Map<String,String> requestParameters = new HashMap();
	private Map<String,String> cookies = new HashMap();
	
	private String currentBaseUrl = null;
	private String elementSelector = null;
	
	public WebappIntegrator()
	{
		
	}
	
	public void setUrlToIntegrate(String urlToIntegrate) 
	{
		this.urlToIntegrate = urlToIntegrate;
	}

	public void setCurrentBaseUrl(String currentBaseUrl) 
	{
		this.currentBaseUrl = currentBaseUrl;
	}
	
	public void setElementSelector(String elementSelector) 
	{
		this.elementSelector = elementSelector;
	}
	
	public void setProxyHost(String proxyHost) 
	{
		this.proxyHost = proxyHost;
	}

	public void setProxyPort(Integer proxyPort) 
	{
		this.proxyPort = proxyPort;
	}

	public String integrate(Map<String,String> returnCookies, Map<String,String> returnHeaders, Map<String,String> statusData, List<String> blockedParameters, String hrefExclusionRegexp, String linkExclusionRegexp, String srcExclusionRegexp) throws Exception
	{
		String responseBody = new PageFetcher().fetchPage(this.urlToIntegrate, method.name(), this.proxyHost, this.proxyPort, cookies, requestProperties, requestParameters, returnCookies, returnHeaders, statusData, blockedParameters);
		
		String baseURI = this.urlToIntegrate;
		if(baseURI.indexOf("?") > -1)
			baseURI = baseURI.substring(0, baseURI.indexOf("?"));
		
		Document doc = Jsoup.parse(responseBody, baseURI);
		//Document doc = Jsoup.parse(responseBody);
		
		String title = doc.title();
		logger.info("title:" + title);
		logger.info("elementSelector:" + elementSelector);
		Element sourceElement = doc.select(elementSelector).first();
		if(sourceElement == null)
			sourceElement = doc.select("#pageContent").first();

		if(sourceElement == null)
			sourceElement = doc.body();

		if(sourceElement != null)
		{	
			Elements links = sourceElement.select("a[href]");
			Elements forms = sourceElement.select("form");
	        Elements media = doc.select("[src]");
	        Elements imports = doc.select("link[href]");

	        for (Element link : links) 
	        {
	        	String href = link.attr("href");
	        	String oldUrl = link.attr("abs:href");
	        	if(!href.matches(hrefExclusionRegexp) && href.indexOf("javascript:") == -1 && oldUrl != null)
	        	{
		        	String newUrl = currentBaseUrl + (currentBaseUrl.indexOf("?") > -1 ? "&" : "?") + "proxyUrl=" + URLEncoder.encode(oldUrl, "utf-8");
		        	link.attr("href", newUrl);
	        	}
	        }
	
	        for (Element src : media) 
	        {
	        	String oldSrc = src.attr("abs:src");
	        	if(!oldSrc.matches(srcExclusionRegexp) && oldSrc != null)
	        	{
	        		logger.info("Changing to oldSrc:" + oldSrc);
		        	src.attr("src", oldSrc);
	        	}
	        }
	        
	        for (Element link : imports) 
	        {
	        	String oldHref = link.attr("abs:href");
	        	if(!oldHref.matches(linkExclusionRegexp) && oldHref != null)
	        	{
		        	logger.info("Changing to oldHref:" + oldHref);
		        	link.attr("href", oldHref);
	        	}
	        }
	        
	        for (Element form : forms) 
	        {
	        	String oldAction = form.attr("abs:action");
	        	logger.info("oldAction:" + oldAction);
	        	if(oldAction == null || oldAction.equals(""))
	        	{
	        		oldAction = this.urlToIntegrate;
		        	logger.info("oldAction:" + oldAction);
		        	String newAction = currentBaseUrl + (currentBaseUrl.indexOf("?") > -1 ? "&" : "?") + "proxyUrl=" + oldAction;
		        	form.attr("action", newAction);
	        	}
	        	else
	        	{
	        		logger.info("oldAction:" + oldAction);
		        	//String newAction = currentBaseUrl + (currentBaseUrl.indexOf("?") > -1 ? "&" : "?") + "proxyUrl=" + URLEncoder.encode(URLEncoder.encode(oldAction, "utf-8"),"utf-8");
		        	String newAction = currentBaseUrl + (currentBaseUrl.indexOf("?") > -1 ? "&" : "?") + "proxyUrl=" + URLEncoder.encode(oldAction, "utf-8");
		        	form.attr("action", newAction);
	        	}
	        }
	
	        return sourceElement.html();
		}
		else
		{
			return doc.body().html();
		}
	}
	
	public void setMethod(String method) 
	{
		if(method != null && method.equalsIgnoreCase("post"))
			this.method = Connection.Method.POST;
	}

	public void setReferrer(String referrer) 
	{
		this.referrer = referrer;
	}

	public void setUserAgent(String userAgent) 
	{
		this.userAgent = userAgent;
	}

	public void setRequestProperties(Map<String, String> requestProperties) 
	{
		this.requestProperties = requestProperties;
	}

	public void setRequestParameters(Map<String, String> requestParameters) 
	{
		this.requestParameters = requestParameters;
	}

	public void setCookies(Map<String, String> cookies) 
	{
		this.cookies = cookies;
	}

	public void setTimeout(Integer timeout) 
	{
		this.timeout = timeout;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		WebappIntegrator wi = new WebappIntegrator();
		wi.setCurrentBaseUrl("http://localhost:8080/infoglueDeliverWorking/ViewPage.action?siteNodeId=3");
		wi.setUrlToIntegrate("https://forum.tewss.telia.se");
		wi.setElementSelector("#ipbwrapper");
		
		try 
		{
			wi.integrate(new HashMap<String,String>(), new HashMap<String,String>(), new HashMap<String,String>(), new ArrayList<String>(), "", "", "");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

}
