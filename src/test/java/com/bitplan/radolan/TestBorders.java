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

import java.util.logging.Level;
import org.junit.Test;
import com.bitplan.radolan.Borders;

/**
 * test the border files
 * 
 * @author wf
 *
 */
public class TestBorders extends BaseTest {
  @Test
  public void testBorders() throws Exception {
    String names[] = { "1_deutschland/3_mittel.geojson",
        "1_deutschland/4_niedrig.geojson", "2_bundeslaender/2_hoch.geojson",
        "2_bundeslaender/3_mittel.geojson", "2_bundeslaender/4_niedrig.geojson",
        "3_regierungsbezirke/2_hoch.geojson",
        "3_regierungsbezirke/3_mittel.geojson",
        "3_regierungsbezirke/4_niedrig.geojson", "4_kreise/2_hoch.geojson",
        "4_kreise/3_mittel.geojson", "4_kreise/4_niedrig.geojson" };
    for (String name : names) {
      Borders borders = new Borders(name);
      if (debug)
        LOGGER.log(Level.INFO, String.format("border %s has %d points", name,
            borders.getPoints().size()));
    }
  }

}
