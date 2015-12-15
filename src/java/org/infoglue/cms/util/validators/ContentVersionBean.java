package org.infoglue.cms.util.validators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;

public class ContentVersionBean implements Map {
	private static final Logger logger = Logger.getLogger(ContentVersionBean.class);

	private Map<String, String> delegate = new HashMap<String, String>();
	private Map<String, ContentTypeAttribute> attributeMap = new HashMap<String, ContentTypeAttribute>();
	
	public ContentVersionBean(ContentTypeDefinitionVO contentType, ContentVersionVO contentVersionVO, String languageCode) {
		if (logger.isDebugEnabled())
		{
			logger.debug("Version value before bean parse. Value: " + contentVersionVO.getVersionValue());
		}
		List<ContentTypeAttribute> contentTypeAttributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(contentType, true, languageCode);
		for (Iterator<ContentTypeAttribute> i=contentTypeAttributes.iterator(); i.hasNext();)
		{
			ContentTypeAttribute attribute =  i.next();
			String name  = attribute.getName();
			String value = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, name, false);
			if (logger.isDebugEnabled())
			{
				logger.debug("Adding key-value to validation bean. Key: <" + name + ">, Value: <" + value + ">");
			}
			delegate.put(name, value);
			attributeMap.put(name, attribute);
		}
	}
	  
	  // -- MAP ---
	public Object get(Object key) { return delegate.get(key); }
	public int size() { return delegate.size(); }
	public boolean isEmpty() { return delegate.isEmpty(); }
	public boolean containsKey(Object key) { return delegate.containsKey(key); }
	public boolean containsValue(Object value) { return delegate.containsValue(value); }
	public Object put(Object key, Object value) { return null; }
	public void putAll(Map t) {}
	public Object remove(Object key) { return null; }
	public void clear() {}
	public Set keySet() { return delegate.keySet(); }
	public Collection values() { return delegate.values(); }
	public Set entrySet() { return delegate.entrySet(); }
	public boolean equals(Object o) { return delegate.equals(o); }
	public int hashCode() { return delegate.hashCode(); }

	public ContentTypeAttribute getAttributeType(String attributeName)
	{
		return attributeMap.get(attributeName);
	}
}
