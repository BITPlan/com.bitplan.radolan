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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;

import org.junit.Test;

import com.bitplan.dateutils.DateUtils;
import com.bitplan.util.CachedUrl;

/**
 * Test the Cache as asked for by
 * https://github.com/BITPlan/com.bitplan.radolan/issues/3
 * 
 * @author wf
 *
 */
public class TestCache extends BaseTest {
  /**
   * test the Cache mechanism
   * 
   * @throws Exception
   */
  @Test
  public void testCache() throws Exception {
    if (!isTravis()) {
      // CachedUrl.debug = true;
      LocalDate start = LocalDate.of(2018, 8, 28);
      LocalDate end = LocalDate.of(2018, 8, 31);
      String knownUrl = KnownUrl.RADOLAN_HISTORY;
      for (LocalDate date = start; date
          .isBefore(end); date = date.plus(Period.ofDays(1))) {
        Date d = DateUtils.asDate(date);
        String url = KnownUrl.getUrlForProduct("sf",
            DateUtils.asLocalDateTime(d));
        String fileUrl=CachedUrl.useCache(url, knownUrl);
        assertTrue(fileUrl.startsWith("file:"));
        File cacheFile = CachedUrl.cacheForUrl(url, knownUrl);
        assertTrue(cacheFile.exists());
        boolean useCache = true;
        assertTrue(".gz", CachedUrl.checkCache(url, useCache).endsWith(".gz"));
        assertTrue(".file:",
            CachedUrl.checkCache(url, useCache).startsWith("file:"));
      }
    }
  }

  @Test
  public void testNoCacheForLatest() throws Exception {
    String url = "https://opendata.dwd.de/weather/radar/radolan/sf/raa01-sf_10000-latest-dwd---bin";
    String knownUrl = "https://opendata.dwd.de/weather/radar/radolan";
    String cacheUrl = CachedUrl.checkCache(url, true);
    File cacheFile = CachedUrl.cacheForUrl(url, knownUrl);
    assertFalse(cacheFile.exists());
    assertEquals(url, cacheUrl);
  }
}
