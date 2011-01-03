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
-- $Id: hibernate-workflow-store-patch.sql,v 1.4 2005/02/24 22:11:32 frank Exp $
--
-- This script performs the database updates required to switch to the hibernate
-- workflow store.
--
-- NOTE: This script makes modifications to your database that may be hard to undo.
-- This script copies the tables it changes before changing them, but it is possible
-- for you to lose data if you run it many times without restoring.  So...
-- BACK UP YOUR DATABASE BEFORE RUNNING!!!!!!!!!  You have been warned.  As long as
-- you are careful, you can use the restore-workflow-tables.sql to recover from
-- errors, but that does not eliminate the need for a database backup prior to running
-- this script.
--
-- Since we can't anticipate every possible data conversion scenario, we leave
-- it to you to determine how to convert the data by leaving you with copies of
-- the original tables.
-- --------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------
-- Copies original OS Workflow tables, preserving the data for safety and for later
-- use in case you need to do some conversions to get it to work with the new
-- tables.
-- --------------------------------------------------------------------------------
create table COPY_OF_OS_WFENTRY select * from OS_WFENTRY;
create table COPY_OF_OS_CURRENTSTEP select * from OS_CURRENTSTEP;
create table COPY_OF_OS_CURRENTSTEP_PREV select * from OS_CURRENTSTEP_PREV;
create table COPY_OF_OS_HISTORYSTEP select * from OS_HISTORYSTEP;
create table COPY_OF_OS_HISTORYSTEP_PREV select * from OS_HISTORYSTEP_PREV;
create table COPY_OF_OS_PROPERTYENTRY select * from OS_PROPERTYENTRY;
create table COPY_OF_OS_STEPIDS select * from OS_STEPIDS;

-- -------------------------------------------------------------------------------
-- Drop OS_STEPIDS; it is not necesary for the hibernate workflow store.
-- -------------------------------------------------------------------------------
drop table OS_STEPIDS;

-- -------------------------------------------------------------------------------
-- Drop foreign key constraints
-- -------------------------------------------------------------------------------
alter table OS_CURRENTSTEP drop foreign key 0_3912;
alter table OS_CURRENTSTEP_PREV drop foreign key 0_3914;
alter table OS_CURRENTSTEP_PREV drop foreign key 0_3915;
alter table OS_HISTORYSTEP drop foreign key 0_3917;
alter table OS_HISTORYSTEP_PREV drop foreign key 0_3919;
alter table OS_HISTORYSTEP_PREV drop foreign key 0_3920;

-- --------------------------------------------------------------------------------
-- Deletes all data from the workflow tables so we can create the auto_increment
-- columns, particularly for OS_WFENTRY, which could have a row with id = 0.  This
-- messes up the alter table statement by causing a duplicate key violation on
-- id = 1.  You'll have to figure out how to move the data from the copies back
-- into the changed tables if you care about preserving the data.
-- --------------------------------------------------------------------------------
delete from OS_CURRENTSTEP;
delete from OS_CURRENTSTEP_PREV;
delete from OS_HISTORYSTEP;
delete from OS_HISTORYSTEP_PREV;
delete from OS_WFENTRY;

-- --------------------------------------------------------------------------------
-- Clear out the property set table, preserving the WYSIWYG config data.
-- --------------------------------------------------------------------------------
delete from OS_PROPERTYENTRY where item_key not like 'repository_%_WYSIWYGConfig';

-- -------------------------------------------------------------------------------
-- Drop primary keys
-- -------------------------------------------------------------------------------
alter table OS_WFENTRY drop primary key;
alter table OS_CURRENTSTEP drop primary key;
alter table OS_HISTORYSTEP drop primary key;
alter table OS_PROPERTYENTRY drop primary key;

-- --------------------------------------------------------------------------------
-- Change primary key columns in OS Workflow tables to auto_increment, so
-- Hibernate can use the "native" ID generator.
-- --------------------------------------------------------------------------------
alter table OS_WFENTRY change ID ID bigint not null auto_increment primary key;
alter table OS_CURRENTSTEP change ID ID bigint not null auto_increment primary key;
alter table OS_HISTORYSTEP change ID ID bigint not null auto_increment primary key;

-- -------------------------------------------------------------------------------
-- Add foreign key constraints to workflow step tables
-- -------------------------------------------------------------------------------
alter table OS_CURRENTSTEP add constraint currentstep_entry_id foreign key (entry_id) references OS_WFENTRY (id);
alter table OS_HISTORYSTEP add constraint historystep_entry_id foreign key (entry_id) references OS_WFENTRY (id);
alter table OS_CURRENTSTEP_PREV add constraint currentstep_id foreign key (id) references OS_CURRENTSTEP (id);
alter table OS_CURRENTSTEP_PREV add constraint currentstep_previous_id foreign key (previous_id) references OS_HISTORYSTEP(id);
alter table OS_HISTORYSTEP_PREV add constraint historystep_id foreign key (id) references OS_HISTORYSTEP (id);
alter table OS_CURRENTSTEP_PREV add constraint historystep_previous_id foreign key (previous_id) references OS_HISTORYSTEP(id);

-- --------------------------------------------------------------------------------
-- Add stepIndex columns to OS_CURRENTSTEP and OS_HISTORYSTEP so the lists
-- in HibernateWorkflowEntry have a place to store their indices
-- --------------------------------------------------------------------------------
alter table OS_CURRENTSTEP add stepIndex int not null;
alter table OS_HISTORYSTEP add stepIndex int not null;

-- --------------------------------------------------------------------------------
-- Rename columns in OS_PROPERTYENTRY and redefined the primary key, altering the
-- table to match what the hibernate property set expects, as defined in the
-- mapping file.
-- --------------------------------------------------------------------------------
alter table OS_PROPERTYENTRY change GLOBAL_KEY entity_name varchar(125) not null;
alter table OS_PROPERTYENTRY add entity_id bigint not null after entity_name;
alter table OS_PROPERTYENTRY change ITEM_KEY entity_key varchar(255) not null;
alter table OS_PROPERTYENTRY change ITEM_TYPE key_type int;
alter table OS_PROPERTYENTRY add boolean_val tinyint after key_type;
alter table OS_PROPERTYENTRY change FLOAT_VALUE double_val double;
alter table OS_PROPERTYENTRY change STRING_VALUE string_val varchar(255);
alter table OS_PROPERTYENTRY add long_val bigint after string_val;
alter table OS_PROPERTYENTRY change NUMBER_VALUE int_val int;
alter table OS_PROPERTYENTRY change DATE_VALUE date_val datetime;
alter table OS_PROPERTYENTRY change DATA_VALUE data_val blob;
alter table OS_PROPERTYENTRY add primary key (entity_name(64), entity_id, entity_key(128));
