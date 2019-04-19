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
package org.openweathermap.weather;

import gov.nasa.worldwind.geom.Angle;

/**
 * coordinate
 * 
 * @author wf
 *
 */
public class Coord {
  double lat;
  double lon;
  private transient Angle latangle;
  private transient Angle lonangle;

  /**
   * construct me from a latitude and longitude
   * 
   * @param lat
   * @param lon
   */
  public Coord(Double lat, Double lon) {
    this.lat = lat;
    this.lon = lon;
    init();
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public void init() {
    latangle = Angle.fromDegreesLatitude(Math.abs(lat));
    lonangle = Angle.fromDegreesLongitude(Math.abs(lon));
  }

  public String getLatDMS() {
    if (latangle == null)
      init();
    String latDMS = String.format("%s %s", latangle.toFormattedDMSString(),
        lat >= 0.0 ? "N" : "S");
    return latDMS;
  }

  public String getLonDMS() {
    if (lonangle == null)
      init();
    String lonDMS = String.format("%s %s", lonangle.toFormattedDMSString(),
        lon >= 0.0 ? "E" : "W");
    return lonDMS;
  }

  /**
   * get the distance to another coordinate
   * see https://stackoverflow.com/a/123305/1497139
   * 
   * @param other
   * @return the distance in km
   */
  public double distance(Coord other) {
    double earthRadius = 6371; // km
    double dLat = Math.toRadians(other.lat - lat);
    double dLng = Math.toRadians(other.lon - lon);
    double sindLat = Math.sin(dLat / 2);
    double sindLng = Math.sin(dLng / 2);
    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
        * Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(other.lat));
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double dist = earthRadius * c;
    return dist;
  }

  /**
   * return the GEO coordinates
   */
  public String toString() {
    String dmsString = getLatDMS() + getLonDMS();
    return dmsString;
  }
}