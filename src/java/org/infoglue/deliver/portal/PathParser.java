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
package org.infoglue.deliver.portal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.portalImpl.core.PortalControlParameter;

/**
 * @author robert
 */
public class PathParser 
{
    private static final Log log = LogFactory.getLog(PathParser.class);

    public static final String MULTI_VALUE = "__";

    private String path;
    private String action;
    private Collection parsedPath;
    private Collection globalNavigation;
    private Map stateFullParameterMap;
    private Map stateLessParameterMap;
    private String pid = null;
    private String actionControl = null;

    public PathParser(String pPath) 
    {
        this.path = pPath.substring(0, pPath.lastIndexOf('/'));
        String tmp = pPath.substring(pPath.lastIndexOf('/') + 1, pPath.length());
        StringTokenizer actionTok = new StringTokenizer(tmp, ";");
        while (actionTok.hasMoreTokens()) 
        {
            String token = actionTok.nextToken();
            if (token.endsWith(".action")) 
            {
                this.action = token;
            } 
            else 
            {
                //TODO: implement me, I'm ;jsessionid=123cheese
            }
        }

        this.parsedPath = new LinkedList();

        for (StringTokenizer tok = new StringTokenizer(this.path, "/."); tok.hasMoreTokens();) 
        {
            this.parsedPath.add(tok.nextElement());
        }

        this.stateFullParameterMap = new HashMap();
        this.stateLessParameterMap = new HashMap();
        this.globalNavigation = new LinkedList();
        for (Iterator iter = parsedPath.iterator(); iter.hasNext();) 
        {
            String element = (String) iter.next();

            if (PortalControlParameter.isControlParameter(element)) 
            {
                if (PortalControlParameter.isStateFullParameter(element)) 
                {
                    if (iter.hasNext()) 
                    {
                        stateFullParameterMap.put(element, iter.next());
                    }
                } 
                else 
                {
                    if (iter.hasNext()) 
                    {
                        stateLessParameterMap.put(element, iter.next());
                    }
                }

            } 
            else 
            {
                globalNavigation.add(element);
            }
        }

        // set the pid from the stateless parametermap
        this.pid = (String) stateLessParameterMap.get(PortalControlURL.PID);
        // set the actioncontrol from the stateless parametermap
        this.actionControl = (String) stateLessParameterMap.get(PortalControlURL.ACTION);
    }

    public Map getStateFullParameterMap() {
        return this.stateFullParameterMap;
    }

    public Map getStateLessParameterMap() {
        return this.stateLessParameterMap;
    }

    public Collection getParsedPath() {
        return parsedPath;
    }

    public Collection getGlobalNavigation() {
        return globalNavigation;
    }

    public String getPath() {
        return this.path;
    }

    public String getAction() {
        return this.action;
    }

    /**
     * Copy all parameters of a map into another
     * 
     * @param parameters Map
     * @return
     */
    public static Map copyParameters(Map parameters) 
    {
        HashMap map = new HashMap(parameters.size());
        for (Iterator iter = parameters.keySet().iterator(); iter.hasNext();) 
        {
            String key = (String) iter.next();
            Object value = parameters.get(key);
            map.put(key, value);
        }
        
        return map;
    }

    /**
     * Get the path excluding control-params
     * 
     * @param path The path excluding contextPath
     * @return "real" path never null
     */
    public static String getRealPath(String path) {
        StringBuffer buf = new StringBuffer();
        StringTokenizer st = new StringTokenizer(path, "/");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.startsWith("_")) {
                break;
            } else {
                buf.append("/");
                buf.append(token);
            }
        }
        return buf.toString();
    }

    /**
     * Extract control-params from path
     * 
     * @param prefix Prefix of control-param
     * @param encodedParameters Path    
     * @param preserveNamespace Keep prefix in key
     * @return
     */
    public static Map parsePathParameters(
        String prefix,
        String encodedParameters,
        boolean preserveNamespace) {
        log.debug("Parsing '" + encodedParameters + "' for " + prefix);
        HashMap map = new HashMap(10);
        StringTokenizer tok = new StringTokenizer(encodedParameters, "/");
        String name = (tok.hasMoreTokens() ? tok.nextToken() : null);
        while (name != null) {
            if (name.startsWith(prefix)) {
                Vector buffer = new Vector();

                String nextName = decodeValues(tok, buffer);
                if (buffer.size() > 0) {
                    if (!preserveNamespace) {
                        name = name.substring(prefix.length());
                    }
                    log.debug("Adding " + name + "=" + buffer);
                    map.put(name, (String[]) buffer.toArray(new String[buffer.size()]));
                }
                name = nextName;
            } else {
                //log.debug("Unknown token: " + name);
                name = (tok.hasMoreTokens() ? tok.nextToken() : null);
            }
        }
        return map;
    }

    private static String decodeValues(StringTokenizer tok, List buffer) {
        if (tok.hasMoreTokens()) {
            String entry = tok.nextToken();
            if (entry.startsWith(MULTI_VALUE)) {
                // Multi-value
                while (tok.hasMoreTokens() && entry.startsWith(MULTI_VALUE)) {
                    buffer.add(entry.substring(MULTI_VALUE.length()));
                    entry = tok.nextToken().replaceAll("%2F", "/");
                }
                return entry;
            } else {
                // Single-value
                buffer.add(entry.replaceAll("%2F", "/"));
                return (tok.hasMoreElements() ? tok.nextToken() : null);
            }
        }
        return null;
    }

    /**
     * Encode control-param values. Multi-value params will be prefixed: "__value1/__value2"
     * 
     * @param values
     * @return
     */
    public static String encodeValues(String[] values) {
        StringBuffer result = new StringBuffer();
        if (values.length == 1) {
            result.append(values[0].replaceAll("/", "%2F"));
        } else {
            for (int i = 0; i < values.length; i++) {
                result.append(MULTI_VALUE);
                result.append(values[i].replaceAll("/", "%2F"));
                if (i < values.length - 1)
                    result.append("/");
            }
        }
        return result.toString();
    }

    /**
     * Return pid if the pid parameter exists in the path otherwise null is returned.
     * @return Returns the pid.
     */
    public String getPid() {
        return pid;
    }

    /**
     * Return the action control parameter if it exists, otherwise it's null.
     * @return Returns the actionControl.
     */
    public String getActionControl() {
        return actionControl;
    }

    public boolean isTargetedRequest() {
        return (this.pid != null || actionControl != null);
    }
}
