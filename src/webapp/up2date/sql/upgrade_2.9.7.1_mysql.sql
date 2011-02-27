ALTER TABLE cmFormEntryValue MODIFY COLUMN value VARCHAR(4096);

CREATE TABLE cmFormEntryAsset (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  formEntryId INTEGER UNSIGNED NOT NULL,
  fileName VARCHAR(255) NOT NULL,
  fileSize INTEGER UNSIGNED NOT NULL,
  assetKey VARCHAR(255) NOT NULL,
  contentType VARCHAR(50) NOT NULL,
  assetBlob BLOB NOT NULL,
  PRIMARY KEY(id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

alter table cmCategory add displayName varchar(4096) default NULL;