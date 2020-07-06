/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.iaik.cryptoslice.utils.manifest;

import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AndroidManifest {
  private static final Logger LOGGER = LoggerFactory.getLogger(AndroidManifest.class);

  public static String getEntryPoint(File manifestFile) {
    return getStringValue(manifestFile, "/manifest/application/@android:name");
  }

  public static boolean allowsBackup(File manifestFile) {
    return getStringValue(manifestFile, "/manifest/application/@android:allowBackup").equals("true");
  }

  public static boolean isDebuggable(File manifestFile) {
    return getStringValue(manifestFile, "/manifest/application/@android:debuggable").equals("true");
  }

  public static int getMinSdkVersion(File manifestFile) {
    // https://developer.android.com/guide/topics/manifest/uses-sdk-element.html
    return getIntValue(manifestFile, "/manifest/uses-sdk/@android:minSdkVersion");
  }

  public static List<String> getPermissions(File manifestFile) {
    // https://developer.android.com/guide/topics/manifest/uses-permission-element.html
    return getStringList(manifestFile, "/manifest/uses-permission/@android:name");
  }

  private static List<String> getStringList(File file, String query) {
    XPath xpath = AndroidXPathFactory.newXPath();

    List<String> nodeText = new ArrayList<>();
    InputStream is = null;
    try {
      is = new FileInputStream(file);

      NodeList list = (NodeList) xpath.evaluate(query, new InputSource(is), XPathConstants.NODESET);
      for (int i = 0; i < list.getLength(); i++) {
        nodeText.add(list.item(i).getTextContent());
      }
    } catch (XPathExpressionException | FileNotFoundException e){
      LOGGER.error("getStringList error", e);
    } finally {
      Closeables.closeQuietly(is);
    }

    return nodeText;
  }

  private static String getStringValue(File file, String query) {
    List<String> results = getStringList(file, query);
    if (!results.isEmpty())
      return results.get(0);

    return "";
  }

  private static int getIntValue(File file, String query) {
    String result = getStringValue(file, query);

    try {
      return Integer.parseInt(result);
    } catch (NumberFormatException e) {
      LOGGER.error("getIntValue error", e);
      return -1;
    }
  }
}
