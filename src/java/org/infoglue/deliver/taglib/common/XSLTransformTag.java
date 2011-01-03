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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.FeatureKeys;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.tinytree.TinyBuilder;
import net.sf.saxon.tinytree.TinyDocumentImpl;

import org.apache.log4j.Logger;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.Timer;
import org.w3c.dom.Document;

/**
 * This tag help modifying texts in different ways. An example is to html-encode strings or replace all 
 * whitespaces with &nbsp; or replacing linebreaks with <br/>  
 */

public class XSLTransformTag extends TemplateControllerTag 
{
	private final static Logger logger = Logger.getLogger(XSLTransformTag.class.getName());

	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8603406098980150888L;
	
	private Object xml;
	private String xmlFile;
	private String xmlString;
	private Object source;
	private boolean cacheStyle = true;
	private String styleFile;
	private String styleString;
	private String outputFormat = "string";
	
	private Map parameters = new HashMap();
	private Map outputParameters = new HashMap();

	/**
	 * Default constructor.
	 */
	public XSLTransformTag() 
	{
		super();
	}
	
	/**
	 * Initializes the parameters to make it accessible for the children tags (if any).
	 * 
	 * @return indication of whether to evaluate the body or not.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doStartTag() throws JspException 
	{
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Process the end tag. Modifies the string according to settings made.  
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		Timer timer = new Timer();
		
		java.lang.System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
		
		Source xmlSource = null;
		
		if(xml != null)
		{
			if(xml instanceof String)
				xmlSource = new StreamSource(new StringReader(this.xmlString));	
			else if(xml instanceof Reader)
				xmlSource = new StreamSource((Reader)xml);
		}
		else if(this.source != null)
		{
			if(logger.isDebugEnabled())	
				logger.info("Input:" + this.source.getClass().getName());
			
			if(this.source instanceof DOMResult)
				xmlSource = new DOMSource(((DOMResult)this.source).getNode());
			else if(this.source instanceof DOMSource)
				xmlSource = (DOMSource)this.source;
			else if(this.source instanceof SAXSource)
				xmlSource = (SAXSource)this.source;
			else if(this.source instanceof StreamSource)
				xmlSource = (StreamSource)this.source;
			else if(this.source instanceof Document)
				xmlSource = new DOMSource((Document)this.source);
			else if(this.source instanceof NodeInfo)
				xmlSource = (Source)this.source;
			else if(this.source instanceof TinyDocumentImpl)
				xmlSource = (TinyDocumentImpl)this.source;
			
			else
				throw new JspException("Bad source - must be either org.w3c.Document or javax.xml.transform.Source");
		}
		else if(this.xmlFile != null)
		{
			xmlSource = new StreamSource(new File(this.xmlFile));
		}
		else if(this.xmlString != null)
		{
			xmlSource = new StreamSource(new StringReader(this.xmlString));			
		}
		
		Templates pss = null;
        Transformer transformer = null;
        
		try 
		{
            pss = tryCache(this.styleFile, this.styleString, cacheStyle);
            transformer = pss.newTransformer();
            
            if(logger.isDebugEnabled())	
				logger.info("outputFormat:" + this.outputFormat);

			if(this.outputFormat.equalsIgnoreCase("string"))
            {
	            java.io.ByteArrayOutputStream outputXmlResult = new java.io.ByteArrayOutputStream();
	            BufferedOutputStream bos = new BufferedOutputStream(outputXmlResult);
	            
	            Iterator parametersIterator = parameters.keySet().iterator();
	            while(parametersIterator.hasNext())
	            {
	            	String name = (String)parametersIterator.next();
	            	Object value = parameters.get(name);
	            	transformer.setParameter(name, value);
	            }
	            Iterator outputParametersIterator = outputParameters.keySet().iterator();
	            while(parametersIterator.hasNext())
	            {
	            	String name = (String)parametersIterator.next();
	            	String value = (String)parameters.get(name);
		            transformer.setOutputProperty(name, value);
	            }
	            transformer.transform(xmlSource, new StreamResult(bos));
	            bos.close();
	            
	            String result = outputXmlResult.toString();
	            setResultAttribute(result);
            }
            else if(this.outputFormat.equalsIgnoreCase("document"))
            {
            	DOMResult domResult = new DOMResult();
                
	            Iterator parametersIterator = parameters.keySet().iterator();
	            while(parametersIterator.hasNext())
	            {
	            	String name = (String)parametersIterator.next();
	            	Object value = parameters.get(name);
	            	transformer.setParameter(name, value);
	            }
	            Iterator outputParametersIterator = outputParameters.keySet().iterator();
	            while(parametersIterator.hasNext())
	            {
	            	String name = (String)parametersIterator.next();
	            	String value = (String)parameters.get(name);
		            transformer.setOutputProperty(name, value);
	            }

	            transformer.transform(xmlSource, domResult);
	            setResultAttribute(domResult.getNode());
	        }
            else if(this.outputFormat.equalsIgnoreCase("tinyDocument"))
            {
	            Iterator parametersIterator = parameters.keySet().iterator();
	            while(parametersIterator.hasNext())
	            {
	            	String name = (String)parametersIterator.next();
	            	Object value = parameters.get(name);
	            	transformer.setParameter(name, value);
	            }
	            Iterator outputParametersIterator = outputParameters.keySet().iterator();
	            while(parametersIterator.hasNext())
	            {
	            	String name = (String)parametersIterator.next();
	            	String value = (String)parameters.get(name);
		            transformer.setOutputProperty(name, value);
	            }

	            TinyBuilder builder = new TinyBuilder();

	            transformer.transform(xmlSource, builder);
	            setResultAttribute(builder.getCurrentRoot());
            }
			
			if(logger.isInfoEnabled())
				timer.printElapsedTime("Saxon Transform to dom document took");            
		} 
		catch (Exception e) 
		{
            logger.error("Error transforming with SAXON:" + e.getMessage(), e);
        }
        finally
        {
        	try
        	{
	            transformer.clearParameters();
	            transformer.reset();
	            parameters.clear();
	            outputParameters.clear();
        	}
        	catch (NoSuchMethodError e) 
        	{
        		logger.warn("Problem resetting transformer -wrong java version:" + e.getMessage());
        	}
        	catch (Exception e) 
        	{
        		logger.warn("Problem resetting transformer:" + e.getMessage(), e);
			}
	        
        	transformer = null;
            pss = null;
            xmlSource = null;
        
    		this.xml = null;
    		this.xmlFile = null;
    		this.source = null;
    		this.xmlString = null;
    		this.styleFile = null;
    		this.styleString = null;
    		this.outputFormat = "string";
    		this.cacheStyle = true;
    		
    		java.lang.System.clearProperty("javax.xml.transform.TransformerFactory");
        }
        		
        return EVAL_PAGE;
    }

       
	/**
	 * Maintain prepared stylesheets in memory for reuse
	 */

    private synchronized Templates tryCache(String url, String xslString, boolean cacheTemplate) throws Exception 
    {
    	Templates x = null;
    	
        if(url != null)
        {
	    	String path = this.getController().getHttpServletRequest().getRealPath(url);
	        if (path==null) 
	        {
	            throw new Exception("Stylesheet " + url + " not found");
	        }
	        
	        x = (Templates)CacheController.getCachedObject("XSLTemplatesCache", path);
	        if (x==null) 
	        {
	            TransformerFactory factory = TransformerFactory.newInstance();
	            x = factory.newTemplates(new StreamSource(new File(path)));
	            if(cacheTemplate)
	            	CacheController.cacheObject("XSLTemplatesCache", path, x);
	        }
        }
        else if(xslString != null)
        {
	        x = (Templates)CacheController.getCachedObject("XSLTemplatesCache", xslString.hashCode());
	        if (x==null) 
	        {
	            TransformerFactory factory = TransformerFactory.newInstance();
	            x = factory.newTemplates(new StreamSource(new StringReader(xslString)));
	            if(cacheTemplate)
		            CacheController.cacheObject("XSLTemplatesCache", xslString.hashCode(), x);
	        }
        }
        
        return x;
    }

    
    public void setXml(String xml) throws JspException
    {
        this.xml = evaluate("XSLTransform", "xmlFile", xmlFile, Object.class);
    }    
    
    public void setXmlFile(String xmlFile) throws JspException
    {
        this.xmlFile = evaluateString("XSLTransform", "xmlFile", xmlFile);
    }    

    public void setXmlString(String xmlString) throws JspException
    {
        this.xmlString = evaluateString("XSLTransform", "xmlString", xmlString);
    }    

    public void setSource(String source) throws JspException
    {
        this.source = evaluate("XSLTransform", "source", source, Object.class);
    }    

    public void setStyleFile(String styleFile) throws JspException
    {
        this.styleFile = evaluateString("XSLTransform", "styleFile", styleFile);
    }    

    public void setStyleString(String styleString) throws JspException
    {
        this.styleString = evaluateString("XSLTransform", "styleString", styleString);
    }    

    public void setCacheStyle(boolean cacheStyle) throws JspException
    {
        this.cacheStyle = cacheStyle;
    }    

    public void setOutputFormat(String outputFormat) throws JspException
    {
        this.outputFormat = evaluateString("XSLTransform", "outputFormat", outputFormat);
    }    

    protected final void addParameter(final String name, final Object value, final String scope)
	{
    	if(scope != null && scope.equalsIgnoreCase("outputProperty"))
    		outputParameters.put(name, value);
    	else
    		parameters.put(name, value);
	}

}
