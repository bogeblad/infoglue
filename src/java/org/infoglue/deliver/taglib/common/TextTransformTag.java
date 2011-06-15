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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * This tag help modifying texts in different ways. An example is to html-encode strings or replace all 
 * whitespaces with &nbsp; or replacing linebreaks with <br/>  
 */

public class TextTransformTag extends TemplateControllerTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8603406098980150888L;
	
	/**
	 * The original text.
	 */
	private String text;

	/**
	 * Should we encode i18n chars
	 */
	private boolean htmlEncode = false;
	
	/**
	 * Should we replace linebreaks with something?
	 */
	private boolean replaceLineBreaks = false;

	/**
	 * The string to replace linebreaks with
	 */
	private String lineBreakReplacer = "<br/>";

	/**
	 * The linebreak char
	 */
	private String lineBreakChar = System.getProperty("line.separator");

	/**
	 * What to replace
	 */
	private String replaceString = null;

	/**
	 * What to replace with
	 */
	private String replaceWithString = null;

	/**
	 * What to append before the text
	 */
	private String prefix = null;

	/**
	 * If set - the string must match this to add the prefix
	 */
	private String addPrefixIfTextMatches = null;

	/**
	 * If set - the string must match this to add the prefix
	 */
	private String addPrefixIfTextNotMatches = null;

	/**
	 * What to append after the text
	 */
	private String suffix = null;

	/**
	 * If set - the string must match this to add the suffix
	 */
	private String addSuffixIfTextMatches = null;

	/**
	 * If set - the string must match this to add the suffix
	 */
	private String addSuffixIfTextNotMatches = null;

	/**
	 * Default constructor.
	 */
	public TextTransformTag() 
	{
		super();
	}
	
	/**
	 * Process the end tag. Modifies the string according to settings made.  
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
	    String modifiedText = text;
	    try
	    {
		    if(replaceString != null && replaceWithString != null)
		    {
		        Pattern pattern = Pattern.compile(replaceString);
		        Matcher matcher = pattern.matcher(modifiedText);
		        modifiedText = matcher.replaceAll(replaceWithString);
		    }
		    
		    if(replaceLineBreaks)
		    {
		    	if(lineBreakChar.toCharArray().length == 1 && ((int)lineBreakChar.toCharArray()[0] == 10))
		    	{
			    	modifiedText = modifiedText.replaceAll("" + (char)13 + lineBreakChar, lineBreakReplacer);	    
		    	}
		    	modifiedText = modifiedText.replaceAll(lineBreakChar, lineBreakReplacer);	    
		    }
	
		    if(this.prefix != null)
		    {
		    	if(this.addPrefixIfTextMatches != null)
		    	{
		    		Pattern pattern = Pattern.compile(this.addPrefixIfTextMatches);
			        Matcher matcher = pattern.matcher(modifiedText);
			        if(matcher.find())
			        	modifiedText = this.prefix + modifiedText;	    		
		    	}
		    	else if(this.addPrefixIfTextNotMatches != null)
		    	{
		    		Pattern pattern = Pattern.compile(this.addPrefixIfTextNotMatches);
			        Matcher matcher = pattern.matcher(modifiedText);
			        if(!matcher.find())
			        	modifiedText = this.prefix + modifiedText;	    		
		    	}
		    	else
		    	{
		    		modifiedText = this.prefix + modifiedText;
		    	}
		    }
		    if(this.suffix != null)
		    {
		    	if(this.addSuffixIfTextMatches != null)
		    	{
		    		Pattern pattern = Pattern.compile(this.addSuffixIfTextMatches);
			        Matcher matcher = pattern.matcher(modifiedText);
			        if(matcher.find())
			        	modifiedText = modifiedText + this.suffix;	    		
		    	}
		    	else if(this.addSuffixIfTextNotMatches != null)
		    	{
		    		Pattern pattern = Pattern.compile(this.addSuffixIfTextNotMatches);
			        Matcher matcher = pattern.matcher(modifiedText);
			        if(!matcher.find())
			        	modifiedText =  modifiedText + this.suffix;	    		
		    	}
		    	else
		    	{
		    		modifiedText = modifiedText + this.suffix;
		    	}
		    }
		    
		    if(htmlEncode)
		        modifiedText = this.getController().getVisualFormatter().escapeHTMLforXMLService(modifiedText);	        
	    }
	    catch (Throwable t) 
	    {
			t.printStackTrace();
		}
	    
	    setResultAttribute(modifiedText);
	    
	    this.text = null;
	    this.prefix = null;
	    this.suffix = null;
	    this.addPrefixIfTextMatches = null;
	    this.addPrefixIfTextNotMatches = null;
	    this.addSuffixIfTextMatches = null;
	    this.addSuffixIfTextNotMatches = null;
	    
        return EVAL_PAGE;
    }

       
    public void setText(String text) throws JspException
    {
        this.text = evaluateString("cropText", "text", text);
    }    
    
    public void setHtmlEncode(boolean htmlEncode)
    {
        this.htmlEncode = htmlEncode;
    }
    
    public void setReplaceLineBreaks(boolean replaceLineBreaks)
    {
        this.replaceLineBreaks = replaceLineBreaks;
    }

    public void setLineBreakChar(String lineBreakChar) throws JspException
    {
        this.lineBreakChar = evaluateString("TextTransform", "lineBreakChar", lineBreakChar);
    }
    
    public void setLineBreakReplacer(String lineBreakReplacer) throws JspException
    {
        this.lineBreakReplacer = evaluateString("TextTransform", "lineBreakReplacer", lineBreakReplacer);
    }
        
    public void setReplaceString(String replaceString) throws JspException
    {
        this.replaceString = evaluateString("TextTransform", "replaceString", replaceString);
    }
    
    public void setReplaceWithString(String replaceWithString) throws JspException
    {
        this.replaceWithString = evaluateString("TextTransform", "replaceWithString", replaceWithString);
    }

	public void setPrefix(String prefix) throws JspException
	{
		this.prefix = evaluateString("TextTransform", "prefix", prefix);
	}
	    
	public void setAddPrefixIfTextMatches(String addPrefixIfTextMatches) throws JspException
	{
		this.addPrefixIfTextMatches = evaluateString("TextTransform", "addPrefixIfTextMatches", addPrefixIfTextMatches);
	}
	
	public void setAddPrefixIfTextNotMatches(String addPrefixIfTextNotMatches) throws JspException
	{
		this.addPrefixIfTextNotMatches = evaluateString("TextTransform", "addPrefixIfTextNotMatches", addPrefixIfTextNotMatches);
	}

	public void setSuffix(String suffix) throws JspException
	{
		this.suffix = evaluateString("TextTransform", "suffix", suffix);
	}

	public void setAddSuffixIfTextMatches(String addSuffixIfTextMatches) throws JspException
	{
		this.addSuffixIfTextMatches = evaluateString("TextTransform", "addSuffixIfTextMatches", addSuffixIfTextMatches);
	}

	public void setAddSuffixIfTextNotMatches(String addSuffixIfTextNotMatches) throws JspException
	{
		this.addSuffixIfTextNotMatches = evaluateString("TextTransform", "addSuffixIfTextNotMatches", addSuffixIfTextNotMatches);
	}
}
