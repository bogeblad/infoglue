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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * A Tag used for rendering images from text. Using the functionality 
 * in BasicTemplateController and ComponentLogic.
 * @see rg.infoglue.deliver.util.graphics.AdvancedImageRenderer
 * @author Per Jonsson per.jonsson@it-huset.se
 */
public class TextRenderTag extends ComponentLogicTag
{
    private static final long serialVersionUID = 523722286871322906L;

    protected Object result;

    protected String text = null;

    private String propertyName = null;

    private Integer contentId = null;

    protected Map renderAttributes = null;

    public TextRenderTag()
    {
        super();
    }

    public int doEndTag() throws JspException
    {
        if ( contentId != null )
        {
            result = this.getController().getRenderedTextUrl( contentId, text, renderAttributes );
        }
        else if ( propertyName != null )
        {
            result = this.getComponentLogic().getRenderedTextUrl( propertyName, text, renderAttributes );
        }
        else
        {
            result = this.getController().getRenderedTextUrl( text, renderAttributes );
        }

        this.produceResult( result );

        return EVAL_PAGE;
    }

    protected void setAttribute( String key, Object value )
    {
        if ( renderAttributes == null )
        {
            renderAttributes = new HashMap();
        }
        if ( key != null && value != null )
        {
            renderAttributes.put( key, value.toString() );
        }
    }

    /**
     * @param propertyName The propertyName to set.
     */
    public void setPropertyName( String propertyName ) throws JspException
    {
        this.propertyName = evaluateString( "textRender", "propertyName", propertyName );
    }

    /**
     * @param contentId The contentId to set.
     */
    public void setContentId( final String contentId ) throws JspException
    {
        this.contentId = evaluateInteger( "textRender", "contentId", contentId );
    }

    /**
     * @param text The text to set.
     */
    public void setText( String text ) throws JspException
    {
        this.text = evaluateString( "textRender", "text", text );
    }

    /**
     * @param align The align to set.
     */
    public void setAlign( String align ) throws JspException
    {
        this.setAttribute( "align", evaluateInteger( "textRender", "align", align ) );
    }

    /**
     * @param backgroundImageUrl The backgroundImageUrl to set.
     */
    public void setBackgroundImageUrl( String backgroundImageUrl ) throws JspException
    {
        this.setAttribute( "backgroundImageUrl",
                evaluateString( "textRender", "backgroundImageUrl", backgroundImageUrl ) );
    }

    /**
     * @param bgColor The bgColor to set.
     */
    public void setBgColor( String bgColor ) throws JspException
    {
        this.setAttribute( "bgColor", evaluateString( "textRender", "bgColor", bgColor ) );
    }

    /**
     * @param fgColor The fgColor to set.
     */
    public void setFgColor( String fgColor ) throws JspException
    {
        this.setAttribute( "fgColor", evaluateString( "textRender", "fgColor", fgColor ) );
    }

    /**
     * @param fontName The fontName to set.
     */
    public void setFontName( String fontName ) throws JspException
    {
        this.setAttribute( "fontName", evaluateString( "textRender", "fontName", fontName ) );
    }

    /**
     * @param fontSize The fontSize to set.
     */
    public void setFontSize( String fontSize ) throws JspException
    {
        this.setAttribute( "fontSize", evaluateInteger( "textRender", "fontSize", fontSize ) );
    }

    /**
     * @param fontStyle The fontStyle to set.
     */
    public void setFontStyle( String fontStyle ) throws JspException
    {
        this.setAttribute( "fontStyle", evaluateInteger( "textRender", "fontStyle", fontStyle ) );
    }

    /**
     * @param imageType The imageType to set.
     */
    public void setImageType( String imageType ) throws JspException
    {
        this.setAttribute( "imageType", evaluateInteger( "textRender", "imageType", imageType ) );
    }

    /**
     * @param maxRows The maxRows to set.
     */
    public void setMaxRows( String maxRows ) throws JspException
    {
        this.setAttribute( "maxRows", evaluateInteger( "textRender", "maxRows", maxRows ) );
    }

    /**
     * @param pad The pad to set.
     */
    public void setPad( String pad ) throws JspException
    {
        this.setAttribute( "pad", evaluateInteger( "textRender", "pad", pad ) );
    }

    /**
     * @param padBottom The padBottom to set.
     */
    public void setPadBottom( String padBottom ) throws JspException
    {
        this.setAttribute( "padBottom", evaluateInteger( "textRender", "padBottom", padBottom ) );
    }

    /**
     * @param padLeft The padLeft to set.
     */
    public void setPadLeft( String padLeft ) throws JspException
    {
        this.setAttribute( "padLeft", evaluateInteger( "textRender", "padLeft", padLeft ) );
    }

    /**
     * @param padRight The padRight to set.
     */
    public void setPadRight( String padRight ) throws JspException
    {
        this.setAttribute( "padRight", evaluateInteger( "textRender", "padRight", padRight ) );
    }

    /**
     * @param padTop The padTop to set.
     */
    public void setPadTop( String padTop ) throws JspException
    {
        this.setAttribute( "padTop", evaluateInteger( "textRender", "padTop", padTop ) );
    }

    /**
     * @param renderWidth The renderWidth to set.
     */
    public void setRenderWidth( String renderWidth ) throws JspException
    {
        this.setAttribute( "renderWidth", evaluateInteger( "textRender", "renderWidth", renderWidth ) );
    }

    /**
     * @param tileBackgroundImage The tileBackgroundImage to set.
     */
    public void setTileBackgroundImage( String tileBackgroundImage ) throws JspException
    {
        this.setAttribute( "tileBackgroundImage", evaluateInteger( "textRender", "tileBackgroundImage",
                tileBackgroundImage ) );
    }

    /**
     * @param trimEdges The trimEdges to set.
     */
    public void setTrimEdges( String trimEdges ) throws JspException
    {
        this.setAttribute( "trimEdges", evaluateInteger( "textRender", "trimEdges", trimEdges ) );
    }
}