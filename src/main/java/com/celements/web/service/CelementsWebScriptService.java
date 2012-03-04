package com.celements.web.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.script.service.ScriptService;

import com.celements.navigation.cmd.DeleteMenuItemCommand;
import com.celements.web.plugin.api.CelementsWebPluginApi;
import com.celements.web.plugin.cmd.PlainTextCommand;
import com.celements.web.sajson.Builder;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("celementsweb")
public class CelementsWebScriptService implements ScriptService {

  private static final String APP_SCRIPT_XPAGE = "app";

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CelementsWebPluginApi.class);

  @Requirement
  Execution execution;

  @Requirement("local")
  EntityReferenceSerializer<String> modelSerializer;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public String getAppScriptURL(String scriptName) {
    return getAppScriptURL(scriptName, "");
  }

  public String getAppScriptURL(String scriptName, String queryString) {
    if (queryString == null) {
      queryString = "";
    }
    if (!"".equals(queryString)) {
      queryString = "&" + queryString;
    }
    return getContext().getDoc().getURL("view", "xpage=" + APP_SCRIPT_XPAGE + "&s="
        + scriptName + queryString, getContext());
  }

  public boolean isAppScriptCurrentPage(String scriptName) {
    String scriptStr = getScriptNameFromURL();
    return (!"".equals(scriptStr) && (scriptStr.equals(scriptName)));
  }

  public String getScriptNameFromURL() {
    String scriptStr = "";
    if (isAppScriptRequest()) {
      scriptStr = getAppScriptNameFromRequestURL();
    }
    return scriptStr;
  }

  public boolean isAppScriptRequest() {
    String xpageStr = getContext().getRequest().getParameter("xpage");
    return APP_SCRIPT_XPAGE.equals(xpageStr)
        && (getAppScriptNameFromRequestURL() != null);
  }

  private String getAppScriptNameFromRequestURL() {
    return getContext().getRequest().getParameter("s");
  }

  public String getCurrentPageURL(String queryString) {
    if(isAppScriptRequest()) {
      return getAppScriptURL(getScriptNameFromURL(), queryString);
    } else {
      return "?" + queryString;
    }
  }

  public String convertToPlainText(String htmlContent) {
    LOGGER.trace("convertToPlainText called on celementsweb script service for ["
        + htmlContent + "].");
    return new PlainTextCommand().convertToPlainText(htmlContent);
  }

  public Builder getNewJSONBuilder() {
    return new Builder();
  }

  public boolean deleteMenuItem(DocumentReference docRef) {
    String docFN = modelSerializer.serialize(docRef);
    try {
      if (getContext().getWiki().getRightService().hasAccessLevel("edit",
          getContext().getUser(), docFN, getContext())) {
        return new DeleteMenuItemCommand().deleteMenuItem(docRef);
      }
    } catch (XWikiException exp) {
      LOGGER.error("Failed to check 'edit' access rights for user ["
          + getContext().getUser() + "] on document [" + docFN + "]");
    }
    return false;
  }

}