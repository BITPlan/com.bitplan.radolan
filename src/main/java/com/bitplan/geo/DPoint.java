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

import gov.nasa.worldwind.geom.Angle;

/**
 * double precision Point Helper class
 * @author wf
 *
 */
public class DPoint {
  public double x;
  public double y;
  /**
   * construct me
   * @param x
   * @param y
   */
  public DPoint(double x, double y) {
    this.x=x;
    this.y=y;
  }
  
  public Angle getLat() {
    return Angle.fromDegrees(x);
  }
  
  public Angle getLon() {
    return Angle.fromDegrees(y);
  }
  
  public String toDMSString() {
    String lat=getLat().toDMSString();
    String lon=getLon().toDMSString();
    return lat+" "+lon;
  }
  
  public String getLatDMS() {
    String latDMS = String.format("%s %s", getLat().toFormattedDMSString().substring(0, 9),
        getLat().degrees >= 0.0 ? "N" : "S");
    return latDMS;
  }

  public String getLonDMS() {
    String lonDMS = String.format("%s %s", getLon().toFormattedDMSString().substring(0, 9),
        getLon().degrees >= 0.0 ? "E" : "W");
    return lonDMS;
  }
  
  /**
   * format lat/lon to Degree minutes 
   * @return a short formatted display version
   */
  public String toFormattedDMSString() {
    String lat=getLatDMS();
    String lon=getLonDMS();
    String dms=(lat+" "+lon);
    dms=dms.replaceAll("\\s+"," ").trim();
    return dms;
  }
}