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
package org.infoglue.cms.applications.workflowtool.util;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetSchema;

/**
 * 
 */
public class InfogluePropertySet implements PropertySet {
	/**
	 * 
	 */
    private final static Logger logger = Logger.getLogger(InfogluePropertySet.class.getName());

	/**
	 * 
	 */
	private static final String UTF8_ENCODING = "utf-8";
	
	/**
	 * 
	 */
	private final PropertySet delegate;

	
	
	/**
	 * 
	 */
	public InfogluePropertySet(final PropertySet delegate) 
	{ 
		this.delegate = delegate; 
	}
	
	/**
	 * 
	 */
	public void setDataString(final String key, final String value) throws PropertyException 
	{
		if(value == null || value.length() == 0)
		{
			if(exists(key))
			{
				remove(key);
			}
		}
		else
		{
			try 
			{
				logger.debug("PropertysetHelper.setData(\"" + key + "\",\"" + value + "\")");
				setData(key, value.getBytes(UTF8_ENCODING));
			} 
			catch(UnsupportedEncodingException e) 
			{
				throw new PropertyException("Unable to set data for [" + key + "].");
			}
		}
		
	}

	/**
	 * 
	 */
	public String getDataString(final String key) throws PropertyException 
	{
		try 
		{
			final byte[] b = getData(key);
			final String value = (b == null) ? null : new String(b, UTF8_ENCODING); 
			logger.debug("PropertysetHelper.getData(\"" + key + "\")=\"" + (value == null ? "NULL" : value) + "\"");
			return value;
		} 
		catch(UnsupportedEncodingException e) 
		{
			throw new PropertyException("Unable to get data for [" + key + "].");
		}
	}
	
	
	/**
	 * 
	 */
	public void removeKeys(final String key, final boolean isPrefix) throws PropertyException 
	{
		if(key == null)
		{
			remove();
		}
		else
		{
			if(isPrefix)
			{
				for(Iterator i = getKeys(key).iterator(); i.hasNext(); )
				{
					remove(i.next().toString());
				}
			}
			else
			{
				if(exists(key))
				{
					remove(key);
				}
			}
		}
	}

	/**
	 * 
	 */
	public String getAsString(final String key)
	{
		if(!exists(key) || getAsActualType(key) == null)
		{
			return null;
		}
		
		switch(getType(key)) 
		{
			case PropertySet.BOOLEAN:
				return new Boolean(getBoolean(key)).toString();
			case PropertySet.DATA:
				return getDataString(key);
			case PropertySet.DATE:
				return getDate(key).toString();
			case PropertySet.DOUBLE:
				return new Double(getDouble(key)).toString();
			case PropertySet.INT:
				return new Integer(getInt(key)).toString();
			case PropertySet.LONG:
				return new Long(getLong(key)).toString();
			case PropertySet.STRING:
				return getString(key);
			case PropertySet.TEXT:
				return getText(key);
			default:
				logger.warn("Unsupported type [" + getType(key) + "].");
		}
		return null;
	}
	
	
	// -----------------------------------------------------------------------------
	// --- delegate
	// -----------------------------------------------------------------------------

	/**
	 * 
	 */
	public boolean exists(String key) throws PropertyException 
	{ 
		return delegate.exists(key); 
	}

	/**
	 * 
	 */
	public Object getAsActualType(String key) throws PropertyException
	{
		return delegate.exists(key) ? delegate.getAsActualType(key) : null; 
	}

	/**
	 * 
	 */
	public boolean getBoolean(String key) throws PropertyException 
	{ 
		return delegate.getBoolean(key); 
	}

	/**
	 * 
	 */
	public byte[] getData(String key) throws PropertyException 
	{ 
		return delegate.getData(key); 
	}

	/**
	 * 
	 */
	public Date getDate(String key) throws PropertyException
	{ 
		return delegate.getDate(key); 
	}

	/**
	 * 
	 */
	public double getDouble(String key) throws PropertyException 
	{ 
		return delegate.getDouble(key); 
	}

	/**
	 * 
	 */
	public int getInt(String key) throws PropertyException 
	{ 
		return delegate.getInt(key); 
	}

	/**
	 * 
	 */
	public Collection getKeys() throws PropertyException 
	{ 
		return delegate.getKeys(); 
	}

	/**
	 * 
	 */
	public Collection getKeys(int type) throws PropertyException 
	{ 
		return delegate.getKeys(type); 
	}

	/**
	 * 
	 */
	public Collection getKeys(String prefix, int type) throws PropertyException 
	{ 
		return delegate.getKeys(prefix, type); 
	}

	/**
	 * 
	 */
	public Collection getKeys(String prefix) throws PropertyException 
	{ 
		return delegate.getKeys(prefix); 
	}

	/**
	 * 
	 */
	public long getLong(String key) throws PropertyException 
	{ 
		return delegate.getLong(key); 
	}

	/**
	 * 
	 */
	public Object getObject(String key) throws PropertyException 
	{ 
		return delegate.getObject(key); 
	}

	/**
	 * 
	 */
	public Properties getProperties(String key) throws PropertyException 
	{ 
		return delegate.getProperties(key); 
	}

	/**
	 * 
	 */
	public PropertySetSchema getSchema() throws PropertyException 
	{ 
		return delegate.getSchema(); 
	}

	/**
	 * 
	 */
	public String getString(String key) throws PropertyException 
	{
		return delegate.getString(key); 
	}

	/**
	 * 
	 */
	public String getText(String key) throws PropertyException 
	{ 
		return delegate.getText(key); 
	}

	/**
	 * 
	 */
	public int getType(String key) throws PropertyException 
	{ 
		return delegate.getType(key); 
	}

	/**
	 * 
	 */
	public Document getXML(String key) throws PropertyException 
	{ 
		return delegate.getXML(key); 
	}

	/**
	 * 
	 */
	public void init(Map config, Map args) 
	{ 
		delegate.init(config, args); 
	}

	/**
	 * 
	 */
	public boolean isSettable(String property) 
	{ 
		return delegate.isSettable(property); 
	}

	/**
	 * 
	 */
	public void remove() throws PropertyException 
	{ 
		delegate.remove(); 
	}

	/**
	 * 
	 */
	public void remove(String key) throws PropertyException 
	{ 
		delegate.remove(key); 
	}

	/**
	 * 
	 */
	public void setAsActualType(String key, Object value) throws PropertyException 
	{ 
		delegate.setAsActualType(key, value); 
	}

	/**
	 * 
	 */
	public void setBoolean(String key, boolean value) throws PropertyException 
	{ 
		delegate.setBoolean(key, value); 
	}

	/**
	 * 
	 */
	public void setData(String key, byte[] value) throws PropertyException 
	{ 
		delegate.setData(key, value); 
	}

	/**
	 * 
	 */
	public void setDate(String key, Date value) throws PropertyException
	{ 
		delegate.setDate(key, value); 
	}

	/**
	 * 
	 */
	public void setDouble(String key, double value) throws PropertyException 
	{ 
		delegate.setDouble(key, value); 
	}

	/**
	 * 
	 */
	public void setInt(String key, int value) throws PropertyException 
	{ 
		delegate.setInt(key, value); 
	}

	/**
	 * 
	 */
	public void setLong(String key, long value) throws PropertyException 
	{ 
		delegate.setLong(key, value); 
	}

	/**
	 * 
	 */
	public void setObject(String key, Object value) throws PropertyException 
	{ 
		delegate.setObject(key, value); 
	}

	/**
	 * 
	 */
	public void setProperties(String key, Properties value) throws PropertyException 
	{ 
		delegate.setProperties(key, value); 
	}

	/**
	 * 
	 */
	public void setSchema(PropertySetSchema schema) throws PropertyException 
	{ 
		delegate.setSchema(schema); 
	}

	/**
	 * 
	 */
	public void setString(String key, String value) throws PropertyException 
	{ 
		delegate.setString(key, value); 
	}

	/**
	 * 
	 */
	public void setText(String key, String value) throws PropertyException 
	{ 
		delegate.setText(key, value); 
	}

	/**
	 * 
	 */
	public void setXML(String key, Document value) throws PropertyException 
	{ 
		delegate.setXML(key, value); 
	}

	/**
	 * 
	 */
	public boolean supportsType(int type) 
	{ 
		return delegate.supportsType(type); 
	}

	/**
	 * 
	 */
	public boolean supportsTypes() 
	{ 
		return delegate.supportsTypes(); 
	}
}