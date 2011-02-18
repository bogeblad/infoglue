set identity_insert cmContentTypeDefinition on; INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('1','<?xml version="1.0" encoding="ISO-8859-1"?><xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified" version="2.0" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xs="http://www.w3.org/2001/XMLSchema"><xs:simpleType name="textarea"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:simpleType name="radiobutton"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:simpleType name="checkbox"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:simpleType name="select"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:simpleType name="textfield"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:complexType name="Image"><xs:all><xs:element name="Attributes"><xs:complexType><xs:all><xs:element name="Title" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined11" label="Title - required"/></values></param><param id="description" inputTypeId="0"><values><value id="undefined87" label="This is the image title"/></values></param><param id="class" inputTypeId="0"><values><value id="undefined80" label="longtextfield"/></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="NavigationTitle" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined27" label="Navigation title - required"/></values></param><param id="description" inputTypeId="0"><values><value id="undefined73" label="This is the label shown on links to this image"/></values></param><param id="class" inputTypeId="0"><values><value id="undefined28" label="longtextfield"/></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="Alt" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined75" label="Alt text"/></values></param><param id="description" inputTypeId="0"><values><value id="undefined20" label="This is the tooltip text for an image"/></values></param><param id="class" inputTypeId="0"><values><value id="undefined86" label="longtextfield"/></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="FullText" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined93" label="Full text"/></values></param><param id="description" inputTypeId="0"><values><value id="undefined77" label="Here you can put in a image description"/></values></param><param id="class" inputTypeId="0"><values><value id="undefined52" label="normaltextarea"/></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"/></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"/></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="true"/></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"/></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all></xs:complexType></xs:element></xs:all></xs:complexType></xs:schema>','Image');
#endquery
set identity_insert cmContentTypeDefinition on; INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('2','<?xml version="1.0" encoding="ISO-8859-1"?><xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified" version="2.0" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xs="http://www.w3.org/2001/XMLSchema"><xs:simpleType name="textarea"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="radiobutton"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="checkbox"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="select"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="textfield"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:complexType name="Meta info"><xs:all><xs:element name="Attributes"><xs:complexType><xs:all><xs:element name="Title" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined75" label="Title - required"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined52" label="Used in the page title"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined94" label="longtextfield"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="NavigationTitle" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined35" label="Navigation title (required)"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined0" label="Used in navigation elements pointing to the page"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined20" label="longtextfield"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="Description" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined31" label="Description"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined86" label="A short description of the page"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined15" label="longtextfield"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="MetaInfo" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined85" label="Meta Information"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined67" label="Keywords made for search engines etc."></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined70" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="DisablePageCache" type="checkbox"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined90" label="Disable PageCache"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined9" label="Check this if your page should not be cached on page-level."></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined98" label="normaltextfield"></value></values></param><param id="values" inputTypeId="1"><values><value id="true" label="Yes"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="ComponentStructure" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined67" label="ComponentStructure"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined38" label="ComponentStructure"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined73" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="false"></value></values></param><param id="enableRelationEditor" inputTypeId="0"><values><value id="enableRelationEditor" label="false"></value></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all></xs:complexType></xs:element></xs:all></xs:complexType></xs:schema>','Meta info');
#endquery
set identity_insert cmContentTypeDefinition on; INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('3','<?xml version="1.0" encoding="ISO-8859-1"?><xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified" version="2.0" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xs="http://www.w3.org/2001/XMLSchema"><xs:simpleType name="textarea"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="radiobutton"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="checkbox"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="select"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="textfield"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:complexType name="Article"><xs:all><xs:element name="Attributes"><xs:complexType><xs:all><xs:element name="Title" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined34" label="Title"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined53" label="This represents the article title"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined94" label="longtextfield"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="NavigationTitle" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined58" label="Navigation title"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined37" label="This represents the article linktitle"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined95" label="longtextfield"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="Leadin" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined28" label="Lead in text"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined79" label="This is an introduction to the full text"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined70" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="true"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="FullText" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined45" label="Full text"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined8" label="This is the article fulltext"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined54" label="hugetextfield"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="500"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="true"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="RelatedArticles" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined75" label="Related Articles"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined5" label="Here you can add related articles"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined57" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="WYSIWYGToolbar" inputTypeId="0"><values><value id="WYSIWYGToolbar" label="Default"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="false"></value></values></param><param id="enableRelationEditor" inputTypeId="0"><values><value id="enableRelationEditor" label="true"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="RelatedAreas" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined93" label="Related areas"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined30" label="Points out related areas on the site"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined83" label="normaltextfield"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="WYSIWYGToolbar" inputTypeId="0"><values><value id="WYSIWYGToolbar" label="Default"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="false"></value></values></param><param id="enableContentRelationEditor" inputTypeId="0"><values><value id="enableContentRelationEditor" label="false"></value></values></param><param id="enableStructureRelationEditor" inputTypeId="0"><values><value id="enableStructureRelationEditor" label="true"></value></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all></xs:complexType></xs:element></xs:all></xs:complexType></xs:schema>','Article');
#endquery
set identity_insert cmContentTypeDefinition on; INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('4','<?xml version="1.0" encoding="ISO-8859-1"?><xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified" version="2.0" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xs="http://www.w3.org/2001/XMLSchema"><xs:simpleType name="textarea"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:simpleType name="radiobutton"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:simpleType name="checkbox"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:simpleType name="select"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:simpleType name="textfield"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:complexType name="HTMLTemplate"><xs:all><xs:element name="Attributes"><xs:complexType><xs:all><xs:element name="Name" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined7" label="Name"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined82" label="This is the name of the template"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined61" label="longtextfield"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="Template" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined16" label="Template HTML"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined90" label="This is the html for the template "></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined12" label="hugetextfield"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="500"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="true"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="ComponentProperties" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined89" label="ComponentProperties"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined40" label="ComponentProperties"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined93" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="false"></value></values></param><param id="enableRelationEditor" inputTypeId="0"><values><value id="enableRelationEditor" label="false"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="GroupName" type="select"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined89" label="Group Name"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined94" label="The name of the group the component should be in"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined63" label="normaltextfield"></value></values></param><param id="values" inputTypeId="1"><values><value id="Basic Pages" label="Basic Pages"></value><value id="Single Content" label="Single Content"></value><value id="Content Iterators" label="Content Iterators"></value><value id="Navigation" label="Navigation"></value><value id="Layout" label="Layout"></value><value id="Templates" label="Templates"></value><value id="Other" label="Other"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="PreTemplate" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined23" label="Pre processing template"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined67" label="This template gets invoked before the render phase"></value></values></param><param id="initialData" inputTypeId="0"><values><value id="undefined67" label="undefined83"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined97" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="WYSIWYGToolbar" inputTypeId="0"><values><value id="WYSIWYGToolbar" label="Default"></value></values></param><param id="WYSIWYGExtraConfig" inputTypeId="0"><values><value id="WYSIWYGExtraConfig" label=""></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="false"></value></values></param><param id="enableContentRelationEditor" inputTypeId="0"><values><value id="enableContentRelationEditor" label="false"></value></values></param><param id="enableStructureRelationEditor" inputTypeId="0"><values><value id="enableStructureRelationEditor" label="false"></value></values></param><param id="enableComponentPropertiesEditor" inputTypeId="0"><values><value id="enableComponentPropertiesEditor" label="false"></value></values></param><param id="activateExtendedEditorOnLoad" inputTypeId="0"><values><value id="activateExtendedEditorOnLoad" label="false"></value></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all></xs:complexType></xs:element></xs:all></xs:complexType></xs:schema>','HTMLTemplate');
#endquery
set identity_insert cmContentTypeDefinition on; INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('5','<?xml version="1.0" encoding="ISO-8859-1"?><xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified" version="2.0" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xs="http://www.w3.org/2001/XMLSchema"><xs:simpleType name="textarea"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="radiobutton"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="checkbox"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="select"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="textfield"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:complexType name="Article"><xs:all><xs:element name="Attributes"><xs:complexType><xs:all><xs:element name="HTMLFormular" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined29" label="HTMLFormular"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined28" label="This area contains the formular"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined15" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="true"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="true"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="FormName" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined25" label="FormName"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined62" label="This name is used to reach a form by name in Javascript for example"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined77" label="longtextfield"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="FormMethod" type="select"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined27" label="Method"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined54" label="This is the method used for sending data"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined24" label="longtextfield"></value></values></param><param id="values" inputTypeId="1"><values><value id="post" label="POST"></value><value id="get" label="GET"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="FormAction" type="select"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined44" label="Action"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined29" label="This is the action we send the form values to"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined36" label="longtextfield"></value></values></param><param id="values" inputTypeId="1"><values><value id="InfoGlueDefaultInputHandler.action" label="Default Handler"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="InputHandlerClassName" type="select"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined70" label="Input handler"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined68" label="This decides what procedure to invoke with the data"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined77" label="longtextfield"></value></values></param><param id="values" inputTypeId="1"><values><value id="org.infoglue.deliver.applications.inputhandlers.MailSender" label="Simple Mail Handler"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="MailSender_fromAddress" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined63" label="MailSender_fromAddress"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined18" label="The address to give as sender in case it is sent by mail "></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined71" label="longtextfield"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="MailSender_toAddress" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined58" label="MailSender_toAddress"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined33" label="The address to send the form data to"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined10" label="longtextfield"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="MailSender_subject" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined26" label="MailSender_subject"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined85" label="The subject to give if the data is sent as mail"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined42" label="longtextfield"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="MailSender_template" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined68" label="MailSender_template"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined50" label="This is the template that formats the mail"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined55" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="true"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="false"></value></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all>    </xs:complexType>  </xs:element>  </xs:all></xs:complexType><xs:simpleType name="assetKeys"><xs:restriction base="xs:string"></xs:restriction></xs:simpleType></xs:schema>','HTMLFormular');
#endquery
set identity_insert cmContentTypeDefinition on; INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('6','<xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified" version="2.0" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xs="http://www.w3.org/2001/XMLSchema"><xs:simpleType name="textarea"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="radiobutton"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="checkbox"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="select"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name="textfield"><xs:restriction base="xs:string"><xs:maxLength value="100"></xs:maxLength></xs:restriction></xs:simpleType><xs:complexType name="Content"><xs:all><xs:element name="Attributes"><xs:complexType><xs:all><xs:element name="UserInputHTML" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined64" label="UserInputHTML"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined98" label="UserInputHTML"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined26" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="false"></value></values></param><param id="enableRelationEditor" inputTypeId="0"><values><value id="enableRelationEditor" label="false"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="ScriptCode" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined22" label="ScriptCode"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined90" label="The code"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined99" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="600"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="false"></value></values></param><param id="enableRelationEditor" inputTypeId="0"><values><value id="enableRelationEditor" label="false"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="UserOutputHTML" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined63" label="UserOutputHTML"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined22" label="UserOutputHTML"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined28" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="false"></value></values></param><param id="enableRelationEditor" inputTypeId="0"><values><value id="enableRelationEditor" label="false"></value></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all></xs:complexType></xs:element> </xs:all></xs:complexType></xs:schema>','TaskDefinition');
#endquery
set identity_insert cmContentTypeDefinition on; INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('7','<?xml version="1.0" encoding="utf-8"?><xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified" version="2.0" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xs="http://www.w3.org/2001/XMLSchema"><xs:simpleType name="textarea"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:simpleType name="radiobutton"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:simpleType name="checkbox"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:simpleType name="select"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:simpleType name="textfield"><xs:restriction base="xs:string"><xs:maxLength value="100"/></xs:restriction></xs:simpleType><xs:complexType name="PageTemplate"><xs:all><xs:element name="Attributes"><xs:complexType><xs:all><xs:element name="Name" type="textfield"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined7" label="Name"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined82" label="This is the name of the page template"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined61" label="longtextfield"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="ComponentStructure" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined16" label="ComponentStructure"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined90" label="This is the page template structure "></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined12" label="hugetextfield"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="500"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all></xs:complexType></xs:element></xs:all></xs:complexType></xs:schema>','PageTemplate');
#endquery

set identity_insert cmContentTypeDefinition off;
#endquery

set identity_insert cmAvailableServiceBinding on; INSERT INTO cmAvailableServiceBinding (availableServiceBindingId, name, description, visualizationAction, isMandatory, isUserEditable, isInheritable) VALUES
  ('1','Template','The page template-file','ViewListTemplate.action','1','1','1');
#endquery
set identity_insert cmAvailableServiceBinding on; INSERT INTO cmAvailableServiceBinding (availableServiceBindingId, name, description, visualizationAction, isMandatory, isUserEditable, isInheritable) VALUES
  ('2','Meta information','The keywords and other metainfo for this page','ViewContentTreeForServiceBinding.action','1','1','1');
#endquery

set identity_insert cmAvailableServiceBinding off;
#endquery


set identity_insert cmAvailableServiceBindingSiteNodeTypeDefinition on;
#endquery

set identity_insert cmAvailableServiceBindingSiteNodeTypeDefinition on; INSERT INTO cmAvailableServiceBindingSiteNodeTypeDefinition (availableServiceBindingSiteNodeTypeDefinitionId, availableServiceBindingId, siteNodeTypeDefinitionId) VALUES
  ('1','1','2');
#endquery
set identity_insert cmAvailableServiceBindingSiteNodeTypeDefinition on; INSERT INTO cmAvailableServiceBindingSiteNodeTypeDefinition (availableServiceBindingSiteNodeTypeDefinitionId, availableServiceBindingId, siteNodeTypeDefinitionId) VALUES
  ('2','2','2');
#endquery
set identity_insert cmAvailableServiceBindingSiteNodeTypeDefinition on; INSERT INTO cmAvailableServiceBindingSiteNodeTypeDefinition (availableServiceBindingSiteNodeTypeDefinitionId, availableServiceBindingId, siteNodeTypeDefinitionId) VALUES
  ('3','2','1');
#endquery

set identity_insert cmAvailableServiceBindingSiteNodeTypeDefinition off;
#endquery


set identity_insert cmLanguage on;
#endquery

set identity_insert cmLanguage on; INSERT INTO cmLanguage (languageId, name, languageCode, charset) VALUES
  ('1','English','en', 'utf-8');
#endquery
set identity_insert cmLanguage on; INSERT INTO cmLanguage (languageId, name, languageCode, charset) VALUES
  ('2','German','de', 'utf-8');
#endquery
set identity_insert cmLanguage on; INSERT INTO cmLanguage (languageId, name, languageCode, charset) VALUES
  ('3','Swedish','sv', 'utf-8');
#endquery

set identity_insert cmLanguage off;
#endquery


set identity_insert cmRepository on;
#endquery

set identity_insert cmRepository on; INSERT INTO cmRepository (repositoryId, name, description, dnsName) VALUES
  ('1','testsite.org','root repository','');
#endquery

set identity_insert cmRepository off;
#endquery


set identity_insert cmRepositoryLanguage on;
#endquery

set identity_insert cmRepositoryLanguage on; INSERT INTO cmRepositoryLanguage (repositoryLanguageId, repositoryId, languageId, isPublished, sortOrder) VALUES
  ('1','1','1','0','0');
#endquery
set identity_insert cmRepositoryLanguage on; INSERT INTO cmRepositoryLanguage (repositoryLanguageId, repositoryId, languageId, isPublished, sortOrder) VALUES
  ('2','1','2','0','0');
#endquery
set identity_insert cmRepositoryLanguage on; INSERT INTO cmRepositoryLanguage (repositoryLanguageId, repositoryId, languageId, isPublished, sortOrder) VALUES
  ('3','1','3','0','0');
#endquery

set identity_insert cmRepositoryLanguage off;
#endquery


set identity_insert cmRepositoryLanguage off; INSERT INTO cmRole (roleName, description) VALUES
  ('administrators','This is the most priviliged group');
#endquery
set identity_insert cmRepositoryLanguage off; INSERT INTO cmRole (roleName, description) VALUES
  ('cmsUser','Must be present to allow any ordinary user to get access.');
#endquery
set identity_insert cmRepositoryLanguage off; INSERT INTO cmRole (roleName, description) VALUES
  ('anonymous','Must be present to model the default anonymous extranet role.');
#endquery


set identity_insert cmServiceDefinition on;
#endquery

set identity_insert cmServiceDefinition on; INSERT INTO cmServiceDefinition (serviceDefinitionId, className, name, description) VALUES
  ('1','org.infoglue.cms.services.CoreContentService','Core content service','Core content service');
#endquery
set identity_insert cmServiceDefinition on; INSERT INTO cmServiceDefinition (serviceDefinitionId, className, name, description) VALUES
  ('2','org.infoglue.cms.services.CoreStructureService','Core structure service','The local structure-service');
#endquery

set identity_insert cmServiceDefinition off;
#endquery


set identity_insert cmServiceDefinitionAvailableServiceBinding on;
#endquery

set identity_insert cmServiceDefinitionAvailableServiceBinding on; INSERT INTO cmServiceDefinitionAvailableServiceBinding (serviceDefinitionAvailableServiceBindingId, serviceDefinitionId, availableServiceBindingId) VALUES
  ('1','1','1');
#endquery
set identity_insert cmServiceDefinitionAvailableServiceBinding on; INSERT INTO cmServiceDefinitionAvailableServiceBinding (serviceDefinitionAvailableServiceBindingId, serviceDefinitionId, availableServiceBindingId) VALUES
  ('2','1','2');
#endquery

set identity_insert cmServiceDefinitionAvailableServiceBinding off;
#endquery


set identity_insert cmSiteNodeTypeDefinition on;
#endquery

set identity_insert cmSiteNodeTypeDefinition on; INSERT INTO cmSiteNodeTypeDefinition (siteNodeTypeDefinitionId, invokerClassName, name, description) VALUES
  ('1','org.infoglue.deliver.invokers.ComponentBasedHTMLPageInvoker','ComponentPage','The new component type page');
#endquery
set identity_insert cmSiteNodeTypeDefinition on; INSERT INTO cmSiteNodeTypeDefinition (siteNodeTypeDefinitionId, invokerClassName, name, description) VALUES
  ('2','org.infoglue.deliver.invokers.HTMLPageInvoker','Normal HTML Page','Normal page');
#endquery

set identity_insert cmSiteNodeTypeDefinition off;
#endquery


INSERT INTO cmSystemUser (userName, password, firstName, lastName, email) VALUES
  ('administrator','changeit','System','Administrator','administrator@your.domain');
#endquery
INSERT INTO cmSystemUser (userName, password, firstName, lastName, email) VALUES
  ('anonymous','anonymous','Anonymous','User','anonymous@infoglue.org');
#endquery


INSERT INTO cmSystemUserRole (userName, roleName) VALUES
  ('administrator','administrators');
#endquery
INSERT INTO cmSystemUserRole (userName, roleName) VALUES
  ('administrator','cmsUser');
#endquery
INSERT INTO cmSystemUserRole (userName, roleName) VALUES
  ('anonymous','anonymous');
#endquery


set identity_insert cmInterceptionPoint on;
#endquery

set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (1,'Repository','Repository.Read','Gives a user access to look at a repository',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (2,'ManagementTool','ManagementTool.Read','Gives a user access to the management tool',0);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (3,'ContentTool','ContentTool.Read','Gives a user access to the content tool',0);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (4,'StructureTool','StructureTool.Read','Gives a user access to the structure tool',0);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (5,'PublishingTool','PublishingTool.Read','Gives a user access to the publishing tool',0);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (6,'Content','Content.Read','Intercepts the read of a content',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (7,'Content','Content.Write','Intercepts the write of a content',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (8,'SiteNodeVersion','SiteNodeVersion.Read','Intercepts the read of a SiteNodeVersion',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (9,'SiteNodeVersion','SiteNodeVersion.Write','Intercepts the write of a SiteNodeVersion',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (10,'Content','Content.Create','Intercepts the creation of a new content or folder',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (11,'Content','Content.Delete','Intercepts the deletion of a content',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (12,'Content','Content.Move','Intercepts the movement of a content',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (13,'Content','Content.SubmitToPublish','Intercepts the submittance to publish of all content versions',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (14,'Content','Content.ChangeAccessRights','Intercepts the attempt to change access rights',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (15,'Content','Content.CreateVersion','Intercepts the creation of a new contentversion',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (16,'ContentVersion','ContentVersion.Delete','Intercepts the deletion of a contentversion',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (17,'ContentVersion','ContentVersion.Write','Intercepts the editing of a contentversion',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (18,'ContentVersion','ContentVersion.Read','Intercepts the read of a contentversion',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (19,'SiteNodeVersion','SiteNodeVersion.CreateSiteNode','Intercepts the creation of a new sitenode',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (20,'SiteNodeVersion','SiteNodeVersion.DeleteSiteNode','Intercepts the deletion of a sitenode',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (21,'SiteNodeVersion','SiteNodeVersion.MoveSiteNode','Intercepts the movement of a sitenode',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (22,'SiteNodeVersion','SiteNodeVersion.SubmitToPublish','Intercepts the submittance to publish of all content versions',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (23,'SiteNodeVersion','SiteNodeVersion.ChangeAccessRights','Intercepts the attempt to change access rights',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES
  (24,'ContentVersion','ContentVersion.Publish','Intercepts the direct publishing of a content version',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES
  (25,'SiteNodeVersion','SiteNodeVersion.Publish','Intercepts the direct publishing of a siteNode version',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (26,'MyDesktopTool','MyDesktopTool.Read','Gives a user access to the MyDesktop tool',0);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (27,'ContentTypeDefinition','ContentTypeDefinition.Read','This point checks access to read/use a content type definition',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (28,'Category','Category.Read','This point checks access to read/use a category',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (29,'Publication','Publication.Write','This point intercepts a new publication',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (30,'Repository','Repository.ReadForBinding','This point intercepts when a user tries to read the repository in a binding dialog',1);
#endquery
set identity_insert cmInterceptionPoint on; INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (31,'Workflow','Workflow.Create','This point checks access to creating a new workflow',1);
#endquery

set identity_insert cmInterceptionPoint off;
#endquery


set identity_insert cmInterceptor on;
#endquery

set identity_insert cmInterceptor on; INSERT INTO cmInterceptor (interceptorId, name, className, description) VALUES
  (1,'InfoGlue Common Access Rights Interceptor','org.infoglue.cms.security.interceptors.InfoGlueCommonAccessRightsInterceptor','Takes care of bla');
#endquery

set identity_insert cmInterceptor off;
#endquery


set identity_insert cmInterceptionPointInterceptor on;
#endquery

set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (1, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (2, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (3, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (4, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (5, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (6, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (7, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (8, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (9, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (10, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (11, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (12, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (13, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (14, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (15, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (16, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (17, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (18, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (19, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (20, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (21, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (22, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (23, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (24, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (25, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (26, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (27, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (28, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (29, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (30, 1);
#endquery
set identity_insert cmInterceptionPointInterceptor on; INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (31, 1);
#endquery

set identity_insert cmInterceptionPointInterceptor off;
#endquery


set identity_insert cmAccessRight on;
#endquery

set identity_insert cmAccessRight on; INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (1, '1', 1);
#endquery
set identity_insert cmAccessRight on; INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (2, '2', 1);
#endquery
set identity_insert cmAccessRight on; INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (3, '3', 1);
#endquery
set identity_insert cmAccessRight on; INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (4, '4', 1);
#endquery
set identity_insert cmAccessRight on; INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (5, NULL, 3);
#endquery
set identity_insert cmAccessRight on; INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (6, NULL, 2);
#endquery
set identity_insert cmAccessRight on; INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (7, NULL, 4);
#endquery
set identity_insert cmAccessRight on; INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (8, NULL, 5);
#endquery
set identity_insert cmAccessRight on; INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (9, NULL, 26);
#endquery

set identity_insert cmAccessRight off;
#endquery


set identity_insert cmAccessRightRole on;
#endquery

set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (1, 1, 'administrators');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (2, 1, 'cmsUser');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (3, 2, 'administrators');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (4, 2, 'cmsUser');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (5, 3, 'administrators');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (6, 3, 'cmsUser');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (7, 4, 'administrators');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (8, 4, 'cmsUser');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (9, 5, 'administrators');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (10, 5, 'cmsUser');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (11, 6, 'administrators');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (12, 6, 'cmsUser');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (13, 7, 'administrators');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (14, 7, 'cmsUser');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (15, 8, 'administrators');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (16, 8, 'cmsUser');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (17, 9, 'administrators');
#endquery
set identity_insert cmAccessRightRole on; INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (18, 9, 'cmsUser');
#endquery

set identity_insert cmAccessRightRole off;
#endquery

