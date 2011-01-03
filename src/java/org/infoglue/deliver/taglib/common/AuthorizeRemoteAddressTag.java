package org.infoglue.deliver.taglib.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.log4j.Logger;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class AuthorizeRemoteAddressTag extends TemplateControllerTag
{	
	private static final long serialVersionUID 	= -7785046611337302799L;
	private final static Logger logger 			= Logger.getLogger(AuthorizeRemoteAddressTag.class.getName());
	private String trustedIPs 					= "";
	private String allowDnsLookupForIPs 		= "";
	private String trustedDomains 				= "";
	
	public AuthorizeRemoteAddressTag()
	{
		super();
	}
	
	public int doEndTag() throws JspException
	{
		produceResult(isTrusted());
		
		trustedIPs 					= "";
		allowDnsLookupForIPs 		= "";
		trustedDomains 				= "";
		
		return EVAL_PAGE;
	}
		
	public Boolean isTrusted() throws JspTagException
	{						
		HttpServletRequest request 	= this.getController().getHttpServletRequest();
		String usersIP 				= request.getRemoteAddr();		
		boolean isTrusted 			= false;
		Boolean returnValue			= null;
		
		if(logger.isInfoEnabled())
		{
			logger.info("Users IP: " + usersIP);		
			logger.info("Trusted IPs: " + trustedIPs);
			logger.info("Allow DNS lookup for IPs: " + allowDnsLookupForIPs);		
			logger.info("Trusted domains: " + trustedDomains);
		}
		
		if (isTrustedIp(usersIP, trustedIPs))
		{
			if(logger.isInfoEnabled())
				logger.info("RESULT: The users IP is trusted.");
			isTrusted = true;
		}
		else
		{
			if (isAllowedToMakeDnsLookup(usersIP, allowDnsLookupForIPs))
			{
				if(logger.isInfoEnabled())
					logger.info("Making DNS lookup.");
				String usersDomain	= request.getRemoteHost();
				if(logger.isInfoEnabled())
					logger.info("Users domain: " + usersDomain);
				
				if (isInTrustedDomain(usersDomain, trustedDomains))
				{
					if(logger.isInfoEnabled())
						logger.info("RESULT: The user is in a trusted domain.");
					isTrusted = true;
				}
				else
				{
					if(logger.isInfoEnabled())
						logger.info("RESULT: The user is NOT in a trusted domain.");
					isTrusted = false;
				}
			}
			else
			{
				if(logger.isInfoEnabled())
					logger.info("RESULT: The users IP is not trusted to do DNS lookups.");
				isTrusted = false;
			}
		}
		
		returnValue = new Boolean(isTrusted);
		
		return returnValue;		
	}
	
	private static boolean isTrustedIp(String usersIP, String trustedIPs)
	{
		boolean isInIpRange = isInIpRange(usersIP, trustedIPs);
		if(logger.isInfoEnabled())
			logger.info("IP is in the trusted range: " + isInIpRange);
		return isInIpRange;
	}
	
	private static boolean isAllowedToMakeDnsLookup(String usersIP, String allowDnsLookupForIPs)
	{		
		boolean isInIpRange = false;
		
		if(logger.isInfoEnabled())
			logger.info("Checking if IP is is allowed to do lookup.");
		
		if (allowDnsLookupForIPs == null || allowDnsLookupForIPs.trim().length() == 0)
		{
			isInIpRange = true;
		}
		else
		{
			isInIpRange = isInIpRange(usersIP, allowDnsLookupForIPs);
		}
		
		if(logger.isInfoEnabled())
			logger.info("Result: " + isInIpRange);
		
		return isInIpRange;
	}
	
	private static boolean isInTrustedDomain(String usersDomain, String trustedDomains)
	{				
		boolean isInTrustedDomain = false;
		
		if (trustedDomains != null && !trustedDomains.trim().equals(""))
		{
			java.util.StringTokenizer st	= new java.util.StringTokenizer(trustedDomains, ",");
			String trustedDomain			= "";
			
			while (st.hasMoreTokens())
			{
				trustedDomain = st.nextToken().trim();
				
				if (!trustedDomain.trim().equals("") && usersDomain.endsWith(trustedDomain))
				{
					isInTrustedDomain = true;
				}
			}
		}
		else
		{
			if(logger.isInfoEnabled())
				logger.info("No trusted domains have been defined.");
		}
		
		if(logger.isInfoEnabled())
			logger.info("Users domain is trusted: " + isInTrustedDomain);
		
		return isInTrustedDomain;
	}
	
	private static boolean isInIpRange(String queryIP, String ipExpression)
	{
		Pattern pattern = Pattern.compile(ipExpression);
		Matcher matcher = pattern.matcher(queryIP);
		boolean isMatch	= matcher.matches();
		
		if(logger.isInfoEnabled())
		{
			logger.info("Checking if the IP " + queryIP + " matches the expression: " + ipExpression);
			logger.info("Result: " + isMatch);
		}
		
		return isMatch;
	}

	public void setTrustedIPs(String trustedIPs) throws JspException
	{
		this.trustedIPs = evaluateString("authorizeRemoteAddressTag", "trustedIPs", trustedIPs);
	}

	public void setAllowDnsLookupForIPs(String allowDnsLookupForIPs) throws JspException
	{
		this.allowDnsLookupForIPs = evaluateString("authorizeRemoteAddressTag", "allowDnsLookupForIPs", allowDnsLookupForIPs);;
	}

	public void setTrustedDomains(String trustedDomains) throws JspException
	{
		this.trustedDomains = evaluateString("authorizeRemoteAddressTag", "trustedDomains", trustedDomains);;
	}
}
