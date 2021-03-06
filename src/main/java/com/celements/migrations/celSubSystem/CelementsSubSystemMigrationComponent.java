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
package com.celements.migrations.celSubSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;

import com.celements.migrations.ISubSystemMigrationManager;
import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("CelementsSubSystem")
public class CelementsSubSystemMigrationComponent implements ISubSystemMigrationManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      CelementsSubSystemMigrationComponent.class);

  SubSystemHibernateMigrationManager injected_MigrationManager;

  @Override
  public String getSubSystemName() {
    return "CelementsSubSystem";
  }

  @Override
  public void startMigrations(XWikiContext context) throws XWikiException {
    SubSystemHibernateMigrationManager celMigrationManager = null;
    try {
      celMigrationManager = getSubSystemHibernateMigrationManager(context);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to instanciate CelementsHibernateMigrationManager!", exp);
    }
    if (celMigrationManager != null) {
      celMigrationManager.startMigrations(context);
    }
  }

  SubSystemHibernateMigrationManager getSubSystemHibernateMigrationManager(
      XWikiContext context) throws XWikiException {
    if (injected_MigrationManager != null) {
      return injected_MigrationManager;
    }
    return new SubSystemHibernateMigrationManager(context, getSubSystemName(),
        ICelementsMigrator.class);
  }

  @Override
  public void initDatabaseVersion(XWikiContext context) {
    try {
      SubSystemHibernateMigrationManager subSystemMigManager = getSubSystemHibernateMigrationManager(
          context);
      subSystemMigManager.initDatabaseVersion(context);
    } catch (XWikiException exp) {
      LOGGER.error("failed to init database version for [{}].", context.getDatabase(), exp);
    }
  }

}
