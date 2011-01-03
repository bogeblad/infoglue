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

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author mattias
 *
 * This class makes it possible to choose freemarker as the template mechanism.
 */

public class FreemarkerTemplateProcessor
{
    private static FreemarkerTemplateProcessor processor = null;
    
    private Configuration cfg; 
    
    private FreemarkerTemplateProcessor()
    {
        cfg = new Configuration();
        // - Templates are stoted in the WEB-INF/templates directory of the Web app.
        //cfg.setServletContextForTemplateLoading(getServletContext(), "WEB-INF/templates");        
    }
    
    public static FreemarkerTemplateProcessor getProcessor() 
    {
        if(processor == null)
            processor = new FreemarkerTemplateProcessor();
        
        return processor;
    }
        
    
    
    public void renderTemplate(Map params, PrintWriter pw, String templateAsString) throws Exception 
    {
        /*
        int index = templateAsString.indexOf("$templateLogic");
        while(index > -1)
        {
            int indexEnd = templateAsString.indexOf(")", index);
            if(index > -1 && indexEnd > index)
            {
                templateAsString = templateAsString.substring(0, index + 1) + "{" + templateAsString.substring(index + 1, indexEnd) + "}" + templateAsString.substring(indexEnd + 1);
            }
            index = templateAsString.indexOf("$templateLogic", index + 1);
        }
        */
        
		int hashCode = templateAsString.hashCode();

		String fileName = "Template_" + hashCode + ".ftl";

        // Get the templat object
        Reader reader = new StringReader(templateAsString);
        Template t = new Template(fileName, reader, cfg);
        
        try 
        {
            t.process(params, pw);
        } 
        catch (TemplateException e) 
        {
            throw new Exception("Error while processing FreeMarker template", e);
        }
    }
}
