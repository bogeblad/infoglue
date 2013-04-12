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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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

    public void zipFolder(String folderToZip, ZipOutputStream zos) 
    { 
        try 
        { 
            File zipDir = new File(folderToZip); 

            String[] dirList = zipDir.list(); 
            byte[] readBuffer = new byte[2156]; 
            int bytesIn = 0; 

            for(int i=0; i<dirList.length; i++) 
            { 
                File f = new File(zipDir, dirList[i]); 
                //System.out.println("File1: " + dirList[i]);
	            if(f.isDirectory()) 
	            { 
	                String filePath = f.getPath(); 
	                zipFolder(filePath, zos); 
	                continue; 
	            } 
                
	            FileInputStream fis = new FileInputStream(f); 
                ZipEntry anEntry = new ZipEntry(f.getName()); 
                zos.putNextEntry(anEntry); 
                while((bytesIn = fis.read(readBuffer)) != -1) 
                { 
                    zos.write(readBuffer, 0, bytesIn); 
                } 
                fis.close(); 
            } 
        } 
	    catch(Exception e) 
	    { 
	        //handle exception 
	    } 
    }
    
    
    public void unzip(File fileToUnzip, File extractDirectory) 
    {
        Enumeration enumEntries;
        ZipFile zip;

        try 
        {
        	zip = new ZipFile(fileToUnzip);
        	enumEntries = zip.entries();
        	while (enumEntries.hasMoreElements()) 
        	{
        		ZipEntry zipentry = (ZipEntry) enumEntries.nextElement();
        		if (zipentry.isDirectory()) 
        		{
        			//System.out.println("Name of Extract directory : " + zipentry.getName());
        			(new File(zipentry.getName())).mkdir();
        			continue;
        		}
        		//System.out.println("Name of Extract fille : " + zipentry.getName());
        		extractFile(zip.getInputStream(zipentry), new FileOutputStream(extractDirectory.getPath() + File.separator + zipentry.getName()));
        	}
        	zip.close();
        } 
        catch (IOException ioe)
        {
        	//System.out.println("There is an IoException Occured :" + ioe);
        	ioe.printStackTrace();
        	return;
        }
    }
        
    public static void extractFile(InputStream inStream, OutputStream outStream) throws IOException 
    {
	    byte[] buf = new byte[1024];
	    int l;
	    while ((l = inStream.read(buf)) >= 0) 
	    {
	    	outStream.write(buf, 0, l);
	    }
	    inStream.close();
	    outStream.close();
    }
}