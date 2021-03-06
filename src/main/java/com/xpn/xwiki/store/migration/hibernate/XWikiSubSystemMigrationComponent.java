/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.store.migration.hibernate;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;

import com.celements.migrations.ISubSystemMigrationManager;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.migration.XWikiDBVersion;
import com.xpn.xwiki.store.migration.XWikiMigratorInterface;

/**
 * TODO MigrationManager in XWiki was improved and "componentized" in 3.4
 * http://lists.xwiki.org/pipermail/devs/2011-October/048619.html
 * http://jira.xwiki.org/browse/XWIKI-7006
 * http://jira.xwiki.org/browse/XWIKI-1859
 * we need to adapt our version to make it compile on unstable branch
 * 
 * Liquibase is used to automate any database schema changes not automatically done by
 * hibernate schema update process. e.g. adding important indexes can be done with
 * liquibase. 
 * http://www.liquibase.org/quickstart
 * 
 * @author fabian
 *
 */
@Component("XWikiSubSystem")
public class XWikiSubSystemMigrationComponent implements ISubSystemMigrationManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      XWikiSubSystemMigrationComponent.class);
  XWikiHibernateMigrationManager injected_MigrationManager;

  public String getSubSystemName() {
    return "XWikiSubSystem";
  }

  public void startMigrations(XWikiContext context) throws XWikiException {
    XWikiHibernateMigrationManager xwikiMigrationManager = null;
    try {
      xwikiMigrationManager = getXWikiHibernateMigrationManager(context);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to instanciate XWikiHibernateMigrationManager!", exp);
    }
    if (xwikiMigrationManager != null) {
      xwikiMigrationManager.startMigrations(context);
    }
  }

  XWikiHibernateMigrationManager getXWikiHibernateMigrationManager(
      XWikiContext context) throws XWikiException {
    if (injected_MigrationManager != null) {
      return injected_MigrationManager;
    }
    return new XWikiHibernateMigrationManager(context);
  }

  public void initDatabaseVersion(XWikiContext context) {
    try {
      XWikiHibernateMigrationManager subSystemMigManager =
          getXWikiHibernateMigrationManager(context);
      List<? extends XWikiMigratorInterface> allMigrations =
          subSystemMigManager.getAllMigrations(context);
      XWikiDBVersion maxVersion = subSystemMigManager.getDBVersion(context);
      //CAUTION: equals is not implemented on XWikiDBVersion!
      if ((maxVersion == null) || (maxVersion.compareTo(new XWikiDBVersion(0)) == 0)) {
        for(XWikiMigratorInterface theMigration : allMigrations) {
          XWikiDBVersion theVersion = theMigration.getVersion();
          if ((maxVersion == null) || (theVersion.compareTo(maxVersion) > 0)) {
            maxVersion = theVersion;
          }
        }
        XWikiDBVersion newVersion = maxVersion.increment();
        LOGGER.info("init database version for subsystem [" + getSubSystemName()
            + "] with  [" + newVersion + "] .");
        subSystemMigManager.setDBVersion(newVersion, context);
      } else {
        LOGGER.info("skip init database version for subsystem [" + getSubSystemName()
            + "] already found version [" + maxVersion + "] .");
      }
    } catch (XWikiException exp) {
      LOGGER.error("failed to init database version for [" + context.getDatabase() + "].",
          exp);
    }
  }

}
