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
package org.infoglue.deliver.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

/**
 * This helper class can compress and decompress a string. Useful in pagecache etc.
 * 
 * @author mattias
 */
public class CompressionHelper
{
    private final static Logger logger = Logger.getLogger(CompressionHelper.class.getName());

    public byte[] compress(String string) 
    {
        byte[] bytes = null;
        
        try
        {
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
		    GZIPOutputStream gz = new GZIPOutputStream(fos);
		    ObjectOutputStream oos = new ObjectOutputStream(gz);
		    oos.writeObject(string);
		    //oos.writeObject("Mattias testar åäö ÅÄÖ");
		    oos.flush();
		    oos.close();
		    fos.close();
		    bytes = fos.toByteArray();

		    return bytes;
        }
        catch(Exception e)
        {
            logger.error("An error occurred when we tried to compress a string:" + e.getMessage(), e);
        }
        
        try
        {
            bytes = string.getBytes("UTF-8");
        }
        catch (Exception e)
        {
            logger.error("An error occurred when we tried to just return the uncompressed bytes:" + e.getMessage(), e);
        }

        return bytes;
    }
    
    public String decompress(byte[] bytes)
    {
        try
        {
        	ByteArrayInputStream fis = new ByteArrayInputStream(bytes);
            GZIPInputStream gs = new GZIPInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(gs);
            String decompressed1 = (String)ois.readObject();
            ois.close();
            fis.close();
            return decompressed1;
        }
        catch(Exception e)
        {
            logger.error("An error occurred when we tried to decompress a string:" + e.getMessage(), e);
        }
        
        return "";
    }

}
