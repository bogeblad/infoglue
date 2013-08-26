package org.infoglue.deliver.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefeatCacheParameters
{
	private boolean defeatCache = false;
	Map<Class, List<Object>> entities = new HashMap<Class,List<Object>>();
	Map<String,Boolean> defeatedKeys = new HashMap<String,Boolean>();

	public boolean getDefeatCache() { return defeatCache; }
	public boolean getDefeatCache(String key) 
	{ 
		if(!defeatCache)
		{
			return defeatCache;
		}
		else
		{
			if(defeatedKeys.get(key) != null && defeatedKeys.get(key).booleanValue())
			{
				return false;
			}
			else
			{
				//defeatedKeys.put(key, new Boolean(true));
				return defeatCache; 
			}
		}
	}
	public void setDefeatCache(boolean defeatCache) { this.defeatCache = defeatCache; }
	public void setDefeatedKey(String key) { defeatedKeys.put(key, new Boolean(true)); }
	
	public void addEntity(Class clazz, List<Object> ids) { this.entities.put(clazz, ids); }
	public Map<Class, List<Object>> getEntities() { return this.entities; }
	
}
