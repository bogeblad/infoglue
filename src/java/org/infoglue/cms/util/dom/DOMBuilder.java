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

package org.infoglue.cms.util.dom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.xml.sax.InputSource;

/**
 * This class is a utility-class for handling DOM-objects.
 * 
 * @author Mattias Bogeblad
 */ 

public class DOMBuilder
{
    private final static Logger logger = Logger.getLogger(DOMBuilder.class.getName());

 	/**
 	 * This method creates a new Document.
 	 */
 	
 	public Document createDocument()
 	{
		return DocumentHelper.createDocument();
 	}
 	
	/**
	 * This method creates a new Document from an xml-string.
	 */
	
 	public Document getDocument(String xml) throws Exception
 	{
		if(xml == null)
			return null;
					
 		try
 		{
			InputSource xmlSource = new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8")));
 			SAXReader xmlReader = new SAXReader();
 			return xmlReader.read(xmlSource);
 			/*
			InputSource xmlSource = new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8")));
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			return buildDocment(parser.getDocument());
 			*/
 		}
 		catch(Exception e)
 		{
 			//e.printStackTrace();
			throw new SystemException(e);
 		}
 	}
 	
 	
	/** converts a W3C DOM document into a dom4j document */
	public Document buildDocment(org.w3c.dom.Document domDocument)
	{
	  DOMReader xmlReader = new DOMReader();
	  return xmlReader.read(domDocument);
	}

 	
	/**
	 * This method creates a new Document from a file.
	 */
	
	public Document getDocument(File file) throws Exception
	{
		if(file == null)
			return null;
			
		Document document = null;
		
		try
		{
			String xml = FileHelper.readUTF8FromFile(file);
			document = getDocument(xml);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
 		
		return document;
	}
 	
	/**
	 * A helper method to get nodes that has a namespace.
	 */

	public Node selectSingleNode(Node contextNode, String xpathExpression, String namespaceName, String namespaceValue)
	{
		Map namespaces = new HashMap();
		namespaces.put(namespaceName, namespaceValue);
		org.dom4j.XPath xpath = contextNode.createXPath( xpathExpression );
		xpath.setNamespaceURIs(namespaces);
		return xpath.selectSingleNode(contextNode);
	}

	
	/**
	 * This method adds an element with a given name to a given document.
	 */
	
 	public Element addElement(Document document, String elementName)
 	{
		Element newElement = document.addElement(elementName);
		return newElement;
 	}

	/**
	 * This method adds an element with a given name to a given parent element.
	 */
 	
	public Element addElement(Element element, String elementName)
	{
		Element newElement = element.addElement(elementName);
		return newElement;
	}

	/**
	 * This method adds an element to a given parent element.
	 */
	
	public void insertElement(Element element, Element childElement)
	{
		element.content().add(childElement);
	}

	/**
	 * This method adds a text-element with a given name to a given parent element.
	 */
	
	public Element addTextElement(Element element, String text)
	{
		Element newElement = element.addText(text);
		return newElement;
	}
	

	/**
	 * This method adds a cdata-element with a given name to a given parent element.
	 */
	
	public Element addCDATAElement(Element element, String text)
	{
		Element newElement = element.addCDATA(text);
		return newElement;
	}

	/**
	 * This method adds an attribute with a given name and value to a given element.
	 */
	
	public void addAttribute(Element element, String attributeName, String attributeValue)
	{
		element.addAttribute(attributeName, attributeValue);
	}

	/**
	 * This method writes a document to file.
	 */
	
	public void write(Document document, String fileName) throws Exception 
	{
		OutputFormat format = OutputFormat.createCompactFormat();
		format.setEncoding("UTF-8");
		XMLWriter writer = new XMLWriter(new FileWriter(fileName), format);
		writer.write(document);
		writer.close();	
	}

	/**
	 * This method writes a document to file nicely.
	 */
	
	public void writePretty(Document document, String fileName) throws IOException 
	{
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileWriter(fileName), format);
		writer.write(document);
		writer.close();	
	}
	
	/**
	 * This method writes a document to System.out.
	 */

	public void writeDebug(Document document) throws Exception 
	{
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter( System.out, format );
		writer.write( document );
	}

	public String encodeString(String value, String encoding) throws Exception
	{
		String encodedValue = new String(value.getBytes("UTF-8"), "ISO-8859-1");
		return encodedValue;
	}

	/**
	 * This method writes a element to System.out.
	 */
	
	public void writeDebug(Element element) throws Exception 
	{
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter( System.out, format );
		writer.write( element );
	}

	/**
	 * This method gets the xml as a string with the correct encoding.
	 */
	
	private String getEncodedString(Element element) throws Exception
	{
		OutputFormat outFormat = OutputFormat.createCompactFormat();
		outFormat.setEncoding("UTF-8");
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		XMLWriter out = new XMLWriter(bao, outFormat);
		out.write(element);
		out.flush();
		String s = bao.toString();
		logger.info("OUT: " + s);
		return s;
	}
	
	public String getFormattedDocument(Document doc, String encoding)
	{
	    return getFormattedDocument(doc, true, false, encoding);
	}
	
	public String getFormattedDocument(Document doc, boolean compact, boolean supressDecl, String encoding)
	{
	    OutputFormat format = compact ? OutputFormat.createCompactFormat() : OutputFormat.createPrettyPrint();
        format.setSuppressDeclaration(supressDecl);
		format.setEncoding(encoding);
		format.setExpandEmptyElements(false);
		StringWriter stringWriter = new StringWriter();
		XMLWriter writer = new XMLWriter(stringWriter, format);
		try
        {
            writer.write(doc);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
		return stringWriter.toString();
	}

}