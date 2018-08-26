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
package com.bitplan.geo;

/**
 * a 2 D projection in a cartesian coordinate system
 * @author wf
 *
 */
public interface Projection {
  public double getOffSetX(); // horizontal offset
  public double getOffSetY(); // vertical offset
  public void setOffSetX(double offSetX);
  public void setOffSetY(double offSetY);
  
  /**
   * get the x resolution km/per grid pixel
   * @return - the x resolution
   */
  public double getResX();
  
  /**
   * get the y resolution km/per grid pixel
   * @return the y resolution
   */
  public double getResY();
  
  public void setResX(double resX);
  public void setResY(double resY);
  
  /**
   * translate a given x,y coordinate of the grid to a coordinate in a system
   * with the given width and height
   * @param p
   * @param width
   * @param height
   * @return the Grid Point
   */
  public IPoint translateViewToGrid(DPoint p, double width, double height);
  
  /**
   * get the gridWidth
   * @return - the grid width
   */
  public int getGridWidth();
  
  /**
   * get the grid Height
   * @return - the grid height
   */
  public int getGridHeight();
}
