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

import java.io.File;
import java.util.Random;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;

/**
 * A Tag used for rendering distorted images with random text.  
 * @author Tommy Berglund <a href="mailto:tommy.berglund@hotmail.com">tommy.berglund@modul1.se</a>
 */

public class GapchaTag extends TextRenderTag 
{
    private final static Logger logger = Logger.getLogger(GapchaTag.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String allowedCharacters = "abcdefghijklmonpqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private String textVariableName = "CAPTHCA_TEXT";
	private int numberOfCharacters = 5;
	private static int requestsNO = 0;
	
	public GapchaTag() 
	{
		super();
	}

	public int doEndTag() throws JspException 
	{
		if(requestsNO > 50)
		{
			cleanOldFiles();
			requestsNO = 0;
		}
		
		// create the random string
		char[] randomCharacters = createRandomCharacters();
		// set the random string in the session
		String sessionVariableName = textVariableName + "_" + System.currentTimeMillis();
		pageContext.getSession().setAttribute( sessionVariableName, new String(randomCharacters) );
		pageContext.setAttribute(textVariableName, sessionVariableName);
		// without spacing it is really hard to read the text
		String randomText = spaceCharacters(randomCharacters);
		try 
		{
			result = this.getController().getRenderedTextUrl( randomText, renderAttributes, true );
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
        
		this.produceResult( result );
		requestsNO++;
		
		return EVAL_PAGE;
	}
	
	public static void cleanOldFiles()
	{
		int i = 0;
        String filePath = CmsPropertyHandler.getDigitalAssetPath0();
        while ( filePath != null )
        {
            logger.info("Cleaning files...");
            File folder = new File(filePath);
            File[] files = folder.listFiles();
            logger.info("files:" + files.length);
            for(int j=0; j<files.length; j++)
            {
            	File file = files[j];
                if(file.getName().startsWith("igcaptcha"))
                {
                	logger.info("file.getName():" + file.getName() + " - " + (System.currentTimeMillis() - file.lastModified()));
                    if(System.currentTimeMillis() - file.lastModified() > 60000)
                    {
                    	logger.info("Deleting:" + file.getName());
                        file.delete();
                    }
                }
            }
            i++;
            filePath = CmsPropertyHandler.getProperty( "digitalAssetPath." + i );
        }
	}

	/**
	 * Sets the number of characters in the random string. Value must be greater
	 * than 0, otherwise default value (5) is used.
	 * @param numberOfCharacters the number of characters
	 */
	public void setNumberOfCharacters( int numberOfCharacters ) 
	{
		if( numberOfCharacters > 0 ) 
		{
			this.numberOfCharacters = numberOfCharacters;
		}
	}

	/**
	 * Sets the variable name by which to store the captcha text in.
	 * @param numberOfCharacters the number of characters
	 */
	public void setTextVariableName( String textVariableName ) 
	{
		this.textVariableName = textVariableName;
	}
	
	
	/**
	 * Creates a char[] of random characters and numbers a-z,A-Z,0-9. 
	 * The  number of characters can be set by an attribute. Default is 5.
	 * @return a char[] of random characters and numbers
	 */
	private char[] createRandomCharacters()
	{
		Random r = new Random();
		StringBuffer sb = new StringBuffer();
		char[] buf = new char[numberOfCharacters];
		for (int i = 0; i < buf.length; i++) 
		{
			buf[i] = allowedCharacters.charAt(r.nextInt(allowedCharacters.length()));
		}
		return buf;
	}
	
	/**
	 * Spaces characters for easier reading
	 * @param characters the characters to space
	 * @return a <code>String</code> of spaced characters
	 */
	private String spaceCharacters( char[] characters ) 
	{
		StringBuffer sb = new StringBuffer(characters.length*2);
		for( int i = 0; i <characters.length; i++ ) 
		{
			sb.append( characters[i] );
			sb.append( " " );
		}
		return sb.toString();	
	}
	
	public void setTwirlAngle(String twirlAspect) throws JspException
	{
		this.setAttribute("twirlAspect", ((Float)evaluate("gapcha", "twirlAspect", twirlAspect, Float.class)).floatValue());
	}

	public void setMarbleXScale(String marbleXScale) throws JspException
	{
		this.setAttribute("marbleXScale", ((Float)evaluate("gapcha", "marbleXScale", marbleXScale, Float.class)).floatValue());
	}

	public void setMarbleYScale(String marbleYScale) throws JspException
	{
		this.setAttribute("marbleYScale", ((Float)evaluate("gapcha", "marbleYScale", marbleYScale, Float.class)).floatValue());
	}

	public void setMarbleTurbulence(String marbleTurbulence) throws JspException
	{
		this.setAttribute("marbleTurbulence", ((Float)evaluate("gapcha", "marbleTurbulence", marbleTurbulence, Float.class)).floatValue());
	}

	public void setMarbleAmount(String marbleAmount) throws JspException
	{
		this.setAttribute("marbleAmount", ((Float)evaluate("gapcha", "marbleAmount", marbleAmount, Float.class)).floatValue());
	}

	public void setAllowedCharacters(String allowedCharacters) throws JspException
	{
		this.allowedCharacters = evaluateString("gapcha", "allowedCharacters", allowedCharacters);
	}
}
