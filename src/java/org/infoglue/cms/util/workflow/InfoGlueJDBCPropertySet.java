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

import java.io.File;
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

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
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

    private static GenericObjectPool connectionPool;
    private static ConnectionFactory connectionFactory;
    private static PoolableConnectionFactory poolableConnectionFactory;
    private static PoolingDriver driver;

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
    private boolean allKeysCached = false;
    private static boolean isRecacheCall = false;
    private static boolean reloadConfiguration = false;
    
    private static Map typeMap = null;
    private static Map valueMap = null;
    private static InfoGlueJDBCPropertySet instance = null;
    
    private static Map typeMapFallback = null;
    private static Map valueMapFallback = null;
    
    //~ Methods ////////////////////////////////////////////////////////////////

    public Collection getKeys(String prefix, int type) throws PropertyException 
    {
    	//logger.info("isRecacheCall:" + isRecacheCall);
    	Map currentTypeMap = typeMap;
    	Map currentValueMap = valueMap;
    	
        if (prefix == null) 
        {
            prefix = "";
        }
        
        Connection conn = null;

        try 
        {
        	logger.info("Getting keys with prefix:" + prefix + " and type: " + type);
            conn = getConnection();

            PreparedStatement ps = null;
            String sql = "SELECT " + colItemKey + "," + colItemType + ", " + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + " FROM " + tableName + " WHERE " + colItemKey + " LIKE ? AND " + colGlobalKey + " = ?";
            
            if(logger.isInfoEnabled())
            {
            	logger.info("app:" + CmsPropertyHandler.getApplicationName());
            	logger.info("operating mode:" + CmsPropertyHandler.getOperatingMode());
            }
            
            if(CmsPropertyHandler.getApplicationName().equalsIgnoreCase("deliver") && CmsPropertyHandler.getOperatingMode().equalsIgnoreCase("3"))
            {
	            sql = "SELECT " + colItemKey + "," + colItemType + ", " + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + " FROM " + tableName;
	            sql += " WHERE ";
	            sql += "" + colItemKey + " LIKE ? AND ";
	            sql += "" + colItemKey + " NOT LIKE 'principal_%_languageCode' AND ";
	        	sql += "" + colItemKey + " NOT LIKE 'principal_%_defaultToolName' AND  ";
	        	sql += "" + colItemKey + " NOT LIKE 'principal_%_defaultGUI' AND  ";
	        	sql += "" + colItemKey + " NOT LIKE 'principal_%_theme' AND  ";
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
            
            if(logger.isInfoEnabled())
            	logger.info("sql:" + sql);
            
            if (type == 0) 
            {
                ps = conn.prepareStatement(sql);
                ps.setString(1, prefix + "%");
                ps.setString(2, globalKey);
            } 
            else 
            {
                sql = sql + " AND " + colItemType + " = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, prefix + "%");
                ps.setString(2, globalKey);
                ps.setInt(3, type);
            }

            Timer t = new Timer();
            
            ArrayList list = new ArrayList();
            ResultSet rs = ps.executeQuery();
            
            int rows = 0;
            while (rs.next()) 
            {
            	rows++;
            	String key = rs.getString(colItemKey);
                int typeId = rs.getInt(colItemType);
            	
            	if(logger.isInfoEnabled())
            		logger.info("key[" + typeId + "]:" + key);
                
                list.add(key);
                
                if(typeMap == null)
                	typeMap = new HashMap();
                if(typeMapFallback == null)
                	typeMapFallback = new HashMap();
                	
            	currentTypeMap = typeMap;
            	if(isRecacheCall)
            		currentTypeMap = typeMapFallback;

            	synchronized (currentTypeMap) 
            	{
            		currentTypeMap.put(key, new Integer(typeId));	
                }
            	
            	Object o = null;
            	
                switch (typeId) {
                case PropertySet.BOOLEAN:

                    int boolVal = rs.getInt(colNumber);
                    o = new Boolean(boolVal == 1);

                    break;

                case PropertySet.DATA:
                    o = rs.getBytes(colData);

                    break;

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
                    logger.error("JDBCPropertySet doesn't support this type yet:" + key + ":" + typeId);
                	//throw new InvalidPropertyTypeException("JDBCPropertySet doesn't support this type yet:" + typeId);
                }

                if(valueMap == null)
                	valueMap = new HashMap();
                if(valueMapFallback == null)
                	valueMapFallback = new HashMap();

            	currentValueMap = valueMap;
            	if(isRecacheCall)
            		currentValueMap = valueMapFallback;

            	synchronized (currentValueMap) 
            	{
            		currentValueMap.put(key, o);					            		
            	}
            }
            if(logger.isInfoEnabled())
	            t.printElapsedTime("All rows in InfoGlueJDBCPropertySet [" + rows + "] took");

            allKeysCached = true;
            if(isRecacheCall)
            {
            	//logger.info("Switching valueMap from:" + valueMap.hashCode() + " --> " + currentValueMap.hashCode());
            	typeMap = currentTypeMap;
            	valueMap = currentValueMap;
            	typeMapFallback = new HashMap();
            	valueMapFallback = new HashMap();
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
        finally 
        {
            closeConnection(conn);
            isRecacheCall = false;
        }
    }
    
    public int getType(String key) throws PropertyException 
    {
        Connection conn = null;
        
		if(enableCache && typeMap != null)
        {
	    	synchronized (typeMap) 
	    	{
		        Integer typeInteger = (Integer)typeMap.get(key);
		        if(typeInteger != null)
		        {
		        	return typeInteger.intValue();
		        }
	        }
    	} 
    	
        try 
        {
            conn = getConnection();

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
            
            if(valueMap != null)
            	valueMap.remove(key);
            if(typeMap != null)
            	typeMap.remove(key);
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
            //logger.info("SQL:" + sql);
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
    	if(enableCache && valueMap == null && !allKeysCached)
    	{
    		//logger.info("Caching...");
    		this.getKeys();
    	}
    	
    	if(enableCache && valueMap != null)
        {
    		synchronized (valueMap) 
	    	{
	    		Object value = valueMap.get(key);
	    		if(value == null && !allKeysCached)
	    		{
	    			//if(key.indexOf("error") > -1)
	    	    	//	logger.info("Should not return... should get value");
	    		}
	    		else
	    		{
			    	if(value != null && !(value instanceof NullObject))
			    		return value;
			    	else if(value instanceof NullObject)
			    		return null;
			    	else if(allKeysCached)
			    		return null;
	    		}
		    }
	    }

    	if(logger.isInfoEnabled())
    		logger.info("Getting value for key:" + key + ":" + type);
    	
        String sql = "SELECT " + colItemType + ", " + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + " FROM " + tableName + " WHERE " + colItemKey + " = ? AND " + colGlobalKey + " = ?";

        Object o = null;
        Connection conn = null;

        try 
        {
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
	                    o = rs.getBytes(colData);
	
	                    break;
	
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
        finally 
        {
            closeConnection(conn);
        }
        
		if(valueMap == null)
			valueMap = new HashMap();

    	synchronized (valueMap) 
    	{
    		if(o != null)
    			valueMap.put(key, o);  		
    		else
    			valueMap.put(key, new NullObject());  		    			
    	}

        return o;
    }

    private void setValues(PreparedStatement ps, int type, String key, Object value) throws SQLException, PropertyException 
    {
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

        switch (type) {
        case PropertySet.BOOLEAN:

            Boolean boolVal = (Boolean) value;
            ps.setInt(5, boolVal.booleanValue() ? 1 : 0);

            break;

        case PropertySet.DATA:

            Data data = (Data) value;
            ps.setBytes(3, data.getBytes());

            break;

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
        
        if(valueMap == null)
        	valueMap = new HashMap();
        if(typeMap == null)
        	typeMap = new HashMap();
        
        valueMap.put(key, value);
        typeMap.put(key, new Integer(type));
    }

    private static long lastErrorCheck = System.currentTimeMillis();
    private static boolean wasDatabaseFaulty = false;
    	
    protected Connection getConnection() throws SQLException 
    {
        Connection conn = null;
        if((wasDatabaseFaulty || !CmsPropertyHandler.getIsValidSetup()) && (System.currentTimeMillis() - lastErrorCheck < 10000))
        	return conn;

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
	            	//if(CmsPropertyHandler.getIsConfigurationFinished())
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
        
        int dbcpMaxActiveInt = 200;
        if(dbcpMaxActive != null && !dbcpMaxActive.equals(""))
        	dbcpMaxActiveInt = Integer.parseInt(dbcpMaxActive);

        logger.info("dbcpMaxActiveInt:" + dbcpMaxActiveInt);

        connectionPool = new GenericObjectPool(null, dbcpMaxActiveInt);
        connectionPool.setTestOnBorrow(true);
        connectionFactory = new DriverManagerConnectionFactory(connectURI, userName, password);
        poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,validationQuery,false,true);
        
        Class.forName("org.apache.commons.dbcp.PoolingDriver");
        driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        
        driver.registerPool("infoGlueJDBCPropertySet",connectionPool);
    }

    public void printDriverStats() throws Exception 
    {
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        ObjectPool connectionPool = driver.getConnectionPool("infoGlueJDBCPropertySet");
        
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
		//logger.info("Setting to null:" + valueMap.hashCode());	

    	if(valueMap != null)
        {
	    	synchronized (valueMap) 
	    	{
	    		valueMap = null;	
			}
        }
        if(typeMap != null)
        {
	    	synchronized (typeMap) 
	    	{
	    		typeMap = null;
			}
        }    	
        
        instance.allKeysCached = false;
    }

    public static void reCache()
    {
    	//clearCaches();
    	/*
    	try
    	{
    		logger.info("Cleared caches - pausing");
    		Thread.sleep(5000);
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
		}
    	*/
    	//logger.info("Recaching keys");
    	isRecacheCall = true;
    	instance.getKeys();
    	//logger.info("Recached keys");
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