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
	        ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Scayt'],
	        ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
	        ['Image'],
	        '/',
	        ['Format'],
	        ['Bold','Italic','Strike'],
	        ['NumberedList','BulletedList'],
	        ['Link','Unlink'],
	        ['Maximize','-','About']
	    ];
	    
	    config.skin = 'office2003';
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
	
	//FCKConfig.EditorAreaCSS = FCKConfig.BasePath + 'css/fck_editorarea.css' ;
	//FCKConfig.ToolbarComboPreviewCSS = '' ;
	FCKConfig.EditorAreaStyles = '' ;
	FCKConfig.EditorAreaCSS = 'WYSIWYGProperties!WYSIWYGEditorAreaCSS.action' ;
	FCKConfig.ToolbarComboPreviewCSS = 'WYSIWYGProperties!WYSIWYGToolbarComboPreviewCSS.action' ;
	
	FCKConfig.DocType = '' ;
	
	FCKConfig.BaseHref = '' ;
	
	FCKConfig.FullPage = false ;
	
	// The following option determines whether the "Show Blocks" feature is enabled or not at startup.
	FCKConfig.StartupShowBlocks = false ;
	
	FCKConfig.Debug = false ;
	FCKConfig.AllowQueryStringDebug = true ;
	
	FCKConfig.SkinPath = FCKConfig.BasePath + 'skins/default/' ;
	FCKConfig.PreloadImages = [ FCKConfig.SkinPath + 'images/toolbar.start.gif', FCKConfig.SkinPath + 'images/toolbar.buttonarrow.gif' ] ;
	
	FCKConfig.PluginsPath = FCKConfig.BasePath + 'plugins/' ;
	
	//FCKConfig.Plugins.Add( 'autogrow' ) ;
	// FCKConfig.Plugins.Add( 'dragresizetable' );
	//FCKConfig.AutoGrowMax = 2000 ;
	
	// FCKConfig.ProtectedSource.Add( /<%[\s\S]*?%>/g ) ;	// ASP style server side code <%...%>
	// FCKConfig.ProtectedSource.Add( /<\?[\s\S]*?\?>/g ) ;	// PHP style server side code
	// FCKConfig.ProtectedSource.Add( /(<asp:[^\>]+>[\s|\S]*?<\/asp:[^\>]+>)|(<asp:[^\>]+\/>)/gi ) ;	// ASP.Net style tags <asp:control>
	
	FCKConfig.AutoDetectLanguage = false;
	#if($principalLanguageCode)
	  FCKConfig.DefaultLanguage = '$principalLanguageCode' ;
	#else
	  FCKConfig.DefaultLanguage = 'en' ;
	#end
	FCKConfig.ContentLangDirection	= 'ltr' ;
	
	FCKConfig.ProcessHTMLEntities	= true ;
	FCKConfig.IncludeLatinEntities	= true ;
	FCKConfig.IncludeGreekEntities	= true ;
	
	FCKConfig.ProcessNumericEntities = false ;
	
	FCKConfig.AdditionalNumericEntities = ''  ;		// Single Quote: "'"
	
	FCKConfig.FillEmptyBlocks	= true ;
	
	FCKConfig.FormatSource		= true ;
	FCKConfig.FormatOutput		= true ;
	FCKConfig.FormatIndentator	= '    ' ;
	
	FCKConfig.StartupFocus	= false ;
	FCKConfig.ForcePasteAsPlainText	= false ;
	FCKConfig.AutoDetectPasteFromWord = true ;	// IE only.
	FCKConfig.ShowDropDialog = true ;
	FCKConfig.ForceSimpleAmpersand	= false ;
	FCKConfig.TabSpaces		= 0 ;
	FCKConfig.ShowBorders	= true ;
	FCKConfig.SourcePopup	= false ;
	FCKConfig.ToolbarStartExpanded	= true ;
	FCKConfig.ToolbarCanCollapse	= true ;
	FCKConfig.IgnoreEmptyParagraphValue = true ;
	FCKConfig.PreserveSessionOnFileBrowser = false ;
	FCKConfig.FloatingPanelsZIndex = 10000 ;
	FCKConfig.HtmlEncodeOutput = false ;
	
	FCKConfig.TemplateReplaceAll = true ;
	FCKConfig.TemplateReplaceCheckbox = true ;
	
	FCKConfig.ToolbarLocation = 'In' ;
	
	FCKConfig.ToolbarSets["Default"] = [
		['Source','DocProps','-','Save','NewPage','Preview','-','Templates'],
		['Cut','Copy','Paste','PasteText','PasteWord','-','Print','SpellCheck'],
		['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
		['Form','Checkbox','Radio','TextField','Textarea','Select','Button','ImageButton','HiddenField'],
		'/',
		['Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'],
		['OrderedList','UnorderedList','-','Outdent','Indent','Blockquote'],
		['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
		['Link','Unlink','Anchor'],
		['Image','Flash','Table','Rule','Smiley','SpecialChar','PageBreak'],
		'/',
		['Style','FontFormat','FontName','FontSize'],
		['TextColor','BGColor'],
		['FitWindow','ShowBlocks','-','About']		// No comma for the last row.
	] ;
	
	FCKConfig.ToolbarSets["Basic"] = [
		['Bold','Italic','-','OrderedList','UnorderedList','-','Link','Unlink','-','Image','FitWindow','Flash','ShowBlocks'],
		'/',['FontFormat']
	] ;
	
	FCKConfig.EnterMode = 'p' ;			// p | div | br
	FCKConfig.ShiftEnterMode = 'br' ;	// p | div | br
	
	FCKConfig.Keystrokes = [
		[ CTRL + 65 /*A*/, true ],
		[ CTRL + 67 /*C*/, true ],
		[ CTRL + 70 /*F*/, true ],
		[ CTRL + 83 /*S*/, true ],
		[ CTRL + 88 /*X*/, true ],
		[ CTRL + 86 /*V*/, 'Paste' ],
		[ SHIFT + 45 /*INS*/, 'Paste' ],
		[ CTRL + 88 /*X*/, 'Cut' ],
		[ SHIFT + 46 /*DEL*/, 'Cut' ],
		[ CTRL + 90 /*Z*/, 'Undo' ],
		[ CTRL + 89 /*Y*/, 'Redo' ],
		[ CTRL + SHIFT + 90 /*Z*/, 'Redo' ],
		[ CTRL + 76 /*L*/, 'Link' ],
		[ CTRL + 66 /*B*/, 'Bold' ],
		[ CTRL + 73 /*I*/, 'Italic' ],
		[ CTRL + 85 /*U*/, 'Underline' ],
		[ CTRL + SHIFT + 83 /*S*/, 'Save' ],
		[ CTRL + ALT + 13 /*ENTER*/, 'FitWindow' ],
		[ CTRL + 9 /*TAB*/, 'Source' ]
	] ;
	
	FCKConfig.ContextMenu = ['Generic','Link','Anchor','Image','Flash','Select','Textarea','Checkbox','Radio','TextField','HiddenField','ImageButton','Button','BulletedList','NumberedList','Table','Form'] ;
	FCKConfig.BrowserContextMenuOnCtrl = false ;
	
	FCKConfig.EnableMoreFontColors = true ;
	FCKConfig.FontColors = '000000,993300,333300,003300,003366,000080,333399,333333,800000,FF6600,808000,808080,008080,0000FF,666699,808080,FF0000,FF9900,99CC00,339966,33CCCC,3366FF,800080,999999,FF00FF,FFCC00,FFFF00,00FF00,00FFFF,00CCFF,993366,C0C0C0,FF99CC,FFCC99,FFFF99,CCFFCC,CCFFFF,99CCFF,CC99FF,FFFFFF' ;
	
	//FCKConfig.FontFormats	= 'p;h1;h2;h3;h4;h5;h6;pre;address;div' ;
	FCKConfig.FontFormats	= 'h2;h4;h3;h5;p' ;
	FCKConfig.FontNames		= 'Arial;Comic Sans MS;Courier New;Tahoma;Times New Roman;Verdana' ;
	FCKConfig.FontSizes		= 'smaller;larger;xx-small;x-small;small;medium;large;x-large;xx-large' ;
	
	FCKConfig.StylesXmlPath		= '$request.contextPath/WYSIWYGProperties!viewStylesXML.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$request.getParameter("languageId")' ;
	FCKConfig.TemplatesXmlPath	= FCKConfig.EditorPath + 'fcktemplates.xml' ;
	
	FCKConfig.SpellChecker			= 'ieSpell' ;	// 'ieSpell' | 'SpellerPages'
	FCKConfig.IeSpellDownloadUrl	= 'http://www.iespell.com/download.php' ;
	FCKConfig.SpellerPagesServerScript = 'server-scripts/spellchecker.php' ;	// Available extension: .php .cfm .pl
	FCKConfig.FirefoxSpellChecker	= false ;
	
	FCKConfig.MaxUndoLevels = 15 ;
	
	FCKConfig.DisableObjectResizing = false ;
	FCKConfig.DisableFFTableHandles = true ;
	
	FCKConfig.LinkDlgHideTarget		= false ;
	FCKConfig.LinkDlgHideAdvanced	= false ;
	
	FCKConfig.ImageDlgHideLink		= false ;
	FCKConfig.ImageDlgHideAdvanced	= false ;
	
	FCKConfig.FlashDlgHideAdvanced	= false ;
	
	FCKConfig.ProtectedTags = '' ;
	
	// This will be applied to the body element of the editor
	FCKConfig.BodyId = '' ;
	FCKConfig.BodyClass = '' ;
	
	FCKConfig.DefaultStyleLabel = '' ;
	FCKConfig.DefaultFontFormatLabel = '' ;
	FCKConfig.DefaultFontLabel = '' ;
	FCKConfig.DefaultFontSizeLabel = '' ;
	
	FCKConfig.DefaultLinkTarget = '' ;
	
	// The option switches between trying to keep the html structure or do the changes so the content looks like it was in Word
	FCKConfig.CleanWordKeepsStructure = false ;
	
	// Only inline elements are valid.
	FCKConfig.RemoveFormatTags = 'b,big,code,del,dfn,em,font,i,ins,kbd,q,samp,small,span,strike,strong,sub,sup,tt,u,var' ;
	
	FCKConfig.CustomStyles = 
	{
		'Red Title'	: { Element : 'h3', Styles : { 'color' : 'Red' } }
	};
	
	// Do not add, rename or remove styles here. Only apply definition changes.
	FCKConfig.CoreStyles = 
	{
		// Basic Inline Styles.
		'Bold'			: { Element : 'b', Overrides : 'strong' },
		'Italic'		: { Element : 'i', Overrides : 'em' },
		'Underline'		: { Element : 'u' },
		'StrikeThrough'	: { Element : 'strike' },
		'Subscript'		: { Element : 'sub' },
		'Superscript'	: { Element : 'sup' },
		
		// Basic Block Styles (Font Format Combo).
		'p'				: { Element : 'p' },
		'div'			: { Element : 'div' },
		'pre'			: { Element : 'pre' },
		'address'		: { Element : 'address' },
		'h1'			: { Element : 'h1' },
		'h2'			: { Element : 'h2' },
		'h3'			: { Element : 'h3' },
		'h4'			: { Element : 'h4' },
		'h5'			: { Element : 'h5' },
		'h6'			: { Element : 'h6' },
		
		// Other formatting features.
		'FontFace' : 
		{ 
			Element		: 'span', 
			Styles		: { 'font-family' : '#("Font")' }, 
			Overrides	: [ { Element : 'font', Attributes : { 'face' : null } } ]
		},
		
		'Size' :
		{ 
			Element		: 'span', 
			Styles		: { 'font-size' : '#("Size","fontSize")' }, 
			Overrides	: [ { Element : 'font', Attributes : { 'size' : null } } ]
		},
		
		'Color' :
		{ 
			Element		: 'span', 
			Styles		: { 'color' : '#("Color","color")' }, 
			Overrides	: [ { Element : 'font', Attributes : { 'color' : null } } ]
		},
		
		'BackColor'		: { Element : 'span', Styles : { 'background-color' : '#("Color","color")' } }
	};
	
	// The distance of an indentation step.
	FCKConfig.IndentLength = 40 ;
	FCKConfig.IndentUnit = 'px' ;
	
	// Alternatively, FCKeditor allows the use of CSS classes for block indentation.
	// This overrides the IndentLength/IndentUnit settings.
	FCKConfig.IndentClasses = [] ;
	
	// [ Left, Center, Right, Justified ]
	FCKConfig.JustifyClasses = [] ;
	
	// The following value defines which File Browser connector and Quick Upload
	// "uploader" to use. It is valid for the default implementaion and it is here
	// just to make this configuration file cleaner.
	// It is not possible to change this value using an external file or even
	// inline when creating the editor instance. In that cases you must set the
	// values of LinkBrowserURL, ImageBrowserURL and so on.
	// Custom implementations should just ignore it.
	var _FileBrowserLanguage	= 'php' ;	// asp | aspx | cfm | lasso | perl | php | py
	var _QuickUploadLanguage	= 'php' ;	// asp | aspx | cfm | lasso | perl | php | py
	
	// Don't care about the following two lines. It just calculates the correct connector
	// extension to use for the default File Browser (Perl uses "cgi").
	var _FileBrowserExtension = _FileBrowserLanguage == 'perl' ? 'cgi' : _FileBrowserLanguage ;
	var _QuickUploadExtension = _QuickUploadLanguage == 'perl' ? 'cgi' : _QuickUploadLanguage ;
	
	FCKConfig.FlashBrowser = true ;
	FCKConfig.FlashBrowserURL = "$request.contextPath/ViewContentVersion!viewAssetsDialogForFCKEditor.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")" ;
	FCKConfig.ImageBrowserWindowWidth  = "880" ;
	FCKConfig.ImageBrowserWindowHeight = "600" ;
	
	FCKConfig.FlashUpload = false ;
	
	FCKConfig.SmileyPath	= FCKConfig.BasePath + 'images/smiley/msn/' ;
	FCKConfig.SmileyImages	= ['regular_smile.gif','sad_smile.gif','wink_smile.gif','teeth_smile.gif','confused_smile.gif','tounge_smile.gif','embaressed_smile.gif','omg_smile.gif','whatchutalkingabout_smile.gif','angry_smile.gif','angel_smile.gif','shades_smile.gif','devil_smile.gif','cry_smile.gif','lightbulb.gif','thumbs_down.gif','thumbs_up.gif','heart.gif','broken_heart.gif','kiss.gif','envelope.gif'] ;
	FCKConfig.SmileyColumns = 8 ;
	FCKConfig.SmileyWindowWidth		= 320 ;
	FCKConfig.SmileyWindowHeight	= 240 ;
	
	//CUSTOM
	FCKConfig.AllowImageSizes = 'false';
	FCKConfig.ImageClasses = 'img_full,img_left_small,img_left_medium,img_left_large,img_left_xlarge,img_right_small,img_right_medium,img_right_large,img_right_xlarge';
	FCKConfig.ImageClassesNames = 'Fullbredd (Bredd 370px),V&auml; - Liten (Bredd 111px),V&auml; - Mellan (Bredd 148px),V&auml; - Stor (Bredd 185px),V&auml; - St&ouml;rst (Bredd 222px),H&ouml; - Liten (Bredd 111px),H&ouml; - Mellan (Bredd 148px),H&ouml; - Stor (Bredd 185px),H&ouml; - St&ouml;rst (Bredd 222px)';
	
	FCKConfig.FontFormats	= 'h2;h4;h3;h5;p' ;
	FCKConfig.FontFormatNames = 'Br&#246dtext;Formaterad;Adress;Rubrik niv&#229 1;Underrubrik;Styckesrubrik;Ingress;Stort citat;Rubrik 6;Div' ;
	
	FCKConfig.LinkBrowser = true ;
	FCKConfig.LinkBrowserURL = "$request.contextPath/ViewLinkDialog!viewLinkDialogForFCKEditorV3.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")" ;
	FCKConfig.LinkBrowserWindowWidth = "770" ;
	FCKConfig.LinkBrowserWindowHeight = "640" ;
	FCKConfig.LinkUpload = true ;
	FCKConfig.LinkUploadURL = '$request.contextPath/CreateDigitalAsset.action?contentVersionId=$!contentVersionId&useFckUploadMessages=true';
	FCKConfig.ImageBrowser = true ;
	FCKConfig.ImageBrowserURL = "$request.contextPath/ViewContentVersion!viewAssetBrowserForFCKEditorV3.action?repositoryId=$!request.getParameter("repositoryId")&contentId=$!request.getParameter("contentId")&languageId=$!request.getParameter("languageId")&assetTypeFilter=.*(jpeg|jpg|gif|png).*";	
	FCKConfig.ImageUploadURL = '$request.contextPath/CreateDigitalAsset.action?contentVersionId=$!contentVersionId&useFckUploadMessages=true';
	FCKConfig.ImageBrowserWindowWidth = "750" ;
	FCKConfig.ImageBrowserWindowHeight = "600" ;
		
	setTimeout("overrideLabels()", 3000);
}

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