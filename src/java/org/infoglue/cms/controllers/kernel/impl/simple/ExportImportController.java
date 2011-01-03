package org.infoglue.cms.controllers.kernel.impl.simple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.contenttool.actions.ExportContentAction;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.impl.simple.InfoGlueExportImpl;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.handlers.DigitalAssetBytesHandler;

public class ExportImportController extends BaseController
{
    private final static Logger logger = Logger.getLogger(ExportContentAction.class.getName());

	public static ExportImportController getController()
	{
		return new ExportImportController();
	}
	
	public String exportContent(List contentIdList, String path, String fileNamePrefix, boolean includeContentTypes, boolean includeCategories) throws Exception
	{
		VisualFormatter vf = new VisualFormatter();
		fileNamePrefix = vf.replaceNonAscii(fileNamePrefix, '_');
		
		String fileName = null;
			
		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			Mapping map = new Mapping();
			String exportFormat = CmsPropertyHandler.getExportFormat();
			
			map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_content_2.5.xml").toString());
			
			db.begin();
	
			List contents = new ArrayList();
			
			Iterator i = contentIdList.iterator();
			while(i.hasNext())
			{
				Integer contentId = (Integer)i.next();
				Content content   = ContentController.getContentController().getContentWithId(contentId, db);
				contents.add(content);
			}
			
			List contentTypeDefinitions = new ArrayList();
			if(includeContentTypes)
				contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionList(db);
			
			List categories = new ArrayList();
			if(includeCategories)
				categories = CategoryController.getController().findAllActiveCategories();
			
			InfoGlueExportImpl infoGlueExportImpl = new InfoGlueExportImpl();
			
			String filePath = CmsPropertyHandler.getDigitalAssetPath();
			String tempFileName = "Export_tmp_" + Thread.currentThread().getId() + "_" + fileNamePrefix + ".xml";
			String tempFileSystemName = filePath + File.separator + tempFileName;
						
			String encoding = "UTF-8";
			File tempFile = new File(tempFileSystemName);
	        FileOutputStream fos = new FileOutputStream(tempFile);
	        OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
	        Marshaller marshaller = new Marshaller(osw);
	        marshaller.setMapping(map);
			marshaller.setEncoding(encoding);
			DigitalAssetBytesHandler.setMaxSize(-1);
	
			infoGlueExportImpl.getRootContent().addAll(contents);
			
			infoGlueExportImpl.setContentTypeDefinitions(contentTypeDefinitions);
			infoGlueExportImpl.setCategories(categories);
			
			marshaller.marshal(infoGlueExportImpl);
			
			osw.flush();
			osw.close();
			
			fileName = fileNamePrefix + "_" + tempFile.length() + ".xml";
			String fileSystemName = filePath + File.separator + fileName;
			File file = new File(fileSystemName);
			tempFile.renameTo(file);
			
			db.rollback();
		} 
		catch (Exception e) 
		{
			logger.error("An error was found exporting a repository: " + e.getMessage(), e);
			db.rollback();
		}
		finally
		{
			db.close();	
		}
		
		return fileName;
	}

	public BaseEntityVO getNewVO()
	{
		return null;
	}
	
}
