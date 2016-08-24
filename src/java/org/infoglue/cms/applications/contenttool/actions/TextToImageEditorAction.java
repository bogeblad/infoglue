/* ===============================================================================
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

package org.infoglue.cms.applications.contenttool.actions;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.management.AvailableServiceBindingVO;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.util.BrowserBean;

/**
 * @author Stefan Sik
 * @version 1.0
 * @since InfoglueCMS 1.2.0
 * 
 */
public class TextToImageEditorAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(TextToImageEditorAction.class.getName());

	private static final long serialVersionUID = 1L;

    private Integer languageId = null;
    private Integer contentId = null;
    private Integer siteNodeId = null;
    private Integer repositoryId = null;
    
    /* (non-Javadoc)
     * @see org.infoglue.cms.applications.common.actions.WebworkAbstractAction#doExecute()
     */
    
    private String[] defaultSizes = 
    	{	"8", "9",
            "10","11","12","13","14","15","16","18","20","22", 
            "24","26","28","30","36","42","48","72" 
        };
    
    private String text = "Sample";
    private String canvasWidth = "200";
    private String canvasHeight = "50";
    private String textStartPosX = "0";
    private String textStartPosY = "0";
    private String textWidth = "20";
    private String textHeight = "20";
    private String fontName = "Serif";
    private String fontStyle = "0";
    private String fontSize = "12";
    private String foregroundColor = "000000";
    private String backgroundColor = "FFFFFF";    
    
    
    private String generatedImage = "";
    private String generatedCommand = "";
    private Collection fontNames = new ArrayList();
    private Collection fonts = new ArrayList();
    private SiteNodeVO siteNodeVO;
    private SiteNodeVersionVO siteNodeVersionVO;
    private SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO;
    private List availableServiceBindings;
    private List serviceBindings;
    
    public int getBoldValue()
    {
        return Font.BOLD;
    }
    public int getItalicValue()
    {
        return Font.ITALIC;
    }
    public String[] getSizes()
    {
        return defaultSizes;
    }
    
    
    private String q(String t)
    {
        return "\"" + t + "\"";
    }
    private String qc(String t)
    {
        return "\"" + t + "\", ";
    }
    private String c(String t)
    {
        return "" + t + ", ";
    }
    
    protected void initialize(Integer siteNodeId) throws Exception
    {
		this.siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
        this.siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(siteNodeId);
		
        if(siteNodeVO.getSiteNodeTypeDefinitionId() != null)
        {
	        this.siteNodeTypeDefinitionVO = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionVOWithId(siteNodeVO.getSiteNodeTypeDefinitionId());
			this.availableServiceBindings = SiteNodeTypeDefinitionController.getController().getAvailableServiceBindingVOList(siteNodeVO.getSiteNodeTypeDefinitionId());
			this.serviceBindings = SiteNodeVersionController.getServiceBindningVOList(siteNodeVersionVO.getSiteNodeVersionId());
		}
    } 
    
    protected String doExecute() throws Exception
    {
        DatabaseWrapper dbWrapper = new DatabaseWrapper(CastorDatabaseService.getDatabase());
    	//Database db = CastorDatabaseService.getDatabase();
		
		beginTransaction(dbWrapper.getDatabase());

		try
		{
	        if(siteNodeId==null)
	            siteNodeId = SiteNodeController.getController().getRootSiteNodeVO(repositoryId).getId();
	        
	        if(languageId==null)
	            languageId = LanguageController.getController().getMasterLanguage(repositoryId).getId();
	        
	        if(contentId==null)
	            contentId = new Integer(-1);
	
	        initialize(siteNodeId);
	        
	        /* An editor to simplify textToImage statements in templates
	         * This class generates visually a getTextAsImageUrl(..)
	         * Use BasicTemplateController.renderString(template)
	         */
	        
	        /*
	         * Setup font lists
	         */
	        
	        Font allFonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
	        for(int i = 0;i<allFonts.length;i++)
	        {
	            fonts.add(allFonts[i]);
	        }
	        
	        String[] fontNamesList = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	        for(int i = 0;i<fontNamesList.length;i++)
	            fontNames.add(fontNamesList[i]);
	        
	        
	        StringBuffer t  = new StringBuffer();
	        t.append("$templateLogic.getStringAsImageUrl(");
	        t.append(qc(text));
	        t.append(c(canvasWidth));
	        t.append(c(canvasHeight));
	        t.append(c(textStartPosX));
	        t.append(c(textStartPosY));
	        t.append(c(textWidth));
	        t.append(c(textHeight));
	        t.append(qc(fontName));
	        t.append(c(fontStyle));
	        t.append(c(fontSize));
	        t.append(qc(foregroundColor));
	        t.append(q(backgroundColor));
	        t.append(")");
	
	        
	        BasicTemplateController templateController = getTemplateController(dbWrapper, siteNodeId, languageId, contentId);
	        generatedImage = templateController.renderString(t.toString());
	        generatedCommand = t.toString();
	        
	    	
	        closeTransaction(dbWrapper.getDatabase());
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(dbWrapper.getDatabase());
			throw new SystemException(e.getMessage());
		}

        return "success";
    }

    public BasicTemplateController getTemplateController(DatabaseWrapper databaseWrapper, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
	{
        BasicTemplateController templateController = new BasicTemplateController(databaseWrapper, this.getInfoGluePrincipal());
		templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
		templateController.setHttpRequest(getRequest());	
		templateController.setBrowserBean(new BrowserBean());
		templateController.setDeliveryControllers(NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId), null, IntegrationDeliveryController.getIntegrationDeliveryController(siteNodeId,languageId, contentId));	
		return templateController;		
	}

	public List getSortedAvailableServiceBindings()
	{
		List sortedAvailableServiceBindings = new ArrayList();
		
		Iterator iterator = this.availableServiceBindings.iterator();
		while(iterator.hasNext())
		{
			AvailableServiceBindingVO availableServiceBinding = (AvailableServiceBindingVO)iterator.next();
			int index = 0;
			Iterator sortedListIterator = sortedAvailableServiceBindings.iterator();
			while(sortedListIterator.hasNext())
			{
				AvailableServiceBindingVO sortedAvailableServiceBinding = (AvailableServiceBindingVO)sortedListIterator.next();
				
				String currentAttribute = availableServiceBinding.getName();
				String sortedAttribute  = sortedAvailableServiceBinding.getName();
				
				if(currentAttribute != null && sortedAttribute != null && currentAttribute.compareTo(sortedAttribute) < 0)
		    	{
		    		break;
		    	}
		    	index++;
			}
			sortedAvailableServiceBindings.add(index, availableServiceBinding);
		}
			
		return sortedAvailableServiceBindings;
	}
	
	/**
	 * This method sorts a list of available service bindings on the name of the binding.
	 */

	public List getSortedAvailableContentServiceBindings()
	{
		List sortedAvailableContentServiceBindings = new ArrayList();
		
		Iterator sortedListIterator = getSortedAvailableServiceBindings().iterator();
		while(sortedListIterator.hasNext())
		{
			AvailableServiceBindingVO sortedAvailableServiceBinding = (AvailableServiceBindingVO)sortedListIterator.next();
			if(sortedAvailableServiceBinding.getVisualizationAction().indexOf("Structure") == -1)
				sortedAvailableContentServiceBindings.add(sortedAvailableServiceBinding);
		}
			
		return sortedAvailableContentServiceBindings;
	}
	public List getSortedAvailableSingleContentServiceBindings()
	{
		List sortedAvailableContentServiceBindings = new ArrayList();
		
		Iterator sortedListIterator = getSortedAvailableServiceBindings().iterator();
		while(sortedListIterator.hasNext())
		{
			AvailableServiceBindingVO sortedAvailableServiceBinding = (AvailableServiceBindingVO)sortedListIterator.next();
			if(sortedAvailableServiceBinding.getVisualizationAction().indexOf("Structure") == -1 &&
			   sortedAvailableServiceBinding.getVisualizationAction().indexOf("Multi") == -1 )
				sortedAvailableContentServiceBindings.add(sortedAvailableServiceBinding);
		}
			
		return sortedAvailableContentServiceBindings;
	}

	public List getAvailableAttributes()
	{
		List sortedAvailableContentServiceBindings = new ArrayList();
		
		Iterator sortedListIterator = getSortedAvailableServiceBindings().iterator();
		while(sortedListIterator.hasNext())
		{
			AvailableServiceBindingVO sortedAvailableServiceBinding = (AvailableServiceBindingVO)sortedListIterator.next();
			if(sortedAvailableServiceBinding.getVisualizationAction().indexOf("Structure") == -1 &&
			   sortedAvailableServiceBinding.getVisualizationAction().indexOf("Multi") == -1 )
			{
				sortedAvailableContentServiceBindings.add(sortedAvailableServiceBinding);
				
			}
			
		}
		
		
		
		return sortedAvailableContentServiceBindings;
	}

	/**
	 * This method sorts a list of available service bindings on the name of the binding.
	 */

	public List getSortedAvailableStructureServiceBindings()
	{
		List sortedAvailableStructureServiceBindings = new ArrayList();
		
		Iterator sortedListIterator = getSortedAvailableServiceBindings().iterator();
		while(sortedListIterator.hasNext())
		{
			AvailableServiceBindingVO sortedAvailableServiceBinding = (AvailableServiceBindingVO)sortedListIterator.next();
			if(sortedAvailableServiceBinding.getVisualizationAction().indexOf("Structure") > -1)
				sortedAvailableStructureServiceBindings.add(sortedAvailableServiceBinding);
		}
			
		return sortedAvailableStructureServiceBindings;
	}

	
	public List getServiceBindings()
	{
		return this.serviceBindings;
	}	
    
    
    public String getGeneratedImage()
    {
        return generatedImage;
    }

    public Collection getFonts()
    {
        return fonts;
    }
    /**
     * @return Returns the backgroundColor.
     */
    public String getBackgroundColor()
    {
        return backgroundColor;
    }
    /**
     * @param backgroundColor The backgroundColor to set.
     */
    public void setBackgroundColor(String backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }
    /**
     * @return Returns the canvasHeight.
     */
    public String getCanvasHeight()
    {
        return canvasHeight;
    }
    /**
     * @param canvasHeight The canvasHeight to set.
     */
    public void setCanvasHeight(String canvasHeight)
    {
        this.canvasHeight = canvasHeight;
    }
    /**
     * @return Returns the canvasWidth.
     */
    public String getCanvasWidth()
    {
        return canvasWidth;
    }
    /**
     * @param canvasWidth The canvasWidth to set.
     */
    public void setCanvasWidth(String canvasWidth)
    {
        this.canvasWidth = canvasWidth;
    }
    /**
     * @return Returns the fontName.
     */
    public String getFontName()
    {
        return fontName;
    }
    /**
     * @param fontName The fontName to set.
     */
    public void setFontName(String fontName)
    {
        this.fontName = fontName;
    }
    /**
     * @return Returns the fontSize.
     */
    public String getFontSize()
    {
        return fontSize;
    }
    /**
     * @param fontSize The fontSize to set.
     */
    public void setFontSize(String fontSize)
    {
        this.fontSize = fontSize;
    }
    /**
     * @return Returns the fontStyle.
     */
    public String getFontStyle()
    {
        return fontStyle;
    }
    /**
     * @param fontStyle The fontStyle to set.
     */
    public void setFontStyle(String fontStyle)
    {
        this.fontStyle = fontStyle;
    }
    /**
     * @return Returns the foregroundColor.
     */
    public String getForegroundColor()
    {
        return foregroundColor;
    }
    /**
     * @param foregroundColor The foregroundColor to set.
     */
    public void setForegroundColor(String foregroundColor)
    {
        this.foregroundColor = foregroundColor;
    }
    /**
     * @return Returns the text.
     */
    public String getText()
    {
        return text;
    }
    /**
     * @param text The text to set.
     */
    public void setText(String text)
    {
        this.text = text;
    }
    /**
     * @return Returns the textHeight.
     */
    public String getTextHeight()
    {
        return textHeight;
    }
    /**
     * @param textHeight The textHeight to set.
     */
    public void setTextHeight(String textHeight)
    {
        this.textHeight = textHeight;
    }
    /**
     * @return Returns the textStartPosX.
     */
    public String getTextStartPosX()
    {
        return textStartPosX;
    }
    /**
     * @param textStartPosX The textStartPosX to set.
     */
    public void setTextStartPosX(String textStartPosX)
    {
        this.textStartPosX = textStartPosX;
    }
    /**
     * @return Returns the textStartPosY.
     */
    public String getTextStartPosY()
    {
        return textStartPosY;
    }
    /**
     * @param textStartPosY The textStartPosY to set.
     */
    public void setTextStartPosY(String textStartPosY)
    {
        this.textStartPosY = textStartPosY;
    }
    /**
     * @return Returns the textWidth.
     */
    public String getTextWidth()
    {
        return textWidth;
    }
    /**
     * @param textWidth The textWidth to set.
     */
    public void setTextWidth(String textWidth)
    {
        this.textWidth = textWidth;
    }
    /**
     * @return Returns the fontNames.
     */
    public Collection getFontNames()
    {
        return fontNames;
    }
    /**
     * @return Returns the contentId.
     */
    public Integer getContentId()
    {
        return contentId;
    }
    /**
     * @param contentId The contentId to set.
     */
    public void setContentId(Integer contentId)
    {
        this.contentId = contentId;
    }
    /**
     * @return Returns the languageId.
     */
    public Integer getLanguageId()
    {
        return languageId;
    }
    /**
     * @param languageId The languageId to set.
     */
    public void setLanguageId(Integer languageId)
    {
        this.languageId = languageId;
    }
    /**
     * @return Returns the repositoryId.
     */
    public Integer getRepositoryId()
    {
        return repositoryId;
    }
    /**
     * @param repositoryId The repositoryId to set.
     */
    public void setRepositoryId(Integer repositoryId)
    {
        this.repositoryId = repositoryId;
    }
    /**
     * @return Returns the siteNodeId.
     */
    public Integer getSiteNodeId()
    {
        return siteNodeId;
    }
    /**
     * @param siteNodeId The siteNodeId to set.
     */
    public void setSiteNodeId(Integer siteNodeId)
    {
        this.siteNodeId = siteNodeId;
    }
    /**
     * @return Returns the generatedCommand.
     */
    public String getGeneratedCommand()
    {
        return generatedCommand;
    }
    /**
     * @param generatedCommand The generatedCommand to set.
     */
    public void setGeneratedCommand(String generatedCommand)
    {
        this.generatedCommand = generatedCommand;
    }
}
