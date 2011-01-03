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

package org.infoglue.deliver.util.webservices;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;

/** 
 * This class helps in requesting information from an webservice.
 * @author Mattias Bogeblad
 */

public class InfoGlueWebServices
{
	private String serviceUrl = "";
	
	private boolean isSuccessfull;
	private String message;
	
	/**
	 * The constructor for this class.
	 */
	
	public InfoGlueWebServices()
	{	
	}
	
	/**
	 * A method to set the serviceUrl which is the endpoint of this call.
	 */
	
	public String getServiceUrl() 
	{
		return serviceUrl;
	}

	/**
	 * A method to get the serviceUrl which is the endpoint of this call.
	 */
	
	public void setServiceUrl(String serviceUrl) 
	{
		this.serviceUrl = serviceUrl;
	}

	/**
	 * This is the method that lets you create a content and versions.
	 */

	public Integer createContent(ContentVO contentVO, Integer parentContentId, Integer contentTypeDefinitionId, Integer repositoryId)
	{
		Integer newContentId = null;
		
		try
		{
		    Service service = new Service();
			Call call = (Call)service.createCall();

			String endpoint = this.serviceUrl;

			call.setTargetEndpointAddress(endpoint); //Set the target service host and service location,
			call.setOperationName(new QName("http://soapinterop.org/", "createContent")); //This is the target services method to invoke.
			call.setEncodingStyle( "http://schemas.xmlsoap.org/soap/encoding/" );

			//register the ContentVO class
	        QName poqn = new QName("http://www.soapinterop.org/ContentVO", "ContentVO");
	        Class cls = ContentVO.class;
	        call.registerTypeMapping(cls, poqn, BeanSerializerFactory.class, BeanDeserializerFactory.class);
	        
			QName qnameAttachment = new QName("urn:EchoAttachmentsService", "DataHandler");

			call.addParameter("contentVO", poqn, ParameterMode.IN); 
			call.addParameter("parentContentId", XMLType.XSD_INT, ParameterMode.IN);
			call.addParameter("contentTypeDefinitionId", XMLType.XSD_INT, ParameterMode.IN);
			call.addParameter("repositoryId", XMLType.XSD_INT, ParameterMode.IN);

			call.setReturnType(XMLType.XSD_INT);

			Object[] args = {contentVO, parentContentId, contentTypeDefinitionId, repositoryId};
			newContentId = (Integer)call.invoke(args); //Add the attachment.			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return newContentId;
	}
	
	/**
	 * This is the method that lets you create a contentversion.
	 */

	public Integer createContentVersion(ContentVersionVO contentVersionVO, Integer contentId, Integer languageId)
	{
		Integer newContentVersionId = null;
		
		try
		{
		    Service service = new Service();
			Call call = (Call)service.createCall();

			String endpoint = this.serviceUrl;

			call.setTargetEndpointAddress(endpoint); //Set the target service host and service location,
			call.setOperationName(new QName("http://soapinterop.org/", "createContentVersion")); //This is the target services method to invoke.
			call.setEncodingStyle( "http://schemas.xmlsoap.org/soap/encoding/" );

			//register the ContentVO class
	        QName poqn = new QName("http://www.soapinterop.org/ContentVersionVO", "ContentVersionVO");
	        Class cls = ContentVersionVO.class;
	        call.registerTypeMapping(cls, poqn, BeanSerializerFactory.class, BeanDeserializerFactory.class);
	        
			QName qnameAttachment = new QName("urn:EchoAttachmentsService", "DataHandler");

			call.addParameter("contentVersionVO", poqn, ParameterMode.IN); 
			call.addParameter("contentId", XMLType.XSD_INT, ParameterMode.IN);
			call.addParameter("languageId", XMLType.XSD_INT, ParameterMode.IN);

			call.setReturnType(XMLType.XSD_INT);

			Object[] args = {contentVersionVO, contentId, languageId};
			newContentVersionId = (Integer)call.invoke(args); //Add the attachment.			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return newContentVersionId;
	}
		
	
	
	/**
	 * This method returns true if the request to the webservice returned successfully.
	 */

	public boolean getIsSuccessfull()
	{
		return this.isSuccessfull;
	}

	/**
	 * This method sets if the request to the webservice returned successfully.
	 */

	public void setIsSuccessfull(boolean isSuccessfull)
	{
		this.isSuccessfull = isSuccessfull;
	}

	/**
	 * This method returns any message coming from the webservice.
	 */

	public String getMessage()
	{
		return this.message;
	}

	/**
	 * This method sets a message from the webservice.
	 */
	
	public void setMessage(String message)
	{
		this.message = message;
	}
}