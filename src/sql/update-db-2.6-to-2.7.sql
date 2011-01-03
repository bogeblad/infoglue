-- ===============================================================================
--
-- Part of the InfoGlue Content Management Platform (www.infoglue.org)
--
-- ===============================================================================
--
--  Copyright (C)
--
-- This program is free software; you can redistribute it and/or modify it under
-- the terms of the GNU General Public License version 2, as published by the
-- Free Software Foundation. See the file LICENSE.html for more information.
--
-- This program is distributed in the hope that it will be useful, but WITHOUT
-- ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License along with
-- this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
-- Place, Suite 330 / Boston, MA 02111-1307 / USA.
--
-- ===============================================================================
--
-- $Id: update-db-2.6-to-2.7.sql,v 1.1 2006/10/24 15:35:20 mattias Exp $
--
-- This script contains the database updates required to go from 2.6 to 2.7.

alter table cmSiteNodeVersion add pageCacheTimeout varchar(20) default NULL;
alter table cmSiteNodeVersion add disableForceIDCheck tinyint(4) NOT NULL default '2';
 
ALTER TABLE cmQualifyer ADD INDEX qualifyerNameIndex(name(50));
ALTER TABLE cmQualifyer ADD INDEX qualifyerValueIndex(value(50));

alter table cmDigitalAsset change assetBlob assetBlob longblob;
