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

package org.infoglue.cms.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
 
/**
 * This class is an utility class meant to be filled with reusable snippets of code concerning handling of XML 
 * in different shapes. No application specific code can be put here!!!
 * 
 * @author Mattias Bogeblad
 */
 
public class XMLHelper
{
	
    /**
     * Serializes the DOM-tree to a stringBuffer. Recursive method!
     *
     * @param node Node to start examining the tree from.
     * @param writeString The StringBuffer you want to fill with xml.
     * @return The StringBuffer containing the xml.
     * 
     * @since 2002-12-12
     * @author Mattias Bogeblad
     */
    
    public static StringBuffer serializeDom(Node node, StringBuffer writeString)
    {
        int type = node.getNodeType();
        try
        {
            switch (type)
            {
                // print the document element
                case Node.DOCUMENT_NODE:
                {
                    writeString.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    writeString = serializeDom(((Document)node).getDocumentElement(), writeString);
                    break;
                }
                // print element with attributes
                case Node.ELEMENT_NODE:
                {
                    writeString.append("<");
                    writeString.append(node.getNodeName());
                    NamedNodeMap attrs = node.getAttributes();
                    for (int i = 0; i < attrs.getLength(); i++)
                    {
                        Node attr = attrs.item(i);
                        String outString = " " + attr.getNodeName() + "=\"" + replaceSpecialCharacters(attr.getNodeValue()) + "\"";
                        writeString.append(outString);
                    }
                    writeString.append(">");
                    NodeList children = node.getChildNodes();
                    if (children != null)
                    {
                        int len = children.getLength();
                        for (int i = 0; i < len; i++)
                        writeString = serializeDom(children.item(i), writeString);
                    }
                    break;
            }
            // handle entity reference nodes
            case Node.ENTITY_REFERENCE_NODE:
            {
                String outString = "&" + node.getNodeName() + ";";
                writeString.append(outString);
                break;
            }
            // print cdata sections
            case Node.CDATA_SECTION_NODE:
            {
                String outString = "<![CDATA[" + node.getNodeValue() + "]]>";
                writeString.append(outString);
                break;
            }
            // print text
            case Node.TEXT_NODE:
            {
                writeString.append(replaceSpecialCharacters(node.getNodeValue()));
                break;
            }
            // print processing instruction
            case Node.PROCESSING_INSTRUCTION_NODE:
            {
                String data = node.getNodeValue();
                String outString = "<?" + node.getNodeName() + " " + data + "?>";
                writeString.append(outString);
                break;
            }
            }
            if (type == Node.ELEMENT_NODE)
            {
            	String outString = "</" + node.getNodeName() + ">";
            	writeString.append(outString);
            }
        }
        catch (Exception e)
        {

        }
        return writeString;
    }


	/**
	 * This method replaces special character with their xml-counterpart.
	 */
	
	public static String replaceSpecialCharacters(String value)
	{
		if(value == null)
			return "";
		
			
		StringBuffer newValue = new StringBuffer();
        for (int i = 0; i < value.length(); i++)
        {   
            char c = value.charAt(i);
            if (c == '&')
            {
                newValue.append("&amp;");
            }
            else if (c == '<')
            {
                newValue.append("&lt;");
            }    
            else if (c == '>')
            {
                newValue.append("&gt;");
            }
            else if (c == '\'')
            {
                newValue.append("&apos;");
            }        
            else if (c == '\"')
            {
                newValue.append("&quot;");
            }    
            else
            {
            	if(isLegalCharacter((int)c))
                	newValue.append(c);
		    }
        }
		return newValue.toString();
	} 
	
	
	private static boolean isLegalCharacter(int ch)
	{
		return ch == 0x9 || ch == 0xA || ch == 0xD || (ch >= 0x20 && ch <= 0xFFFD) || (ch >= 0x1000 && ch <= 0x7FFFFFFF);

	}
	
	/**
	 * This method fetches the document-root(DOM) from a xml-document located on disc specified
	 * by user argument.
	 * 
     * @since 2002-12-16
     * @author Mattias Bogeblad
     */
	  
	public static Document readDocumentFromFile(File xmlFile) throws IOException, SAXException
	{
		InputSource xmlSource = new InputSource(new FileReader(xmlFile));
		DOMParser parser = new DOMParser();
		parser.parse(xmlSource);
		return parser.getDocument();
	}
	
		
	/**
	 * This method fetches the document-root(DOM) from a xml-document located in a byte[] specified
	 * by user argument.
	 * 
     * @since 2002-12-16
     * @author Mattias Bogeblad
     */
	  
	public static Document readDocumentFromByteArray(byte[] xml) throws IOException, SAXException
	{
		InputSource xmlSource = new InputSource(new BufferedInputStream(new ByteArrayInputStream(xml)));
		DOMParser parser = new DOMParser();
		parser.parse(xmlSource);
		Document document = parser.getDocument();
		parser = null;
		return document;
	}

}