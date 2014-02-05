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

package org.infoglue.cms.controllers.kernel.impl.simple;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.FormEntry;
import org.infoglue.cms.entities.management.FormEntryAsset;
import org.infoglue.cms.entities.management.FormEntryAssetVO;
import org.infoglue.cms.entities.management.FormEntryVO;
import org.infoglue.cms.entities.management.FormEntryValue;
import org.infoglue.cms.entities.management.FormEntryValueVO;
import org.infoglue.cms.entities.management.GeneralOQLResult;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.impl.simple.FormEntryAssetImpl;
import org.infoglue.cms.entities.management.impl.simple.FormEntryImpl;
import org.infoglue.cms.entities.management.impl.simple.FormEntryValueImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallFormEntryImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallFormEntryValueImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.util.Timer;


/**
 * @author Mattias Bogeblad
 */

public class FormEntryController extends BaseController
{
    private final static Logger logger = Logger.getLogger(FormEntryController.class.getName());

	/**
	 * Factory method
	 */

	public static FormEntryController getController()
	{
		return new FormEntryController();
	}

    public FormEntryVO getFormEntryVOWithId(Integer formEntryId) throws SystemException, Bug
    {
		return (FormEntryVO) getVOWithId(FormEntryImpl.class, formEntryId);
    }

    public FormEntry getFormEntryWithId(Integer formEntryId, Database db) throws SystemException, Bug
    {
		return (FormEntry) getObjectWithId(FormEntryImpl.class, formEntryId, db);
    }

    public FormEntryAsset getFormEntryAssetWithId(Integer formEntryAssetId, Database db) throws SystemException, Bug
    {
		return (FormEntryAsset) getObjectWithId(FormEntryAssetImpl.class, formEntryAssetId, db);
    }

    public List getFormEntryVOList() throws SystemException, Bug
    {
		List redirectVOList = getAllVOObjects(FormEntryImpl.class, "formEntryId");

		return redirectVOList;
    }

    public List getFormEntryVOList(Database db) throws SystemException, Bug
    {
		List redirectVOList = getAllVOObjects(FormEntryImpl.class, "formEntryId", db);

		return redirectVOList;
    }

	/**
	 * Returns the RepositoryVO with the given name.
	 * 
	 * @param name
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public List getFormEntryValueVOList(Integer formContentId, String fieldName) throws SystemException, Bug
	{
		List formEntryValueVOList = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.FormEntryValueImpl f WHERE f.formEntry.formContentId = $1 AND f.name = $2 order by formEntryValueId");
			oql.bind(formContentId);
			oql.bind(fieldName);
			
			QueryResults results = oql.execute(Database.READONLY);

			while (results.hasMore()) 
			{
				formEntryValueVOList.add(((FormEntryValue)results.next()).getValueObject());
			}
			
			results.close();
			oql.close();
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e);
		}
		
		return formEntryValueVOList;	
	}

	/**
	 * Returns the RepositoryVO with the given name.
	 * 
	 * @param name
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public List getFormEntryVOList(Integer formContentId) throws SystemException, Bug
	{
		List formEntryVOList = null;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			formEntryVOList = getFormEntryVOList(formContentId, db);
			//Collection formEntryList = getFormEntryList(formContentId, db);
			//formEntryVOList = toVOList(formEntryList);
				
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return formEntryVOList;	
	}

	/**
	 * Returns the RepositoryVO with the given name.
	 * 
	 * @param name
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public List getFormEntryValueVOList(Integer formEntryId) throws SystemException, Bug
	{
		List formEntryValueVOList = null;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			FormEntry formEntry = getFormEntryWithId(formEntryId, db);
			formEntryValueVOList = toVOList(formEntry.getFormEntryValues());
				
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return formEntryValueVOList;	
	}

	/**
	 * Returns the RepositoryVO with the given name.
	 * 
	 * @param name
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public Map<Integer,List> getFormEntryValuesMap(Integer formContentId) throws SystemException, Bug
	{
		Map<Integer,List> formEntryValuesMap = null;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			formEntryValuesMap = getFormEntryValuesMap(formContentId, db);
				
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return formEntryValuesMap;	
	}

	/**
	 * Returns the RepositoryVO with the given name.
	 * 
	 * @param name
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public Map<Integer,List> getFormEntryValuesMap(Integer formContentId, Database db) throws SystemException, Bug
	{
		Map<Integer,List> formEntryValuesMap = new HashMap<Integer,List>();
				
		try
		{
			//OQLQuery oql = db.getOQLQuery("SELECT fev FROM org.infoglue.cms.entities.management.impl.simple.SmallFormEntryValueImpl fev WHERE fev.formEntry.formContentId = $1 order by fev.formEntryValueId");
			OQLQuery oql = db.getOQLQuery("CALL SQL SELECT fev.id, fev.name, fev.value, fev.formEntryId FROM cmFormEntryValue fev, cmFormEntry fe WHERE fev.formEntryId = fe.id AND fe.formContentId = $1 order by fev.id AS org.infoglue.cms.entities.management.impl.simple.SmallFormEntryValueImpl");
			oql.bind(formContentId);
			
			QueryResults results = oql.execute(Database.READONLY);

			while (results.hasMore()) 
			{
				SmallFormEntryValueImpl fev = (SmallFormEntryValueImpl)results.next();
				List values = formEntryValuesMap.get(fev.getFormEntryId());
				if(values == null)
				{
					values = new ArrayList();
					formEntryValuesMap.put(fev.getFormEntryId(), values);
				}
				values.add(fev.getValueObject());
			}
			
			//System.out.println("formEntryValuesMap:" + formEntryValuesMap);
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of form entries. Reason:" + e.getMessage(), e);    
		}
		
		return formEntryValuesMap;	
	}

	/**
	 * Returns the RepositoryVO with the given name.
	 * 
	 * @param name
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public Map<Integer,List> getFormEntryAssetsMap(Integer formContentId, Database db) throws SystemException, Bug
	{
		Map<Integer,List> formEntryAssetsMap = new HashMap<Integer,List>();
				
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT fea FROM org.infoglue.cms.entities.management.impl.simple.FormEntryAssetImpl fea WHERE fea.formEntry.formContentId = $1 order by fea.formEntryAssetId");
			//OQLQuery oql = db.getOQLQuery("CALL SQL SELECT fev.id, fev.name, fev.value, fev.formEntryId FROM cmFormEntryValue fev, cmFormEntry fe WHERE fev.formEntryId = fe.id AND fe.formContentId = $1 order by fev.id AS org.infoglue.cms.entities.management.impl.simple.SmallFormEntryValueImpl");
			oql.bind(formContentId);
			
			QueryResults results = oql.execute(Database.READONLY);

			while (results.hasMore()) 
			{
				FormEntryAssetImpl fea = (FormEntryAssetImpl)results.next();
				List values = formEntryAssetsMap.get(fea.getFormEntry().getFormEntryId());
				if(values == null)
				{
					values = new ArrayList();
					formEntryAssetsMap.put(fea.getFormEntry().getFormEntryId(), values);
				}
				values.add(fea.getValueObject());
			}
			
			//System.out.println("formEntryAssetsMap:" + formEntryAssetsMap);
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of form entries. Reason:" + e.getMessage(), e);    
		}
		
		return formEntryAssetsMap;	
	}

	/**
	 * Returns a list of entry assets for a given entry.
	 * 
	 * @param name
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public List getFormEntryAssetVOList(Integer formEntryId) throws SystemException, Bug
	{
		List formEntryAssetVOList = null;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			FormEntry formEntry = getFormEntryWithId(formEntryId, db);
			formEntryAssetVOList = toVOList(formEntry.getFormEntryAssets());
				
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return formEntryAssetVOList;	
	}

	/**
	 * Returns a list of entry assets for a given entry.
	 * 
	 * @param name
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public String getFormEntryAssetUrl(Database db, Integer formEntryAssetId, DeliveryContext deliveryContext) throws SystemException, Bug, Exception
	{
		String assetUrl = "";
		assetUrl = URLComposer.getURLComposer().composeDigitalAssetUrl("", null, "", deliveryContext); 
		
		FormEntryAsset formEntryAsset = getFormEntryAssetWithId(formEntryAssetId, db);
		
		String fileName = formEntryAsset.getId() + "_" + formEntryAsset.getFileName();
		String filePath = CmsPropertyHandler.getDigitalAssetPath();
		
		dumpDigitalAsset(formEntryAsset, fileName, filePath);
		
		SiteNodeVO siteNodeVO = NodeDeliveryController.getNodeDeliveryController(deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), deliveryContext.getContentId()).getSiteNodeVO(db, deliveryContext.getSiteNodeId());
		String dnsName = CmsPropertyHandler.getWebServerAddress();
		if(siteNodeVO != null)
		{
			RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(siteNodeVO.getRepositoryId(), db);
			if(repositoryVO.getDnsName() != null && !repositoryVO.getDnsName().equals(""))
				dnsName = repositoryVO.getDnsName();
		}

		assetUrl = URLComposer.getURLComposer().composeDigitalAssetUrl(dnsName, null, fileName, deliveryContext); 

		return assetUrl;	
	}

	public String getFormEntryAssetUrl(Integer formEntryAssetId, DeliveryContext deliveryContext) throws SystemException, Bug, Exception
	{
		String assetUrl = "";
		assetUrl = URLComposer.getURLComposer().composeDigitalAssetUrl("", null, "", deliveryContext); 

		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			FormEntryAsset formEntryAsset = getFormEntryAssetWithId(formEntryAssetId, db);
			
			String fileName = formEntryAsset.getId() + "_" + formEntryAsset.getFileName();
			String filePath = CmsPropertyHandler.getDigitalAssetPath();
			
			dumpDigitalAsset(formEntryAsset, fileName, filePath);
			
			SiteNodeVO siteNodeVO = NodeDeliveryController.getNodeDeliveryController(deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), deliveryContext.getContentId()).getSiteNodeVO(db, deliveryContext.getSiteNodeId());
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNodeVO != null)
			{
				RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(siteNodeVO.getRepositoryId(), db);
				if(repositoryVO.getDnsName() != null && !repositoryVO.getDnsName().equals(""))
					dnsName = repositoryVO.getDnsName();
			}
			assetUrl = URLComposer.getURLComposer().composeDigitalAssetUrl(dnsName, null, fileName, deliveryContext); 

			rollbackTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.error("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

		return assetUrl;	
	}

	public File dumpDigitalAsset(FormEntryAsset formEntryAsset, String fileName, String filePath) throws Exception
	{
		Timer timer = new Timer();
		File tmpOutputFile = new File(filePath + File.separator + Thread.currentThread().getId() + "_tmp_" + fileName);
		File outputFile = new File(filePath + File.separator + fileName);
		//logger.warn("outputFile:" + filePath + File.separator + fileName + ":" + outputFile.length());
		if(outputFile.exists())
		{
			//logger.warn("The file allready exists so we don't need to dump it again..");
			return outputFile;
		}

		try 
		{
			InputStream inputStream = formEntryAsset.getAssetBlob();
			logger.info("inputStream:" + inputStream + ":" + inputStream.getClass().getName() + ":" + formEntryAsset);
			synchronized(inputStream)
			{
				logger.info("reading inputStream and writing to disk....");
				
				FileOutputStream fos = new FileOutputStream(tmpOutputFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				BufferedInputStream bis = new BufferedInputStream(inputStream);
				
				int character;
				int i=0;
		        while ((character = bis.read()) != -1)
		        {
					bos.write(character);
					i++;
		        }
		        
		        if(i == 0)
		        	logger.info("Wrote " + i + " chars to " + fileName);
		        
				bos.flush();
			    fos.close();
				bos.close();
					
		        bis.close();

		        logger.info("done reading inputStream and writing to disk....");
			}
			
			logger.info("Time for dumping file " + fileName + ":" + timer.getElapsedTime());

			if(tmpOutputFile.length() == 0 || outputFile.exists())
			{
				logger.info("written file:" + tmpOutputFile.length() + " - removing temp and not renaming it...");	
				tmpOutputFile.delete();
				logger.info("Time for deleting file " + timer.getElapsedTime());
			}
			else
			{
				logger.info("written file:" + tmpOutputFile.length() + " - renaming it to " + outputFile.getAbsolutePath());	
				tmpOutputFile.renameTo(outputFile);
				logger.info("Time for renaming file " + timer.getElapsedTime());
			}	
		}
		catch (IOException e) 
		{
			throw new Exception("Could not write file " + outputFile.getAbsolutePath() + " - error reported:" + e.getMessage(), e);
	    }
		
        return outputFile;
	}
	
	/**
	 * Returns the Repository with the given name fetched within a given transaction.
	 * 
	 * @param name
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public List<FormEntryVO> getFormEntryVOList(Integer formContentId, Database db) throws SystemException, Bug
	{
		List<FormEntryVO> formEntryList = new ArrayList<FormEntryVO>();
		
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.SmallFormEntryImpl f WHERE f.formContentId = $1 order by formEntryId");
			oql.bind(formContentId);
			
			QueryResults results = oql.execute();

			while (results.hasMore()) 
			{
				formEntryList.add(((SmallFormEntryImpl)results.next()).getValueObject());
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of form entries. Reason:" + e.getMessage(), e);    
		}
		
		return formEntryList;		
	}

	/**
	 * Returns the Repository with the given name fetched within a given transaction.
	 * 
	 * @param name
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public List getFormEntryList(Integer formContentId, Database db) throws SystemException, Bug
	{
		List formEntryList = new ArrayList();
		
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.FormEntryImpl f WHERE f.formContentId = $1 order by formEntryId");
			oql.bind(formContentId);
			
			QueryResults results = oql.execute();

			while (results.hasMore()) 
			{
				formEntryList.add(results.next());
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of form entries. Reason:" + e.getMessage(), e);    
		}
		
		return formEntryList;		
	}

	/**
	 * Returns the Repository with the given name fetched within a given transaction.
	 * 
	 * @param name
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public List<String> getFormEntryValueNames(Integer formContentId, Database db) throws SystemException, Bug
	{
		List<String> formEntryValueNames = new ArrayList<String>();
		
		try
		{
			OQLQuery oql = db.getOQLQuery("CALL SQL select (select max(fev2.id) from cmFormEntryValue fev2 where fev2.name = fev.name) as id, fev.name as column1Value, '' as column2Value, '' as column3Value, '' as column4Value, '' as column5Value, '' as column6Value, '' as column7Value from cmFormEntry fe, cmFormEntryValue fev where fev.formEntryId = fe.id AND fe.formcontentid = $1 group by fev.name AS org.infoglue.cms.entities.management.GeneralOQLResult");
			oql.bind(formContentId);
			
			QueryResults results = oql.execute(Database.READONLY);
			while (results.hasMore()) 
			{
				GeneralOQLResult resultBean = (GeneralOQLResult)results.next();
				formEntryValueNames.add(resultBean.getValue1());
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of form entries. Reason:" + e.getMessage(), e);    
		}
		
		return formEntryValueNames;		
	}

	/**
	 * Returns the Repository with the given name fetched within a given transaction.
	 * 
	 * @param name
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public Map<Integer, String> getFormEntryNamedValueList(Integer formContentId, String name, Database db) throws SystemException, Bug
	{
		Map<Integer,String> formEntryValueNames = new HashMap<Integer,String>();
		
		try
		{
			OQLQuery oql = db.getOQLQuery("CALL SQL select fe.id as id, fev.value as column1Value, '' as column2Value, '' as column3Value, '' as column4Value, '' as column5Value, '' as column6Value, '' as column7Value from cmFormEntry fe, cmFormEntryValue fev where fev.formEntryId = fe.id AND fe.formContentId = $1 AND fev.name = $2 AS org.infoglue.cms.entities.management.GeneralOQLResult");
			oql.bind(formContentId);
			oql.bind(name);
			QueryResults results = oql.execute(Database.READONLY);
			while (results.hasMore()) 
			{
				GeneralOQLResult resultBean = (GeneralOQLResult)results.next();
				formEntryValueNames.put(resultBean.getId(), resultBean.getValue1());
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of form entries. Reason:" + e.getMessage(), e);    
		}
		
		return formEntryValueNames;		
	}

	/**
	 * Returns the Repository with the given name fetched within a given transaction.
	 * 
	 * @param name
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public Map<Integer,Integer> getFormEntryCountMap(Database db) throws SystemException, Bug
	{
		Map<Integer,Integer> formEntryCountMap = new HashMap<Integer,Integer>();
		
		try
		{
			OQLQuery oql = db.getOQLQuery("CALL SQL select formcontentid as id, count(*) as column1Value, '' as column2Value, '' as column3Value, '' as column4Value, '' as column5Value, '' as column6Value, '' as column7Value from cmFormEntry group by formContentId AS org.infoglue.cms.entities.management.GeneralOQLResult");
			
			QueryResults results = oql.execute();

			while (results.hasMore()) 
			{
				GeneralOQLResult resultBean = (GeneralOQLResult)results.next();
				formEntryCountMap.put(resultBean.getId(), new Integer(resultBean.getValue1()));
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a list of form entries. Reason:" + e.getMessage(), e);    
		}
		
		return formEntryCountMap;		
	}

    public FormEntryVO create(FormEntryVO redirectVO) throws ConstraintException, SystemException
    {
        FormEntry formEntry = new FormEntryImpl();
        formEntry.setValueObject(redirectVO);
        formEntry = (FormEntry) createEntity(formEntry);
        return formEntry.getValueObject();
    }

    public FormEntry create(FormEntryVO formEntryVO, List<FormEntryValueVO> formEntryValueVOList, Database db) throws ConstraintException, SystemException, Exception
    {
        FormEntry formEntry = new FormEntryImpl();
        formEntry.setValueObject(formEntryVO);
        formEntry = (FormEntry) createEntity(formEntry, db);
        
        Iterator<FormEntryValueVO> formEntryValueVOListIterator = formEntryValueVOList.iterator();
        while(formEntryValueVOListIterator.hasNext())
        {
        	FormEntryValueVO formEntryValueVO = formEntryValueVOListIterator.next();
        	
        	FormEntryValue formEntryValue = new FormEntryValueImpl();
        	formEntryValue.setFormEntry(formEntry);
        	formEntry.getFormEntryValues().add(formEntryValue);
        	formEntryValue.setValueObject(formEntryValueVO);
        	formEntryValue = (FormEntryValue) createEntity(formEntryValue, db);
        }
	        
        return formEntry;
    }

	public void createAsset(FormEntryAssetVO newAsset, FormEntry formEntry, InputStream is, Integer id, InfoGluePrincipal principal, Database db) throws Exception
	{
    	FormEntryAsset formEntryAsset = new FormEntryAssetImpl();
    	formEntryAsset.setFormEntry(formEntry);
    	formEntry.getFormEntryAssets().add(formEntryAsset);
    	formEntryAsset.setValueObject(newAsset);
    	formEntryAsset.setAssetBlob(is);

    	formEntryAsset = (FormEntryAsset) createEntity(formEntryAsset, db);
	}

    public void delete(FormEntryVO formEntryVO) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
        
    	beginTransaction(db);
 		try
        {		
 			FormEntry formEntry = getFormEntryWithId(formEntryVO.getId(), db);
 			
 			Collection formEntryValues = formEntry.getFormEntryValues();
 			Iterator formEntryValuesIterator = formEntryValues.iterator();
 			while(formEntryValuesIterator.hasNext())
 			{
 				FormEntryValue value = (FormEntryValue)formEntryValuesIterator.next();
 				//value.getFormEntry().getFormEntryValues().remove(value);
 				formEntryValuesIterator.remove();
 				db.remove(value);
 			}
 			
 			Collection formEntryAssets = formEntry.getFormEntryAssets();
 			Iterator formEntryAssetsIterator = formEntryAssets.iterator();
 			while(formEntryAssetsIterator.hasNext())
 			{
 				FormEntryAsset asset = (FormEntryAsset)formEntryAssetsIterator.next();
 				//asset.getFormEntry().getFormEntryAssets().remove(asset);
 				formEntryAssetsIterator.remove();
 				db.remove(asset);
 			}
 	    	
 	        db.remove(formEntry);

 	        commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    }
    

    public FormEntryVO update(FormEntryVO formEntryVO) throws ConstraintException, SystemException
    {
    	return (FormEntryVO) updateEntity(FormEntryImpl.class, formEntryVO);
    }
    
	/**
	 * This method removes a Repository from the system and also cleans out all depending repositoryLanguages.
	 */
    /*
    public void delete(FormEntryVO formEntryVO, boolean forceDelete, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    {
		Database db = CastorDatabaseService.getDatabase();
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		beginTransaction(db);

		try
		{
			FormEntry formEntry = getFormEntryWithId(formEntryVO.getId(), db);
			
			Collection entryValues = formEntry.getFormEntryValues();
			Iterator entryValuesIterator = entryValues.iterator();
			while(entryValuesIterator.hasNext())
			{
				deleteEntity(
				entryValuesIterator.remove();
			}
			
			deleteEntity(RepositoryImpl.class, repositoryVO.getRepositoryId(), db);
	
			//If any of the validations or setMethods reported an error, we throw them up now before create.
			ceb.throwIfNotEmpty();
    
			commitTransaction(db);
		}
		catch(ConstraintException ce)
		{
			logger.warn("An error occurred so we should not completes the transaction:" + ce, ce);
			rollbackTransaction(db);
			throw ce;
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not completes the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
    } 
	*/
    
	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new ContentTypeDefinitionVO();
	}

}
