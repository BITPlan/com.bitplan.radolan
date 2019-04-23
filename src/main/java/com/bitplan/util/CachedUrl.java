/**
 * Copyright (c) 2018 BITPlan GmbH
 *
 * http://www.bitplan.com
 *
 * This file is part of the Opensource project at:
 * https://github.com/BITPlan/com.bitplan.radolan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Parts which are derived from https://gitlab.cs.fau.de/since/radolan are also
 * under MIT license.
 */
package com.bitplan.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.bitplan.radolan.KnownUrl;

/**
 * allows reading from url that might get replaced by cached data
 * 
 * @author wf
 *
 */
public class CachedUrl {
  // if not set the default $HOME/.radolan will be used
  public static String cacheRootPath = null;
  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("com.bitplan.util");
  public static boolean debug = false;

  /**
   * get the cache File for the given url in reference to the given knownUrl
   * 
   * @param url
   * @param knownUrl
   * @return - the cacheFile
   */
  public static File cacheForUrl(String url, String knownUrl) {
    String filePath = url.substring(knownUrl.length(), url.length());
    if (cacheRootPath == null)
      cacheRootPath = System.getProperty("user.home") + java.io.File.separator
          + ".radolan";
    File cacheRoot = new File(cacheRootPath);
    if (!cacheRoot.exists()) {
      if (debug)
        LOGGER.log(Level.INFO,
            "Creating radolan data cache directory " + cacheRoot.getPath());
      cacheRoot.mkdirs();
    }
    File cacheFile = new File(cacheRoot, filePath);
    return cacheFile;
  }

  /**
   * use the cache for the given URL
   * 
   * @param url
   * @param knownUrl
   * @return - the URL of the cached file
   * @throws Exception
   *           - if the URL is malformed
   */
  public static String useCache(String url, String knownUrl) throws Exception {
    File cacheFile = cacheForUrl(url, knownUrl);
    if (!cacheFile.exists()) {
      URL uri = new URL(url);
      if (debug)
        LOGGER.log(Level.INFO,
            String.format("caching %s to %s", url, cacheFile.getPath()));
      // cache the URL content
      FileUtils.copyURLToFile(uri, cacheFile);
    } else {
      if (debug)
        LOGGER.log(Level.INFO,
            "getting cached file from " + cacheFile.getPath());
    }
    return cacheFile.toURI().toURL().toExternalForm();
  }

  /**
   * if the cache is not active or the url is not starting with a known url then
   * return the url as is when the local cache is active then check if the url
   * content is not available locally and if needed then read the url content to
   * the cache after having made sure the file is available locally return the
   * url for the file
   * 
   * @param url
   * @param useCache
   *          - true if the cache is active
   * @return the (potentially replaced)
   * @throws Exception
   */
  public static String checkCache(String url, boolean useCache)
      throws Exception {
    if (!useCache)
      return url;
    if (url.contains("-latest-"))
      return url;
    for (String knownUrl : KnownUrl.knownUrls) {
      if (url.startsWith(knownUrl)) {
        return useCache(url, knownUrl);
      }
    }
    return url;
  }

  /**
   * read the bytes for the given url
   * 
   * @param url
   * @param useCache
   * @return - the bytes
   * @throws Exception
   */
  public static byte[] readBytes(String url, boolean useCache)
      throws Exception {
    url = CachedUrl.checkCache(url, useCache);
    InputStream inputStream = new URL(url).openStream();
    byte[] bytes = readBytes(inputStream);
    inputStream.close();
    return bytes;
  }
  
  /**
   * read a string from the given url - potentially using the cache
   * @param url
   * @param useCache
   * @param encoding
   * @return - the string
   * @throws Exception
   */
  public static String readString(String url,boolean useCache,String encoding) throws Exception {
    byte[] bytes=readBytes(url,useCache);
    String text=new String(bytes,encoding);
    return text;
  }

  /**
   * read the bytes from the given (potentially zipped) inputstream
   * 
   * @param inputStream
   * @return the bytes
   * @throws Exception
   */
  public static byte[] readBytes(InputStream inputStream) throws Exception {
    byte[] lbytes = IOUtils.toByteArray(inputStream);
    // https://tools.ietf.org/html/rfc1952
    // check for gzip header
    if (lbytes.length < 2) {
      throw new Exception("input is empty");
    }
    int magic = ((lbytes[0] & 0xff) << 8) | (lbytes[1] & 0xff);
    if (magic == 0x1f8b) {
      int zippedLength = lbytes.length;
      InputStream gzStream = new GZIPInputStream(
          new ByteArrayInputStream(lbytes));
      byte[] unzipped = IOUtils.toByteArray(gzStream);
      lbytes = unzipped;
      String msg = String.format("unzipped %d to %d bytes", zippedLength,
          lbytes.length);
      if (debug)
        LOGGER.log(Level.INFO, msg);
    }
    return lbytes;
  }

}
