FCKConfig.CustomConfigurationsPath = '' ;
//FCKConfig.EditorAreaCSS = '$request.contextPath/ViewPage.action?siteNodeId=100130' ;
FCKConfig.EditorAreaCSS = '/infoglueDeliverWorking/ViewPage.action?siteNodeId=100670';

FCKConfig.BaseHref = '' ;
FCKConfig.FullPage = false ;
FCKConfig.Debug = false ;
FCKConfig.SkinPath = FCKConfig.BasePath + 'skins/default/' ;
FCKConfig.PluginsPath = FCKConfig.BasePath + 'plugins/' ;
FCKConfig.DefaultLanguage		= 'en' ;
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

FCKConfig.StyleFontFormats = 'false';
FCKConfig.StyleStyles = 'false';
//FCKConfig.AllowImageSizes = 'false';
FCKConfig.ImageClasses = 'img_left_portrait,img_left_letter,img_right_letter,img_right_portrait,img_scaled';
FCKConfig.ImageClassesNames = 'Stående vänster,Liggande vänster,Liggande höger,Stående höger,Fullbredd';

FCKConfig.MaxUndoLevels = 15 ;

FCKConfig.ToolbarSets["Default"] = [
	['Cut','Copy','Paste','PasteText','Print','Undo','Redo','Find','Replace','RemoveFormat'],
	['Bold','Italic'],
	['OrderedList','UnorderedList'],
	['Link','Unlink','Anchor'],
	['Image','Table','Rule','SpecialChar','Source'],
    ['Style','FontFormat']
] ;
FCKConfig.ToolbarSets["Default2"] = [
	['Cut','Copy','Paste','PasteText','Print','Undo','Redo','Find','Replace','RemoveFormat'],
	['Bold','Italic'],
	['OrderedList','UnorderedList'],
	['Link','Unlink','Anchor'],
	['Image','Table','Rule','SpecialChar','Source'],
    ['Style','FontFormat']
] ;

FCKConfig.ContextMenu = ['Generic','Link','Anchor','Image','Select','Textarea','Checkbox','Radio','TextField','HiddenField','ImageButton','Button','BulletedList','NumberedList','TableCell','Table','Form'] ;

FCKConfig.FontFormats	= 'p;h1;h2;h3' ;
FCKConfig.FontFormatNames = 'Br&#246dtext;Formaterad;Adress;Rubrik niv&#229 1;Rubrik niv&#229 2;Rubrik niv&#229 3;Rubrik niv&#229 4;Stort citat;Rubrik 6;Div' ;

FCKConfig.ListClasses = 'fillista';
FCKConfig.ListClassesNames = 'Fillista';

FCKConfig.StylesXmlPath	= '$request.contextPath/WYSIWYGProperties!viewStylesXML.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")' ;

FCKConfig.SpellChecker			= 'ieSpell' ;	// 'ieSpell' | 'SpellerPages'
FCKConfig.IeSpellDownloadUrl	= 'http://www.iespell.com/rel/ieSpellSetup211325.exe' ;
FCKConfig.LinkBrowser = true ;
FCKConfig.LinkBrowserURL = "$request.contextPath/ViewLinkDialog!viewLinkDialogForFCKEditor.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")" ;
FCKConfig.LinkBrowserWindowWidth	= "770" ;
FCKConfig.LinkBrowserWindowHeight	= "660" ;
FCKConfig.ImageBrowser = true ;
FCKConfig.ImageBrowserURL = "$request.contextPath/ViewContentVersion!viewAssetsDialogForFCKEditor.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")";	
FCKConfig.ImageBrowserWindowWidth  = "750" ;
FCKConfig.ImageBrowserWindowHeight = "600" ;