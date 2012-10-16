package org.infoglue.deliver.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefeatCacheParameters
{
	private boolean defeatCache = false;
	Map<Class, List<Object>> entities = new HashMap<Class,List<Object>>();

	public boolean getDefeatCache() { return defeatCache; }
	public void setDefeatCache(boolean defeatCache) { this.defeatCache = defeatCache; }
	
	public void addEntity(Class clazz, List<Object> ids) { this.entities.put(clazz, ids); }
	public Map<Class, List<Object>> getEntities() { return this.entities; }
	
}
