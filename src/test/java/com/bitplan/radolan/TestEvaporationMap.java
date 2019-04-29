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

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openweathermap.weather.Coord;

import com.bitplan.display.BorderDraw;
import com.bitplan.display.EvaporationView;
import com.bitplan.geo.GeoProjection;
import com.bitplan.geo.ProjectionImpl;

import cs.fau.de.since.radolan.Translate;
import de.dwd.geoserver.Observation;
import de.dwd.geoserver.Station;
import de.dwd.geoserver.StationManager;
import javafx.application.Platform;

/**
 * test a map for Evaporation data
 */
public class TestEvaporationMap extends TestBorders {

  @Test
  public void testEvaporationMap() throws Exception {
    if (!super.isTravis()) {
      StationManager sm = StationManager.init();
      String name = "3_regierungsbezirke/3_mittel.geojson";
      GeoProjection projection = new ProjectionImpl(900, 900);
      Translate.calibrateProjection(projection);
      for (int day = 1; day <= 5; day++) {
        BorderDraw borderDraw = prepareBorderDraw(projection, name);
        EvaporationView evapView = new EvaporationView(sm, day, 5);

        Platform.runLater(() -> borderDraw.drawBorders());
        Platform.runLater(
            () -> evapView.drawInterpolated(borderDraw, 5.0, 80, 80, 0.5));
        // Platform.runLater(() -> evapView.draw(borderDraw, 30., 0.4));

        Thread.sleep(SHOW_TIME * 10);
      }
      sampleApp.close();
    }
  }

  @Test
  public void testEvapView() throws Exception {
    StationManager sm = StationManager.init();
    EvaporationView evapView = new EvaporationView(sm, 1, 5);
    Map<Coord, List<Station>> gridMap = evapView.prepareGrid(47.0, 150, 150);
    Statistics stats = new Statistics();
    for (Coord c : gridMap.keySet()) {
      List<Station> stations = gridMap.get(c);
      if (debug)
        System.out
            .println(String.format("%3d %s", stations.size(), c.toString()));
      if (c.getLon() > 8.35 && c.getLon() < 12.173 && c.getLat() > 47.843
          && c.getLat() < 53.645) {
        stats.add(stations.size());
        assertTrue(c.toString(), stations.size() >= 3);
      }
      for (Station station : stations) {
        if (debug)
          System.out.println(String.format("\t %5.1f km %s",
              c.distance(station.getCoord()), station.toString()));
      }
    }
    System.out.println(stats.toString());
  }

  @Test
  public void testInterpolation() throws Exception {
    StationManager sm = StationManager.init();
    EvaporationView evapView = new EvaporationView(sm, 1, 5);
    debug = true;
    if (debug)
      EvaporationView.debug = true;
    Map<Coord, List<Station>> gridMap = evapView.prepareGrid(47.0, 150, 150);
    Coord c = new Coord(51.243, 6.519);
    Coord gc = evapView.getClosest(c, gridMap.keySet());
    double evap = evapView.getInverseWeighted(gc, gridMap.get(gc), 2.0);
    if (debug)
      System.out.println(String.format("%5.1f mm", evap));
  }

  @Test
  public void testObservationHistory() throws Exception {
    showMemory();
    StationManager sm = StationManager.init();
    // for (Station station : sm.getStationMap().values()) {
    // showMemory();
    Station station = sm.getStationMap().get("5064");
    List<Observation> obs = station.getObservationHistory(sm);
    debug = true;
    if (debug)
      System.out.println(station.toString());

    for (Observation observation : obs) {
      if (debug) {
        System.out.println(observation.toString());
      }
    }
    // }
  }

  @Test
  public void testObservationHistoryInterpolated() throws Exception {
    if (!super.isTravis()) {
      StationManager sm = StationManager.init();
      Coord schiefbahn = new Coord(51.244, 6.52);
      double radius = 47.0;
      // debug = true;
      List<Station> stations = sm.getStationsWithinRadius(schiefbahn, radius);
      if (debug) {
        for (Station station : stations) {
          System.out.println(station.toString());
        }
      }
    }
  }

  public void showMemory() {
    Runtime runtime = Runtime.getRuntime();
    int mb = 1024 * 1024;
    String memMsg = "Used Memory:"
        + (runtime.totalMemory() - runtime.freeMemory()) / mb + "\n";

    // Print free memory
    memMsg += "Free Memory:" + runtime.freeMemory() / mb + "\n";

    // Print total available memory
    memMsg += "Total Memory:" + runtime.totalMemory() / mb + "\n";

    // Print Maximum available memory
    memMsg += "Max Memory:" + runtime.maxMemory() / mb;
    System.out.println(memMsg);
  }
}
