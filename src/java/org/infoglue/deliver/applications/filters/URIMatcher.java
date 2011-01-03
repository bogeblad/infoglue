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

package org.infoglue.deliver.applications.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: lbj
 * Date: 31-01-2004
 * Time: 22:04:30
 * To change this template use Options | File Templates.
 */

public class URIMatcher
{
    private Pattern[] patterns = new Pattern[0];

    // ---- Constructor ----

    private URIMatcher()
    {
    }

    public URIMatcher(Pattern[] patterns)
    {
        this.patterns = patterns;
    }

    // ---- Public methods ----

    public static URIMatcher compilePatterns(String[] strings, boolean caseSensitive) throws PatternSyntaxException
    {
        URIMatcher uriMatcher = new URIMatcher();
        List patterns = new ArrayList();
        if (strings != null && strings.length > 0) 
        {
            for (int i=0;i<strings.length;i++) 
            {
                String str = strings[i];
                if (str != null) 
                {
                    str = str.trim();
                    if (str.length() > 0) 
                    {
                        String patternStr = patternize(str);

                        Pattern pattern = null;
                        if(caseSensitive)
                            pattern = Pattern.compile(patternStr);
                        else
                            pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
                        
                        patterns.add(pattern);
                    }
                }
            }
        }
        uriMatcher.setPatterns((Pattern[])patterns.toArray(new Pattern[0]));
        return uriMatcher;
    }

    public boolean matches(String URI)
    {
        if (URI != null && patterns != null && patterns.length > 0) 
        {
            for (int i=0;i<patterns.length;i++) 
            {
            	if (patterns[i].matcher(URI).matches())
                    return true;
            }
        }
        return false;
    }

    // ---- Private and protected methods ----

    private static String patternize(String str)
    {
        str = str.replaceAll("\\.", "\\\\.");
        str = str.replaceAll("\\:", "\\\\:");
        str = str.replaceAll("\\*", ".*");
        return str;
    }

    private void setPatterns(Pattern[] patterns)
    {
        this.patterns = patterns;
    }

} 