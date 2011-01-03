package org.infoglue.cms.util.workflow.hibernate;

import com.opensymphony.module.propertyset.hibernate.PropertySetItemImpl;


/**
 * Quickfix
 */
public class InfogluePropertySetItemImpl extends PropertySetItemImpl implements InfogluePropertySetItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3905527111610348337L;
	
	/**
	 * 
	 */
	private byte[] dataVal;

	/**
	 *
	 */
    public InfogluePropertySetItemImpl() {
		super();
    }

	/**
	 *
	 */
    public InfogluePropertySetItemImpl(String entityName, long entityId, String key) {
		super(entityName, entityId, key);
    }

	/**
	 *
	 */
    public void setDataVal(byte[] dataVal) {
        this.dataVal = dataVal;
    }

	/**
	 *
	 */
    public byte[] getDataVal() {
        return dataVal;
    }
}
