/**
 * 
 */
package org.infoglue.deliver.util.webservices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class DynamicWebserviceSerializer 
{
	/**
	 * 
	 */
	private static final Object MARKER = null;
	
	/**
	 * 
	 */
	private Map legalTypes;
	
	/**
	 * 
	 */
	public DynamicWebserviceSerializer() 
	{
		super();
	}

	/**
	 * 
	 */
	public DynamicWebserviceSerializer(final Map legalTypes) 
	{
		super();
		this.legalTypes = legalTypes;
	}
	
	/**
	 * 
	 */
	public List serializeCollection(final Collection collection) 
	{
		final Collection c = (collection == null) ? new ArrayList() : collection;
		
		final List result = createResultList(c.getClass(), c.size());
		for(final Iterator i = collection.iterator(); i.hasNext(); )
		{
			result.addAll(serializeObject(i.next()));
		}
		return result;
	}
	
	/**
	 * 
	 */
	public List serializeMap(final Map map) {
		final Map m = (map == null) ? new HashMap() : map;
		
		final List result = createResultList(m.getClass(), m.size());
		for(final Iterator i = map.keySet().iterator(); i.hasNext(); )
		{
			final Object key   = i.next();
			final Object value = map.get(key);
			result.addAll(serializeObject(key));
			result.addAll(serializeObject(value));
		}
		return result;
	}
	
	/**
	 * 
	 */
	public List serializeDynamicWebserviceElement(final DynamicWebserviceElement element) {
		final List arguments = element.serialize();
		for(final Iterator i = arguments.iterator(); i.hasNext(); )
		{
			checkType(i.next());
		}
		final List result = createResultList(element.getClass(), arguments.size());
		result.addAll(arguments);
		return result;
	}
	
	/**
	 * 
	 */
	private List serializeObject(final Object o) {
		if(o instanceof Map) 
		{
			return serializeMap((Map) o);
		}
		if(o instanceof Collection)
		{
			return serializeCollection((Collection) o);
		}
		if(o instanceof DynamicWebserviceElement)
		{
			return serializeDynamicWebserviceElement((DynamicWebserviceElement) o);
		}
		checkType(o);
		final List dummy = new ArrayList();
		dummy.add(o);
		return dummy;
	}

	/**
	 * 
	 */
	private List createResultList(final Class c, final int size)
	{
		final List list = new ArrayList();
		list.add(MARKER);
		list.add(c.getName());
		list.add(new Integer(size).toString());
		return list;
	}
	
	/**
	 * 
	 */
	private void checkType(final Object o)
	{
		if(o == null)
		{
			throw new IllegalArgumentException("Null objects are not allowed in collections...");
		}
		/*
		if(!legalTypes.containsKey(o.getClass()))
		{
			throw new IllegalArgumentException("Non-standard elements not allowed in collections...");
		}
		*/
	}

	/**
	 * 
	 */
	public Object deserialize(final Object[] array) throws Throwable
	{
		final List list = new ArrayList();
		for(int i=0; i<array.length; ++i)
		{
			list.add(array[i]);
		}
		return deserializeElement(list);
	}
	
	/**
	 * 
	 */
	private Object deserializeMap(final List c, final Map map, final int count) throws Throwable
	{
		for(int i=0; i<count; ++i)
		{
			final Object key   = deserializeElement(c);
			final Object value = deserializeElement(c);
			map.put(key, value);
		}
		return map;
	}
	
	/**
	 * 
	 */
	private Object deserializeCollection(final List c, final Collection collection, final int count) throws Throwable
	{
		for(int i=0; i<count; ++i)
		{
			Object o = deserializeElement(c);
			collection.add(o);
		}
		return collection;
	}
	
	/**
	 * 
	 */
	private Object deserializeDynamicWebserviceElement(final List c, final DynamicWebserviceElement element, final int count) throws Throwable
	{
		final List arguments = new ArrayList();
		for(int i=0; i<count; ++i)
		{
			arguments.add(c.remove(0));
		}
		element.deserialize(arguments);
		return element;
	}
	
	/**
	 * 
	 */
	private Object deserializeElement(final List c) throws Throwable
	{
		Object o = c.remove(0);
		if(o == MARKER)
		{
			final Object oo = Class.forName(c.remove(0).toString()).newInstance();
			int count = new Integer(c.remove(0).toString()).intValue();
			if(oo instanceof Map)
			{
				return deserializeMap(c, (Map) oo, count);
			}
			if(oo instanceof Collection)
			{
				return deserializeCollection(c, (Collection) oo, count);
			}
			return deserializeDynamicWebserviceElement(c, (DynamicWebserviceElement) oo, count);
		}
		return o;
	}
}
