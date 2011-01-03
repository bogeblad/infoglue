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
package org.infoglue.deliver.util.graphics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.xml.sax.InputSource;

/**
 * @author Stefan Sik
 *
 */
public class FOPHelper {
	
	public void generatePDF(String input, File output) throws IOException, FOPException
	{
		FileOutputStream fileOutputStream = new FileOutputStream(output);
		try
		{
			Driver driver = new Driver();
			Logger logger = new FOPCmsLogger();
			
			driver.setLogger(logger);
			driver.setRenderer(Driver.RENDER_PDF);
			driver.setInputSource(new InputSource(new StringReader(input)));
			driver.setOutputStream(fileOutputStream);
			driver.run();
		}		
		catch(FOPException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			throw e;
		}
		finally 
		{
			fileOutputStream.close();
		} 
	}
}
