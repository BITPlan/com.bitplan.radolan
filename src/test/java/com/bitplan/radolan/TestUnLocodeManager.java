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

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Test;

import com.bitplan.geo.DPoint;
import com.bitplan.geo.UnLocode;
import com.bitplan.geo.UnLocodeManager;

/**
 * test the UnLocode Manager
 * 
 * @author wf
 *
 */
public class TestUnLocodeManager extends BaseTest {

  @Test
  public void testUnLocodeManager() throws IOException {
    UnLocodeManager ulm = UnLocodeManager.getInstance();
    assertNotNull(ulm);
    // System.out.println(ulm.unLocodes.size());
    assertEquals(9657, ulm.unLocodes.size());
    UnLocodeManager nm = UnLocodeManager.getEmpty();
    UnLocode code = new UnLocode();
    code.countryCode = "DE";
    code.setCoords("4806N 00937E");
    nm.unLocodes.add(code);
    assertEquals(
        "{\n" + "  \"unLocodes\": [\n" + "    {\n"
            + "      \"coords\": \"4806N 00937E\",\n"
            + "      \"countryCode\": \"DE\"\n" + "    }\n" + "  ]\n" + "}",
        nm.asJson());
    assertEquals(48.1, code.getLat(), 0.001);
    assertEquals(9.616, code.getLon(), 0.001);
    // assertEquals("?",code.toString());

  }

  @Test
  public void testLookupByLatLon() throws IOException {
    UnLocodeManager ulm = UnLocodeManager.getInstance();
    assertNotNull(ulm);
    // System.out.println(ulm.unLocodes.size());
    assertEquals(9657, ulm.unLocodes.size());
    // Bielefeld? - das gibts doch gar nicht!
    // debug = true;
    Map<Double, UnLocode> closeCities = ulm.lookup(52.034, 8.529, 20);
    if (debug)
      for (UnLocode city : closeCities.values()) {
        LOGGER.log(Level.INFO, city.toString());
      }
    assertEquals(14, closeCities.size());
  }

  /**
   * test looking up by Name
   * 
   * @throws Exception
   */
  @Test
  public void testLookupByName() throws Exception {
    UnLocodeManager ulm = UnLocodeManager.getInstance();
    assertNotNull(ulm);
    // debug = true;
    String[] names = { "Neuss", "Köln", "Düsseldorf", "Mönchengladbach",
        "Krefeld", "Willich", "Münster" };
    DPoint[] latlons = { new DPoint(51.18, 6.68), new DPoint(50.95, 6.93),
        new DPoint(51.23, 6.78), new DPoint(51.20, 6.43),
        new DPoint(51.33, 6.57), new DPoint(51.27, 6.55),
        new DPoint(49.92, 8.87) };
    int i = 0;
    for (String name : names) {
      UnLocode city = ulm.lookup(name);
      if (debug) {
        LOGGER.log(Level.INFO,
            String.format(Locale.ENGLISH,
                "lookup of city %s -> %s lat %.2f,%.2f", name, city.toString(),
                city.getLat(), city.getLon()));
      }
      assertEquals(city.getName() + " lat", latlons[i].x, city.getLat(), 0.01);
      assertEquals(city.getName() + " lon", latlons[i].y, city.getLon(), 0.01);
      i++;
    }
  }

  @Test
  public void testLookupByLatLonPerformance() {
    UnLocodeManager ulm = UnLocodeManager.getInstance();
    // UnLocodeManager.debug=true;
    assertNotNull(ulm);
    // approx 1 second for 320 lookups
    for (int i = 1; i <= 9; i++) {
      UnLocode cityToFind = ulm.unLocodes.get(ulm.unLocodes.size() - i);
      ulm.lookup(cityToFind.getLat(), cityToFind.getLon(), 20.0);
    }
  }

  @Test
  public void testBounds() {
    //debug =true;
    UnLocodeManager ulm = UnLocodeManager.getInstance();
    DPoint max = new DPoint(0, 0);
    DPoint min = new DPoint(360, 360);
    int issues = 0;
    int nocoord=0;
    for (UnLocode city : ulm.unLocodes) {

      double lat = city.getLat();
      double lon = city.getLon();
      if (lat > 0) {
        if (city.getCoords().length() != 12) {
          if (debug)
            System.out.println("12?" + city + " " + city.getCoords());
          issues++;
        } else if (lon < 0) {
          if (debug)
            System.out.println("lat <0" + city + " " + city.getCoords());
          issues++;
        } else if (lon > 20) {
          if (debug)
            System.out.println("lon >20" + city + " " + city.getCoords());
          issues++;
        } else {
          if (lat < min.x)
            min.x = lat;
          if (lat > max.x)
            max.x = lat;
          if (lon < min.y)
            min.y = lon;
          if (lon > max.y)
            max.y = lon;
        }
      } else {
        nocoord++;
      }
    }
    String msg = String.format(Locale.ENGLISH,"bounds lat %.2f lon %.2f - lat %.2f lon %.2f",
        min.x, min.y, max.x, max.y);
    if (debug)
      LOGGER.log(Level.INFO, msg+" "+issues+" issues");
    assertEquals("bounds lat 40.80 lon 1.82 - lat 59.22 lon 15.00",msg);
    assertEquals(46,issues);
    assertEquals(1398,nocoord);
  }
}
