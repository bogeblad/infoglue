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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.servlet.jsp.JspException;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.Logger;
import org.infoglue.cms.util.CmsPropertyHandler;

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
	/** If set this value will be interpreted as a variable name to store the Captcha instance ticket.
	 * If this value is set the tag will not store the Captcha ticket in the sessions. That means that
	 * the tag caller has to handle the ticket and pass it with the form submit. */
	private String ticket;

	private static final String DEFAULT_PASSWORD = "TOPSECRETPASSWORDTHATNOONEKNOWS";
	private String password ;
	private static final byte[] SALT = {
		(byte) 0xde, (byte) 0x73, (byte) 0x10, (byte) 0xa2,
		(byte) 0xde, (byte) 0x73, (byte) 0x10, (byte) 0xa2,
	};
	
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
		if (ticket == null)
		{
			logger.info("Generating Gaptcha with session stored verification");
			String sessionVariableName = textVariableName + "_" + System.currentTimeMillis();
			pageContext.getSession().setAttribute( sessionVariableName, new String(randomCharacters) );
			pageContext.setAttribute(textVariableName, sessionVariableName);
		}
		else
		{
			try
			{
				logger.info("Generating Gaptcha with encoded ticket");
				pageContext.setAttribute(ticket, encodeTicket(new String(randomCharacters)));
			}
			catch (Exception ex)
			{
				logger.error("Error generating encrypted ticket for Gapcha. Message: " + ex.getMessage());
				logger.warn("Error generating encrypted ticket for Gapcha.", ex);
				throw new JspException("Error generating captcha");
			}
		}
		// set the random string in the session
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
	
	private String encodeTicket(String characters) throws GeneralSecurityException, UnsupportedEncodingException
	{
		if (password == null)
		{
			password = DEFAULT_PASSWORD;
		}
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(password.toCharArray()));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return Base64.encode(pbeCipher.doFinal(characters.getBytes("UTF-8")));
	}
	
	/* default */ static String decodeTicket(String ticket, String password) throws GeneralSecurityException, IOException
	{
		if (password == null)
		{
			password = DEFAULT_PASSWORD;
		}
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(password.toCharArray()));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return new String(pbeCipher.doFinal(Base64.decode(ticket)), "UTF-8");
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
	
	public void setTwirlAngle(Object twirlAspect) throws JspException
	{
		this.setAttribute("twirlAspect", (Float)twirlAspect);
	}

	public void setMarbleXScale(Object marbleXScale) throws JspException
	{
		this.setAttribute("marbleXScale", (Float)marbleXScale);
	}

	public void setMarbleYScale(Object marbleYScale) throws JspException
	{
		this.setAttribute("marbleYScale", (Float)marbleYScale);
	}

	public void setMarbleTurbulence(Object marbleTurbulence) throws JspException
	{
		this.setAttribute("marbleTurbulence", (Float)marbleTurbulence);
	}

	public void setMarbleAmount(Object marbleAmount) throws JspException
	{
		this.setAttribute("marbleAmount", (Float)marbleAmount);
	}

	public void setTicket(String ticket)
	{
		this.ticket = ticket;
	}

	public void setPassword(String password) throws JspException
	{
		this.password = evaluateString("gapcha", "password", password);
	}

	public static void main(String[] args) throws UnsupportedEncodingException, GeneralSecurityException, IOException
	{
		System.out.println("Begin");
		GapchaTag tag = new GapchaTag();
		String enc1 = tag.encodeTicket("apa");
		System.out.println("Test 1: apa = " + GapchaTag.decodeTicket(enc1, null) + " || " + enc1);
		String enc2 = tag.encodeTicket("bepa123");
		System.out.println("Test 2: bepa123 = " + GapchaTag.decodeTicket(enc2, null) + " || " + enc2);
		
		System.out.println("Test 3: ? = " + GapchaTag.decodeTicket("Bfck1bYW8r4=", null));
		
		System.out.println("End");
	}

}
