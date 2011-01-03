package org.infoglue.cms.util.sorters;

import java.util.Comparator;
import java.util.List;

import org.infoglue.cms.entities.management.InterceptionPointVO;


/**
 * @author Mattias Bogeblad
 *
 */

public class AverageInvokingTimeComparator implements Comparator
{
	public int compare(Object o1, Object o2) 
	{
		List list1 = (List)o2;
		List list2 = (List)o1;
	
		Long averageTime1 = (Long)list1.get(1);
		Long averageTime2 = (Long)list2.get(1);
		
		return averageTime1.compareTo(averageTime2);
	}

}
