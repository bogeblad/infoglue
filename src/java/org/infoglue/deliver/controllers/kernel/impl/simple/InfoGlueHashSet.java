package org.infoglue.deliver.controllers.kernel.impl.simple;

import java.util.HashSet;

public class InfoGlueHashSet extends HashSet 
{

	public boolean add(Object item)
	{
		if(item == null)
		{
			try
			{
				throw new Exception("Do not insert null here...");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return super.add(item);
	}
}
