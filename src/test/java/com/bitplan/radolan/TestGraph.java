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
import java.util.function.Function;

import org.apache.commons.lang.time.StopWatch;
import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Ignore;
import org.junit.Test;

import com.bitplan.dateutils.DateUtils;
import com.bitplan.geo.DPoint;
import com.bitplan.geo.IPoint;

import cs.fau.de.since.radolan.Composite;
import de.dwd.geoserver.StationManager;

/**
 * test Graph handling
 * 
 * @author wf
 *
 */
public class TestGraph extends BaseTest {

  @Test
  public void testRainEventSequence() throws Throwable {
    if (!super.isTravis()) {
      String dateStr = "2019-04-15 23:50";
      Date startDate = KnownUrl.hourFormat.parse(dateStr);
      LocalDateTime startTime = DateUtils.asLocalDateTime(startDate);
      DPoint schiefbahn = new DPoint(51.244, 6.52);
      Composite.debug = true;
      for (int day = 0; day < 4; day++) {
        LocalDateTime wday = startTime.plus(Period.ofDays(day));
        String url = KnownUrl.getUrlForProduct("sf", wday);
        Composite comp = new Composite(url);
        DPoint gdp = comp.translateLatLonToGrid(schiefbahn.x, schiefbahn.y);
        IPoint gp = new IPoint(gdp);
        float rain = comp.getValue(gp.x, gp.y);
        Date wdate = DateUtils.asDate(wday);
        System.out.println(String.format("%s %5.2f mm",
            KnownUrl.hourFormat.format(wdate), rain));
      }
    }
  }

  @Ignore
  // compare speed of different Io Modes - 
  /*write
graphml=  0,6 s
graphson=  1,8 s
gryo=  2,1 s
read
graphml=  1,7 s
graphson= 22,7 s
gryo=  2,7 s*/
  public void testGraphIo() throws Exception {
    String[] modes = { IO.graphml, IO.graphson, IO.gryo };
    StationManager sm = StationManager.init();
   
    System.out.println("write");
    for (String mode : modes) {
      StationManager.setStoreMode(mode);
      StopWatch sw = new StopWatch();
      sw.start();
      sm.write();
      sw.stop();
      System.out
          .println(String.format("%s=%5.1f s", mode, sw.getTime() / 1000.0));
    }
    System.out.println("read");
    for (String mode : modes) {
      StationManager.setStoreMode(mode);
      StopWatch sw = new StopWatch();
      sw.start();
      sm.setGraph(TinkerGraph.open());
      sm.read(StationManager.getGraphFile());
      sw.stop();
      System.out
          .println(String.format("%s=%5.1f s", mode, sw.getTime() / 1000.0));
    }
  }
}
