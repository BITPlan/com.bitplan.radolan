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

import org.junit.Test;

import cs.fau.de.since.radolan.Composite;

/**
 * test Zoom functionality
 * @author wf
 *
 */
public class TestZoom extends BaseTest {
  
  @Test
  public void testScreenTranslate() {
    Projection proj=new ProjectionImpl(900,900);
    IPoint p=new IPoint(400,400);
    DPoint pt = proj.translateGridToView(p, 450, 450);
    assertEquals(200.0,pt.x,0.0001);
    assertEquals(200.0,pt.y,0.0001);
    IPoint p2=proj.translateViewToGrid(pt,450,450);
    assertEquals(p.x,p2.x);
    assertEquals(p.y,p2.y);
  }
  
  @Test
  public void testZoom() {
    String products[] = { "sf", "rw", };
    for (String product : products) {
      String url = String.format(
          "https://opendata.dwd.de/weather/radar/radolan/%s/raa01-%s_10000-latest-dwd---bin",
          product, product);
      testRadolan(url, 4, product + ".png", null, "-l", "Willich","-z","30");
    }
  }

}
