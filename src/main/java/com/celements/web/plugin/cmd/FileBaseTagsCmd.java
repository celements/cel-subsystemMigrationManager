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
package com.celements.web.plugin.cmd;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.navigation.TreeNode;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class FileBaseTagsCmd {

  private static Log mLogger = LogFactory.getFactory().getInstance(FileBaseTagsCmd.class);

  public static final String FILEBASE_TAG_CLASS = "Classes.FilebaseTag";
  private IWebUtils celUtils = WebUtils.getInstance();

  public List<TreeNode> getAllFileBaseTags(XWikiContext context) {
    return celUtils.getSubNodesForParent("", getTagSpaceName(context),
        new InternalRightsFilter(), context);
  }

  public String getTagSpaceName(XWikiContext context) {
    String centralFileBaseName = getCentralFileBaseFullName(context) + ".";
    return centralFileBaseName.substring(0, centralFileBaseName.indexOf('.'));
  }

  public String getCentralFileBaseFullName(XWikiContext context) {
    return context.getWiki().getWebPreference("cel_centralfilebase", "", context);
  }

  public boolean existsTagWithName(String tagName, XWikiContext context) {
    if (context.getWiki().exists(getTagFullName(tagName, context), context)) {
      String tagFullName = getTagFullName(tagName, context);
      for (TreeNode node : getAllFileBaseTags(context)) {
        if (tagFullName.equals(node.getFullName())) {
          return true;
        }
      }
    }
    return false;
  }

  public String getTagFullName(String tagName, XWikiContext context) {
    return getTagSpaceName(context) + "." + tagName;
  }

  public XWikiDocument getTagDocument(String tagName, boolean createIfNotExists,
      XWikiContext context) {
    XWikiDocument tagDoc = null;
    try {
      tagDoc = context.getWiki().getDocument(getTagFullName(tagName, context), context);
      if (!existsTagWithName(tagName, context)) {
        if (createIfNotExists) {
          BaseObject menuItemObj = tagDoc.newObject("Celements2.MenuItem", context);
          menuItemObj.setIntValue("menu_position", getAllFileBaseTags(context).size());
          menuItemObj.setStringValue("menu_parent", "");
          menuItemObj.setStringValue("part_name", "");
          context.getWiki().saveDocument(tagDoc, "Added by Navigation", context);
          celUtils.flushMenuItemCache(context);
        }
      }
    } catch (XWikiException exp) {
      mLogger.error("Failed to get tag document [" + getTagFullName(tagName, context)
          +"].", exp);
    }
    return tagDoc;
  }

  void inject_celUtils(IWebUtils mockUtils) {
    this.celUtils = mockUtils;
  }

}
