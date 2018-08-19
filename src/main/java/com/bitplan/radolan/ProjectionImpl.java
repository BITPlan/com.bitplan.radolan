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

import java.util.logging.Logger;

import cs.fau.de.since.radolan.Translate;

/**
 * Default Projection implementation
 * 
 * @author wf
 *
 */
public class ProjectionImpl implements Projection {
  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("cs.fau.de.since.radolan");

  // switch on for debugging
  public static boolean debug = false;

  private double offSetX; // horizontal projection offset
  private double offSetY; // vertical projection offset

  private double Rx; // horizontal resolution in km/px
  private double Ry; // vertical resolution in km/px

  protected int Dx; // data width
  protected int Dy; // data height

  private boolean projection; // coordinate translation available

  public ProjectionImpl() {
    
  }
  
  /**
   * create a default 1:1 projection with the given width and height
   * @param width
   * @param height
   */
  public ProjectionImpl(int width,int height) {
    Dx=width;
    Dy=height;
    setResX(1);
    setResY(1);
    setOffSetX(0);
    setOffSetY(0);
    setProjection(true);
  }

  public double getResX() {
    return Rx;
  }

  public void setResX(double rx) {
    Rx = rx;
  }

  public double getResY() {
    return Ry;
  }

  public void setResY(double ry) {
    Ry = ry;
  }

  public double getOffSetX() {
    return offSetX;
  }

  public void setOffSetX(double offSetX) {
    this.offSetX = offSetX;
  }

  public double getOffSetY() {
    return offSetY;
  }

  public void setOffSetY(double offSetY) {
    this.offSetY = offSetY;
  }

  public boolean isProjection() {
    return projection;
  }

  public void setProjection(boolean pProjection) {
    projection = pProjection;
  }

  @Override
  public int getGridWidth() {
    return Dx;
  }

  @Override
  public int getGridHeight() {
    return Dy;
  }

  /**
   * translate the given coordinates to a double precision point
   * 
   * @param lat
   * @param lon
   * @return the translation
   */
  public DPoint translateLatLonToGrid(double lat, double lon) {
    return Translate.translate(this, lat, lon);
  }

  /**
   * translate a coordinate to lat/lon
   * 
   * @param p
   * @return the lat/lon point
   */
  public DPoint translateGridToLatLon(DPoint p) {
    return Translate.translateXYtoLatLon(this, p);
  }

  /**
   * translate the given x,y coordinate to a view with the given width and
   * height
   * 
   * @param p
   *          - the original cartesian grid coordinate
   * @param width
   * @param height
   * @return - the translated point
   */
  public DPoint translateGridToView(IPoint p, double width, double height) {
    DPoint pt = new DPoint(p.x, p.y);
    pt.x = p.x * width / this.getGridWidth();
    pt.y = p.y * height / this.getGridHeight();
    return pt;
  }

  /**
   * translate a given x,y coordinate of the composite grid to a coordinate in a
   * system with the given width and height
   * 
   * @param p
   * @param width
   * @param height
   * @return the Grid Point
   */
  public IPoint translateViewToGrid(DPoint p, double width, double height) {
    DPoint pt = new DPoint(p.x, p.y);
    pt.x = p.x * this.getGridWidth() / width;
    pt.y = p.y * this.getGridHeight() / height;
    return new IPoint(pt);
  }

}
