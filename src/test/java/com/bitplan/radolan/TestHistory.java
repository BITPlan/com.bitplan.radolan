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

import org.junit.Test;

import com.bitplan.geo.DPoint;

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
      // Composite.debug = true;
      CompositeManager cm = new CompositeManager();
      DPoint schiefbahn = new DPoint(51.244, 6.52);
      for (int daysAgo = 1; daysAgo <= 14; daysAgo++) {
        float rain = cm.getRainSum(daysAgo, schiefbahn);
        System.out.println(String.format("%3d: %5.2f mm", daysAgo, rain));
      }
    }
  }

}
