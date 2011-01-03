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

package org.infoglue.cms.applications.common.actions;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.infoglue.cms.applications.common.Session;
import org.infoglue.cms.applications.common.ValueConverter;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.LabelController;
import org.infoglue.cms.exception.ConfigurationError;
import org.infoglue.cms.util.StringManager;
import org.infoglue.cms.util.StringManagerFactory;

import webwork.view.velocity.WebWorkVelocityServlet;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 *
 * This class puts some things into the context object that you should
 * be aware of (check the superclasses as well):
 * <pre>
 * "ui" - the StringManagerChain handling all localized strings.
 * </pre>
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */
public class VelocityServlet extends WebWorkVelocityServlet
{
	private static final long serialVersionUID = 408929363112264207L;

	private static final String PACKAGE_NAMES_INIT_PARAM = "packageNames";

	private String packageNames[];

	/**
	 * Performs initialization of this servlet. Called by the servlet container on loading.
	 *
	 * @param configuration The servlet configuration to apply.
	 *
	 * @exception ServletException
	 */
	public void init(ServletConfig configuration) throws ServletException
	{
		super.init(configuration);
		initializePackageNames(configuration.getInitParameter(PACKAGE_NAMES_INIT_PARAM));
	}

	/**
	 * @param commaSeparatedPackageNames comma-separareted list of package names.
	 */
	private void initializePackageNames(String commaSeparatedPackageNames)
	{
		if (commaSeparatedPackageNames == null)
		{
			throw new ConfigurationError("web.xml not properly configured, did not contain the " + PACKAGE_NAMES_INIT_PARAM + " init param for the VelocityServlet.");
		}

		final StringTokenizer st = new StringTokenizer(commaSeparatedPackageNames, ",");
		this.packageNames = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); ++i)
		{
			this.packageNames[i] = st.nextToken();
		}
	}

	/**
	 * @param locale
	 */
	private StringManager getStringManagerChain(Locale locale)
	{
		return StringManagerFactory.getPresentationStringManager(this.packageNames, locale);
	}

	protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context context) throws Exception
	{
		final HttpSession httpSession = request.getSession();
		final Session session = new Session(httpSession);

		
		//<todo>this should definitely not be placed here
        if(session.getLocale() == null || session.getLocale().getLanguage() == null || session.getLocale().getLanguage().equalsIgnoreCase(""))
		{
	        session.setLocale(java.util.Locale.ENGLISH);
		}
        else
        {
            try
            {
            	ResourceBundle sk = ResourceBundle.getBundle("org.infoglue.cms.applications.PresentationStrings", session.getLocale());
            	if(!sk.getLocale().equals(session.getLocale()))
            	{
            		session.setLocale(java.util.Locale.ENGLISH);
            	}
            }
            catch (Throwable e) 
            {
				System.out.println("Error:" + e.getMessage() + " - setting locale to english");
            	session.setLocale(java.util.Locale.ENGLISH);
            }        	
        }
        //</todo>

        if(session.getLocale() == null || session.getLocale().getLanguage() == null || session.getLocale().getLanguage().equalsIgnoreCase(""))
        {	
        	context.put("ui", LabelController.getController(java.util.Locale.ENGLISH));
        	//context.put("ui", getStringManagerChain(java.util.Locale.ENGLISH));
        }
        else
        {
        	context.put("ui", LabelController.getController(session.getLocale()));
		    //context.put("ui", getStringManagerChain(session.getLocale()));
        }
        
		context.put("formatter", new VisualFormatter());
		context.put("converter", new ValueConverter());

		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");

		return super.handleRequest(request, response, context);
	}
	
	private String getPreferredLanguageCode(HttpServletRequest request)
	{
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    return ps.getString("principal_" + request.getRemoteUser() + "_languageCode");
	}

	private String getPreferredToolName(HttpServletRequest request)
	{
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    return ps.getString("principal_" + request.getRemoteUser() + "_defaultToolName");
	}

	private String getDefaultGUI(HttpServletRequest request)
	{
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    return ps.getString("principal_" + request.getRemoteUser() + "_defaultGUI");
	}

	private String getTheme(HttpServletRequest request)
	{
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    return ps.getString("principal_" + request.getRemoteUser() + "_theme");
	}

	private String getPreferredRepositoryId(HttpServletRequest request)
	{
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    return ps.getString("principal_" + request.getRemoteUser() + "_defaultRepositoryId");
	}

}