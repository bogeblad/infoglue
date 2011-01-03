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
-- $Id: restore-workflow-tables.sql,v 1.3 2005/02/24 22:11:32 frank Exp $
--
-- Restores the original workflow tables from copies made by
-- backup-workflow-tables.sql.
-- --------------------------------------------------------------------------------
set foreign_key_checks=0;

drop table OS_WFENTRY;
drop table OS_CURRENTSTEP;
drop table OS_CURRENTSTEP_PREV;
drop table OS_HISTORYSTEP;
drop table OS_HISTORYSTEP_PREV;
drop table OS_PROPERTYENTRY;
drop table OS_STEPIDS;

set foreign_key_checks=1;

create table OS_WFENTRY select * from COPY_OF_OS_WFENTRY;
create table OS_CURRENTSTEP select * from COPY_OF_OS_CURRENTSTEP;
create table OS_CURRENTSTEP_PREV select * from COPY_OF_OS_CURRENTSTEP_PREV;
create table OS_HISTORYSTEP select * from COPY_OF_OS_HISTORYSTEP;
create table OS_HISTORYSTEP_PREV select * from COPY_OF_OS_HISTORYSTEP_PREV;
create table OS_PROPERTYENTRY select * from COPY_OF_OS_PROPERTYENTRY;
create table OS_STEPIDS select * from COPY_OF_OS_STEPIDS;

set foreign_key_checks=0;

drop table COPY_OF_OS_WFENTRY;
drop table COPY_OF_OS_CURRENTSTEP;
drop table COPY_OF_OS_CURRENTSTEP_PREV;
drop table COPY_OF_OS_HISTORYSTEP;
drop table COPY_OF_OS_HISTORYSTEP_PREV;
drop table COPY_OF_OS_PROPERTYENTRY;
drop table COPY_OF_OS_STEPIDS;

set foreign_key_checks=1;
