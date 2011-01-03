package org.infoglue.common.webappintegrator;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.URI;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebappIntegrator 
{
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

	public String integrate(Map<String,String> returnCookies, Map<String,String> returnHeaders, Map<String,String> statusData, List<String> blockedParameters) throws Exception
	{
		String responseBody = new PageFetcher().fetchPage(this.urlToIntegrate, method.name(), this.proxyHost, this.proxyPort, cookies, requestProperties, requestParameters, returnCookies, returnHeaders, statusData, blockedParameters);

		String baseURI = this.urlToIntegrate;
		if(baseURI.indexOf("?") > -1)
			baseURI = baseURI.substring(0, baseURI.indexOf("?"));
		
		Document doc = Jsoup.parse(responseBody, baseURI);
		//Document doc = Jsoup.parse(responseBody);
		
		String title = doc.title();
		System.out.println("title:" + title);
		System.out.println("elementSelector:" + elementSelector);
		Element sourceElement = doc.select(elementSelector).first();
		//System.out.println("ipbwrapper:" + ipbwrapper.html());
		if(sourceElement == null)
			sourceElement = doc.body();

		if(sourceElement != null)
		{	
			Elements links = sourceElement.select("a[href]");
			Elements forms = sourceElement.select("form[action]");
	        Elements media = doc.select("[src]");
	        Elements imports = doc.select("link[href]");
	
	        
	        for (Element link : links) 
	        {
	        	String href = link.attr("href");
	        	System.out.println("href:" + href);
	        	String oldUrl = link.attr("abs:href");
	        	System.out.println("oldUrl:" + oldUrl);
	        	if(href.indexOf("javascript:") == -1 && oldUrl != null)
	        	{
		        	String newUrl = currentBaseUrl + (currentBaseUrl.indexOf("?") > -1 ? "&" : "?") + "proxyUrl=" + URLEncoder.encode(oldUrl, "utf-8");
		        	link.attr("href", newUrl);
	        	}
	        }
	
	        for (Element src : media) 
	        {
	        	String oldSrc = src.attr("abs:src");
	        	src.attr("src", oldSrc);
	        }
	        
	        for (Element link : imports) 
	        {
	        	String oldHref = link.attr("abs:href");
	        	link.attr("href", oldHref);
	        }
	        
	        for (Element form : forms) 
	        {
	        	String oldAction = form.attr("abs:action");
	        	System.out.println("oldAction:" + oldAction);
	        	System.out.println("encodedOldAction:" + URLEncoder.encode(oldAction, "utf-8"));
	        	String escapedOldAction = new URI(oldAction).getEscapedURI();
	        	System.out.println("escapedOldAction:" + escapedOldAction);
	        	
	        	String newAction = currentBaseUrl + (currentBaseUrl.indexOf("?") > -1 ? "&" : "?") + "proxyUrl=" + URLEncoder.encode(URLEncoder.encode(oldAction, "utf-8"),"utf-8");
	        	form.attr("action", newAction);
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

	private static void print(String msg, Object... args) 
	{
        System.out.println(String.format(msg, args));
    }

	private static String trim(String s, int width) 
	{
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
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
			wi.integrate(new HashMap<String,String>(), new HashMap<String,String>(), new HashMap<String,String>(), new ArrayList<String>());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

}
