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

import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.openweathermap.weather.Coord;

import com.bitplan.geo.DPoint;

import de.dwd.geoserver.Observation;
import de.dwd.geoserver.Station;
import de.dwd.geoserver.StationManager;

/**
 * test the History
 * 
 * @author wf
 *
 */
public class TestHistory extends BaseTest {

  @Test
  public void testRainEventSequence() throws Throwable {
    if (!super.isTravis()) {
      DPoint schiefbahn = new DPoint(51.244, 6.52);
      // Composite.debug = true;
      CompositeManager cm = new CompositeManager();
      for (int daysAgo = 1; daysAgo <= 14; daysAgo++) {
        float rain = cm.getRainSum(daysAgo, schiefbahn);
        System.out.println(String.format("%3d: %5.2f mm", daysAgo, rain));
      }
    }
  }

  @Test
  public void testRainAndEvaporation() throws Throwable {
    if (!super.isTravis()) {
      int pastDays = 7;
      double radius = 36.0;
      DPoint schiefbahn = new DPoint(51.244, 6.52);
      Coord schiefbahnC = new Coord(schiefbahn.x, schiefbahn.y);
      CompositeManager cm = new CompositeManager();
      StationManager sm = StationManager.init();
      StationManager.debug=false;
      List<Station> stations = sm.getStationsWithinRadius(schiefbahnC, radius);
      HashMap<String, List<Observation>> evapmap = new HashMap<String, List<Observation>>();
      for (Station station : stations) {
        List<Observation> obs = station.getObservationHistory(sm);
        evapmap.put(station.id, obs);
      }

      double power = 2.0;
      System.out.println(String.format("Willich Schiefbahn -  %s %s",schiefbahnC.getLatDMS(), schiefbahnC.getLonDMS()));
      System.out.println("#   |    date    | rain  | evap  | total | balance");
      String line      = "----+------------+-------+-------+-------+--------";
      System.out.println(line);
      double rainsum = 0.;
      double evapsum = 0.;
      double total = 0.;
      double balance = 0.0;
      LocalDate today=LocalDate.now();
      for (int daysAgo = pastDays; daysAgo >= 1; daysAgo--) {
        float rain = cm.getRainSum(daysAgo, schiefbahn);
        final int dayIndex = daysAgo - 1;
        double evap = sm.getInverseWeighted(schiefbahnC, stations, power,
            station -> {
              List<Observation> obs = evapmap.get(station.id);
              Observation o = obs.get(dayIndex);
              return o.getValue();
            });
        rainsum += rain;
        evapsum += evap;
        total += rain - evap;
        balance+=Math.min(rain,15)-evap;
        balance=Math.min(balance, 15);
        balance=Math.max(balance, -15);
        LocalDate rainDate = today.plusDays(-daysAgo);
        String dateStr=rainDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        System.out.println(String.format("%3d | %s | %5.1f | %5.1f | %5.1f | %5.1f", daysAgo,
            dateStr,rain, evap, rain - evap, balance));
      }
      System.out.println(line);
      System.out.println(String.format("sum |            | %5.1f | %5.1f | %5.1f", rainsum,
          evapsum, total));
    }
  }
  
  @Test
  public void testEvapHistory() throws Exception {
    String toenisVorstId="5064";
    StationManager sm = StationManager.init();
    Station toenisVorst = sm.getStationMap().get(toenisVorstId);
    assertNotNull(toenisVorst);
    System.out.println(toenisVorst.toString());
    List<Observation> obs = toenisVorst.getObservationHistory(sm);
    for (Observation ob:obs) {
      System.out.println(ob);
    }
  }

}
