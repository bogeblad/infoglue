package org.infoglue.cms.util.sorters;

import java.util.Comparator;

import org.infoglue.cms.entities.management.InterceptionPointVO;


/**
 * @author Mattias Bogeblad
 *
 */

public class DefaultInterceptionPointComparator implements Comparator
{

	public int compare(Object o1, Object o2) 
	{
		int result = 0;
		InterceptionPointVO interceptionPointVO2 = (InterceptionPointVO)o2;
		InterceptionPointVO interceptionPointVO1 = (InterceptionPointVO)o1;
	
		int orderColumnResult = interceptionPointVO1.getName().compareTo(interceptionPointVO2.getName());
		
		if(orderColumnResult != 0)
			result = orderColumnResult;
		
		return result;
	}

}
