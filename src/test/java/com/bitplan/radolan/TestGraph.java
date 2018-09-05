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

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Date;

import org.junit.Test;

import com.bitplan.dateutils.DateUtils;
import com.bitplan.geo.DPoint;
import com.bitplan.geo.IPoint;

import cs.fau.de.since.radolan.Composite;

/**
 * test Graph handling
 * @author wf
 *
 */
public class TestGraph extends BaseTest {

  @Test
  public void testRainEventSequence() throws Throwable {
    if (!super.isTravis()) {
      String dateStr="2018-08-01 23:50";
      Date startDate = KnownUrl.hourFormat.parse(dateStr);
      LocalDateTime startTime=DateUtils.asLocalDateTime(startDate);
      DPoint schiefbahn=new DPoint(51.244,6.52);
      for (int day=0;day<35;day++) {
        LocalDateTime wday = startTime.plus(Period.ofDays(day));
        String url=KnownUrl.getUrlForProduct("sf", wday);
        Composite comp=new Composite(url);
        DPoint gdp = comp.translateLatLonToGrid(schiefbahn.x, schiefbahn.y);
        IPoint gp=new IPoint(gdp);
        float rain = comp.getValue(gp.x, gp.y);
        Date wdate = DateUtils.asDate(wday);
        System.out.println(String.format("%s %4.2f mm",KnownUrl.hourFormat.format(wdate),rain));
      }
    }
  }
}
