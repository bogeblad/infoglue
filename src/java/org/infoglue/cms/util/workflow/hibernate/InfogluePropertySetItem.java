package org.infoglue.cms.util.workflow.hibernate;

import com.opensymphony.module.propertyset.hibernate.PropertySetItem;


/**
 * Quickfix
 */
public interface InfogluePropertySetItem extends PropertySetItem {
	/**
	 * 
	 */
	byte[] getDataVal();
	
	/**
	 * 
	 */
	void setDataVal(byte[] dataVal);
}
