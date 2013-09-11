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

package org.infoglue.cms.util.workflow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.InstallationController;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.deliver.util.NullObject;
import org.infoglue.deliver.util.Timer;

import com.opensymphony.module.propertyset.InvalidPropertyTypeException;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.database.JDBCPropertySet;
import com.opensymphony.util.Data;



/**
 * This is an implementation of a property set manager for JDBC. 
 *
 * @author Mattias Bogeblad
 */

public class InfoGlueJDBCPropertySet extends JDBCPropertySet 
{
    private final static Logger logger = Logger.getLogger(InfoGlueJDBCPropertySet.class.getName());

    private static GenericObjectPool connectionPool = null;
    private static ConnectionFactory connectionFactory = null;
    private static PoolableConnectionFactory poolableConnectionFactory = null;
    private static PoolingDriver driver = null;

    String colData;
    String colDate;
    String colFloat;
    String colGlobalKey;
    String colItemKey;
    String colItemType;
    String colNumber;
    String colString;
    
    private String userName;
    private String password;
    private String driverClassName;
    private String url;

    private String dbcpWhenExhaustedAction = null;
	private String dbcpMaxActive = null;
	private String dbcpMaxWait = null;
	private String dbcpMaxIdle = null;
	private String dbcpValidationQuery = null;

    // args
    String globalKey;
    String tableName;

    private boolean enableCache = true;
    private boolean allKeysCachedType5 = false;
    private boolean allKeysCachedType10 = false;
    private static boolean isRecacheCall = false;
    private static boolean reloadConfiguration = false;
    
    private static Map type5Map = null;
    private static Map type10Map = null;
    private static Map valueMapType5 = null;
    private static Map valueMapType10 = null;
    private static InfoGlueJDBCPropertySet instance = null;
    
    private static Map typeMap5Fallback = null;
    private static Map valueMap5Fallback = null;
    private static Map typeMap10Fallback = null;
    private static Map valueMap10Fallback = null;
    
    //~ Methods ////////////////////////////////////////////////////////////////

    public Collection getKeys(String prefix, int type) throws PropertyException
    {
    	Timer t = new Timer();

    	//System.out.println("isRecacheCall:" + isRecacheCall); //valueMapType5 == null && !allKeysCachedType5
    	Map currentType5Map = type5Map;
    	Map currentValue5Map = valueMapType5;
    	Map currentType10Map = type10Map;
    	Map currentValue10Map = valueMapType10;
    	
        if (prefix == null) 
        {
            prefix = "";
        }
        
        Connection conn = null;

        try 
        {
        	//System.out.println("Getting keys with prefix:" + prefix + " and type: " + type + " and globalKey:" + globalKey);
            logger.info("Getting keys with prefix:" + prefix + " and type: " + type + " and globalKey:" + globalKey);
            conn = getConnection();
            //t.printElapsedTime("Connection took..");
        	if(conn == null)
        	{
        		System.out.println("braking as connection was not yet ready");
        		throw new PropertyException("Problem getting connection"); 
        	}

            PreparedStatement ps = null;
            //String sql = "SELECT " + colItemKey + "," + colItemType + ", " + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + " FROM " + tableName + " WHERE " + colItemKey + " LIKE ? AND " + colGlobalKey + " = ?";

        	String sql = "SELECT " + colItemKey + "," + colItemType + ", " + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + " FROM " + tableName;
            if(type != 10)
            {
            	sql =  "SELECT " + colItemKey + "," + colItemType + ", " + colString + ", " + colDate + ", '' AS " + colData + ", " + colFloat + ", " + colNumber + " FROM " + tableName;
            }
            
            sql += " WHERE ";
            sql += "((" + colItemKey + " NOT LIKE 'content_%' AND ";
        	sql += "" + colItemKey + " NOT LIKE 'principal_%' AND  ";
        	sql += "" + colItemKey + " NOT LIKE 'repository_%_WYSIWYGConfig' AND  ";
        	sql += "" + colItemKey + " NOT LIKE 'repository_%_StylesXML' AND  ";
    		sql += "" + colItemKey + " NOT LIKE 'repository_%_defaultFolderContentTypeName' AND  ";
    		sql += "" + colItemKey + " NOT LIKE 'repository_%_defaultTemplateRepository' AND  ";
    		sql += "" + colItemKey + " NOT LIKE 'siteNode_%_enabledLanguages' AND  ";
    		sql += "" + colItemKey + " NOT LIKE 'siteNode_%_disabledLanguages') OR  ";
    		sql += "(" + colItemKey + " LIKE 'siteNode_%_enabledLanguages' AND string_val <> '') OR ";
    		sql += "(" + colItemKey + " LIKE 'siteNode_%_disabledLanguages' AND string_val <> ''))  AND  ";
    		sql += "" + colGlobalKey + " = ? ";
            
    		//System.out.println("sql:" + sql);
            if(logger.isInfoEnabled())
            {
            	logger.info("app:" + CmsPropertyHandler.getApplicationName());
            	logger.info("operating mode:" + CmsPropertyHandler.getOperatingMode());
            }
            /*
            if(CmsPropertyHandler.getApplicationName().equalsIgnoreCase("deliver") && CmsPropertyHandler.getOperatingMode().equalsIgnoreCase("3"))
            {
	            sql = "SELECT " + colItemKey + "," + colItemType + ", " + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + " FROM " + tableName;
	            sql += " WHERE ";
	            sql += "" + colItemKey + " LIKE ? AND ";
	            sql += "" + colItemKey + " NOT LIKE 'principal_%_languageCode' AND ";
	        	sql += "" + colItemKey + " NOT LIKE 'principal_%_defaultToolId' AND  ";
	    		sql += "" + colItemKey + " NOT LIKE 'content_%_allowedContentTypeNames' AND  ";
	    		sql += "" + colItemKey + " NOT LIKE 'content_%_defaultContentTypeName' AND  ";
	    		sql += "" + colItemKey + " NOT LIKE 'content_%_initialLanguageId' AND  ";
	    		sql += "" + colItemKey + " NOT LIKE 'repository_%_defaultFolderContentTypeName' AND  ";
	    		sql += "" + colItemKey + " NOT LIKE 'repository_%_defaultTemplateRepository' AND  ";
	    		sql += "" + colItemKey + " NOT LIKE 'repository_%_parentRepository' AND  ";
	    		sql += "" + colItemKey + " NOT LIKE 'repository_%_WYSIWYGConfig' AND  ";
	    		sql += "" + colItemKey + " NOT LIKE 'repository_%_StylesXML' AND  ";
	    		sql += "" + colItemKey + " NOT LIKE 'repository_%_extraProperties' AND  ";
	    		sql += "" + colGlobalKey + " = ? ";
            }
            */
            
            if(logger.isInfoEnabled())
            	logger.info("sql:" + sql);
            //System.out.println("sql:" + sql);
            
            if (type == 0) 
            {
            	//System.out.println("conn:" + conn);
            	//System.out.println("sql:" + sql);
                ps = conn.prepareStatement(sql);
                //ps.setString(1, prefix + "%");
                //ps.setString(2, globalKey);
                ps.setString(1, globalKey);
            	//System.out.println("arg1:" + prefix + "%");
                //System.out.println("arg2:" + globalKey);
            } 
            else 
            {
                sql = sql + " AND " + colItemType + " = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, prefix + "%");
                ps.setString(2, globalKey);
                ps.setInt(3, type);
                //System.out.println("arg1:" + prefix + "%");
                //System.out.println("arg2:" + globalKey);
                //System.out.println("arg3:" + type);
            }

            ArrayList list = new ArrayList();
            ResultSet rs = ps.executeQuery();
            logger.info("All rows " + sql);

            int rows = 0;
            while (rs.next()) 
            {
            	rows++;
            	String key = rs.getString(colItemKey);
                int typeId = rs.getInt(colItemType);
            	
            	//System.out.println("key[" + typeId + "]:" + key);
                
            	if(logger.isInfoEnabled())
            		logger.info("key[" + typeId + "]:" + key);
                
                list.add(key);
                
                if(type == 5 && type5Map == null)
                	type5Map = new HashMap();
                if(type == 5 && typeMap5Fallback == null)
                	typeMap5Fallback = new HashMap();

                if(type == 10 && type10Map == null)
                	type10Map = new HashMap();
                if(type == 10 && typeMap10Fallback == null)
                	typeMap10Fallback = new HashMap();

            	currentType5Map = type5Map;
            	currentType10Map = type10Map;
            	if(isRecacheCall)
            	{
            		currentType5Map = typeMap5Fallback;
            		currentType10Map = typeMap10Fallback;
            	}

            	if(type == 5)
            	{
	            	synchronized (currentType5Map) 
	            	{
	            		currentType5Map.put(key, new Integer(typeId));	
	                }
            	}
            	if(type == 10)
            	{
	            	synchronized (currentType10Map) 
	            	{
	            		currentType10Map.put(key, new Integer(typeId));	
	                }
            	}
            	
            	Object o = null;
            	
                switch (typeId) {
                case PropertySet.BOOLEAN:

                    int boolVal = rs.getInt(colNumber);
                    o = new Boolean(boolVal == 1);

                    break;

                case PropertySet.DATA:
                {
                	//Ugly fix for old type of column in oracle which we used to run LONG RAW. We converted to blob and the code is different
                	String columnTypeName = rs.getMetaData().getColumnTypeName(5);
                	logger.info("columnTypeName: " + columnTypeName); 
                	if(this.driverClassName.indexOf("oracle") > -1 && columnTypeName != null && columnTypeName.indexOf("RAW") == -1)
                	{
                		//System.out.println("Getting as blob");
            	        Blob blob = rs.getBlob(colData);
	                	//System.out.println("blob:" + blob);
	                	if(blob != null)
	                	{
	                		try
	                		{
	                			InputStream in = blob.getBinaryStream();
	                			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                			byte[] buffer = new byte[(int)blob.length()];
	                			InputStream is = in;
    			                while (is.read(buffer) > 0) {
    			                	baos.write(buffer);
    			                }
    			                baos.flush();
    			                String s = baos.toString();
    			                //System.out.println("S: " + s + "...");
    			                o = s.getBytes();
	                		}
	                		catch (Exception e) 
	                		{
	                			e.printStackTrace();
							}
	                	}
	                	else
	                	{
	                		o = null;
	                	}
                	}
                	else
                	{
                		//System.out.println("Getting as raw bytes:" + key);
                		o = rs.getBytes(colData);
                	}
                	
                    break;
                }
                case PropertySet.DATE:
                    o = rs.getTimestamp(colDate);

                    break;

                case PropertySet.DOUBLE:
                    o = new Double(rs.getDouble(colFloat));

                    break;

                case PropertySet.INT:
                    o = new Integer(rs.getInt(colNumber));

                    break;

                case PropertySet.LONG:
                    o = new Long(rs.getLong(colNumber));

                    break;

                case PropertySet.STRING:
                    o = rs.getString(colString);

                    break;

                default:
                    logger.info("JDBCPropertySet doesn't support this type yet:" + key + ":" + typeId);
                }

                if(type != 10 && valueMapType5 == null)
                	valueMapType5 = new HashMap();
                if(type != 10 && valueMap5Fallback == null)
                	valueMap5Fallback = new HashMap();

                if(type == 10 && valueMapType10 == null)
                	valueMapType10 = new HashMap();
                if(type == 10 && valueMap10Fallback == null)
                	valueMap10Fallback = new HashMap();

            	currentValue5Map = valueMapType5;
            	currentValue10Map = valueMapType10;
            	if(isRecacheCall)
            	{
            		currentValue5Map = valueMap5Fallback;
            		currentValue10Map = valueMap10Fallback;            		
            	}
            	
            	//System.out.println("Caching:" + key + "=" + o + "(type " + type + ")");
            	if(type != 10)
            	{
	            	synchronized (currentValue5Map) 
	            	{
	            		currentValue5Map.put(key, o);					            		
	            	}
            	}
            	if(type == 10)
            	{
	            	synchronized (currentValue10Map) 
	            	{
	            		currentValue10Map.put(key, o);					            		
	            	}
            	}
            }
            logger.warn("All rows in InfoGlueJDBCPropertySet [" + rows + "] took: " + t.getElapsedTime());

            allKeysCachedType5 = true;
            if(type == 10)
            	allKeysCachedType10 = true;
            if(isRecacheCall)
            {
            	//System.out.println("Switching valueMap from:" + valueMap.hashCode() + " --> " + currentValueMap.hashCode());
            	type5Map = currentType5Map;
            	valueMapType5 = currentValue5Map;
            	typeMap5Fallback = new HashMap();
            	valueMap5Fallback = new HashMap();

            	type10Map = currentType10Map;
            	valueMapType10 = currentValue10Map;
            	typeMap10Fallback = new HashMap();
            	valueMap10Fallback = new HashMap();
            }
            
            rs.close();
            ps.close();

            return list;
        } 
        catch (SQLException e) 
        {
        	logger.error("Problem getting keys due to an SQL exception:" + e.getCause().getMessage(), e);
            throw new PropertyException(e.getMessage());
        }
        catch (Throwable tr) 
        {
        	logger.error("Problem getting keys:" + tr.getMessage(), tr);
            throw new PropertyException(tr.getMessage());
        }
        /*
        catch (UnsupportedEncodingException ue) 
        {
            throw new PropertyException(ue.getMessage());
        } 
        */
        finally 
        {
            closeConnection(conn);
            isRecacheCall = false;
        }
    }
    
    public int getType(String key) throws PropertyException 
    {
        Connection conn = null;
        
		if(enableCache && type5Map != null)
        {
	    	synchronized (type5Map) 
	    	{
		        Integer typeInteger = (Integer)type5Map.get(key);
		        if(typeInteger != null)
		        {
		        	return typeInteger.intValue();
		        }
	        }
    	} 
		if(enableCache && type10Map != null)
        {
	    	synchronized (type10Map) 
	    	{
		        Integer typeInteger = (Integer)type10Map.get(key);
		        if(typeInteger != null)
		        {
		        	return typeInteger.intValue();
		        }
	        }
    	} 
    	
        try 
        {
            conn = getConnection();
            
            //System.out.println("globalKey:" + globalKey);
            //System.out.println("key:" + key);
            String sql = "SELECT " + colItemType + " FROM " + tableName + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, globalKey);
            ps.setString(2, key);

            ResultSet rs = ps.executeQuery();
            int type = 0;

            if (rs.next()) 
            {
                type = rs.getInt(colItemType);
            }

            rs.close();
            ps.close();

            return type;
        } 
        catch (SQLException e) 
        {
            throw new PropertyException(e.getMessage());
        } 
        finally 
        {
            closeConnection(conn);
        }
    }

    public boolean exists(String key) throws PropertyException 
    {
        return getType(key) != 0;
    }

    public void init(Map config, Map args) 
    {
    	reloadConfiguration = false;

        // args
        globalKey = (String) args.get("globalKey");
        
        tableName = (String) config.get("table.name");
        colGlobalKey = (String) config.get("col.globalKey");
        colItemKey = (String) config.get("col.itemKey");
        colItemType = (String) config.get("col.itemType");
        colString = (String) config.get("col.string");
        colDate = (String) config.get("col.date");
        colData = (String) config.get("col.data");
        colFloat = (String) config.get("col.float");
        colNumber = (String) config.get("col.number");

        this.userName = (String) config.get("username");
        this.password = (String) config.get("password");
        this.driverClassName = (String) config.get("driverClassName");
        this.url = (String) config.get("url");
        if(this.url.equalsIgnoreCase("@database.url@"))
        	reloadConfiguration = true;

        this.dbcpWhenExhaustedAction = (String) config.get("dbcp.whenExhaustedAction");
        this.dbcpMaxActive = (String) config.get("dbcp.maxActive");
        this.dbcpMaxWait = (String) config.get("dbcp.maxWait");
        this.dbcpMaxIdle = (String) config.get("dbcp.maxIdle");
        this.dbcpValidationQuery = (String) config.get("dbcp.validationQuery");
                
        if(this.dbcpWhenExhaustedAction != null && (this.dbcpWhenExhaustedAction.length() == 0 || this.dbcpWhenExhaustedAction.indexOf("@") > -1))
        	this.dbcpWhenExhaustedAction = null;

        if(this.dbcpMaxActive != null && (this.dbcpMaxActive.length() == 0 || this.dbcpMaxActive.indexOf("@") > -1))
        	this.dbcpMaxActive = null;

        if(this.dbcpMaxWait != null && (this.dbcpMaxWait.length() == 0 || this.dbcpMaxWait.indexOf("@") > -1))
        	this.dbcpMaxWait = null;

        if(this.dbcpMaxIdle != null && (this.dbcpMaxIdle.length() == 0 || this.dbcpMaxIdle.indexOf("@") > -1))
        	this.dbcpMaxIdle = null;

        if(this.dbcpValidationQuery != null && (this.dbcpValidationQuery.length() == 0 || this.dbcpValidationQuery.indexOf("@") > -1))
        	this.dbcpValidationQuery = null;

        instance = this;
    }
    
    public void remove(String key) throws PropertyException 
    {
        Connection conn = null;

        try 
        {
            conn = getConnection();

            String sql = "DELETE FROM " + tableName + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, globalKey);
            ps.setString(2, key);
            ps.executeUpdate();
            ps.close();
            
            if(valueMapType5 != null)
            	valueMapType5.remove(key);
            if(type5Map != null)
            	type5Map.remove(key);

            if(valueMapType10 != null)
            	valueMapType10.remove(key);
            if(type10Map != null)
            	type10Map.remove(key);
        } 
        catch (SQLException e) 
        {
            throw new PropertyException(e.getMessage());
        } 
        finally 
        {
            closeConnection(conn);
        }
    }

    protected void setImpl(int type, String key, Object value) throws PropertyException 
    {
        if (value == null) 
        {
            throw new PropertyException("JDBCPropertySet does not allow for null values to be stored");
        }

        Connection conn = null;

        try 
        {
        	if(this.colItemKey == null || this.globalKey == null)
            {
            	try 
                {
    				reloadConfiguration();
    			} 
                catch (Exception e1) 
    			{
    				e1.printStackTrace();
    			}
            }
        	
            conn = getConnection();

            String sql = "UPDATE " + tableName + " SET " + colString + " = ?, " + colDate + " = ?, " + colData + " = ?, " + colFloat + " = ?, " + colNumber + " = ?, " + colItemType + " = ? " + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            setValues(ps, type, key, value);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows != 1) 
            {
                // ok, this is a new value, insert it
                sql = "INSERT INTO " + tableName + " (" + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + ", " + colItemType + ", " + colGlobalKey + ", " + colItemKey + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                ps = conn.prepareStatement(sql);
                setValues(ps, type, key, value);
                ps.executeUpdate();
                ps.close();            
            }
        } 
        catch (SQLException e) 
        {
            try 
            {
				reloadConfiguration();
			} 
            catch (Exception e1) 
			{
				e1.printStackTrace();
			}
            throw new PropertyException(e.getMessage());
        } 
        finally 
        {
            closeConnection(conn);
        }
    }

    protected Object get(int type, String key) throws PropertyException 
    {	    	
    	if(key.indexOf("allowedAdminIP") == -1 && key.indexOf("_") == -1 && globalKey.equals("infoglue"))
    	{
    		logger.info("Returning null for key:" + key);
    		return null;
    	}
    	
    	if(type != 10 && enableCache && valueMapType5 == null && !allKeysCachedType5)
    	{
    		logger.info("Caching as:" + valueMapType5 + ":" + allKeysCachedType5);
    		this.getKeys();
    	}
    	if(type == 10 && enableCache && valueMapType10 == null && !allKeysCachedType10)
    	{
    		//System.out.println("Caching...");
    		this.getKeys();
    	}
    	
    	if(type != 10 && enableCache && valueMapType5 != null)
        {
	    	synchronized (valueMapType5) 
	    	{
	    		Object value = valueMapType5.get(key);
    			//System.out.println("value:" + value + ":" + allKeysCachedType5);
	    		if(value == null && !allKeysCachedType5)
	    		{
	    		}
	    		else
	    		{
	    			if(value != null && !(value instanceof NullObject))
			    		return value;
			    	else if(value instanceof NullObject)
			    		return null;
			    	else
			    	{
			    		if(key.indexOf("usePasswordEncryption") > -1 ||
		    			   key.indexOf("ipAddressesToFallbackToBasicAuth") > -1 ||
		    			   key.indexOf("inputCharacterEncoding") > -1 ||
		    			   key.indexOf("anonymous.username") > -1 ||
		    			   key.indexOf("niceURIEncoding") > -1 ||
		    			   key.indexOf("defaultNumberOfYearsBeforeExpire") > -1 ||
		    			   key.indexOf("setDerivedLastModifiedInLive") > -1 ||
		    			   key.indexOf("digitalAssetPath.0") > -1 ||
		    			   key.indexOf("enableNiceURIInWorking") > -1 ||
		    			   key.indexOf("useImprovedContentCategorySearch") > -1 ||
		    			   key.indexOf("useAccessBasedProtocolRedirects") > -1 ||
		    			   key.indexOf("useHashCodeInCaches") > -1 ||
		    			   key.indexOf("useSynchronizationOnCaches") > -1 ||
		    			   key.indexOf("cacheSettings") > -1 ||
		    			   key.indexOf("digitalAssetPath") > -1 ||
		    			   key.indexOf("digitalAssetBaseUrl") > -1 ||
		    			   key.indexOf("anonymous.password") > -1 ||
		    			   key.indexOf("extranetCookieTimeout") > -1 ||
		    			   key.indexOf("pageKey") > -1 ||
		    			   key.indexOf("editOnSite") > -1 ||
		    			   key.indexOf("decoratedPageInvoker") > -1 ||
		    			   key.indexOf("internalDeliveryUrls") > -1 ||
		    			   key.indexOf("mail.smtp.user") > -1 ||
		    			   key.indexOf("mail.smtp.password") > -1 ||
		    			   key.indexOf("mail.contentType") > -1 ||
		    			   key.indexOf("propertiesParser") > -1 ||
		    			   key.indexOf("componentEditorUrl") > -1 ||
		    			   key.indexOf("unprotectedProtocolName") > -1 ||
		    			   key.indexOf("unprotectedProtocolPort") > -1 ||
		    			   key.indexOf("protectedProtocolName") > -1 ||
		    			   key.indexOf("protectedProtocolPort") > -1 ||
		    			   key.indexOf("componentRendererAction") > -1 ||
		    			   key.indexOf("componentRendererUrl") > -1 ||
		    			   key.indexOf("encodeValidateUrl") > -1 ||
		    			   key.indexOf("encodeCasServiceUrl") > -1 ||
		    			   key.indexOf("helpUrl") > -1 ||
		    			   key.indexOf("headerHTML") > -1 ||
		    			   key.indexOf("tree") > -1 ||
		    			   key.indexOf("authorizerClass") > -1 ||
    					   key.indexOf("invalidLoginUrl") > -1 ||
						   key.indexOf("successLoginBaseUrl") > -1 ||
						   key.indexOf("serverName") > -1 ||
						   key.indexOf("casRenew") > -1 ||
						   key.indexOf("casProxyValidateUrl") > -1 ||
						   key.indexOf("authConstraint") > -1 ||
		    			   key.indexOf("session.timeout") > -1
			    			)
		    			{
		    				logger.info("Returning null!!!!!!!!!:" + key);
		    				return null;
		    			}
			    	}
			    }
		    }
	    }
    	if(type == 10 && enableCache && valueMapType10 != null)
        {
	    	synchronized (valueMapType10) 
	    	{
	    		Object value = valueMapType10.get(key);
	    		if(value == null && !allKeysCachedType10)
	    		{
	    		}
	    		else
	    		{
			    	if(value != null && !(value instanceof NullObject))
			    		return value;
			    	else if(value instanceof NullObject)
			    		return null;
			    }
		    }
	    }

    	//System.out.println("Getting value for key:" + key + ":" + type);
    	/*
		if(key.equalsIgnoreCase("serverNode_-1_logPath"))
		{
	        String sql = "SELECT accessRightId, interceptionPointId from cmAccessRight where interceptionPointId = 6";
	        Object o = null;
	        Connection conn = null;
	        
	        Timer t = new Timer();
	        try 
	        {
	            conn = getConnection();

	            PreparedStatement ps = conn.prepareStatement(sql);

	            ResultSet rs = ps.executeQuery();
	            while(rs.next()) 
	            {
	            	System.out.print(".");
	            	rs.getInt("accessRightId");
	            	//rs.getString("parameters");
	            	rs.getInt("interceptionPointId");
	            }
	            t.printElapsedTime("Done...............");
	        }
	        catch (Exception e) 
	        {
	        	e.printStackTrace();
			}
	        finally 
	        {
	            closeConnection(conn);
	        }
		}
		*/
    	Timer t = new Timer();
    	
    	if(logger.isInfoEnabled())
    		logger.info("Getting value for key:" + key + ":" + type);

        String sql = "SELECT " + colItemType + ", " + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + " FROM " + tableName + " WHERE " + colItemKey + " = ? AND " + colGlobalKey + " = ?";
        if(type == 0 || type == 5)
        	sql = "SELECT " + colItemType + ", " + colString + ", " + colDate + ", '' AS " + colData + ", " + colFloat + ", " + colNumber + " FROM " + tableName + " WHERE " + colItemKey + " = ? AND " + colGlobalKey + " = ?";

        //System.out.println("sql:" + sql);
        //System.out.println("key:" + key);
        //System.out.println("globalKey:" + globalKey);

		//Thread.dumpStack();

        Object o = null;
        Connection conn = null;

        try 
        {
        	//t.printElapsedTime("SQL creation took..");

            conn = getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, key);
            ps.setString(2, globalKey);

            int propertyType;
            ResultSet rs = ps.executeQuery();

            if (rs.next()) 
            {
                propertyType = rs.getInt(colItemType);

                if (propertyType != type) 
                {
                    throw new InvalidPropertyTypeException();
                }

                switch (type) 
                {
	                case PropertySet.BOOLEAN:
	
	                    int boolVal = rs.getInt(colNumber);
	                    o = new Boolean(boolVal == 1);
	
	                    break;
	
	                case PropertySet.DATA:
	                {
	                	//Ugly fix for old type of column in oracle which we used to run LONG RAW. We converted to blob and the code is different
	                	String columnTypeName = rs.getMetaData().getColumnTypeName(4);
	                	logger.info("columnTypeName: " + columnTypeName); 
	                	if(this.driverClassName.indexOf("oracle") > -1 && columnTypeName != null && columnTypeName.indexOf("RAW") == -1)
	                	{
	                		//System.out.println("Getting as blob");
	            	        Blob blob = rs.getBlob(colData);
		                	//System.out.println("blob:" + blob);
		                	if(blob != null)
		                	{
		                		try
		                		{
		                			InputStream in = blob.getBinaryStream();
		                			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		                			byte[] buffer = new byte[(int)blob.length()];
		                			InputStream is = in;
	    			                while (is.read(buffer) > 0) {
	    			                	baos.write(buffer);
	    			                }
	    			                baos.flush();
	    			                String s = baos.toString();
	    			                //System.out.println("S: " + s + "...");
	    			                o = s.getBytes();
		                		}
		                		catch (Exception e) 
		                		{
		                			e.printStackTrace();
								}
		                	}
		                	else
		                	{
		                		o = null;
		                	}
	                	}
	                	else
	                	{
	                		o = rs.getBytes(colData);
	                	}
						
	                    break;
	                }
	                case PropertySet.DATE:
	                    o = rs.getTimestamp(colDate);
	
	                    break;
	
	                case PropertySet.DOUBLE:
	                    o = new Double(rs.getDouble(colFloat));
	
	                    break;
	
	                case PropertySet.INT:
	                    o = new Integer(rs.getInt(colNumber));
	
	                    break;
	
	                case PropertySet.LONG:
	                    o = new Long(rs.getLong(colNumber));
	
	                    break;
	
	                case PropertySet.STRING:
	                    o = rs.getString(colString);
	
	                    break;
	
	                default:
	                    throw new InvalidPropertyTypeException("JDBCPropertySet doesn't support this type yet.");
                }
            }

            rs.close();
            ps.close();

    		//if(key.indexOf("error") > -1)
    		//	System.out.println("o1:" + o);	
            
            //t.printElapsedTime("Read took + " + type + "=" + o);
        } 
        catch (SQLException e) 
        {
        	logger.error("Problem getting property from database:" + e.getMessage());
        	throw new PropertyException(e.getMessage());
        } 
        catch (NumberFormatException e) 
        {
        	logger.error("Problem getting property from database:" + e.getMessage());
        	throw new PropertyException(e.getMessage());
        }
        /*
        catch (UnsupportedEncodingException ue) 
        {
        	throw new PropertyException(ue.getMessage());
        }
        */
        finally 
        {
            closeConnection(conn);
        }
        
		if(type != 10 && valueMapType5 == null)
			valueMapType5 = new HashMap();
		if(type == 10 && valueMapType10 == null)
			valueMapType10 = new HashMap();
		
		if(type != 10)
		{
	    	synchronized (valueMapType5) 
	    	{
	    		if(o != null)
	    			valueMapType5.put(key, o);  		
	    		else
	    			valueMapType5.put(key, new NullObject());  		    			
	    	}
		}
		if(type == 10)
		{
	    	synchronized (valueMapType10) 
	    	{
	    		if(o != null)
	    			valueMapType10.put(key, o);  		
	    		else
	    			valueMapType10.put(key, new NullObject());  		    			
	    	}
		}
		
    	//if(key.indexOf("error") > -1)
    	//	System.out.println("o:" + o);

        return o;
    }

    private void setValues(PreparedStatement ps, int type, String key, Object value) throws SQLException, PropertyException 
    {
    	//System.out.println("key:" + key);
    	//if(!key.equalsIgnoreCase("serverNode_-1_shortcuts"))
    	//	return;
    	
        // Patched by Edson Richter for MS SQL Server JDBC Support!
        String driverName;

        try 
        {
            driverName = ps.getConnection().getMetaData().getDriverName().toUpperCase();
        } 
        catch (Exception e) 
        {
            driverName = "";
        }

        ps.setNull(1, Types.VARCHAR);
        ps.setNull(2, Types.TIMESTAMP);

        // Patched by Edson Richter for MS SQL Server JDBC Support!
        // Oracle support suggestion also Michael G. Slack
        if ((driverName.indexOf("SQLSERVER") >= 0) || (driverName.indexOf("ORACLE") >= 0)) 
        {
            ps.setNull(3, Types.BINARY);
        } 
        else 
        {
            ps.setNull(3, Types.BLOB);
        }

        ps.setNull(4, Types.FLOAT);
        ps.setNull(5, Types.NUMERIC);
        ps.setInt(6, type);
        ps.setString(7, globalKey);
        ps.setString(8, key);

        //System.out.println(type);
        
        switch (type) {
        case PropertySet.BOOLEAN:

            Boolean boolVal = (Boolean) value;
            ps.setInt(5, boolVal.booleanValue() ? 1 : 0);

            break;

        case PropertySet.DATA:
        {
        	Data data = (Data) value;
        	ps.setBytes(3, data.getBytes());
        	/*
        	if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("oracle"))
        	{
        		ps.setBinaryStream(3,new ByteArrayInputStream(data.getBytes()),data.getBytes().length);
        	}
        	else
        	{
	            ps.setBytes(3, data.getBytes());
        	}
        	*/
        	break;
        }
        case PropertySet.DATE:

            Date date = (Date) value;
            ps.setTimestamp(2, new Timestamp(date.getTime()));

            break;

        case PropertySet.DOUBLE:

            Double d = (Double) value;
            ps.setDouble(4, d.doubleValue());

            break;

        case PropertySet.INT:

            Integer i = (Integer) value;
            ps.setInt(5, i.intValue());

            break;

        case PropertySet.LONG:

            Long l = (Long) value;
            ps.setLong(5, l.longValue());

            break;

        case PropertySet.STRING:
            ps.setString(1, (String) value);

            break;

        default:
            throw new PropertyException("This type isn't supported!");
        }
        
        if(type != 10 && valueMapType5 == null)
			valueMapType5 = new HashMap();
        if(type != 10 && type5Map == null)
        	type5Map = new HashMap();
		if(type == 10 && valueMapType10 == null)
			valueMapType10 = new HashMap();
		if(type == 10 && type10Map == null)
			type10Map = new HashMap();
		
		if(type != 10)
		{
	        valueMapType5.put(key, value);
	        type5Map.put(key, new Integer(type));
		}
		if(type == 10)
		{
			valueMapType10.put(key, value);
	        type10Map.put(key, new Integer(type));
		}
    }

    private static long lastErrorCheck = -1;
    private static boolean wasDatabaseFaulty = false;

    protected Connection getConnection() throws SQLException 
    {
    	//System.out.println("getConnection start");
        Connection conn = null;
        if(lastErrorCheck > -1)
        {
	        if((wasDatabaseFaulty || !CmsPropertyHandler.getIsValidSetup())/* && (System.currentTimeMillis() - lastErrorCheck < 10000)*/)
	        {	
	        	//System.out.println("Returning : " + wasDatabaseFaulty + ": " + CmsPropertyHandler.getIsValidSetup() + " : " + (System.currentTimeMillis() - lastErrorCheck));
	        	return conn;
	        }
	    }
        else
        	lastErrorCheck = System.currentTimeMillis();
        
        wasDatabaseFaulty = false;
        
        boolean reloadingConfiguration = false;
		
        if(reloadConfiguration)
        {
        	try 
        	{
        		reloadingConfiguration = true;
        		System.out.println("Reloading config........");
        		reloadConfiguration();
	        	//connectionPool = null;
			} 
        	catch (Exception e) 
        	{
				e.printStackTrace();
			}
        }
        	
		try 
		{
	        if(connectionPool == null || reloadingConfiguration)
	        {
		        logger.info("Establishing connection to database '" + this.url + "'");
		        
		        try 
	            {
	                setupDriver(url, this.userName, this.password);
	            } 
	            catch (Exception e) 
	            {
	            	if(CmsPropertyHandler.getIsConfigurationFinished())
	            		logger.error("Error setting up driver for [" + this.url + "]: " + e.getMessage());
	            }
	            logger.info("Done.");
	        }
	        
        	conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:infoGlueJDBCPropertySet");
	        
	        if(logger.isDebugEnabled())
	        {
	        	logger.debug("Fetched connection from pool...");
	        	printDriverStats();
	        }
            //conn = DriverManager.getConnection(url, this.userName, this.password);
		} 
		catch (Exception ex) 
		{
        	//if(CmsPropertyHandler.getIsConfigurationFinished())
        	//logger.error("Error connecting to [" + this.url + "]: " + ex.getMessage(), ex);
        	logger.error("Error connecting to [" + this.url + "]: " + ex.getMessage());
        	lastErrorCheck = System.currentTimeMillis();
        	
        	int reason = InstallationController.getController().getBrokenDatabaseReason();
        	if(reason == InstallationController.DATABASE_PARAMETERS_MISSING || reason == InstallationController.DATABASE_SERVER_DOWN || reason == InstallationController.DATABASE_SERVER_MISSING_DATABASE)
        	{
        		wasDatabaseFaulty = true;
        	}
        	//ex.printStackTrace();
		}
		
        return conn;
    }
    
    private void closeConnection(Connection conn) 
    {
        try 
        {
            if ((conn != null) && !conn.isClosed()) 
            {
                conn.close();
            }
        } 
        catch (SQLException e) 
        {
           logger.error("Could not close connection");
        }
    }
    
    public void setupDriver(String connectURI, String userName, String password) throws Exception 
    {
    	String validationQuery = "select 1 from cmInfoGlueProperties";
    	
    	logger.info("Setting up driver.");
        Class.forName(this.driverClassName).newInstance();
        
        logger.info("dbcpWhenExhaustedAction:" + dbcpWhenExhaustedAction);
        logger.info("dbcpMaxActive:" + dbcpMaxActive);
        logger.info("dbcpMaxWait:" + dbcpMaxWait);
        logger.info("dbcpMaxIdle:" + dbcpMaxIdle);
        logger.info("dbcpValidationQuery:" + dbcpValidationQuery);
        validationQuery = dbcpValidationQuery;
        //System.out.println("validationQuery:" + validationQuery);
        
        int dbcpMaxActiveInt = 200;
        if(dbcpMaxActive != null && !dbcpMaxActive.equals(""))
        	dbcpMaxActiveInt = Integer.parseInt(dbcpMaxActive);

        logger.info("dbcpMaxActiveInt:" + dbcpMaxActiveInt);

        connectionPool = new GenericObjectPool(null, dbcpMaxActiveInt);
        connectionPool.setTestOnBorrow(false);
        //connectionPool.setTestOnReturn(true);
        connectionPool.setTestWhileIdle(true);
        connectionPool.setTimeBetweenEvictionRunsMillis(10000);

		Properties properties = new Properties();
		properties.put("user", userName);
		properties.put("password", password);
		properties.put("defaultRowPrefetch","1000");

        connectionFactory = new DriverManagerConnectionFactory(connectURI, properties /*userName, password*/);
        poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,validationQuery,false,true);
        
        poolableConnectionFactory.getPool().addObject();

        Class.forName("org.apache.commons.dbcp.PoolingDriver");
        driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        
        driver.registerPool("infoGlueJDBCPropertySet",connectionPool);
    }

    public void printDriverStats() throws Exception 
    {
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        ObjectPool connectionPool = driver.getConnectionPool("infoGlueJDBCPropertySet");
        
        //System.out.println("NumActive: " + connectionPool.getNumActive());
        //System.out.println("NumIdle: " + connectionPool.getNumIdle());

        if(logger.isInfoEnabled())
        {
	        logger.info("NumActive: " + connectionPool.getNumActive());
	        logger.info("NumIdle: " + connectionPool.getNumIdle());
        }
    }

    public void shutdownDriver() throws Exception 
    {
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        driver.closePool("infoGlueJDBCPropertySet");
    }
 
    public static void clearCaches()
    {
    	/*
    	try
    	{
    		throw new Exception("Clearing caches.......................................................");
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
    	}
		*/
		//System.out.println("Setting to null:" + valueMap.hashCode());	

    	if(valueMapType5 != null)
        {
	    	synchronized (valueMapType5) 
	    	{
	    		valueMapType5 = null;	
			}
        }
        if(type5Map != null)
        {
	    	synchronized (type5Map) 
	    	{
	    		type5Map = null;
			}
        }   
        
        if(valueMapType10 != null)
        {
	    	synchronized (valueMapType5) 
	    	{
	    		valueMapType5 = null;	
			}
        }
        if(type10Map != null)
        {
	    	synchronized (type10Map) 
	    	{
	    		type10Map = null;
			}
        }    
                
        instance.allKeysCachedType5 = false;
        instance.allKeysCachedType10 = false;
    }

    
    public static void reCache()
    {
    	//clearCaches();
    	/*
    	try
    	{
    		System.out.println("Cleared caches - pausing");
    		Thread.sleep(5000);
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
		}
    	*/
    	//System.out.println("Recaching keys");
    	isRecacheCall = true;
    	instance.getKeys();
    	//System.out.println("Recached keys");
    }

    public void reloadConfiguration() throws Exception
    {
    	//logger.warn("Reloading configuration...");
    	String oldGlobalKey = this.globalKey;
    	//logger.warn("oldGlobalKey:" + oldGlobalKey);
    	
    	DOMBuilder domBuilder = new DOMBuilder();
    	String propertySetXMLPath = CastorDatabaseService.class.getResource("/propertyset.xml").getPath();
    	String content = FileHelper.getFileAsString(new File(propertySetXMLPath));
    	//logger.info("propertyset.xml:\n" + content);
    	Document doc = domBuilder.getDocument(content);
    	
    	Element propertySet = (Element)doc.selectSingleNode("//propertyset[@name='jdbc']");
    	
        String name = propertySet.attributeValue("name");
        String clazz = propertySet.attributeValue("class");
        
        // get args now
        List args = propertySet.selectNodes("arg");
        Map argsMap = new HashMap();
        Iterator argsIterator = args.iterator();
        while(argsIterator.hasNext())
        {
            Element arg = (Element) argsIterator.next();
            String argName = arg.attributeValue("name");
            String argValue = arg.attributeValue("value");
            argsMap.put(argName, argValue);
            //logger.info("" + argName + "=" + argValue);
        }
        
        init(argsMap, argsMap);
        
        //Resetting the key to the old global key as this should not be reset
        this.globalKey = oldGlobalKey;
    }
    
    public static void initReloadConfiguration() throws Exception
    {
    	reloadConfiguration = true;
    }
} 