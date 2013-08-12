if(typeof CKEDITOR != "undefined")
{
	CKEDITOR.editorConfig = function( config )
	{
		config.toolbar_Mini =
	    [
	        ['Bold','Italic','Link','Unlink','Image']
	    ];
	
		config.toolbar_Basic =
	    [
	        ['Bold','Italic','NumberedList','BulletedList','Link','Unlink','Image','Flash','Maximize','Format']
	    ];
		config.toolbar_Default =
		[
			['Source','-','Save','Preview','Print','Template'],
			['Cut','Copy','PasteText','Undo','Redo'],
			['Find','SelectAll','SpellChecker','Scayt'],
			['Bold','Italic','Subscript','Superscript','RemoveFormat'],
			['NumberedList','BulletedList'],
			['Link','Unlink','Anchor'],
			['Image','Flash','SpecialChar','Iframe'],
			['Format','FontSize'],
			['Maximize','ShowBlocks']
		];
	    
	    config.toolbar_Full =
    	[
    	    ['Source','-','Save','NewPage','Preview','-','Templates'],
    	    ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print', 'SpellChecker', 'Scayt'],
    	    ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
    	    ['Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField'],
    	    ['BidiLtr', 'BidiRtl'],
    	    '/',
    	    ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
    	    ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote','CreateDiv'],
    	    ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
    	    ['Link','Unlink','Anchor'],
    	    ['Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak'],
    	    '/',
    	    ['Styles','Format','Font','FontSize'],
    	    ['TextColor','BGColor'],
    	    ['Maximize', 'ShowBlocks','-','About']
    	];
	    config.skin = 'moono';
	   	config.contentsCss = '/infoglueDeliverWorking/ViewPage.action?siteNodeId=65';
	    	
	    config.filebrowserBrowseUrl = '$request.contextPath/ViewLinkDialog!viewLinkDialogForFCKEditorV3.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")';
	    config.filebrowserImageBrowseUrl = '$request.contextPath/ViewContentVersion!viewAssetBrowserForFCKEditorV3.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")&assetTypeFilter=*';
	    config.filebrowserImageUploadUrl = '$request.contextPath/CreateDigitalAsset.action?contentVersionId=$!contentVersionId&useFckUploadMessages=true';
	    config.filebrowserUploadUrl = '$request.contextPath/CreateDigitalAsset.action?contentVersionId=$!contentVersionId&useFckUploadMessages=true';
	};
}
else if(typeof FCKConfig != "undefined")
{
	FCKConfig.CustomConfigurationsPath = '' ;
	FCKConfig.EditorAreaCSS = '/infoglueDeliverWorking/ViewPage.action?siteNodeId=65';
	
	FCKConfig.BaseHref = '' ;
	FCKConfig.FullPage = false ;
	FCKConfig.Debug = false ;
	FCKConfig.SkinPath = FCKConfig.BasePath + 'skins/default/' ;
	FCKConfig.PluginsPath = FCKConfig.BasePath + 'plugins/' ;
	FCKConfig.AutoDetectLanguage = false;
	#if($principalLanguageCode)
	  FCKConfig.DefaultLanguage = '$principalLanguageCode' ;
	#else
	  FCKConfig.DefaultLanguage = 'en' ;
	#end
	FCKConfig.ContentLangDirection	= 'ltr' ;
	FCKConfig.EnableXHTML		= true ;
	FCKConfig.EnableSourceXHTML	= true ;
	FCKConfig.FillEmptyBlocks	= true ;
	FCKConfig.FormatSource		= true ;
	FCKConfig.FormatOutput		= true ;
	FCKConfig.FormatIndentator	= '    ' ;
	FCKConfig.GeckoUseSPAN	= true ;
	FCKConfig.StartupFocus	= false ;
	FCKConfig.ForcePasteAsPlainText	= true ;
	FCKConfig.ForceSimpleAmpersand	= false ;
	FCKConfig.TabSpaces		= 0 ;
	FCKConfig.ShowBorders	= true ;
	FCKConfig.UseBROnCarriageReturn	= false ;
	FCKConfig.ToolbarStartExpanded	= true ;
	FCKConfig.ToolbarCanCollapse	= true ;
	
	//FCKConfig.Plugins.Add( 'autogrow' ) ;
	//FCKConfig.AutoGrowMax = 2000 ;
	
	FCKConfig.StyleFontFormats = 'false';
	FCKConfig.StyleStyles = 'false';
	FCKConfig.AllowImageSizes = 'false';
	
	//FCKConfig.ImageClasses = '';
	//FCKConfig.ImageClassesNames = '';
	
	FCKConfig.LinkClasses = 'pdf,doc,xls';
	FCKConfig.LinkClassesNames = 'PDF link,Word link,Excel link';
	
	FCKConfig.MaxUndoLevels = 15 ;
	
	FCKConfig.ToolbarSets["Mini"] = [
		['Bold','Italic','-','Link','Unlink','-','Image']
	] ;
	
	FCKConfig.ToolbarSets["Basic"] = [
		['Bold','Italic','-','OrderedList','UnorderedList','-','Link','Unlink','-','Image','FitWindow','Flash','ShowBlocks'],
		'/',['FontFormat']
	] ;
	
	FCKConfig.ToolbarSets["Default"] = [
		['Cut','Copy','PasteText','Print','Undo','Redo','Find','Replace','SpecialChar','Source'],
		'/',
		['Bold','Italic'],
		['OrderedList','UnorderedList'],
		['Link','Unlink','Anchor'],
		['Image','Rule','SpellCheck'],
	    ['Style','FontFormat']
	] ;
	
	
	FCKConfig.ContextMenu = ['Generic','Link','Anchor','Image','Select','Textarea','Checkbox','Radio','TextField','HiddenField','ImageButton','Button','BulletedList','NumberedList','TableCell','Table','Form'] ;
	
	FCKConfig.FontFormats	= 'h2;h4;h3;h5;p' ;
	FCKConfig.FontFormatNames = 'Paragraph;Formatted;Adress;Headline 2;Headline 3;Headline 4;Headline 5;Blockquote;Headline 6;Div' ;
	
	//FCKConfig.StylesXmlPath	= '$request.contextPath/WYSIWYGProperties!viewStylesXML.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")' ;
	
	FCKConfig.SpellChecker = 'ieSpell' ;
	FCKConfig.LinkBrowser = true ;
	FCKConfig.LinkBrowserURL = "$request.contextPath/ViewLinkDialog!viewLinkDialogForFCKEditorV3.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")" ;
	//FCKConfig.LinkBrowserURL = "$request.contextPath/ViewLinkDialog!viewLinkDialogForFCKEditor.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")" ;
	FCKConfig.LinkBrowserWindowWidth	= "770" ;
	FCKConfig.LinkBrowserWindowHeight	= "640" ;
	FCKConfig.ImageBrowser = true ;
	FCKConfig.ImageBrowserURL = "$request.contextPath/ViewContentVersion!viewAssetBrowserForFCKEditorV3.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")&assetTypeFilter=*";	
	//FCKConfig.ImageBrowserURL = "$request.contextPath/ViewContentVersion!viewAssetsDialogForFCKEditor.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")";	
	FCKConfig.ImageUploadURL = '$request.contextPath/CreateDigitalAsset.action?contentVersionId=$!contentVersionId&useFckUploadMessages=true';
	FCKConfig.LinkUpload = true ;
	FCKConfig.LinkUploadURL = '$request.contextPath/CreateDigitalAsset.action?contentVersionId=$!contentVersionId&useFckUploadMessages=true';
	FCKConfig.ImageBrowserWindowWidth  = "750" ;
	FCKConfig.ImageBrowserWindowHeight = "600" ;
	
	function overrideLabels()
	{
		//alert("Lang:" + FCKConfig.DefaultLanguage);
		if(FCKConfig.DefaultLanguage == "sv")
		{
			FCKLang.DlgBtnBrowseServer = "Välj från InfoGlue...";
			FCKLang.DlgImgBtnUpload = "Ladda upp till InfoGlue";
			FCKLang.DlgLnkBtnUpload = "Ladda upp till InfoGlue";
		    FCKLang.DlgImgURL = "URL (glöm ej http:// om du anger extern URL)";
		    FCKLang.DlgLnkURL = "URL (glöm ej http:// om du anger extern URL)";
		}
		else
		{
			FCKLang.DlgBtnBrowseServer = "Choose from InfoGlue...";
			FCKLang.DlgImgBtnUpload = "Send to InfoGlue";
			FCKLang.DlgLnkBtnUpload = "Send to InfoGlue";
		    FCKLang.DlgImgURL = "URL (don't forget http:// if you state an external url)";
		    FCKLang.DlgLnkURL = "URL (glöm ej http:// om du anger extern URL)";
		}
	}
	
	setTimeout("overrideLabels()", 3000);
}