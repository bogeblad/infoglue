package org.infoglue.cms.util;

import java.lang.reflect.*;
import java.util.*;

/**
 * A utility class that can find a matching method in a class/object
 * 
 * @author mattias.bogeblad@gmail.com
 */

public class ReflectionHelper 
{
    private static Map<Class,Class> primitiveMap = new HashMap<Class,Class>();
    
    static 
    {
		primitiveMap.put(boolean.class, Boolean.class);
		primitiveMap.put(byte.class, Byte.class);
		primitiveMap.put(char.class, Character.class);
		primitiveMap.put(short.class, Short.class);
		primitiveMap.put(int.class, Integer.class);
		primitiveMap.put(long.class, Long.class);
		primitiveMap.put(float.class, Float.class);
		primitiveMap.put(double.class, Double.class);
    }

    public static Method getCompatibleMethod(Class c, String methodName, Class... paramTypes) 
    {
		Method[] methods = c.getMethods();
		for (int i = 0; i < methods.length; i++) 
		{
		    Method m = methods[i];
	
		    if (!m.getName().equals(methodName)) 
		    {
		    	continue;
		    }
	
		    Class<?>[] actualTypes = m.getParameterTypes();
		    if (actualTypes.length != paramTypes.length) 
		    {
		    	continue;
		    }
	
		    boolean found = true;
		    for (int j = 0; j < actualTypes.length; j++) 
		    {
				if (!actualTypes[j].isAssignableFrom(paramTypes[j])) 
				{ 
				    if (actualTypes[j].isPrimitive()) 
				    {
				    	found = primitiveMap.get(actualTypes[j]).equals(paramTypes[j]);
				    }
				    else if (paramTypes[j].isPrimitive()) 
				    {
				    	found = primitiveMap.get(paramTypes[j]).equals(actualTypes[j]);
				    }
				}
		
				if (!found) 
				{
				    break;
				}
		    }
	
		    if (found) 
		    {
		    	return m;
		    }
		}
	
		return null;
    }
}