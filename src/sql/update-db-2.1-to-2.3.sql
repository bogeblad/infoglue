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
-- $Id: update-db-2.1-to-2.3.sql,v 1.1 2006/04/12 15:35:20 mattias Exp $
--
-- This script contains the database updates required to go from 2.1 to 2.5.

-- Adds needed table for user access
DROP TABLE IF EXISTS cmAccessRightUser;

CREATE TABLE cmAccessRightUser (
  accessRightUserId int(11) NOT NULL auto_increment,
  accessRightId int(11) NOT NULL default '0',
  userName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightUserId)
) TYPE=InnoDB;


-- Adds needed table for server nodes
DROP TABLE IF EXISTS cmServerNode;

CREATE TABLE cmServerNode (
  serverNodeId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  dnsName text NOT NULL,
  PRIMARY KEY  (serverNodeId)
) TYPE=InnoDB;


-- Adds new disable language column
alter table cmSiteNodeVersion add disableLanguages tinyint(4) NOT NULL default '2';
