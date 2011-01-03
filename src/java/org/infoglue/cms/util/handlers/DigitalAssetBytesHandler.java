/* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */

package org.infoglue.cms.util.handlers;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.TypeValidator;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.mapping.FieldHandler;
import org.exolab.castor.mapping.ValidityException;
import org.exolab.castor.xml.XMLFieldHandler;
import org.exolab.castor.xml.handlers.CollectionFieldHandler;
import org.infoglue.cms.applications.managementtool.actions.ImportRepositoryAction;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;

import java.util.StringTokenizer;

import java.lang.reflect.Array;

/**
 * A  FieldHandler for the XML Schema Collection type.
 * TODO : support all kind of XSList
 * @author <a href="blandin@intalio.com">Arnaud Blandin</a>
 * @version $Revision: 1.7 $ $Date: 2009/04/19 19:06:15 $
**/
public class DigitalAssetBytesHandler implements FieldHandler 
{	
    public final static Logger logger = Logger.getLogger(DigitalAssetBytesHandler.class.getName());

	private static int maxSize = -1;

    private static ThreadLocal maxSizeTL = new ThreadLocal() 
    {
        protected synchronized Object initialValue() 
        {
            return new Integer(maxSize);
        }
    };

    public static int getMaxSize() 
    {
        return ((Integer) (maxSizeTL.get())).intValue();
    }

    public static void setMaxSize(int maxSize) 
    {
        maxSizeTL.set(maxSize);
    }

    //----------------/
    //- Constructors -/
    //----------------/

    public DigitalAssetBytesHandler()
    {
    	super();
    }
    
    /**
     * Returns the value of the field from the object.
     *
     * @param object The object
     * @return The value of the field
     * @throws IllegalStateException The Java object has changed and
     *  is no longer supported by this handler, or the handler is not
     *  compatiable with the Java object
     */
    public Object getValue( Object object ) throws IllegalStateException
    {
    	byte[] returnArray = null;
    	
    	DigitalAssetImpl asset = (DigitalAssetImpl)object;
    	if((getMaxSize() != -1 && asset.getAssetFileSize() > getMaxSize())/* || asset.getAssetFileSize() == 0*/)
    	{
    		returnArray = "archived".getBytes();
    	}
    	else
    		returnArray = asset.getAssetBytes();
    	
    	if(returnArray == null)
    	{
    		logger.warn("returnArray:" + returnArray + " for " + asset.getId() + "-" + asset.getAssetKey());
        	returnArray = "archived".getBytes();
        	logger.warn("fixed returnArray:" + returnArray + " for " + asset.getId() + "-" + asset.getAssetKey());
    	}
    	
    	return returnArray;
    }


    /**
     * Sets the value of the field on the object.
     *
     * @param object The object
     * @param value The new value
     * @throws IllegalStateException The Java object has changed and
     *  is no longer supported by this handler, or the handler is not
     *  compatiable with the Java object
     * @thorws IllegalArgumentException The value passed is not of
     *  a supported type
     */
    public void setValue( Object object, Object value ) throws IllegalStateException, IllegalArgumentException
    {
    	DigitalAssetImpl asset = (DigitalAssetImpl)object;
    	if(value != null)
    		asset.setAssetBytes((byte[])value);
    	else
    		asset.setAssetBytes("archived".getBytes());
    }


    /**
     * Creates a new instance of the object described by this field.
     *
     * @param parent The object for which the field is created
     * @return A new instance of the field's value
     * @throws IllegalStateException This field is a simple type and
     *  cannot be instantiated
     */
    public Object newInstance( Object parent ) throws IllegalStateException
    {
        return null;
    }


    /**
     * Sets the value of the field to a default value.
     *
     * Reference fields are set to null, primitive fields are set to
     * their default value, collection fields are emptied of all
     * elements.
     *
     * @param object The object
     * @throws IllegalStateException The Java object has changed and
     *  is no longer supported by this handler, or the handler is not
     *  compatiable with the Java object
     */
    public void resetValue( Object object ) throws IllegalStateException, IllegalArgumentException
    {
        //
    }



    /**
     * @deprecated No longer supported
     */
    public void checkValidity( Object object ) throws ValidityException, IllegalStateException
    {
        // do nothing
    }

}