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
import java.util.Map;
import java.util.logging.Level;

import org.junit.Test;

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
    assertEquals(48.06, code.getLat(), 0.001);
    assertEquals(9.37, code.getLon(), 0.001);
    // assertEquals("?",code.toString());

  }

  @Test
  public void testLookup() throws IOException {
    UnLocodeManager ulm = UnLocodeManager.getInstance();
    assertNotNull(ulm);
    // System.out.println(ulm.unLocodes.size());
    assertEquals(9657, ulm.unLocodes.size());
    // Bielefeld? - das gibts doch gar nicht!
    Map<Double, UnLocode> closeCities = ulm.lookup(52.034, 8.529, 20);
    if (debug)
      for (UnLocode city : closeCities.values()) {
        LOGGER.log(Level.INFO,city.toString());
      }
    assertEquals(9, closeCities.size());
  }
}
