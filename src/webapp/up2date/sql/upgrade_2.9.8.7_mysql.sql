ALTER TABLE cmSiteNodeVersion ADD forceProtocolChange int NOT NULL DEFAULT 0;
ALTER TABLE cmSubscription MODIFY COLUMN entityName varchar(100) DEFAULT NULL;
ALTER TABLE cmSubscription MODIFY COLUMN entityId varchar(200) DEFAULT NULL;
ALTER TABLE cmDigitalAsset MODIFY COLUMN assetContentType varchar(255);