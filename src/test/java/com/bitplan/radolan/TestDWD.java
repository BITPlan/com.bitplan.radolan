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

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.structure.Column;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.openweathermap.weather.Coord;
import org.openweathermap.weather.Location;

import de.dwd.geoserver.Observation;
import de.dwd.geoserver.Station;
import de.dwd.geoserver.StationManager;
import de.dwd.geoserver.WFS;
import de.dwd.geoserver.WFS.Feature;
import de.dwd.geoserver.WFS.WFSResponse;
import de.dwd.geoserver.WFS.WFSType;

/**
 * test Open data services of Deutscher Wetterdienst
 * 
 * @author wf
 *
 */
public class TestDWD {
  public final int EXPECTED_STATIONS = 67;
  public final int DAYS = 7;
  public final int EXPECTED_OBSERVATIONS = EXPECTED_STATIONS * DAYS;
  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("com.bitplan.radolan");
  public static boolean debug = true;

  /**
   * test DWD Data
   * 
   * @throws Exception
   */
  @Test
  public void testWFS() throws Exception {
    // TestSuite.debug = true;
    WFS.debug = TestSuite.debug;
    Location location = Location.byName("Knickelsdorf/DE");
    if (TestSuite.debug)
      System.out.println(location.toString());
    Coord coord = location.getCoord();
    assertNotNull(coord);
    WFSResponse wfsresponse = WFS.getResponseAt(WFS.WFSType.RR, coord, 0.5);
    assertNotNull(wfsresponse);
    assertEquals("FeatureCollection", wfsresponse.type);
    assertEquals(9, wfsresponse.totalFeatures);
    if (TestSuite.debug)
      for (Feature feature : wfsresponse.features) {
        System.out.println(feature.toString());
        System.out.println(String.format("%5.1f km",
            feature.geometry.getCoord().distance(coord)));
        System.out
            .println(String.format("\t%s", feature.properties.toString()));
      }
    Station dusStation = wfsresponse.getClosestStation(coord);
    assertEquals(
        "Düsseldorf(1078) - 18,6 km   51° 17’ 45.60” N   6° 46’  6.96” E",
        dusStation.toString());
  }

  @Test
  public void testGetAllStations() throws Exception {
    Map<String, Station> stations = Station.getAllStations();
    assertEquals(EXPECTED_STATIONS, stations.size());
  }

  public Station getDUSStation() {
    Coord duscoord = new Coord(51.296, 6.7686);
    Station dusStation = new Station("1078", "Düsseldorf", duscoord, 18.6);
    return dusStation;
  }

  @Test
  public void testEvaporationHistoryFromDWDStation() throws Exception {
    WFS.debug = TestSuite.debug;
    Station dusStation = getDUSStation();
    WFSResponse wfsResponse = WFS.getEvaporationHistory(dusStation);
    assertNotNull(wfsResponse);
    assertTrue(wfsResponse.totalFeatures >= 1);
  }

  @Test
  public void testRainHistoryFromDWDStation() throws Exception {
    WFS.debug = TestSuite.debug;
    Station dusStation = getDUSStation();
    WFSResponse wfsResponse = WFS.getRainHistory(dusStation);
    assertNotNull(wfsResponse);
    assertEquals(3, wfsResponse.totalFeatures);
  }

  @Test
  public void testStationManager() throws Exception {
    StationManager sm = StationManager.init();
    assertEquals(EXPECTED_STATIONS,
        sm.g().V().hasLabel("station").count().next().longValue());
    StationManager.reset();
    sm = StationManager.getInstance();
    assertEquals(EXPECTED_STATIONS,
        sm.g().V().hasLabel("station").count().next().longValue());
  }

  @Test
  public void testStationById() throws Exception {
    StationManager sm = StationManager.init();
    Station dus = sm.byId("1078");
    assertEquals("Düsseldorf", dus.getName());
    assertEquals(6.7686, dus.getCoord().getLon(), 0.001);
    assertEquals(51.296, dus.getCoord().getLat(), 0.001);
  }

  @Test
  public void testGetObservations() throws Exception {
    StationManager sm = StationManager.init();
    long obsCount1 = sm.g().V().hasLabel("observation")
        .has("name", "evaporation").count().next().longValue();
    Observation.getObservations(sm, WFSType.VPGB);
    long obsCount2 = sm.g().V().hasLabel("observation")
        .has("name", "evaporation").count().next().longValue();
    if (debug)
      System.out.println(String.format("%3d -> %3d", obsCount1, obsCount2));
    assertEquals(EXPECTED_OBSERVATIONS, obsCount2);

    long sCount = sm.g().V().hasLabel("observation").has("name", "evaporation")
        .in("has").count().next().longValue();
    assertEquals(EXPECTED_OBSERVATIONS, sCount);
  }

  /**
   * show the given map entries
   * 
   * @param map
   */
  public void showMap(String title, Map<Object, Object> map) {
    System.out.println(title + ":" + map.values().size());
    for (Entry<Object, Object> entry : map.entrySet()) {
      System.out
          .println(String.format("\t%s=%s", entry.getKey(), entry.getValue()));
    }
  }

  public void showVertex(String title, Vertex v) {
    System.out.println(title + ":" + v.label() + "(" + v.id() + ")");
    v.properties().forEachRemaining(p -> {
      System.out
          .println(String.format("\t%s=%s", p.key(), p.value().toString()));
    });
  }

  public void showObject(String title, Object object) {
    System.out.println(
        title + "(" + object.getClass().getName() + "):" + object.toString());
  }

  /**
   * show a map of number
   */
  public void showNumberMap(String title, Map<Object, Object> map,
      String format, String unit) {
    System.out.println(title + ":" + map.values().size());
    for (Entry<Object, Object> evap : map.entrySet()) {
      String key = (String) evap.getKey();
      Number value = (Number) evap.getValue();
      System.out
          .println(String.format("%30s=" + format + " %s", key, value, unit));
    }
  }

  @Test
  public void testGetEvaporationHistory() throws Exception {
    File evapdir = new File("src/test/data/geoserver");
    StationManager sm = StationManager.init();
    long obsCount1 = sm.g().V().hasLabel("observation")
        .has("name", "evaporation").count().next().longValue();
    Observation.getObservations(sm, evapdir);
    long obsCount2 = sm.g().V().hasLabel("observation")
        .has("name", "evaporation").count().next().longValue();
    if (debug)
      System.out.println(String.format("%3d -> %3d", obsCount1, obsCount2));
    assertEquals(EXPECTED_OBSERVATIONS, obsCount2);
    sm.g().V().hasLabel("observation").has("name", "evaporation").group()
        .by("stationid").by(values("value").sum()).order(Scope.local)
        .by(Column.values, Order.desc)
        .forEachRemaining(m -> showNumberMap("sum", m, "%5.1f", "mm"));
    sm.g().V().hasLabel("observation").has("name", "evaporation").group()
        .by("stationid").by(values("value").count()).order(Scope.local)
        .by(Column.values, Order.desc)
        .forEachRemaining(m -> showNumberMap("count", m, "%3d", ""));
    Map<Object, Long> countMap = (Map<Object, Long>) sm.g().V()
        .hasLabel("observation").has("name", "evaporation").groupCount()
        .by("stationid").order(Scope.local).by(Column.keys, Order.asc).next();
    System.out.println(countMap.size());
    sm.g().V().hasLabel("observation").has("name", "evaporation").group()
        .by("stationid").by(values("value").mean()).order(Scope.local)
        .by(Column.values, Order.desc)
        .forEachRemaining(m -> showNumberMap("mean", m, "%5.1f", "mm"));
    long edgeCount=sm.g().E().hasLabel("has").count().next().longValue();
    assertEquals(EXPECTED_OBSERVATIONS,edgeCount);
    sm.g().E().hasLabel("has").
       group()
        .by(outV().values("name"))
        .by(inV().values("value").count()).
       order(Scope.local)
        .by(Column.values, Order.desc)
        .forEachRemaining(m -> showNumberMap("count", m, "%3d", ""));
    sm.g().E().hasLabel("has").
    group()
     .by(outV().values("name"))
     .by(inV().values("value").mean()).
    order(Scope.local)
     .by(Column.values, Order.desc)
    .forEachRemaining(m -> showNumberMap("mean", m, "%5.1f", "mm"));
    sm.g().E().hasLabel("has").
    group()
     .by(outV().values("name"))
     .by(inV().values("value").sum()).
    order(Scope.local)
     .by(Column.values, Order.desc)
    .forEachRemaining(m -> showNumberMap("sum", m, "%5.1f", "mm"));
  }
}
