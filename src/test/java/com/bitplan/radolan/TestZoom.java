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

import com.bitplan.geo.DPoint;
import com.bitplan.geo.IPoint;
import com.bitplan.geo.GeoProjection;
import com.bitplan.geo.ProjectionImpl;
import com.bitplan.javafx.WaitableApp;

/**
 * test Zoom functionality
 * @author wf
 *
 */
public class TestZoom extends BaseTest {
  
  @Test
  public void testScreenTranslate() {
    GeoProjection proj=new ProjectionImpl(900,900);
    IPoint p=new IPoint(400,400);
    DPoint pt = proj.translateGridToView(p, 450, 450);
    assertEquals(200.0,pt.x,0.0001);
    assertEquals(200.0,pt.y,0.0001);
    IPoint p2=proj.translateViewToGrid(pt,450,450);
    assertEquals(p.x,p2.x);
    assertEquals(p.y,p2.y);
  }
  
  @Test
  public void testZoom() throws Exception {
    int wait=4;
    String products[] = { "sf", "rw", };
    WaitableApp.waitTimeOutSecs=wait;
    for (String product : products) {
      String url = KnownUrl.getUrl(product, "latest");
      testRadolan(url,wait, product + ".png", null,  "-d", "-l", "Willich","-z","30");
    }
  }

}
