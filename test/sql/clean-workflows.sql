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
-- $Id: clean-workflows.sql,v 1.2 2005/02/28 18:28:19 jed Exp $
--
-- WARNING: This script will blast all data from the OSWorkflow tables.  DO NOT
-- RUN IT unless you are absolutely sure you want to get rid of all the workflow
-- data!  You have been warned.
-------------------------------------------------------------------------------
set foreign_key_checks=0;

delete from OS_CURRENTSTEP;
delete from OS_CURRENTSTEP_PREV;
delete from OS_HISTORYSTEP;
delete from OS_HISTORYSTEP_PREV;
delete from OS_PROPERTYENTRY;
delete from OS_WFENTRY;

set foreign_key_checks=1;
