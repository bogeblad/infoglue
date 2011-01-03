/*
 * $Id: DomainUtils.java,v 1.2 2006/03/06 18:49:15 mattias Exp $
 *****************************************************************************/
package org.infoglue.cms.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * Utility class for the domain objects.  This class cannot be instantiated.
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public final class DomainUtils
{
	private DomainUtils() {}

	/**
	 * Compares two objects for equality, considering the possibility that one or both may be null.
	 * @param o1 an object
	 * @param o2 another object
	 * @return true if o1 is null and o1 == o2, or o1.equals(o2) returns true; otherwise returns false.
	 */
	public static boolean equals(Object o1, Object o2)
	{
		return (o1 == null) ? o1 == o2 : o1.equals(o2);
	}

	/**
	 * Compares two collections for equality, considering the possibility that one or both may be null.
	 * @param c1 a collection
	 * @param c2 another collection
	 * @return true if c1.size() == c2.size(), c1.containsAll(c2), and c2.containsAll(c1).  This keeps the semantics
	 * consistent across the various collection implementations.
	 */
	public static boolean equals(Collection c1, Collection c2)
	{
		if (c1 == null || c2 == null)
			return c1 == c2;

		if (c1.size() != c2.size())
			return false;

		return (c1 instanceof SortedSet && c2 instanceof SortedSet)
				? containsAll((SortedSet)c1, (SortedSet)c2)
				: c1.containsAll(c2) && c2.containsAll(c1);
	}

	/**
	 * Compares two comparable objects, considering the possibility that one or both may be null.
	 * @param c1 an object
	 * @param c2 another object
	 * @return 0 if o1 and 02 are null, -1 if c1 is not null and c2 is null,
	 * 			1 if c1 is null and c2 is not null, or c1.compareTo(o2) otherwise.
	 */
	public static int compare(Comparable c1, Comparable c2)
	{
		if(c1 == null && c2 == null)
			return 0;

		if(c1 != null && c2 == null)
			return -1;

		if(c1 == null && c2 != null)
			return 1;

		return c1.compareTo(c2);
	}

	/*
	 * Indicates whether the given sorted sets contains the same elements in the same order.  We need this method because
	 * of the way sorted sets are implemented, TreeSet in particular: the objects added to the set are used as keys in the
	 * underlying map, while the values in the map is some internal constant of type Object (TreeSet.PRESENT, to be exact).
	 * AbstractCollection.contains() called equals() on the values in the map, which are NOT the objects that were added.
	 * To get around this, we check each item in the set in succession; since they are sorted, the same elements should
	 * appear in the same order.  If we make it through the entire collection, we return true if both iterators have no
	 * more elements; otherwise we return false;
	 */
	private static boolean containsAll(SortedSet s1, SortedSet s2)
	{
		Iterator i = s1.iterator(), j = s2.iterator();

		while (i.hasNext() && j.hasNext())
			if (!i.next().equals(j.next()))
				return false;

		return !i.hasNext() && !j.hasNext();
	}

	/**
	 * Returns an appropriate hash code for the given object, considering the possibilty that the object may be null.
	 * @param s a string
	 * @return if s is null, returns 0, otherwise returns s.hashCode().
	 */
	public static int hashCode(Object s)
	{
		return (s == null) ? 0 : s.hashCode();
	}
}
