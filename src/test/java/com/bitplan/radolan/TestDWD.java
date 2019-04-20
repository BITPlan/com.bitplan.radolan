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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.logging.Logger;

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
 * @author wf
 *
 */
public class TestDWD {

  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("com.bitplan.radolan");

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
    WFSResponse wfsresponse = WFS.getResponseAt(WFS.WFSType.RR,coord, 0.5);
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
    assertEquals("Düsseldorf(1078) - 18,6 km   51° 17’ 45.60” N   6° 46’  6.96” E", dusStation.toString());
  }
  
  public final int EXPECTED_STATIONS=67;
  
  @Test
  public void testGetAllStations() throws Exception {
    Map<String, Station> stations = Station.getAllStations();
    assertEquals(EXPECTED_STATIONS,stations.size());
  }
  
  public Station getDUSStation() {
    Coord duscoord=new Coord(51.296,6.7686);
    Station dusStation=new Station("1078","Düsseldorf",duscoord,18.6);
    return dusStation;
  }
  
  @Test
  public void testEvaporationHistoryFromDWDStation() throws Exception {
    WFS.debug=TestSuite.debug;
    Station dusStation=getDUSStation();
    WFSResponse wfsResponse=WFS.getEvaporationHistory(dusStation);
    assertNotNull(wfsResponse);
    assertTrue(wfsResponse.totalFeatures>0);
  }
  
  @Test
  public void testRainHistoryFromDWDStation() throws Exception {
    WFS.debug=TestSuite.debug;
    Station dusStation=getDUSStation();
    WFSResponse wfsResponse=WFS.getRainHistory(dusStation);
    assertNotNull(wfsResponse);
    assertEquals(3,wfsResponse.totalFeatures);
  }
 
  @Test
  public void testStationManager() throws Exception {
    StationManager sm = StationManager.init();
    assertEquals(EXPECTED_STATIONS,sm.g().V().count().next().longValue());
    StationManager.reset();
    sm = StationManager.getInstance();
    assertEquals(EXPECTED_STATIONS,74,sm.g().V().count().next().longValue());
  }
  
  @Test
  public void testStationById() throws Exception {
    StationManager sm=StationManager.init();
    Station dus = sm.byId("1078");
    assertEquals("Düsseldorf",dus.getName());
    assertEquals(6.7686,dus.getCoord().getLon(),0.001);
    assertEquals(51.296,dus.getCoord().getLat(),0.001);
  }
  
  @Test
  public void testGetObservations() throws Exception {
    StationManager sm=StationManager.init();
    Observation.getObservations(sm,WFSType.VPGB);
    long obsCount = sm.g().V().hasLabel("observation").count().next().longValue();
    System.out.println(obsCount);
    sm.write();
    long sCount=sm.g().V().hasLabel("observation").in("has").count().next().longValue();
    System.out.println(sCount);
  }

}
