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

/*
 * Created on 2003-apr-04
 * 
 * ERROR IN REMOVEPACKAGE, FIX IT! 
 * 
 * */
package org.infoglue.cms.controllers.kernel.impl.simple;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Ant;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.up2date.UpdateCollection;
import org.infoglue.cms.entities.up2date.UpdatePackage;
import org.xml.sax.InputSource;


/**
 * 
 * Core infoglue up2date functionallity is provided through
 * this controller. 
 * 
 * The constructor takes two arguments
 * 1: url to the updateservers xml-file of available packages
 *    This delegates the problem of mirror handling and finding 
 *    a working updateserver to the calling class.   
 * 
 * 2: path to up2date directory residing directly below the toplevel
 *    codebase directory. Ant operates with this directory as the basedirectory
 *    so the ant-files delivered by the updateserver should operate on ".." to
 *    get access to the base directory.
 * 
 * The update process briefly:
 * 
 * Overview:
 * Available updates are served by a InfoGlue CMS repository
 * where developers easily can publish update packages and
 * deployment logic for installing those packages. 
 * 
 * The list of available updates, are served in xml-format from
 * the InfoGlue Update Server. When requesting this document, a list
 * of allready installed packages is passed to the server along with
 * the query.
 *  
 * The returned document looks something like this:
 * 
 * 	<update-collection>
 *		<update-package>
 *			<package-id>pid</package-id>
 *			<title>Title</title>
 *			<description>breif description</description>
 *			<url>http://url_to_ant_install_script</url>
 *			<details-url>http://url_to_html-info_page_describing_the_package</details-url>
 *		</update-package>
 *		<update-package>
 *			...
 *		<update-package>
 *	</update-collection> 
 * 
 * This document is retrieved by this controller on the client system 
 * and is unmarshaled to objects and placed in a list of available packages.
 * 
 * The list is presented to the user (administrator of the client system) 
 * in the management tool. Where he may chose to access the detail page on
 * the server, or install the package.
 * 
 * Package Installation:
 * When the user has decided that the has come to install an update. This class
 * retrieves the ant-script (actually using ants get task) that shall be used 
 * for the installation. The url to the script is found in packagedefinition. 
 * 
 * The file is stored locally on the client system in CMS_BASEDIR/up2date/PID.xml
 * Another ant session is started, using this file, and running target "runUpdate"
 * Typically this target gets a zip-file from the server and unpacks it over the 
 * current codebase. But first a backup is made. But all this is up to
 * the Update Server. A target "unInstall" should also exist in this file.
 * Current setup at www.infoglue.org updateserver also executes two additional targets
 * beforeUpdate and afterUpdate which can be edited by the packageauthor for each package
 * 
 * After the update is made, the package object are added to the list of installed
 * packages, and later marshalled to CMS_BASEDIR/up2date/installed.xml
 * 
 * 
 * 
 * @author Stefan Sik
 * 
 * 
 * 
 */
public class UpdateController extends BaseController
{
	private final static Logger logger = Logger.getLogger(UpdateController.class.getName());

	/**
	 * 
	 */
	
	// Initial handling of compatibility in the server response.
	// Make this better later
	private String protocolVersion = "20";
	// Protocol version 12 adds title to the package description
	 
	private String path = "";
	private String url = "";
	Mapping mapping;
	
	public UpdateController(String url, String path) throws FileNotFoundException, IOException, MappingException 
	{
		// set variables
		// Create and load the castor xml mapping
		this.path = path;
		this.url = url;
		mapping = new Mapping();
		mapping.loadMapping(new InputSource(new FileReader(path + "mapping.xml")));
	}

	public void refreshAvailableUpdates()
	{
		// Expire the cached availableUpdates list
		UpdateListHandler.setAvailableUpdates(null);
	}
	
	public Date getLatestRefresh()
	{
		return UpdateListHandler.getLatestRefresh();
	}
	
	public Vector getAvailableUpdates()
	{
		logger.info("Get Available updates");

		if (UpdateListHandler.getAvailableUpdates() == null)
		{
			logger.info("Getting from updateserver");
			Vector ret = new Vector();		
	
			 try 
			 {
				Unmarshaller unmar = new Unmarshaller(mapping);
				unmar.setWhitespacePreserve(true);
				
				// Fix url to updateserver, so that it filters
				// packages allready installed locally
				// also submit updatesystemversion
                if(url.indexOf("?") == -1)
                    url += "?v=" + protocolVersion;
                else
                    url += "&v=" + protocolVersion;
				
				Vector installed = getInstalledUpdates();
				Iterator iterator = installed.iterator();
				
				while (iterator.hasNext()) {
					  UpdatePackage u = (UpdatePackage) iterator.next();
					  url += "&p=" + u.getPackageId();
				}
				
				URL u = new URL(url);
				URLConnection urlConn = u.openConnection();
				urlConn.setAllowUserInteraction(false); 
				urlConn.setUseCaches (false); 
				
				UpdateCollection coll = (UpdateCollection) unmar.unmarshal( new InputStreamReader(urlConn.getInputStream()));
				ret = coll.getUpdatePackageList();
			}
			catch (ValidationException e2) 
			{
				e2.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (MappingException e) {
				e.printStackTrace();
			} catch (MarshalException e) {
				e.printStackTrace();
			} 
	
			UpdateListHandler.setAvailableUpdates(ret);
		}
		return UpdateListHandler.getAvailableUpdates();
	}
	
	public Vector getInstalledUpdates()
	{
		logger.info("GetInstalled Updates");

		Unmarshaller unmar = null;
		UpdateCollection coll = new UpdateCollection();
		try 
		{
			unmar = new Unmarshaller(mapping);
			unmar.setWhitespacePreserve(true);
			coll = (UpdateCollection) unmar.unmarshal(new InputSource(new FileReader(path + "installed.xml")));
		} 
		catch (MarshalException e2) 
		{
			logger.info("Marshal exception");
		} 
		catch (ValidationException e2) 
		{
			e2.printStackTrace();
		} 
		catch (FileNotFoundException e2) 
		{
			logger.info("No packages found");
		}
		catch (MappingException e1) 
		{
			e1.printStackTrace();
		}

		logger.info("Leaving GetInstalled Updates");
		return coll.getUpdatePackageList();
	}

	private void addInstalledUpdate(UpdatePackage upd)
	{
			Unmarshaller unmar = null;
			try {
				unmar = new Unmarshaller(mapping);
				unmar.setWhitespacePreserve(true);
			} catch (MappingException e1) {
				e1.printStackTrace();
			}
			UpdateCollection coll = new UpdateCollection();
			try {
				coll =
					(UpdateCollection) unmar.unmarshal(
						new InputSource(
							new FileReader(path + "installed.xml")));
			} catch (MarshalException e2) {
				e2.printStackTrace();
			} catch (ValidationException e2) {
				e2.printStackTrace();
			} catch (FileNotFoundException e2) {
				logger.info("No previous installations");
			}
			coll.getUpdatePackageList().add(upd);
			
			Marshaller marshaller = null;
			try {
				marshaller =
					new Marshaller(
						new FileWriter(new File(path + "installed.xml")));
			} catch (IOException e3) {
				e3.printStackTrace();
			}
			try {
				marshaller.setMapping(mapping);
			} catch (MappingException e4) {
				e4.printStackTrace();
			}
			try {
				marshaller.marshal(coll);
			} catch (MarshalException e5) {
				e5.printStackTrace();
			} catch (ValidationException e5) {
				e5.printStackTrace();
			}
	}

	private void removeInstalledUpdate(UpdatePackage upd)
	{

		Unmarshaller unmar = null;
		try {
			unmar = new Unmarshaller(mapping);
			unmar.setWhitespacePreserve(true);
		} catch (MappingException e1) {
			e1.printStackTrace();
		}
		UpdateCollection coll = new UpdateCollection();
		try {
			coll =
				(UpdateCollection) unmar.unmarshal(
					new InputSource(
						new FileReader(path + "installed.xml")));
		} catch (MarshalException e2) {
			e2.printStackTrace();
		} catch (ValidationException e2) {
			e2.printStackTrace();
		} catch (FileNotFoundException e2) {
			logger.info("No previous installations");
		}

			// Find the update with id updatePackageId
			Iterator iterator = coll.getUpdatePackageList().iterator();
			while (iterator.hasNext()) {
				  UpdatePackage u = (UpdatePackage) iterator.next();
				  if(upd.getPackageId().compareTo(u.getPackageId())==0)
				  {
					coll.getUpdatePackageList().remove(u);
					iterator = coll.getUpdatePackageList().iterator();
				  }
			}


			
			Marshaller marshaller = null;
			try {
				marshaller =
					new Marshaller(
						new FileWriter(new File(path + "installed.xml")));
			} catch (IOException e3) {
				e3.printStackTrace();
			}
			try {
				marshaller.setMapping(mapping);
			} catch (MappingException e4) {
				e4.printStackTrace();
			}
			try {
				marshaller.marshal(coll);
			} catch (MarshalException e5) {
				e5.printStackTrace();
			} catch (ValidationException e5) {
				e5.printStackTrace();
			}
	}
	
	public void runUpdatePackage(String updatePackageId, OutputStreamWriter writer) throws IOException
	{
		// Find the update with id updatePackageId
		Vector updates = getAvailableUpdates();
		Iterator iterator = updates.iterator();
		while (iterator.hasNext()) {
			  UpdatePackage u = (UpdatePackage) iterator.next();
			  if(updatePackageId.compareTo(u.getPackageId())==0)
			  {
			  	logger.info("Found package to install: " + u.getPackageId());
			  	runUpdatePackage(u, writer);
			  	break;
			  }
		}
		
	}

	public void unInstallPackage(String updatePackageId, PrintWriter out) throws MalformedURLException
	{
		// Find the update with id updatePackageId
		Vector updates = getInstalledUpdates();
		Iterator iterator = updates.iterator();
		while (iterator.hasNext()) {
			  UpdatePackage u = (UpdatePackage) iterator.next();
			  if(updatePackageId.compareTo(u.getPackageId())==0)
			  {
			  	logger.info("Found package to uninstall: " + u.getPackageId());
				unInstallPackage(u, out);
				break;
			  }
		}
		
	}

	public void runUpdatePackage(UpdatePackage upd, final OutputStreamWriter out) throws IOException
	{
		
		/* TODO:
		 * Capture output from ant session and return it, så that we can produce
		 * a summary of the installation to the user.
		 */
	    try {
	    
			// Get the zip-file!
			String url = upd.getDecodedBinaryUrl();
			out.write("Downloading the package");
			out.flush();
			String zipFile = path + "autoUpdate/" + upd.getPackageId() + ".zip";
			getFile(url, zipFile, upd.getBinarySize().intValue(), out);
			
			// Get the ant-file and run it!
			String antFile = path + upd.getPackageId() + ".xml";
			getFile(upd.getDecodedUrl(), antFile, -1, out);
			out.flush();
	
			// Run the ant file!
			final class AntRun extends Ant {
				public AntRun() {
					project = new Project();
					project.init();
				}	
			}
			AntRun a = new AntRun();
			a.setAntfile(antFile);
			a.setDir(new File(path));
			a.setTarget("runUpdate");
			out.write("Executing ant update script...<br>");
			out.flush();
			a.execute();
	
	
			// Add this update!
			out.write("Finalizing...<br>");
			out.flush();
			refreshAvailableUpdates();
			addInstalledUpdate(upd);
	
			// We are done
			out.write("DONE!<br>");
			out.flush();
	    }
	    catch(BuildException e)
	    {
			out.write(e.getMessage());
			out.write("<br>");
	        
	    } catch (MalformedURLException e)
        {
			out.write(e.getMessage());
			out.write("<br>");
        }
	}
	
	public void unInstallPackage(UpdatePackage upd, final PrintWriter out) throws MalformedURLException
	{
		
		/* TODO:
		 * Capture output from ant session and return it, så that we can produce
		 * a summary of the installation to the user.
		 * 
		 * Should maybe use outputstream instead of printwriter
		 */
		out.println("About to run uninstallation...<br>");

		final String antfile = upd.getPackageId();
		String destFile = path + antfile + ".xml";

		// Run the ant file!
		final class AntRun extends Ant {
			public AntRun() {
				project = new Project();
				project.init();
			}	
		}
		AntRun a = new AntRun();
		a.setAntfile(destFile);
		a.setDir(new File(path));
		a.setTarget("unInstall");
		out.println("Running Ant...<br>");
		a.execute();


		// Add this update!
		out.println("Finalizing...<br>");
		refreshAvailableUpdates();

		removeInstalledUpdate(upd);
		
		// We are done
		out.println("DONE!<br>");
	}
	
	
	
	private void getFile(String src, String dest, int size, OutputStreamWriter out) throws IOException
	{
		File destFile = new File(dest);
		destFile.getParentFile().mkdirs();
		URL u = new java.net.URL(src);
		URLConnection urlConn = u.openConnection();
		urlConn.setAllowUserInteraction(false); 
		urlConn.setUseCaches (false); 
		InputStream is = urlConn.getInputStream();
		String fileName = dest.substring(dest.lastIndexOf("/"));

		FileOutputStream fo = new FileOutputStream(new File(dest));

		int cnt = 0;
		int cntcheck = 0;
		int steps = 50;
		int interval = size / steps;
		
        out.write("<font color='blue'><pre>");
        for(int x = 0; x< steps;x++)
			out.write("_");
        out.write("<br>");
		out.flush();

		
		byte[] buffer = new byte[64];// Or any length you want
	    BufferedInputStream br = new BufferedInputStream(urlConn.getInputStream());
	    for (int lengthRead = 0; (lengthRead = br.read(buffer)) >= 0;)
	    {
	        cnt+=lengthRead;
	        fo.write(buffer, 0, lengthRead);
	        
	        if(size > 0)
	        {
				if(cnt % (size / steps) < cntcheck)
				{
					out.write("*");
					out.flush();
				}
		        cntcheck = cnt % (size / steps);
	        }
	    }
        if(size <= 0)
        {
            for(int x = 0; x< steps;x++)
				out.write("*");
        }
		out.flush();

		out.write("</font><font color='green'> [OK] - " + fileName + "</font>");
		out.write("<br>");
        out.write("</pre>");
		out.flush();
		is.close();
		fo.close();
		out.flush();
	}

    public BaseEntityVO getNewVO()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
