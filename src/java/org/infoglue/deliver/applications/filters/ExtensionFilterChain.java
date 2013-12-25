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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ExtensionFilterChain implements FilterChain 
{
    private FilterChain chain;
    private List<Filter> filters = new ArrayList<Filter>();
    private Iterator<Filter> iterator;

    public ExtensionFilterChain(FilterChain chain) 
    {
        this.chain = chain;
    }

    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException 
    {
        if (iterator == null) 
        {
            iterator = filters.iterator();
        }

        if (iterator.hasNext()) 
        {
            iterator.next().doFilter(request, response, this);
        } 
        else 
        {
            chain.doFilter(request, response);
        }
    }

    public void addFilter(Filter filter) 
    {
        if (iterator != null) 
        {
            throw new IllegalStateException();
        }

        filters.add(filter);
    }

}