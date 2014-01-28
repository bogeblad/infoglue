CKEDITOR.on('dialogDefinition', function(ev) {
	if (ev.data.name == "link") {
		var dialogDefinition = ev.data.definition;
		//This overides the event that checks if the url is a IG-url or not. Changed the urlOnChangeTestOther-regexp
		dialogDefinition.getContents("info").get("url").onKeyUp = function() {
			this.allowOnChange = false;
			var protocolCmb = this.getDialog().getContentElement( 'info', 'protocol' ),
			url = this.getValue(),
			urlOnChangeProtocol = /^(http|https|ftp|news):\/\/(?=.)/i,
			urlOnChangeTestOther = /^((javascript:)|[#\/\.\?]|.*templateLogic|DownloadAsset)/i;
	
			var protocol = urlOnChangeProtocol.exec( url );
			if ( protocol ) {
				this.setValue( url.substr( protocol[ 0 ].length ) );
				protocolCmb.setValue( protocol[ 0 ].toLowerCase() );
			} else if ( urlOnChangeTestOther.test( url ) )
				protocolCmb.setValue( '' );
	
			this.allowOnChange = true;
		};
	}
});