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
package com.bitplan.radolan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.logging.Level;

import org.junit.Test;

import com.bitplan.dateutils.DateUtils;

/**
 * test the known urls handling
 * 
 * @author wf
 *
 */
public class TestKnownUrls extends BaseTest {

  /**
   * https://stackoverflow.com/a/3584332/1497139 Pings a HTTP URL. This
   * effectively sends a HEAD request and returns <code>true</code> if the
   * response code is in the 200-399 range.
   * 
   * @param url
   *          The HTTP URL to be pinged.
   * @param timeout
   *          The timeout in millis for both the connection timeout and the
   *          response read timeout. Note that the total timeout is effectively
   *          two times the given timeout.
   * @return <code>true</code> if the given HTTP URL has returned response code
   *         200-399 on a HEAD request within the given timeout, otherwise
   *         <code>false</code>.
   */
  @SuppressWarnings("restriction")
  public static boolean pingURL(String url, int timeout) {
    url = url.replaceFirst("^https", "http"); // Otherwise an exception may be
                                              // thrown on invalid SSL
                                              // certificates.

    try {
      URLConnection connection = new URL(url).openConnection();

      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);
      if (connection instanceof HttpURLConnection) {
        HttpURLConnection hconnection = (HttpURLConnection) connection;
        hconnection.setRequestMethod("HEAD");
        int responseCode = hconnection.getResponseCode();
        return (200 <= responseCode && responseCode <= 399);
      } else if (connection instanceof sun.net.www.protocol.ftp.FtpURLConnection) {
        sun.net.www.protocol.ftp.FtpURLConnection ftpConnection = (sun.net.www.protocol.ftp.FtpURLConnection) connection;
        ftpConnection.connect();
        ftpConnection.close();
        return true;
      } else {
        throw new IllegalArgumentException("pingURL can not handle url for "
            + connection.getClass().getName());
      }
    } catch (IOException exception) {
      return false;
    }
  }

  @Test
  public void testKnownUrls() throws Exception {
    assertEquals(
        "https://opendata.dwd.de/weather/radar/radolan/sf/raa01-sf_10000-latest-dwd---bin",
        KnownUrl.getUrl("dailySum", "latest"));
    assertEquals(
        "https://opendata.dwd.de/weather/radar/radolan/sf/raa01-sf_10000-latest-dwd---bin",
        KnownUrl.getUrl("sF", "latest"));
    LocalDateTime ago2h = LocalDateTime.now().minusHours(2);
    Date ago2hDate = DateUtils.asDate(ago2h);
    String products[] = { "sf", "rw", "ry" };
    String alias[] = { "daily", "hourly", "5min" };
    int i = 0;
    for (String product : products) {
      String timeStamp = KnownUrl.getTimeStampForProduct(product, ago2h);
      String expected = KnownUrl.RADOLAN_OPENDATA + "/" + product + "/"
          + KnownUrl.getFileNameForProduct(product, timeStamp);
      assertEquals(expected, KnownUrl.getUrl(alias[i], timeStamp));
      String ago2hString = KnownUrl.hourFormat.format(ago2hDate);
      assertEquals(String.format("%04d-%02d-%02d %02d:%02d", ago2h.getYear(),
          ago2h.getMonthValue(), ago2h.getDayOfMonth(), ago2h.getHour(),
          ago2h.getMinute()), ago2hString);
      String ago2hHourUrl = KnownUrl.getUrl(alias[i], ago2hString);
      assertEquals(expected, ago2hHourUrl);
      // System.out.println(ago2hHourUrl);
      switch (product) {
      case "sf":
      case "rw":
        assertTrue(ago2hHourUrl, ago2hHourUrl.contains("50-dwd"));
        break;
      case "ry":
        assertTrue(ago2hHourUrl,
            ago2hHourUrl.contains("5-dwd") || ago2hHourUrl.contains("0-dwd"));
        break;
      }
      i++;
    }
  }

  int TIME_OUT = 400;

  @Test
  public void testRecent() throws Exception {
    //debug=true;
    int fails=0;
    for (int hours = 46; hours <= 50; hours++) {
      LocalDateTime agoh = LocalDateTime.now().minusHours(hours);
      Date agohDate = DateUtils.asDate(agoh);
      String agohString = KnownUrl.hourFormat.format(agohDate);
      String url = KnownUrl.getUrl("sf", agohString);
      boolean ok = pingURL(url, TIME_OUT);
      if (debug)
        LOGGER.log(Level.INFO, String.format("%3d h:%s %s %s", hours,
            ok ? "✓" : " ❌", agohString, url));
      if (hours>48)
        assertTrue(url.startsWith("ftp"));
      if (!ok)
        fails++;
    }
    assertEquals(0,fails);
  }
  
  @Test
  public void testHistory() throws Exception {
    debug=true;
    String [] times= {"2006-11-01 23:50","2017-07-05 16:50"};
    for (String time:times) {
      String url = KnownUrl.getUrl("sf", time);
      boolean ok = pingURL(url, TIME_OUT);
      if (debug)
        LOGGER.log(Level.INFO, String.format("time:%s %s %s", time,
            ok ? "✓" : " ❌", url));
      assertTrue(ok);
    }
  }

}
