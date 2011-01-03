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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.log4j.Logger;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * 
 */
class Parameter
{
	public final String name;
	public final QName type;
	
	public Parameter(final String name, final QName type)
	{
		this.name = name;
		this.type = type;
	}
}

/**
 * 
 */
public class DynamicWebservice 
{
	/**
	 * The class logger.
	 */
    private final static Logger logger = Logger.getLogger(DynamicWebservice.class.getName());
    
    /**
     * 
     */
    private static final String PRINCIPAL_ARGUMENT_NAME = "principal";
    
	/**
	 * 
	 */
	private static final String DEFAULT_NAMESPACE_URI = "http://soapinterop.org/";
	
	/**
	 * 
	 */
	private String targetEndpointAddress;
	
	/**
	 * 
	 */
	private String operationName;
	
	/**
	 * 
	 */
	private QName returnType;
	
	/**
	 * 
	 */
	private final List parameters = new ArrayList();
	
	/**
	 * 
	 */
	private final List arguments = new ArrayList();

	/**
	 * 
	 */
	private final Map mappings = new HashMap(); // <Class> -> <QName>
	
	/**
	 * 
	 */
	private final Map standardMappings = new HashMap(); // <Class> -> <QName>
	
	/**
	 * 
	 */
	private Service service;
	
	/**
	 * 
	 */
	private Call call;
	
	/**
	 * 
	 */
	private Object result;
	
	/**
	 * 
	 */
	private DynamicWebserviceSerializer serializer;
	
	
	/**
	 * 
	 */
	public DynamicWebservice(final InfoGluePrincipal remotePrincipal) 
	{
		super();
		configureStandardMappings();
		serializer = new DynamicWebserviceSerializer(standardMappings);
		addArgument(PRINCIPAL_ARGUMENT_NAME, remotePrincipal.getName());
	}

	/**
	 * 
	 */
	public void callService()
	{
		try
		{
			service = new Service();
			call = (Call) service.createCall();
			
			configureBasic();
			configureMappings();
			configureParameters();
			
			result = call.invoke(arguments.toArray());			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	private void configureStandardMappings()
	{
		standardMappings.put(Boolean.class, XMLType.XSD_BASE64);
		standardMappings.put(Boolean.class, XMLType.XSD_BOOLEAN);
		standardMappings.put(Double.class,  XMLType.XSD_DOUBLE);
		standardMappings.put(Float.class,   XMLType.XSD_FLOAT);
		standardMappings.put(Integer.class, XMLType.XSD_INT);
		standardMappings.put(Long.class,    XMLType.XSD_LONG);
		standardMappings.put(String.class,  XMLType.XSD_STRING);
	}
	
	/**
	 * 
	 */
	private void configureBasic()
	{
		call.setTimeout(30000);
		call.setTargetEndpointAddress(targetEndpointAddress);
		call.setOperationName(new QName(DEFAULT_NAMESPACE_URI, operationName));
		call.setEncodingStyle(Call.ENCODINGSTYLE_URI_PROPERTY);
		call.setReturnType(returnType);
	}
	
	/**
	 * 
	 */
	private void configureParameters()
	{
		for(final Iterator i = parameters.iterator(); i.hasNext(); )
		{
			final Parameter parameter = (Parameter) i.next();
			call.addParameter(parameter.name, parameter.type, ParameterMode.IN);
		}
	}
	
	/**
	 * 
	 */
	private void configureMappings()
	{
		for(final Iterator i = mappings.keySet().iterator(); i.hasNext(); )
		{
			final Class clazz = (Class) i.next();
			final QName type  = (QName) mappings.get(clazz);
			call.registerTypeMapping(clazz, type, BeanSerializerFactory.class, BeanDeserializerFactory.class);
		}
	}
	
	/**
	 * 
	 */
	public Object getResult()
	{
		return result;
	}
	
	/**
	 * 
	 */
	public void setTargetEndpointAddress(final String targetEndpointAddress) 
	{
		logger.debug("targetEndpointAddress=[" + targetEndpointAddress + "]");
		this.targetEndpointAddress = targetEndpointAddress;
	}

	/**
	 * 
	 */
	public void setOperationName(final String operationName) 
	{
		logger.debug("operationName=[" + operationName + "]");
		this.operationName = operationName;
	}
	
	/**
	 * 
	 */
	public void setReturnType(final Class c) 
	{
		logger.debug("returnType=[" + (c==null ? "null" : getClassName(c)) + "]");
		returnType = mappingForClass(c); // null is ok
	}

	/**
	 * 
	 */
	public void setReturnType(final Class c, QName type) 
	{
		logger.debug("returnType=[" + (c==null ? "null" : type.getLocalPart()) + "]");
		this.mappings.put(c, type);
		returnType = mappingForClass(c); // null is ok
	}

	/**
	 * 
	 */
	public void addArgument(final String name, final Object value)
	{
		assertNameNotNull(name);
		logger.debug("addArgument=[" + name + "," + value + "]");
		addArgument(name, mappingForClass(value.getClass()), value);
	}
	
	/**
	 * 
	 */
	public void addArgument(final String name, final Map value)
	{
		assertNameNotNull(name);

		logger.debug("addArgument=[" + name + "," + value + "] (Map)");
		addArgument(name, XMLType.SOAP_ARRAY, serializer.serializeMap(value).toArray());
	}
	
	/**
	 * 
	 */
	public void addArgument(final String name, final Collection value)
	{
		assertNameNotNull(name);

		logger.debug("addArgument=[" + name + "," + value + "] (Collection)");
		addArgument(name, XMLType.SOAP_ARRAY, serializer.serializeCollection(value).toArray());
	}

	/**
	 * 
	 */
	public void addArgument(final String name, final Collection value, final Class[] objectClass)
	{
		assertNameNotNull(name);

		logger.debug("addArgument=[" + name + "," + value + "] (Collection)");
		for(int i=0; i<objectClass.length; i++)
		{
			this.mappings.put(objectClass[i], new QName("infoglue", getClassName(objectClass[i])));
			mappingForClass(objectClass[i], "infoglue");
		}
		addArgument(name, XMLType.SOAP_ARRAY, serializer.serializeCollection(value).toArray());
	}

	/**
	 * 
	 */
	public void addArgument(final String name, final Map value, final Class[] objectClass)
	{
		assertNameNotNull(name);

		logger.debug("addArgument=[" + name + "," + value + "] (Map)");
		for(int i=0; i<objectClass.length; i++)
		{
			this.mappings.put(objectClass[i], new QName("infoglue", getClassName(objectClass[i])));
			mappingForClass(objectClass[i], "infoglue");
		}
		addArgument(name, XMLType.SOAP_ARRAY, serializer.serializeMap(value).toArray());
	}
	

	/**
	 * 
	 */
	public void addNonSerializedArgument(final String name, final Collection value)
	{
		assertNameNotNull(name);

		logger.debug("addArgument=[" + name + "," + value + "] (Collection)");
		addArgument(name, XMLType.SOAP_ARRAY, value);
	}

	/**
	 * 
	 */
	private void assertNameNotNull(final String argument) 
	{
		if(argument == null)
		{
			throw new IllegalArgumentException("A parameter name can't be null.");
		}
	}
	
	/**
	 * 
	 */
	private void addArgument(final String name, final QName type, final Object value)
	{
	    parameters.add(new Parameter(name, type));
		arguments.add(value);
	}

	/**
	 * 
	 */
	private QName mappingForClass(final Class c)
	{
		if(c == null)
		{
			return null;
		}
		if(standardMappings.containsKey(c))
		{
			return (QName) standardMappings.get(c);
		}
		if(mappings.containsKey(c))
		{
			return (QName) mappings.get(c);
		}
		return addMapping(c);
	}

	/**
	 * 
	 */
	private QName mappingForClass(final Class c, final String namespace)
	{
		if(c == null)
		{
			return null;
		}
		if(standardMappings.containsKey(c))
		{
			return (QName) standardMappings.get(c);
		}
		if(mappings.containsKey(c))
		{
			return (QName) mappings.get(c);
		}
		return addMapping(c, namespace);
	}

	/**
	 * 
	 */
	private QName addMapping(final Class c) {
		final String className = getClassName(c);
		final QName type = new QName(DEFAULT_NAMESPACE_URI + className, className);
		mappings.put(c, type);
		logger.debug("addMapping=[" + c.getName() + "," + type + "]");
		return type;
	}

	/**
	 * 
	 */
	private QName addMapping(final Class c, String namespace) {
		final String className = getClassName(c);
		final QName type = new QName(namespace + className, className);
		mappings.put(c, type);
		logger.debug("addMapping=[" + c.getName() + "," + type + "]");
		return type;
	}

	/**
	 * 
	 */
	public String getClassName(final Class c)
	{
		final int firstChar = c.getName().lastIndexOf('.') + 1;
		return (firstChar > 0) ? c.getName().substring(firstChar) : c.getName();
	}
}