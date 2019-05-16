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

import com.bitplan.display.BorderDraw;
import com.bitplan.display.MapView;
import com.bitplan.geo.UnLocodeManager;

import cs.fau.de.since.radolan.Composite;
import de.dwd.geoserver.Observation;
import de.dwd.geoserver.Station;
import de.dwd.geoserver.StationManager;

/**
 * debug setting
 * @author wf
 *
 */
public class Debug {

  /**
   * activate the static debug flags of components used
   */
  public static void activateDebug() {
    Composite.debug = true;
    RadolanApp.debug = true;
    Radolan2Image.debug = true;
    Zoom.debug = true;
    BorderDraw.debug=true;
    MapView.debug=true;
    DisplayContext.debug = true;
    UnLocodeManager.debug = true;
    StationManager.debug=true;
    Station.debug=true;
    Observation.debug=true;
  }

}
