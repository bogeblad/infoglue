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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * A support class for nice static methods whoch could be used every where.
 * @author Per Jonsson - per.jonsson@it-huset.se
 */
public class Support
{

    /**
     * Converts a property like string to a map. Where the rowdelmiter is "\n"
     * and and property value delimiter is "="
     * @param text the text to parse/convert
     * @return a map with values or an empty map if none.
     * @author Per Jonsson - per.jonsson@it-huset.se
     */

    public static Map convertTextToMap( String text )
    {
        return convertTextToMap( text, "=", "\n" );
    }

    /**
     * Converts a property like string to a map. Where the rowdelmiter and
     * property value delimiter can be defined.
     * @param text the text to parse/convert
     * @param propertyValueDelim the delimiter between the property and value,
     *            ie "="
     * @param rowDelim the row delimiter, normaly \n.
     * @return a map with values or an empty map if none.
     * @author Per Jonsson - per.jonsson@it-huset.se
     */
    public static Map convertTextToMap( String text, String propertyValueDelim, String rowDelim )
    {
        Map map = new HashMap();
        if ( text != null )
        {
            StringTokenizer rowTok = new StringTokenizer( text, rowDelim, false );
            while ( rowTok.hasMoreTokens() )
            {
                String propVal = rowTok.nextToken();
                int index = propVal.indexOf( propertyValueDelim );
                if ( index > 0 )
                {
                    map.put( propVal.substring( 0, index ).trim(), propVal.substring( index + 1 ).trim() );
                }
            }
        }
        return map;
    }

    /**
     * Converts (rather loads) an text String to a Properties object and returns
     * it.
     * @param text the text to convert into a Properties / Map object
     * @return an Map with key values or empty map if none found.
     */
    public static Map convertTextToProperties( String text )
    {
        Properties properties = new Properties();
        try
        {
        	ByteArrayInputStream is = new ByteArrayInputStream( text.getBytes("ISO-8859-1") );
            properties.load( is );
            is.close();
        }
        catch ( Exception ignore )
        {
            // ignore
        }
        
        return properties;
    }

    /**
     * @param args
     */
    public static void main( String[] args )
    {
        // TODO Auto-generated method stub

    }

}
