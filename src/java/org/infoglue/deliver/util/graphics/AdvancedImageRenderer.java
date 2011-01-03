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

package org.infoglue.deliver.util.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.infoglue.cms.util.CmsPropertyHandler;

import com.jhlabs.image.MarbleFilter;
import com.jhlabs.image.TwirlFilter;

/**
 * Renders images and saves them.
 * @author Per Jonsson - per.jonsson@it-huset.se
 * 
 * @version 1.1 fixed reading properties from file, some optimizations and added a imageFileFormat,
 */
public class AdvancedImageRenderer
{
    private static final long serialVersionUID = -1377395059993980530L;

    private final static Logger logger = Logger.getLogger( AdvancedImageRenderer.class.getName() );

    // type of image, colordepth etc.
    private int imageType = BufferedImage.TYPE_4BYTE_ABGR;

    // An template image to get the right rendering attributes for the font renderer
    // don't change this in runtime. 
    private static BufferedImage templateImage = null;

    // the rendered image
    private BufferedImage renderedImage = null;

    // Fontname
    private String fontName = "Dialog";

    // style of font
    private int fontStyle = Font.PLAIN;

    // size of font
    private int fontSize = 18;

    // font to render
    private Font font = null;

    // font color
    private Color fgColor = new Color( 0, 0, 0, 255 ); // black

    //background color
    private Color bgColor = new Color( 255, 255, 255, 255 ); // white

    // width of the rendered image, maxwidth if used with trimedges
    private int renderWidth = 200;

    // the textalign
    private int align = 0; // 0 = left, 1 = right , 2 = center

    // top padding in pixels
    private int padTop = 4;

    // bottom padding in pixels
    private int padBottom = 4;

    // left padding in pixels
    private int padLeft = 4;

    // right padding in pixels
    private int padRight = 4;

    // maximum number of textrows 
    private int maxRows = 20;
    
    // default imageFormatName
    private String imageFormatName = "png";

    // 0 = notrim, 1 = left, 2 = right, 3 = left and right
    private int trimEdges = 0;

    // an url to for the background
    private String backgroundImageUrl = null;

    // just for caching
    private BufferedImage backgroundImage = null;

    // 0 = no, 1 = horizontal, 2 = vertical, 3 = both
    private int tileBackgroundImage = 0;

    //Capctha-params
	private float twirlAspect = 1.1f;
	private float marbleXScale = 2.0f;
	private float marbleYScale = 2.0f;
	private float marbleTurbulence = 0.9f;
	private float marbleAmount = 0.9f;

    
    private static Map renderHints = null;

    // cached map of the methods
    private static Map methodMap = null;
    
    // if config is read from propertyfile these are stored here. 
    private static Map defaultConfigMap = new HashMap();

    /**
     * Creates a new instance of tne NewImageRenderer and reads in properties
     * from the property file if exists. The propertieas must have the suffix of
     * "rendertext" ie. rendertext.fontname.
     */
    public AdvancedImageRenderer()
    {
        // precalc some setters for faster seach
    	if ( methodMap == null )
    	{
    		methodMap = new HashMap();
	        Method[] methods = this.getClass().getDeclaredMethods();
	        String name = null;
	        for ( int i = 0; i < methods.length; i++ )
	        {
	            name = methods[ i ].getName().toLowerCase();
	            if ( name.startsWith( "set" ) && methods[ i ].getParameterTypes().length == 1 )
	            {
	            	name = name.substring( "set".length() );
	                methodMap.put( name , methods[ i ] );
	                // Add the default config from properties to the defaultConfig map if exists.
	                String propVal = CmsPropertyHandler.getProperty( "rendertext." + name );
	                if ( propVal != null && propVal.trim().length() > 0 )
	                {
	                    //this.setAttribute( name, propVal.toLowerCase() );
	                	defaultConfigMap.put( name, propVal.toLowerCase() );
	                }
	            }
	        }
	        logger.debug( defaultConfigMap );
    	}
        if ( renderHints == null )
        {
        	renderHints = new HashMap();
	        renderHints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
	        renderHints.put( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY );
	        renderHints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
	        renderHints.put( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
	        renderHints.put( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY );
	        renderHints.put( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE );
        }
    }

    /**
     * Write the rendered image to a file. 
     * @param file the file of the image to create.
     * @return true if success, false if error
     */
    public boolean writeImage( File file )
    {
        boolean success = false;
        try
        {
            success = ImageIO.write( this.renderedImage, this.imageFormatName , file );
        }
        catch ( Exception e )
        {
            logger.error( "Couldn't write Image file : " + file, e );
        }
        return success;
    }

    /**
     * Renders a text returnes the rendered picture.
     * @param text The text to be rendered.
     * @return an rendered image
     */
    public BufferedImage renderImage( CharSequence text, Map renderAttributes )
    {
        AttributedString attributedString = new AttributedString( text.toString() );
        return this.renderImage( attributedString, renderAttributes );
    }

    /**
     * Renders a text returnes the rendered picture.
     * @param attributedString an attributed string, to enable multicolored or
     *            similar texts.
     * @return an rendered image
     */
    public BufferedImage renderImage( AttributedString attributedString, Map renderAttributes )
    {
    	// Copy the defaultconfig and merge with the supplied render attributes. 
    	Map tempMap = new HashMap( defaultConfigMap );
    	if ( renderAttributes != null )
    	{
    		tempMap.putAll( renderAttributes );
    	}
    	renderAttributes = tempMap;
        if ( renderAttributes != null && renderAttributes.size() > 0 )
        {
            Iterator keyIter = renderAttributes.entrySet().iterator();
            while ( keyIter.hasNext() )
            {
                Map.Entry entry = (Map.Entry)keyIter.next();
                String key = entry.getKey().toString().trim().toLowerCase();
                if ( hasAttribute( key ) && entry.getValue() != null )
                {
                    setAttribute( key, entry.getValue().toString() );
                }
            }
        }
        // Set TemplateImage
        if ( templateImage == null )
        {
            templateImage = new BufferedImage( 8, 8, imageType );
        }

        font = new Font( fontName, fontStyle, fontSize );

        // renderWidth = getSize().width; // temp when testing

        float wrappingWidth = renderWidth - ( padLeft + padRight );
        
        if ( wrappingWidth < 0 )
        {
        	throw new IllegalArgumentException( "The renderwidth (" + renderWidth + ") is lesser than the total padding ("
        			+ ( padLeft + padRight ) + "), modify your settings.");
        }

        Graphics2D g2d = templateImage.createGraphics();
        g2d.setRenderingHints( renderHints );

        attributedString.addAttribute( TextAttribute.FONT, font );
        attributedString.addAttribute( TextAttribute.FOREGROUND, fgColor );

        FontRenderContext context = g2d.getFontRenderContext();

        AttributedCharacterIterator iterator = attributedString.getIterator();
        LineBreakMeasurer measurer = new LineBreakMeasurer( iterator, context );

        TextLayout layout = null;
        // precalculating the render pictureheight
        double renderHeight = padTop + padBottom;
        int numRows = 0;
        while ( measurer.getPosition() < iterator.getEndIndex() )
        {
            if ( ( layout = measurer.nextLayout( wrappingWidth ) ) == null || ( numRows >= maxRows ) )
            {
                break;
            }
            numRows++;
            renderHeight += layout.getAscent() + layout.getDescent() + layout.getLeading();
        }

        renderedImage = new BufferedImage( renderWidth, (int)( renderHeight + 0.5 ), templateImage.getType() );
        Graphics2D img2d = renderedImage.createGraphics();
        img2d.setRenderingHints( renderHints );

        img2d.setColor( fgColor );

        checkAndSetBackground();

        Point2D.Float pen = new Point2D.Float( padLeft, padTop );

        context = img2d.getFontRenderContext();
        iterator = attributedString.getIterator();
        measurer = new LineBreakMeasurer( iterator, context );
        numRows = 0;
        while ( measurer.getPosition() < iterator.getEndIndex() )
        {
            if ( ( layout = measurer.nextLayout( wrappingWidth ) ) == null || ( numRows >= maxRows ) )
            {
                break;
            }
            numRows++;
            pen.y += layout.getAscent();

            float dx = 0.0f;
            if ( align == 1 || !layout.isLeftToRight() ) // align right
            {
                dx = ( wrappingWidth - layout.getVisibleAdvance() );
            }
            else if ( align == 2 ) // align center
            {
                dx = ( wrappingWidth - layout.getVisibleAdvance() ) / 2;
            }

            layout.draw( img2d, pen.x + dx, pen.y );
            pen.y += layout.getDescent() + layout.getLeading();
        }

        // check and trim
        renderedImage = horizontalTrim();

        return renderedImage;
    }

    /**
     * Checks the attributes and set the correct background.
     */
    private void checkAndSetBackground()
    {
        Graphics2D img2d = renderedImage.createGraphics();
        img2d.setBackground( bgColor );
        img2d.clearRect( 0, 0, renderedImage.getWidth(), renderedImage.getHeight() );

        if ( backgroundImageUrl != null )
        {
            try
            {
                if ( backgroundImage == null )
                {
                    URLConnection connection = new URL( backgroundImageUrl ).openConnection();
                    // Hmm... Only 1.5 these below. 
                    //connection.setConnectTimeout( 1000 * 5 ); // set the timeout to 5 seconds.
                    //connection.setReadTimeout( 1000 * 5 );	// set the timeout to 5 seconds.
                    InputStream is = connection.getInputStream();
                    backgroundImage = ImageIO.read( is );
                    is.close();
                }

                if ( tileBackgroundImage == 1 && backgroundImage.getWidth() < renderedImage.getWidth() ) // horizontal
                {
                    int xnum = (int)( renderedImage.getWidth() / backgroundImage.getWidth() + 0.5 ) + 1;
                    while ( xnum-- >= 0 )
                    {
                        img2d.drawImage( backgroundImage, backgroundImage.getWidth() * xnum, 0, null );
                    }
                }
                if ( tileBackgroundImage == 2 && backgroundImage.getHeight() < renderedImage.getHeight() ) // vertical
                {
                    int ynum = (int)( renderedImage.getHeight() / backgroundImage.getHeight() + 0.5 ) + 1;
                    while ( ynum-- >= 0 )
                    {
                        img2d.drawImage( backgroundImage, 0, backgroundImage.getHeight() * ynum, null );
                    }
                }
                if ( tileBackgroundImage == 3 && backgroundImage.getHeight() < renderedImage.getHeight() ) // vertical
                {
                    int ynum = (int)( renderedImage.getHeight() / backgroundImage.getHeight() + 0.5 ) + 1;
                    while ( ynum-- >= 0 )
                    {
                        int xnum = (int)( renderedImage.getWidth() / backgroundImage.getWidth() + 0.5 ) + 1;
                        while ( xnum-- >= 0 )
                        {
                            img2d.drawImage( backgroundImage, backgroundImage.getWidth() * xnum, backgroundImage.getHeight() * ynum, null );
                        }
                    }
                }

                if ( tileBackgroundImage == 0 )
                {
                    img2d.drawImage( backgroundImage, 0, 0, null );
                }
            }
            catch ( IOException ioe )
            {
                logger.error( "Error in reading backgoundImageUrl: " + backgroundImageUrl, ioe );
            }
        }
    }

    /**
     * Trims the edges of the image.
     * @return a new trimmed image from the original. 
     */
    private BufferedImage horizontalTrim()
    {
        if ( trimEdges == 0 )
        {
            return renderedImage;
        }

        int imgHeight = renderedImage.getHeight();
        int imgWidth = renderedImage.getWidth();
        
        int bgRGB = bgColor.getRGB(); // get the background color

        // check and trim left side
        int w = 0;
        int leftPos = 0, rightPos = 0;

        if ( this.trimEdges == 1 || this.trimEdges == 3 )
        {
            loop: for ( w = 0; w < imgWidth; w++ )
            {
                int imgRGB = 0;
                for ( int y = 0; y < imgHeight; y++ )
                {
                    imgRGB = renderedImage.getRGB( w, y );
                    if ( imgRGB != bgRGB )
                    {
                        break loop;
                    }
                }
            }

            leftPos = ( w > 0 ) ? w - 1 : 0;
            leftPos -= padLeft;
            // ensure none negative numbers
            leftPos = ( leftPos <= 0 ) ? 0 : leftPos;
        }

        // check and trim right side
        if ( this.trimEdges == 2 || this.trimEdges == 3 )
        {
            loop: for ( w = ( imgWidth - 1 ); w >= 0; w-- )
            {
                int imgRGB = 0;
                for ( int y = 0; y < imgHeight; y++ )
                {
                    imgRGB = renderedImage.getRGB( w, y );
                    if ( imgRGB != bgRGB )
                    {
                        break loop;
                    }
                }
            }
            rightPos = w + 1;
            rightPos += padRight;
            // ensure not outside
            rightPos = ( rightPos > imgWidth ) ? imgWidth - 1 : rightPos;
        }
        else
        {
            rightPos = imgWidth - 1;
        }
        return renderedImage.getSubimage( leftPos, 0, rightPos - leftPos, imgHeight - 1 );
    }

	public void distortImage() 
	{
		// twirl filter
		TwirlFilter tf = new TwirlFilter();
		float angle = this.twirlAspect;
		tf.setAngle( angle );
		tf.filter(renderedImage, renderedImage);
		
		// Marble Filter
		MarbleFilter mf = new MarbleFilter();
		mf.setXScale( this.marbleXScale );
		mf.setYScale( this.marbleYScale );
		mf.setTurbulence( this.marbleTurbulence );
		mf.setAmount( this.marbleAmount );
		mf.filter(renderedImage, renderedImage);		
	}

    
    /**
     * Check if this class has a specific attribute, name of attribute is case
     * insensitive. ie. "fontname", "fontsize", "bgcolor"
     * @param attributeName name of the attribute to check
     * @return true if attribute exisist, false otherwise
     */
    public boolean hasAttribute( CharSequence attributeName )
    {
        return methodMap.containsKey( attributeName.toString().toLowerCase() );
    }

    /**
     * Using reflection to set the fields corresponing to the attribute. tries
     * to convert to the right object. The attribute is caseinsesitive. <br>
     * If it's a color value it has to be a string in the format
     * "252:123:133:255" where they are "R:G:B:A" values from 0-255.
     * @param attribute the field/property to set
     * @param value the value to set.
     */
    public void setAttribute( CharSequence attribute, CharSequence value )
    {
    	logger.debug("set attribute: " + attribute + " = " + value );
        Method method = (Method)methodMap.get( attribute );
        if ( method != null )
        {
            try
            {
                Class[] params = method.getParameterTypes();
                Class param = params[ 0 ];
                if ( param.isPrimitive() )
                {
                    if ( param.getName().equals( "int" ) )
                    {
                        method.invoke( this, new Object[]
                        { new Integer(Integer.parseInt( value.toString() )) } );
                    }
                    else if ( param.getName().equals( "float" ) )
                    {
                        method.invoke( this, new Object[]
                        { new Float(Float.parseFloat( value.toString() )) } );
                    }
                    else if ( param.getName().equals( "double" ) )
                    {
                        method.invoke( this, new Object[]
                        { new Double(Double.parseDouble( value.toString() )) } );
                    }
                    else if ( param.getName().equals( "boolean" ) )
                    {
                        method.invoke( this, new Object[]
                        { new Boolean(Boolean.parseBoolean( value.toString() )) } );
                    }
                }
                else if ( param.equals( String.class ) )
                {
                    method.invoke( this, new Object[]
                    { value.toString() } );
                }
                else if ( param.equals( Color.class ) )
                {
                    method.invoke( this, new Object[]
                    { ColorHelper.getColor( value.toString() ) } );
                }

            }
            catch ( Exception e )
            {
                logger.warn( "Error in setting properties: " + attribute + " = " + value, e );
            }
        }
        else
        {
        	logger.warn( "No attribut, named: " + attribute + " found, value =" + value );
        }
    }

    /**
     * @param align The align to set.
     */
    public void setAlign( int align )
    {
        this.align = align;
    }

    /**
     * @param backgroundImage The backgroundImage to set.
     */
    public void setBackgroundImage( BufferedImage backgroundImage )
    {
        this.backgroundImage = backgroundImage;
    }

    /**
     * @param backgroundImageUrl The backgroundImageUrl to set.
     */
    public void setBackgroundImageURL( String backgroundImageUrl )
    {
        this.backgroundImageUrl = backgroundImageUrl;
    }

    /**
     * @param bgColor The bgColor to set.
     */
    public void setBgColor( Color bgColor )
    {
        this.bgColor = bgColor;
    }

    /**
     * @param fgColor The fgColor to set.
     */
    public void setFgColor( Color fgColor )
    {
        this.fgColor = fgColor;
    }

    /**
     * @param fontName The fontName to set.
     */
    public void setFontName( String fontName )
    {
        this.fontName = fontName;
    }

    /**
     * @param fontSize The fontSize to set.
     */
    public void setFontSize( int fontSize )
    {
        this.fontSize = fontSize;
    }

    /**
     * @param fontStyle The fontStyle to set.
     */
    public void setFontStyle( int fontStyle )
    {
        this.fontStyle = fontStyle;
    }

    /**
     * @param imageType The imageType to set.
     */
    public void setImageType( int imageType )
    {
        this.imageType = imageType;
    }

    /**
     * @param padBottom The padBottom to set.
     */
    public void setPadBottom( int padBottom )
    {
        this.padBottom = padBottom;
    }

    /**
     * @param padLeft The padLeft to set.
     */
    public void setPadLeft( int padLeft )
    {
        this.padLeft = padLeft;
    }

    /**
     * @param padRight The padRight to set.
     */
    public void setPadRight( int padRight )
    {
        this.padRight = padRight;
    }

    /**
     * Sets all paddings to the same value.
     * @param pad The padRight, padLeft, padTop andpadBottom to set.
     */
    public void setPad( int pad )
    {
        this.padRight = pad;
        this.padLeft = pad;
        this.padTop = pad;
        this.padBottom = pad;
    }

    /**
     * @param padTop The padTop to set.
     */
    public void setPadTop( int padTop )
    {
        this.padTop = padTop;
    }

    /**
     * @param renderHints The renderHints to set.
     */
    public void setRenderHints( Map renderHints )
    {
        this.renderHints = renderHints;
    }

    /**
     * @param renderWidth The renderWidth to set.
     */
    public void setRenderWidth( int renderWidth )
    {
        this.renderWidth = renderWidth;
    }

    /**
     * @param templateImage The templateImage to set.
     */
    public void setTemplateImage( BufferedImage templateImage )
    {
        this.templateImage = templateImage;
    }

    /**
     * @param tileBackgroundImage The tileBackgroundImage to set.
     */
    public void setTileBackgroundImage( int tileBackgroundImage )
    {
        this.tileBackgroundImage = tileBackgroundImage;
    }

    /**
     * @param backgroundImageUrl The backgroundImageUrl to set.
     */
    public void setBackgroundImageUrl( String backgroundImageUrl )
    {
        this.backgroundImageUrl = backgroundImageUrl;
    }

    /**
     * @param maxRows The maxRows to set.
     */
    public void setMaxRows( int maxRows )
    {
        this.maxRows = maxRows;
    }

    /**
     * @param trimEdges The trimEdges to set.
     */
    public void setTrimEdges( int trimEdges )
    {
        this.trimEdges = trimEdges;
    }

    /**
     * @param imageFormatName the format of the image. ie ( PNG, GIF);
     */
    public void setImageFormatName( String imageFormatName )
    {
        this.imageFormatName = imageFormatName;
    }
    
    /**
     * Get the image format name, default is "png" if none is set. 
     * @return a string with the image format name used by the renderer.
     */
    public String getImageFormatName()
    {
    	return this.imageFormatName;
    }
    
	public void setTwirlAspect(float twirlAspect)
	{
		this.twirlAspect = twirlAspect;
	}

	public void setMarbleXScale(float marbleXScale)
	{
		this.marbleXScale = marbleXScale;
	}

	public void setMarbleYScale(float marbleYScale)
	{
		this.marbleYScale = marbleYScale;
	}

	public void setMarbleTurbulence(float marbleTurbulence)
	{
		this.marbleTurbulence = marbleTurbulence;
	}

	public void setMarbleAmount(float marbleAmount)
	{
		this.marbleAmount = marbleAmount;
	}

}
