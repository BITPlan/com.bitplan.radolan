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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

/**
 * utility functions for reading Json from url or zipped url
 * @author wf
 *
 */
public class JsonUtil {

  /**
   * get a gzipped json string from the given url
   * 
   * @param url
   * @return the string read
   * @throws MalformedURLException
   * @throws IOException
   */
  public static String readGZIP(String url)
      throws MalformedURLException, IOException {
    InputStream urlStream = new URL(url).openStream();
    InputStream gzStream = new GZIPInputStream(urlStream);
    String json = read(gzStream);
    urlStream.close();
    return json;
  }

  /**
   * read from the given url
   * @param url
   * @return the string read
   * @throws MalformedURLException
   * @throws IOException
   */
  public static String read(String url)
      throws MalformedURLException, IOException {
    InputStream urlStream = new URL(url).openStream();
    String json = read(urlStream);
    return json;
  }

  /**
   * read a json string from the given input stream
   * @param stream
   * @return - the json string
   * @throws IOException
   */
  public static String read(InputStream stream) throws IOException {
    StringWriter jsonWriter = new StringWriter();
    IOUtils.copy(stream, jsonWriter, "UTF-8");
    String json = jsonWriter.toString();
    stream.close();
    return json;
  }

}
