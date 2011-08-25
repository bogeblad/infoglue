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
 
package org.infoglue.cms.io;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.log4j.Logger;
import org.infoglue.deliver.util.Timer;

public class FileHelper
{
	private final static Logger logger = Logger.getLogger(FileHelper.class.getName());
	
	/**
	 * Writes the file to the hard disk. If the file doesn't exist a new file is created.
	 * @author Mattias Bogeblad
	 * @param text The text you want to write to the file.
	 * @param file The file to save to
	 * @param is_append Dictates if the text should be appended to the existing file. 
	 * If is_append == true; The text will be added to the existing file.
	 * If is_append == false; The text will overwrite the existing contents of the file.
	 *
	 * @exception java.lang.Exception
	 * @since 2002-12-12
	 */
 
	public synchronized static void writeToFile(File file, String text, boolean isAppend) throws Exception
	{
		PrintWriter pout = new PrintWriter(new FileWriter(file, isAppend));
		pout.println(text);    
		pout.close();
	}   

	/**
	 * Writes the file to the hard disk. If the file doesn't exist a new file is created.
	 * @author Mattias Bogeblad
	 * @param text The text you want to write to the file.
	 * @param file The file to save to
	 * @param is_append Dictates if the text should be appended to the existing file. 
	 * If is_append == true; The text will be added to the existing file.
	 * If is_append == false; The text will overwrite the existing contents of the file.
	 *
	 * @exception java.lang.Exception
	 * @since 2002-12-12
	 */
 
 	//TODO - this is not right.
	public synchronized static void writeUTF8ToFileSpecial(File file, String text, boolean isAppend) throws Exception
	{
		/*
		FileOutputStream fos = new FileOutputStream(file, isAppend);
		Writer out = new OutputStreamWriter(fos, "UTF-8");
		out.write(text);
		out.flush();
		out.close();
		*/
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, isAppend));
		dos.writeBytes(text);
		dos.flush();
		dos.close();
		
	}   
	
	//TODO - this is not right.
	public synchronized static void writeUTF8(File file, String text, boolean isAppend) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(file, isAppend);
		Writer out = new OutputStreamWriter(fos, "UTF-8");
		out.write(text);
		out.flush();
		out.close();
	}   
	
	public synchronized static void write(File file, String text, boolean isAppend, String charSet) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(file, isAppend);
		Writer out = new OutputStreamWriter(fos, charSet);
		out.write(text);
		out.flush();
		out.close();
		fos.close();
	}   
	public synchronized static void writeUTF8ToFile(File file, String text, boolean isAppend) throws Exception
	{
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
        out.write(text);
        out.flush();
        out.close();
	}
	
	/**
	 * Writes the file to the hard disk. If the file doesn't exist a new file is created.
	 * @author Mattias Bogeblad
	 * @param text The text you want to write to the file.
	 * @param file The file to save to
	 * @param is_append Dictates if the text should be appended to the existing file. 
	 * If is_append == true; The text will be added to the existing file.
	 * If is_append == false; The text will overwrite the existing contents of the file.
	 *
	 * @exception java.lang.Exception
	 * @since 2002-12-12
	 */
 
	public synchronized static String readUTF8FromFile(File file) throws Exception
	{
	    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
	    String str = in.readLine();
	    
		StringBuffer sb = new StringBuffer();
		
		int ch;
		while ((ch = in.read()) > -1) {
			sb.append((char)ch);
		}
		in.close();
		
		return sb.toString();
	}   
	
	/**
	 * This method reads a file from the disk and converts it to an byte[].
	 * @author Mattias Bogeblad
	 * @param file The file read bytes from
	 *
	 * @exception java.lang.Exception
	 * @since 2002-12-12
	 */
	
	public static byte[] getFileBytes(File file) throws Exception
	{
		FileInputStream fis = new FileInputStream(file);
		byte[] fileBytes = new byte[(int)file.length()];
		fis.read(fileBytes);
		fis.close();
 
		return fileBytes;
	}
	
	
	/**
	 * This method reads a file from the disk into a string.
	 * @author Mattias Bogeblad
	 * @param file The file reads from
	 *
	 * @exception java.lang.Exception
	 * @since 2002-12-12
	 */
	
	public static String getFileAsString(File file) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		
		FileInputStream fis = new FileInputStream(file);
		int c;
		while((c = fis.read()) != -1)
		{
			sb.append((char)c);
		}
	    
		fis.close();
    	
		return sb.toString();
	}

	/**
	 * This method reads a file from the disk into a string.
	 * @author Mattias Bogeblad
	 * @param file The file reads from
	 *
	 * @exception java.lang.Exception
	 * @since 2002-12-12
	 */
	
	public static String getFileAsString(File file, String charEncoding) throws Exception
	{
	    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), charEncoding));
	    
		StringBuffer sb = new StringBuffer();
		
		int ch;
		while ((ch = in.read()) > -1) {
			sb.append((char)ch);
		}
		in.close();
		
		return sb.toString();
	}

	public static String getFileAsStringOpt(File file) throws Exception 
	{
		InputStream in = null;
		byte[] b = new byte[(int)file.length()];
		try 
		{
		    in = new FileInputStream(file);
		    in.read(b);
		} 
		finally 
		{
		    if (in != null) 
		    {
		        try 
		        {
		            in.close();
		        } 
		        catch (IOException e) {
		        }
		    }
		}
		return new String(b);
	}
			
	public static String getFileAsStringOpt(File file, String charEncoding) throws Exception 
	{
		InputStream in = null;
		byte[] b = new byte[(int)file.length()];
		try 
		{
		    in = new FileInputStream(file);
		    in.read(b);
		} 
		finally 
		{
		    if (in != null) 
		    {
		        try 
		        {
		            in.close();
		        } 
		        catch (IOException e) {
		        }
		    }
		}
		
		if(logger.isInfoEnabled())
			logger.info("charEncoding:" + charEncoding);
		
		Timer t = null;
		if(logger.isInfoEnabled())
			t = new Timer();
		
		Charset charset = Charset.forName(charEncoding); 
		CharsetDecoder decoder = charset.newDecoder(); 

		CharBuffer cbuf = decoder.decode(ByteBuffer.wrap(b)); 
		String result = cbuf.toString(); 
		
		if(logger.isInfoEnabled())
			t.printElapsedTimeMicro("Decoding took");
		
		return result;
	}
	/**
	 * This method reads a file from the disk into a string.
	 * @author Mattias Bogeblad
	 * @param file The file reads from
	 *
	 * @exception java.lang.Exception
	 * @since 2002-12-12
	 */
	
	public static String getStreamAsString(InputStream inputStream) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		
		if(inputStream != null)
		{
			int c;
			while((c = inputStream.read()) != -1)
			{
				sb.append((char)c);
			}
		    
			inputStream.close();
		}
		    	
		return sb.toString();
	}

	
	/**
	 * This method writes a file with data from a byte[].
	 * @author Mattias Bogeblad
	 * @param file The file to save to
	 *
	 * @exception java.lang.Exception
	 * @since 2002-12-12
	 */
	
	public static void writeToFile(File file, byte[] data) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos, data.length);
		for(int i=0; i < data.length; i++)
		{ 
			bos.write(data[i]);
		}
    	
		bos.flush();
		bos.close();
		fos.close();
	}
	
	/**
	 * Reading the x last lines of a file
	 */
	public static String tail(File file, int numberOfLines) throws Exception
	{
		StringBuffer result = new StringBuffer("");
		
		if(file.length() == 0)
			return "The log file was empty";
		
        RandomAccessFile raf = new RandomAccessFile(file, "r");
    
        // Read a character
        char ch = raf.readChar();
    
        // Seek to end of file
        if(file.length() > numberOfLines * 150)
        	raf.seek(file.length() - (numberOfLines * 150));
    
        raf.readLine();
        
        // Append to the end
        String lineData = "";
        while((lineData = raf.readLine()) != null)
        {
        	result.append(lineData).append('\n');
        }
        
        raf.close();
        
        return result.toString();
	}

	public static boolean deleteDirectory(File path) 
	{
	    if( path.exists() ) 
	    {
	    	File[] files = path.listFiles();
	    	for(int i=0; i<files.length; i++) 
	    	{
	    		if(files[i].isDirectory()) 
	    		{
	    			deleteDirectory(files[i]);
	    		}
	    		else 
	    		{
	    			files[i].delete();
	    		}
	    	}
	    }

	    return( path.delete() );
	}

	/**
	 * This method unzips the cms war-file.
	 */
	
	public static List<File> unzipFile(File file, String targetFolder) throws Exception
	{
		return unzipFile(file, targetFolder, null, true);
	}
	
	/**
	 * This method unzips the cms war-file.
	 */
	
	public static List<File> unzipFile(File file, String targetFolder, String[] skipFileTypes, boolean skipHiddenFiles) throws Exception
	{
		new File(targetFolder).mkdirs();
		
		List unzippedFiles = new ArrayList<File>();
		
    	Enumeration entries;
    	
    	ZipFile zipFile = new ZipFile(file);
    	
      	entries = zipFile.entries();
      	/*
      	Map fEntries = getEntries(zipFile);
        String[] names = (String[]) fEntries.keySet().toArray(new String[] {});
        Arrays.sort(names);
        
        for (int i = 0; i < names.length; i++) 
        {
            String name = names[i];
            ZipEntry entry = (ZipEntry) fEntries.get(name);
        */

        while(entries.hasMoreElements()) 
      	{
        	ZipEntry entry = (ZipEntry)entries.nextElement();
        	logger.info("entry:" + entry.getName() + ":" + entry.isDirectory());
        	
        	if(entry.isDirectory()) 
	        {
        		if((entry.getName().startsWith(".") || entry.getName().startsWith("__")) && skipHiddenFiles)
        			continue;
        		 
	          	(new File(targetFolder + File.separator + entry.getName())).mkdirs();
	          	continue;
	        }
        	
	        //System.err.println("Extracting file: " + this.cmsTargetFolder + File.separator + entry.getName());
	        boolean skip = false;
	        if(skipFileTypes != null)
	        {
		        for(String skipFileType : skipFileTypes)
		        {
		        	if(entry.getName().endsWith(skipFileType))
		        		skip = true;
		        }
	        }
	        if(skipHiddenFiles && (entry.getName().startsWith(".") || entry.getName().startsWith("__")))
	        	skip = true;
	        
	        if(!skip)
	        {	
	        	File targetFile = new File(targetFolder + File.separator + entry.getName());
	        	
        		String parent = targetFile.getParent();
                if (parent != null && parent.length() > 0) 
                {
                	File dir = new File(parent);
                	if (dir != null) {
                		dir.mkdirs();
                	}
                }

	        	//targetFile.mkdirs();
	        	copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(targetFile)));
	        	unzippedFiles.add(targetFile);
	        }
	        
	    }
	
	    zipFile.close();
	    
	    return unzippedFiles;
	}
	
	/** Get all the entries in a ZIP file. */
	protected static Map getEntries(ZipFile zf) 
	{
	    Enumeration e = zf.entries();
	    Map m = new HashMap();
	    while (e.hasMoreElements()) 
	    {
	    	ZipEntry ze = (ZipEntry) e.nextElement();
	    	m.put(ze.getName(), ze);
	    }
	    return m;
	}
	  
	/**
	 * This method unjars a file.
	 */
	
	public static void unjarFile(File file, String targetFolder) throws Exception
	{
		unjarFile(file, targetFolder, null);
	}
	
	/**
	 * This method unjars a file.
	 */
	
	public static void unjarFile(File file, String targetFolder, String[] skipFileTypes) throws Exception
	{
    	Enumeration entries;
    	
    	JarFile zipFile = new JarFile(file);
    	
    	(new File(targetFolder + File.separator + "META-INF")).mkdir();
    	
      	entries = zipFile.entries();

      	while(entries.hasMoreElements()) 
      	{
        	ZipEntry entry = (ZipEntry)entries.nextElement();
        	logger.info("entry:" + entry.getName());
        	
	        if(entry.isDirectory()) 
	        {
	          	(new File(targetFolder + File.separator + entry.getName())).mkdir();
	          	continue;
	        }
	
	        //System.err.println("Extracting file: " + this.cmsTargetFolder + File.separator + entry.getName());
	        boolean skip = false;
	        if(skipFileTypes != null)
	        {
		        for(String skipFileType : skipFileTypes)
		        {
		        	if(entry.getName().endsWith(skipFileType))
		        		skip = true;
		        }
	        }
	        
	        if(!skip)
	        {	
	        	File targetFile = new File(targetFolder + File.separator + entry.getName());
	        	//targetFile.mkdirs();
	        	copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(targetFile)));
	        }
	    }
	
	    zipFile.close();
	}

	/**
	 * Just copies the files...
	 */
	
	private static void copyInputStream(InputStream in, OutputStream out) throws IOException
	{
	    byte[] buffer = new byte[1024];
    	int len;

    	while((len = in.read(buffer)) >= 0)
      		out.write(buffer, 0, len);

    	in.close();
    	out.close();    	
  	}

}