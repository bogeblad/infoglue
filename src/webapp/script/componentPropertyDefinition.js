//////////////////////////////////////////////////////
// File: ComponentPropertyDefinition.js
//
// Author: Mattias Bogeblad
// 
// Purpose: To have a generic ComponentPropertyDefinition.
//
//////////////////////////////////////////////////////


/**
 * ComponentPropertyDefinition object
 */

function ComponentPropertyDefinition(name, displayName, type, entity, multiple, assetBinding, assetMask, isPuffContentForPage, allowedContentTypeNames, description, defaultValue, allowLanguageVariations, WYSIWYGEnabled, WYSIWYGToolbar, dataProvider, dataProviderParameters, autoCreateContent, autoCreateContentMethod, autoCreateContentPath, customMarkup, allowMultipleSelections)
{
	this.name 						= name;
	this.displayName				= displayName;
	this.type 						= type;
	this.entity						= entity;
	this.multiple					= multiple;
	this.assetBinding 				= assetBinding;
	this.assetMask 					= assetMask;
	this.isPuffContentForPage		= isPuffContentForPage;
	this.allowedContentTypeNames 	= allowedContentTypeNames;
	this.description				= description;
	this.defaultValue				= defaultValue;
	this.allowLanguageVariations	= allowLanguageVariations;
	this.WYSIWYGEnabled				= WYSIWYGEnabled;
	this.WYSIWYGToolbar				= WYSIWYGToolbar;
	this.dataProvider 				= dataProvider;
	this.dataProviderParameters		= dataProviderParameters;
	this.allowMultipleSelections    = allowMultipleSelections;
	this.autoCreateContent			= autoCreateContent;
	this.autoCreateContentMethod	= autoCreateContentMethod;
	this.autoCreateContentPath		= autoCreateContentPath;
	this.customMarkup 				= customMarkup;
	this.options					= new Vector(0);
	  
  	this.getName 					= getName;
  	this.getDisplayName 			= getDisplayName;
  	this.getType 					= getType;
  	this.getEntity 					= getEntity;
  	this.getMultiple				= getMultiple;
  	this.getAssetBinding			= getAssetBinding;
  	this.getAssetMask				= getAssetMask;
	this.getIsPuffContentForPage	= getIsPuffContentForPage;
  	this.getAllowedContentTypeNames = getAllowedContentTypeNames;
	this.getDescription				= getDescription;
	this.getOptions					= getOptions;
	this.getDefaultValue			= getDefaultValue;
	this.getAllowLanguageVariations	= getAllowLanguageVariations;
	this.getWYSIWYGEnabled			= getWYSIWYGEnabled;
	this.getWYSIWYGToolbar			= getWYSIWYGToolbar;
	this.getDataProvider			= getDataProvider;
	this.getDataProviderParameters	= getDataProviderParameters;
	this.getAllowMultipleSelections	= getAllowMultipleSelections;
	this.getAutoCreateContent		= getAutoCreateContent;
	this.getAutoCreateContentMethod	= getAutoCreateContentMethod;
	this.getAutoCreateContentPath	= getAutoCreateContentPath;
	this.getCustomMarkup			= getCustomMarkup;
	
  	this.setName 					= setName;
  	this.setDisplayName 			= setDisplayName;
  	this.setType 					= setType;
  	this.setEntity 					= setEntity;
  	this.setMultiple				= setMultiple;
  	this.setAssetBinding			= setAssetBinding;
  	this.setAssetMask				= setAssetMask;
  	this.setIsPuffContentForPage	= setIsPuffContentForPage;
  	this.setAllowedContentTypeNames = setAllowedContentTypeNames;
	this.setDescription				= setDescription;
	this.setDefaultValue			= setDefaultValue;
	this.setAllowLanguageVariations	= setAllowLanguageVariations;
	this.setWYSIWYGEnabled			= setWYSIWYGEnabled;
	this.setWYSIWYGToolbar			= setWYSIWYGToolbar;
	this.setDataProvider			= setDataProvider;
	this.setDataProviderParameters	= setDataProviderParameters;
	this.setAllowMultipleSelections	= setAllowMultipleSelections;
	this.setAutoCreateContent		= setAutoCreateContent;
	this.setAutoCreateContentMethod	= setAutoCreateContentMethod;
	this.setAutoCreateContentPath	= setAutoCreateContentPath;
	this.setCustomMarkup			= setCustomMarkup;
}
  
function getName()
{
  	return this.name;
}

function getDisplayName()
{
  	return this.displayName;
}

function getType()
{
  	return this.type;
}

function getEntity()
{
  	return this.entity;
}

function getMultiple()
{
  	return this.multiple;
}

function getAssetBinding()
{
  	return this.assetBinding;
}

function getAssetMask()
{
  	return this.assetMask;
}

function getIsPuffContentForPage()
{
	return this.isPuffContentForPage;
}

function getAllowedContentTypeNames()
{
	return this.allowedContentTypeNames;
}

function getDescription()
{
	return this.description;
}

function getOptions()
{
	return this.options;
}

function setName(name)
{
  	this.name = name;
}

function setDisplayName(displayName)
{
  	this.displayName = displayName;
}

function setType(type)
{
  	this.type = type;
}

function setEntity(entity)
{
	this.entity = entity;
}

function setMultiple(multiple)
{
	this.multiple = multiple;
}

function setAssetBinding(assetBinding)
{
  	this.assetBinding = assetBinding;
}

function setAssetMask(assetMask)
{
  	this.assetMask = assetMask;
}

function setIsPuffContentForPage(isPuffContentForPage)
{
	this.isPuffContentForPage = isPuffContentForPage;
}

function setAllowedContentTypeNames(allowedContentTypeNames)
{
	this.allowedContentTypeNames = allowedContentTypeNames;
}

function setDescription(description)
{
	this.description = description;
}

function getAllowLanguageVariations()
{
	return this.allowLanguageVariations;
}

function setAllowLanguageVariations(allowLanguageVariations)
{
	this.allowLanguageVariations = allowLanguageVariations;
}

function getDefaultValue()
{
	return this.defaultValue;
}

function setDefaultValue(defaultValue)
{
	this.defaultValue = defaultValue;
}

function getWYSIWYGEnabled()
{
	return this.WYSIWYGEnabled;
}

function setWYSIWYGEnabled(WYSIWYGEnabled)
{
	this.WYSIWYGEnabled = WYSIWYGEnabled;
}

function getWYSIWYGToolbar()
{
	return this.WYSIWYGToolbar;
}

function setWYSIWYGToolbar(WYSIWYGToolbar)
{
	this.WYSIWYGToolbar = WYSIWYGToolbar;
}

function getDataProvider()
{
	return this.dataProvider;
}

function setDataProvider(dataProvider)
{
	this.dataProvider = dataProvider;
}

function getDataProviderParameters()
{
	return this.dataProviderParameters;
}

function setDataProviderParameters(dataProviderParameters)
{
	this.dataProviderParameters = dataProviderParameters;
}

function getAllowMultipleSelections()
{
	return this.allowMultipleSelections;
}

function setAllowMultipleSelections(allowMultipleSelections)
{
	this.allowMultipleSelections = allowMultipleSelections;
}

function getAutoCreateContent()
{
	return this.autoCreateContent;
}

function setAutoCreateContent(autoCreateContent)
{
	this.autoCreateContent = autoCreateContent;
}

function getAutoCreateContentMethod()
{
	return this.autoCreateContentMethod;
}

function setAutoCreateContentMethod(autoCreateContentMethod)
{
	this.autoCreateContentMethod = autoCreateContentMethod;
}

function getAutoCreateContentPath()
{
	return this.autoCreateContentPath;
}

function setAutoCreateContentPath(autoCreateContentPath)
{
	this.autoCreateContentPath = autoCreateContentPath;
}

function getCustomMarkup()
{
	return this.customMarkup;
}

function setCustomMarkup(customMarkup)
{
	this.customMarkup = customMarkup;
}
