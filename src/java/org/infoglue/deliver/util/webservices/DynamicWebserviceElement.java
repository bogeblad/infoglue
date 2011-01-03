/**
 * 
 */
package org.infoglue.deliver.util.webservices;

import java.util.List;

/**
 * Furthermore, a public non-arguments constructor ...
 */
public interface DynamicWebserviceElement 
{
	/**
	 * 
	 */
	public List serialize();

	/**
	 * 
	 */
	public void deserialize(List list);
}
