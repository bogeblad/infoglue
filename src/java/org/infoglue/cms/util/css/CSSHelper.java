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

package org.infoglue.cms.util.css;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;

import com.steadystate.css.parser.CSSOMParser;

/**
 * @author mattias
 *
 * This class helps us read and handle CSS-files.
 */

public class CSSHelper 
{
    private final static Logger logger = Logger.getLogger(CSSHelper.class.getName());

    private String cssUrl = null;
    
	public static CSSHelper getHelper()
	{
	    return new CSSHelper();
	}

	public void setCSSUrl(String url)
	{
	    this.cssUrl = url;
	}
	
	public List getCSSRules()
	{
	    List list = new ArrayList();
	    
	    try
	    {
		    Reader r = new InputStreamReader(new URL(this.cssUrl).openStream());
	        
	        CSSOMParser parser = new CSSOMParser();
            InputSource is = new InputSource(r);

            CSSStyleSheet stylesheet = parser.parseStyleSheet(is);
            CSSRuleList rules = stylesheet.getCssRules();

            for (int i = 0; i < rules.getLength(); i++) {
                CSSRule rule = rules.item(i);
                list.add(rule);
            }


	    }
	    catch(Exception e)
	    {
	    	logger.warn("An error occurred when reading css-rules: " + e.getMessage(), e);
	    }
			
		return list;
	}

}
