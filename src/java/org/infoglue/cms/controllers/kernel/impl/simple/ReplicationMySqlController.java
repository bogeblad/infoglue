/* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */

package org.infoglue.cms.controllers.kernel.impl.simple;

import java.sql.ResultSet;

import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.NotificationMessage;


/**
 * ReplicationController.java
 * Created on 2002-okt-30
 * @author Stefan Sik, ss@frovi.com
 * ss
 *
 * In this description I use the words Master, Slave and versions
 *
 * Versions are rows in cmContentVersion and in cmSiteNodeVersion
 *
 * In this context Slave is synonym with the MySQL database
 * running and feeding the deliver1.0 application on the Live Site
 * (www.frovi.com)
 *
 * Master is the MySQL database used by cmstool1.0
 *
 * Refactoring needed.
 * Should implement generic replication interface
 * that is used by the tool. This controller is specific
 * for mysql and for our current server topology.
 *
 * Also write another replication implementation that
 * uses a simpler replication mechanism, to ensure that
 * the interface is generic and complete. (nice to have)
 *
 *
 * Purpose of this controller is to ensure that the replika
 * (the Live Database that the deliver engine uses), are
 * up to date with the tool database.
 *
 * But wait, there is more. The replika may NOT contain
 * any versions that has state <> 3 and isActive flag <> 1
 *
 * To fulfill these requirements with our current setup,
 * we are going to use the replication mechanism built in to mySQL.
 *
 * In mySQL repl. The responsibility for syncronizing the slave with the master
 * lies on the slave in the form of a thread that runs in mySQL on the slave.
 * The slave thread reads from the masters log-bin file and executes the sql
 * commands required for updating itself. It's a pull type of arrangement.
 *
 * We can start and stop this slave thread with sql commands to the slave server.
 *
 * We are going to solve it like this:
 * 1 - Lock the master tables (no updates during sync)
 * 1.1 - maybe read lock (if possible) the slave tables
 * 2 - Start the slave thread on the slave.
 * 3 - Wait for the update to finish.
 * 4 - Unlock the master tables.
 * 5 - Delete all unwanted rows on the slave.
 *
 * This solution has one little problem. At one moment we do have working material
 * on the slave server. What happens if this controller dies before it deletes the
 * unwanted versions. Maybe we should read lock on the slave during update?
 * What happens if for some unknown reason the slave-tread starts. There is no
 * way (I know of) to filter data that is to be replicated.
 *
 * Alternate solutions:
 * sol 1 - 	Always run the slave-thread.
 * 			Ensure that the deliver engine only delivers versions with
 * 			the required criterias. Then this controller class would be
 * 			redundant.
 *
 * sol 2 - 	On the slave server run a process that check (and deletes) the versions.
 * 			and always run the slave thread. (This class is redundant)
 *
 * sol 3 - 	In the tool, on publish, push the wanted versions to a publishing table
 * 			or possibly a publishing database. Then setup slave to replicate only
 * 			that database or tables. Always run the slave thread.
 *
 *
 *
 *
 * This controller should be called from PublishingController.
 *
 * References:
 * http://www.mysql.com/doc/en/Replication_HOWTO.html
 * http://www.mysql.com/doc/en/Replication_SQL.html
 * http://www.mysql.com/doc/en/Replication_FAQ.html
 *
 *
 */
public class ReplicationMySqlController // implements IGenericCmsReplication
{
	public static void updateSlaveServer() throws Exception
	{
		String bin_log = "";
		Integer position = new Integer(0);

		// Sync the servers
		MysqlJDBCService mySql = new MysqlJDBCService();

		// If mysql is not setup for replication, dont try it 
		if(mySql.isEnabled())
		{
			ResultSet res = mySql.executeMasterSQL("SHOW MASTER STATUS");
			if (res.first())
			{
				bin_log = res.getString("File");
				position = new Integer(res.getInt("Position"));
				// mySql.executeSlaveSQL("SLAVE START");
				mySql.executeSlaveSQL("SELECT MASTER_POS_WAIT('" + bin_log + "', " + position + ")");
				// mySql.executeSlaveSQL("SLAVE STOP");
			}

			mySql.closeMaster();
			mySql.closeSlave();

			// Expire cache on livesites?
			NotificationMessage notificationMessage = new NotificationMessage("ReplicationMySqlController.updateSlaveServer():", NotificationMessage.PUBLISHING_TEXT, "Editor - name unknown", NotificationMessage.PUBLISHING, new Integer(-1), "");
			ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
		}
	}
}
