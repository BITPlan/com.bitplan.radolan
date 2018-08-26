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
 * abstraction of a projection
 * @author wf
 *
 */
public interface GeoProjection extends Projection{
  
  /**
   * set the bounding Rectangle
   * @param geoRect
   */
  public void setBounds(GeoRect geoRect);
  /**
   * get the bounding Rectangle
   * @return
   */
  public GeoRect getBounds();

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
  
  boolean isProjection(); // true if the projection is available 
  public void setProjection(boolean projection);
}
