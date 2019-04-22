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
package com.bitplan.display;

import cs.fau.de.since.radolan.FloatFunction;
import cs.fau.de.since.radolan.vis.Vis;
import cs.fau.de.since.radolan.vis.Vis.ColorRange;
import javafx.scene.paint.Color;

/**
 * display of evaporation
 * @author wf
 *
 */
public class EvaporationView {
  
  
  public static final ColorRange[] DWD_Style_Colors= {
      new ColorRange(  6.0f,  999999.0f,Color.rgb( 204,0, 0)),
      new ColorRange(  5.0f,  5.9999f,Color.rgb(220,91, 0)),
      new ColorRange(  4.0f,  4.9999f,Color.rgb(232,174, 0)),  
      new ColorRange(  3.0f,  3.9999f,Color.rgb(237,237, 0)),
      new ColorRange(  2.0f,  2.9999f,Color.rgb(127,195, 0)),
      new ColorRange(  1.0f,  1.9999f,Color.rgb(68,171, 0)),
      new ColorRange(  0.0f,  0.9999f,Color.rgb(0,139,0))
  };
  
  public static FloatFunction<Color> heatmap = Vis.RangeMap(DWD_Style_Colors);

}
