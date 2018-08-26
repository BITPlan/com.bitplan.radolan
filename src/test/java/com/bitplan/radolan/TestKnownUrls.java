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

import java.util.Date;
import java.time.LocalDateTime;

import org.junit.Test;

import com.bitplan.dateutils.DateUtils;

/**
 * test the known urls handling
 * 
 * @author wf
 *
 */
public class TestKnownUrls {

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
        assertTrue(ago2hHourUrl, ago2hHourUrl.contains("5-dwd") || ago2hHourUrl.contains("0-dwd"));
        break;
      }
      i++;
    }
  }

}
