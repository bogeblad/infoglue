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

package org.infoglue.cms.applications.common;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.CacheController;

public class VisualFormatter
{
    private final static Logger logger = Logger.getLogger(VisualFormatter.class.getName());
    
    public VisualFormatter()
    {
    }
    
    public String formatDate(Date date, String pattern)
    {	
        if(date == null)
            return "";
            
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String dateString = formatter.format(date);

        return dateString;
    }

	public String formatDate(Date date, Locale locale, String pattern)
	{	
		if(date == null)
			return "";
            
		// Format the current time.
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale);
		String dateString = formatter.format(date);

		return dateString;
	}
	
    public Date parseDate(String dateString, String pattern)
    {	
    	if(dateString == null)
            return new Date();
        
        Date date = new Date();    
        
        try
        {
	        // Format the current time.
	        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
	        date = formatter.parse(dateString);
	    }
        catch(Exception e)
        {
            logger.info("Error parsing date:" + dateString);
        }
        
        return date;
    }

	public Date parseDate(String dateString, Locale locale, String pattern)
	 {	
		 if(dateString == null)
			 return new Date();
        
		 Date date = new Date();    
        
		 try
		 {
			 // Format the current time.
			 SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale);
			 date = formatter.parse(dateString);
		 }
		 catch(Exception e)
		 {
		     logger.info("Error parsing date:" + dateString);
		 }
        
		 return date;
	 }
	
	/**
	 * This method converts all non-standard characters to html-equivalents.
	 */
	
	public final String escapeHTML(String s)
	{
		if(s == null)
			return null;
			
	    StringBuffer sb = new StringBuffer();
		int n = s.length();
	    for (int i = 0; i < n; i++) 
	    {
	       	char c = s.charAt(i);
    	   	switch (c) 
	       	{
				case '<': sb.append("&lt;"); break;
	         	case '>': sb.append("&gt;"); break;
	         	case '&': sb.append("&amp;"); break;
	         	case '"': sb.append("&quot;"); break;
	         	/*
	         	case '�': sb.append("&agrave;");break;
	         	case '�': sb.append("&Agrave;");break;
	         	case '�': sb.append("&acirc;");break;
	         	case '�': sb.append("&Acirc;");break;
	         	case '�': sb.append("&auml;");break;
	         	case '�': sb.append("&Auml;");break;
	         	case '�': sb.append("&aring;");break;
	         	case '�': sb.append("&Aring;");break;
	         	case '�': sb.append("&aelig;");break;
	         	case '�': sb.append("&AElig;");break;
	         	case '�': sb.append("&ccedil;");break;
	         	case '�': sb.append("&Ccedil;");break;
	         	case '�': sb.append("&eacute;");break;
	         	case '�': sb.append("&Eacute;");break;
	         	case '�': sb.append("&egrave;");break;
	         	case '�': sb.append("&Egrave;");break;
	         	case '�': sb.append("&ecirc;");break;
	         	case '�': sb.append("&Ecirc;");break;
	         	case '�': sb.append("&euml;");break;
	         	case '�': sb.append("&Euml;");break;
	         	case '�': sb.append("&iuml;");break;
	         	case '�': sb.append("&Iuml;");break;
	         	case '�': sb.append("&ocirc;");break;
	         	case '�': sb.append("&Ocirc;");break;
	         	case '�': sb.append("&ouml;");break;
	         	case '�': sb.append("&Ouml;");break;
	         	case '�': sb.append("&oslash;");break;
	         	case '�': sb.append("&Oslash;");break;
	         	case '�': sb.append("&szlig;");break;
	         	case '�': sb.append("&ugrave;");break;
	         	case '�': sb.append("&Ugrave;");break;         
	         	case '�': sb.append("&ucirc;");break;         
	         	case '�': sb.append("&Ucirc;");break;
	         	case '�': sb.append("&uuml;");break;
	         	case '�': sb.append("&Uuml;");break;
	         	case '�': sb.append("&reg;");break;         
	         	case '�': sb.append("&copy;");break;   
	         	case '�': sb.append("&euro;"); break;
	         	*/

	         	default:  sb.append(c); break;
	      	}
	   	}
	   	return sb.toString();
	}
	

	/**
	 * 
	 * Temporary method, please do not use. (SS, 2004-12-13)
	 * @deprecated
	 */
	
	public final String escapeHTMLforXMLService(String s)
	{
		if(s == null)
			return null;
			
		StringBuffer sb = new StringBuffer();
		int n = s.length();
		for (int i = 0; i < n; i++) 
		{
			char c = s.charAt(i);
			switch (c) 
			{
				case '<': sb.append("&lt;"); break;
				case '>': sb.append("&gt;"); break;
				case '&': sb.append("&amp;"); break;
				case '"': sb.append("&quot;"); break;
				case '�': sb.append("&agrave;");break;
				case '�': sb.append("&Agrave;");break;
				case '�': sb.append("&acirc;");break;
				case '�': sb.append("&Acirc;");break;
				case '�': sb.append("&auml;");break;
				case '�': sb.append("&Auml;");break;
				case '�': sb.append("&aring;");break;
				case '�': sb.append("&Aring;");break;
				case '�': sb.append("&aelig;");break;
				case '�': sb.append("&AElig;");break;
				case '�': sb.append("&ccedil;");break;
				case '�': sb.append("&Ccedil;");break;
				case '�': sb.append("&eacute;");break;
				case '�': sb.append("&Eacute;");break;
				case '�': sb.append("&egrave;");break;
				case '�': sb.append("&ograve;");break;
				case '�': sb.append("&Egrave;");break;
				case '�': sb.append("&ecirc;");break;
				case '�': sb.append("&Ecirc;");break;
				case '�': sb.append("&euml;");break;
				case '�': sb.append("&Euml;");break;
				case '�': sb.append("&iuml;");break;
				case '�': sb.append("&Iuml;");break;
				case '�': sb.append("&ocirc;");break;
				case '�': sb.append("&Ocirc;");break;
				case '�': sb.append("&ouml;");break;
				case '�': sb.append("&Ouml;");break;
				case '�': sb.append("&oslash;");break;
				case '�': sb.append("&Oslash;");break;
				case '�': sb.append("&szlig;");break;
				case '�': sb.append("&ugrave;");break;
				case '�': sb.append("&Ugrave;");break;         
				case '�': sb.append("&ucirc;");break;         
				case '�': sb.append("&Ucirc;");break;
				case '�': sb.append("&uuml;");break;
				case '�': sb.append("&Uuml;");break;
				case '�': sb.append("&reg;");break;         
				case '�': sb.append("&copy;");break;   
				case '�': sb.append("&euro;"); break;
				case '\'': sb.append("&#146;"); break;
				
				default:  sb.append(c); break;
			}
		}
		return sb.toString();
	}
	
	public final String escapeExtendedHTML(String s)
	{
		if(s == null)
			return null;
			
		StringBuffer sb = new StringBuffer();
		int n = s.length();
		for (int i = 0; i < n; i++) 
		{
			char c = s.charAt(i);
			switch (c) 
			{
				case '<': sb.append("&lt;"); break;
				case '>': sb.append("&gt;"); break;
				case '&': sb.append("&amp;"); break;
				case '"': sb.append("&quot;"); break;
				/*
				case '�': sb.append("&agrave;");break;
				case '�': sb.append("&Agrave;");break;
				case '�': sb.append("&acirc;");break;
				case '�': sb.append("&Acirc;");break;
				case '�': sb.append("&auml;");break;
				case '�': sb.append("&Auml;");break;
				case '�': sb.append("&aring;");break;
				case '�': sb.append("&Aring;");break;
				case '�': sb.append("&aelig;");break;
				case '�': sb.append("&AElig;");break;
				case '�': sb.append("&ccedil;");break;
				case '�': sb.append("&Ccedil;");break;
				case '�': sb.append("&eacute;");break;
				case '�': sb.append("&Eacute;");break;
				case '�': sb.append("&egrave;");break;
				case '�': sb.append("&Egrave;");break;
				case '�': sb.append("&ecirc;");break;
				case '�': sb.append("&Ecirc;");break;
				case '�': sb.append("&euml;");break;
				case '�': sb.append("&Euml;");break;
				case '�': sb.append("&iuml;");break;
				case '�': sb.append("&Iuml;");break;
				case '�': sb.append("&ocirc;");break;
				case '�': sb.append("&Ocirc;");break;
				case '�': sb.append("&ouml;");break;
				case '�': sb.append("&Ouml;");break;
				case '�': sb.append("&oslash;");break;
				case '�': sb.append("&Oslash;");break;
				case '�': sb.append("&szlig;");break;
				case '�': sb.append("&ugrave;");break;
				case '�': sb.append("&Ugrave;");break;         
				case '�': sb.append("&ucirc;");break;         
				case '�': sb.append("&Ucirc;");break;
				case '�': sb.append("&uuml;");break;
				case '�': sb.append("&Uuml;");break;
				case '�': sb.append("&reg;");break;         
				case '�': sb.append("&copy;");break;   
				case '�': sb.append("&euro;"); break;
	         	*/
				case '\'': sb.append("&#146;"); break;

				default:  sb.append(c); break;
			}
		}
		return sb.toString();
	}
	
	/**
	 * This method converts all non-standard characters to html-equivalents.
	 */
	
	public final String escapeForJavascripts(String s)
	{
		if(s == null)
			return null;
			
		StringBuffer sb = new StringBuffer();
		int n = s.length();
		for (int i = 0; i < n; i++) 
		{
			char c = s.charAt(i);
			if(c == '\'') sb.append("\\'");
			else sb.append(c);
		}
		
		return sb.toString();
	}

	/**
	 * This method converts all non-standard characters to html-equivalents.
	 */
	
	public final String cleanForJavascriptStrings(String s)
	{
		if(s == null)
			return null;

	    String lineSep = System.getProperty("line.separator");
		s = s.replaceAll(lineSep, "<br/>");
		s = s.replaceAll("\n", "<br/>");
		s = s.replaceAll("\r", "<br/>");
		     
		StringBuffer sb = new StringBuffer();
		int n = s.length();
		for (int i = 0; i < n; i++) 
		{
		    char c = s.charAt(i);
			switch (c) 
			{
				case '\'': sb.append("\\'"); break;
				case '"': sb.append("&quot;"); break;
				
				default:  sb.append(c); break;
			}
		}

		return sb.toString();
	}

	public final String cleanForJavascriptStrings(String s, String lineReplaceString)
	{
		if(s == null)
			return null;

	    String lineSep = System.getProperty("line.separator");
		if(lineSep != null)
			s = s.replaceAll(lineSep, lineReplaceString);
		s = s.replaceAll("\n", lineReplaceString);
		s = s.replaceAll("\r", lineReplaceString);
		     
		StringBuffer sb = new StringBuffer();
		int n = s.length();
		for (int i = 0; i < n; i++) 
		{
		    char c = s.charAt(i);
			switch (c) 
			{
				case '\'': sb.append("\\'"); break;
				case '"': sb.append("&quot;"); break;
				
				default:  sb.append(c); break;
			}
		}

		return sb.toString();
	}

	/**
	 * This method converts all non-standard characters to html-equivalents.
	 */
	
	public final String escapeForAdvancedJavascripts(String s)
	{
		if(s == null)
			return null;
			
		StringBuffer sb = new StringBuffer();
		int n = s.length();
		for (int i = 0; i < n; i++) 
		{
		    char c = s.charAt(i);
			switch (c) 
			{
				case '\'': sb.append("\\'"); break;
				case '"': sb.append("&quot;"); break;
				default:  sb.append(c); break;
			}
		}
		
		return sb.toString();
	}
	
	
	public final String replaceNonAscii(String s, char character)
	{
		if(s == null)
			return null;
			
		StringBuffer sb = new StringBuffer();
		int n = s.length();
		for (int i = 0; i < n; i++) 
		{
			char c = s.charAt(i);
			if(c < 128 && c > 32)
			{
			    if(Character.isLetterOrDigit(c) ||  c == '-' || c == '_' || c == '.')
			        sb.append(c);
			    else
			        sb.append(character);
			}
			else
			{
			    sb.append(character);
			}
		}
		return sb.toString();
	}

	public final String replaceNonAscii(String s, String character)
	{
		if(s == null)
			return null;
			
		StringBuffer sb = new StringBuffer();
		int n = s.length();
		for (int i = 0; i < n; i++) 
		{
			char c = s.charAt(i);
			if(c < 128 && c > 32)
			{
			    if(Character.isLetterOrDigit(c) ||  c == '-' || c == '_' || c == '.')
			        sb.append(c);
			    else
			        sb.append(character);
			}
			else
			{
			    sb.append(character);
			}
		}
		return sb.toString();
	}

	/**
	 * This method replaces all non-ascii-characters with a corresponding one defined in the system properties-object. 
	 * If not defined there it replaces the char with the default character.
	 * @param s
	 * @param defaultCharacter
	 * @return
	 */
	
	public final String replaceNiceURINonAsciiWithSpecifiedChars(String s, String defaultCharacter)
	{
		if(s == null)
			return null;
		
		boolean toLowerCase = CmsPropertyHandler.getNiceURIUseLowerCase();
		Properties properties = CmsPropertyHandler.getCharacterReplacingMapping();
		
		StringBuffer sb = new StringBuffer();
		int n = s.length();
		for (int i = 0; i < n; i++) 
		{
			char c = s.charAt(i);
			if(c < 128 && c > 32)
			{
			    if(Character.isLetterOrDigit(c) ||  c == '-' || c == '_' || c == '.')
			        sb.append(c);
			    else
			    {
			    	String replaceChar = properties.getProperty("" + c);
			        if(replaceChar != null && !replaceChar.equals(""))
			        	sb.append(replaceChar);
			        else if(defaultCharacter != null && !defaultCharacter.equalsIgnoreCase("none"))
			        	sb.append(defaultCharacter);
			    }
			}
			else
			{
		    	String replaceChar = properties.getProperty("" + c);
		        if(replaceChar != null && !replaceChar.equals(""))
		        	sb.append(replaceChar);
		        else if(defaultCharacter != null && !defaultCharacter.equalsIgnoreCase("none"))
		        	sb.append(defaultCharacter);
			}
		}
		
		return (toLowerCase ? sb.toString().toLowerCase() : sb.toString());
	}

	/**
	 * This method replaces all non-ascii-characters with a corresponding one defined in the system properties-object. 
	 * If not defined there it replaces the char with the default character.
	 * @param s
	 * @param defaultCharacter
	 * @return
	 */
	
	public final String replaceNonAsciiWithNumericEntity(String s)
	{
		if(s == null)
			return null;
		
		return StringEscapeUtils.escapeHtml(s);
	}

	/**
	 * This method converts all non-standard characters to html-equivalents.
	 */
	
	public final String encode(String s) throws Exception
	{
		if(s == null)
			return null;
		
        String encodedString = (String)CacheController.getCachedObjectFromAdvancedCache("encodedStringsCache", s);
        if(encodedString == null)
        {
        	encodedString = URLEncoder.encode(s, "UTF-8");
        	CacheController.cacheObjectInAdvancedCache("encodedStringsCache", s, encodedString, null, false);
        }

		return encodedString;
	}

	public final String encodeBase64(String s) throws Exception
	{
		if(s == null)
			return null;
		
		return Base64.encodeBase64URLSafeString(s.getBytes("utf-8"));
	}
	
	/**
	 * This method converts all non-standard characters to html-equivalents.
	 */
	
	public final String encodeURI(String s) throws Exception
	{
		if(s == null)
			return null;
		
		String encoding = CmsPropertyHandler.getURIEncoding();
		
		String encodedString = (String)CacheController.getCachedObjectFromAdvancedCache("encodedStringsCache", s);
        if(encodedString == null)
        {
        	encodedString = URLEncoder.encode(s, encoding);
        	CacheController.cacheObjectInAdvancedCache("encodedStringsCache", s, encodedString, null, false);
        }
        
		return encodedString;
	}

	
	public final String encodeURI(String s, String encoding) throws Exception
	{
		if(s == null)
			return null;
		
		String encodedString = (String)CacheController.getCachedObjectFromAdvancedCache("encodedStringsCache", s);
        if(encodedString == null)
        {
        	encodedString = URLEncoder.encode(s, encoding);
        	CacheController.cacheObjectInAdvancedCache("encodedStringsCache", s, encodedString, null, false);
        }
        
		return encodedString;
	}

    public String formatFileSize(Object fileSizeObject)
    {	
    	if(fileSizeObject == null)
            return "";
        
    	String fileSizeString = "";
    	Long fileSize = null;
        if(fileSizeObject instanceof String)
        	fileSize = new Long((String)fileSizeObject);
        else if(fileSizeObject instanceof Integer)
        	fileSize = new Long((Integer)fileSizeObject);
        else if(fileSizeObject instanceof Long)
        	fileSize = (Long)fileSizeObject;
        
        if(fileSize.longValue() >= 1000000000000L)
        	fileSizeString = "" + fileSize / (1000 * 1000 * 1000 * 1000) + " TB";
        if(fileSize.longValue() >= 1000000000)
        	fileSizeString = "" + fileSize / (1000 * 1000 * 1000) + " GB";
        else if(fileSize.longValue() >= 1000000)
        	fileSizeString = "" + fileSize / (1000 * 1000) + " MB";
        else if(fileSize.longValue() >= 1000)
        	fileSizeString = "" + fileSize / 1000 + " KB";
        else
        	fileSizeString = "" + fileSize + " Bytes";
        	
        return fileSizeString;
    }

}