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
package cs.fau.de.since.radolan;

import com.bitplan.geo.DPoint;
import com.bitplan.geo.GeoRect;

/**
 * defines Corners of a Grid
 * @author wf
 *
 */
public class CornerPoints implements GeoRect {
  private DPoint minEdge;
  private DPoint maxEdge;
  
  /**
   * construct me from four values
   * @param originTop
   * @param originLeft
   * @param edgeBottom
   * @param edgeRight
   */
  public CornerPoints(double originTop, double originLeft, double edgeBottom,
      double edgeRight) {
    super();
    minEdge=new DPoint(originTop,originLeft);
    maxEdge=new DPoint(edgeBottom,edgeRight);
  }

  @Override
  public DPoint getMinEdge() {
    return minEdge;
  }
  
  @Override
  public DPoint getMaxEdge() {
    return maxEdge;
  }
}