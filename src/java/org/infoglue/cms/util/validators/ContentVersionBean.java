package org.infoglue.cms.util.validators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;

public class ContentVersionBean implements Map {
	private Map delegate = new HashMap();
	
	public ContentVersionBean(ContentTypeDefinitionVO contentType, ContentVersionVO contentVersionVO, String languageCode) {
		List contentTypeAttributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(contentType, true, languageCode);
		for(Iterator i=contentTypeAttributes.iterator(); i.hasNext();) {
			ContentTypeAttribute attribute = (ContentTypeAttribute) i.next();
			String name  = attribute.getName();
			String value = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, name, false);
			delegate.put(name, value);
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
}
