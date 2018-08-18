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

import java.time.Duration;

import com.bitplan.radolan.DPoint;
import com.bitplan.radolan.IPoint;

import cs.fau.de.since.radolan.Catalog.Unit;

/**
 * abstraction of the Radar Image
 * @author wf
 *
 */
public interface RadarImage {
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
  
  /**
   * get the value at the given grid position
   * @param x
   * @param y
   * @return - the value
   */
  public float getValue(int x, int y);
  
  /**
   * translate the given coordinates to a double precision point
   * 
   * @param lat
   * @param lon
   * @return the translation
   */
  public DPoint translateLatLonToGrid(double lat, double lon);

  /**
   * translate a coordinate to lat/lon
   * 
   * @param p
   * @return the lat/lon point
   */
  public DPoint translateGridToLatLon(DPoint p);
  
  /**
   * translate a Grid point to a view point
   * @param p
   * @param width - of the view
   * @param height - of the view
   * @return - coordinate in the view
   */
  public DPoint translateGridToView(IPoint p, double width, double height);
  
  /**
   * translate a given x,y coordinate of the composite grid to a coordinate in a system
   * with the given width and height
   * @param p
   * @param width
   * @param height
   * @return the Grid Point
   */
  public IPoint translateViewToGrid(DPoint p, double width, double height);
  
  /**
   * get the data unit of the image
   * @return - the data Unit
   */
  public Unit getDataUnit();
  
  /**
   * get the interval displayed
   * @return - the interval
   */
  public Duration getInterval();
  
  /**
   * return true if this is a projection
   * @return
   */
  public boolean isProjection();
  
}