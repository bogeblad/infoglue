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

public class Pattern 
{

    private int position;
    private String url;

    public Pattern(String url) 
    {
        this.position = url.startsWith("*") ? 1 : url.endsWith("*") ? -1 : 0;
        this.url = url.replaceAll("/?\\*", "");
    }

    public boolean matches(String path) 
    {
        return (position == -1) ? path.startsWith(url)
             : (position == 1) ? path.endsWith(url)
             : path.equals(url);
    }

}