package org.infoglue.cms.entities.kernel;

import java.io.Serializable;

import org.infoglue.cms.util.DomainUtils;

/**
 * This base class for persistent object makes it easy to define domain objects and
 * have them implement the basic core services of an object that will allow them to
 * treated properly by Lists, Sets, HashMaps, Comparators, etc. Once you have these
 * services ironed out, it becomes much easier to deal with your domain objects and
 * test the servies that use them
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public abstract class Persistent implements BaseEntityVO, Comparable, Serializable
{
	/**
	 * Returns if this persisten object is unsaved (no generated id)
	 */
	public boolean isUnsaved()
	{
		return getId() == null;
	}

	/**
	 * Returns if this persisten object is saved (has generated id)
	 */
	public boolean isSaved()
	{
		return getId() != null;
	}

	/**
	 * Compares an object to this one for order.  The comparison is based on ID unless both are unsaved, in which case
	 * the comparison is done using hashCode().  This allows us to add multiple unsaved Persistents to sets even though
	 * they have the same ID.
	 * @param o the object to compare
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
	 * specified object
	 * @throws ClassCastException if o is not an instance of Persistent
	 * @throws NullPointerException if o is null
	 * @see #hashCode
	 */
	public int compareTo(Object o)
	{
		Persistent p = (Persistent)o;
		return (isUnsaved() && p.isUnsaved())
					? new Integer(hashCode()).compareTo(new Integer(p.hashCode()))
					: DomainUtils.compare(getId(), p.getId());
	}

	/**
	 * Compares an object to this one for equality.
	 * @param o the object to compare
	 * @return true if this == o or o != null and getClass().equals(o.getClass()) and compareTo(o) == 0
	 * @see #compareTo
	 */
	public boolean equals(Object o)
	{
		return super.equals(o) || (o != null && getClass().equals(o.getClass()) && compareTo(o) == 0);
	}

	/**
	 * Creates a hash code for this persistent.
	 * @return Object.hashCode() if unsaved, otherwise returns the id's hashCode()
	 */
	public int hashCode()
	{
		return (isUnsaved()) ? super.hashCode() : DomainUtils.hashCode(getId());
	}

	/**
	 * Returns a string representation of this object.  Subclasses that require a customized string representation should
	 * override toStringBuffer() rather than this method.
	 * @return toStringBuffer().toString()
	 * @see #toStringBuffer
	 */
	public String toString()
	{
		return toStringBuffer().toString();
	}

	/**
	 * Returns a string buffer representation of this object.  The idea is that subclasses can grab the string buffer
	 * returned by this method and append to it, to save some gratuitous string creation.
	 * @return a StringBuffer representation of this object
	 */
	protected StringBuffer toStringBuffer()
	{
		StringBuffer buffer = new StringBuffer(getClass().getName()).append(' ');
		buffer.append("id=").append(getId());
		return buffer;
	}

	/**
	 * Appends the given persistent to the given string buffer.  This rather odd-looking method makes it easy for
	 * subclasses overriding toStringBuffer() (or anyone else, for that matter) to add only the ID of a related object
	 * to a string buffer without having to check for null.  In practice, adding entire objects to the buffer returned
	 * by an overridden toStringBuffer() can make for a long string that's hard to read when debugging.  This method
	 * helps restore some sanity when dealing with a large object that has a lot of relationships.
	 * @param buffer the desired StringBuffer
	 * @param persistent	the desired Persistent
	 * @see #toStringBuffer
	 */
	public static void append(StringBuffer buffer, Persistent persistent)
	{
		buffer = (persistent == null) ? buffer.append(persistent) : buffer.append(persistent.getId());
	}
}
