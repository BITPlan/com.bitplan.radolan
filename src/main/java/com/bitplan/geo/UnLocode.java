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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.bitplan.json.JsonAble;

import gov.nasa.worldwind.geom.Angle;

/**
 * United nations location code
 * 
 * @author wf
 *
 */
public class UnLocode implements JsonAble {
  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("com.bitplan.geo");

  String coords;
  public String countryCode;
  String locode;
  String name;
  String nameWoDiacritics;

  public String getCoords() {
    return coords;
  }

  public void setCoords(String coords) {
    this.coords = coords;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getLocode() {
    return locode;
  }

  public void setLocode(String locode) {
    this.locode = locode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNameWoDiacritics() {
    return nameWoDiacritics;
  }

  public void setNameWoDiacritics(String nameWoDiacritics) {
    this.nameWoDiacritics = nameWoDiacritics;
  }

  public String toString() {
    String result = "";
    if (this.getLocode() != null) {
      result += this.getLocode() + "/";
    } else {
      result += "? ?  /";
    }
    if (this.getCountryCode() != null) {
      result += this.getCountryCode();
    } else {
      result += "? ";
    }
    if (this.getNameWoDiacritics() != null) {
      result += " " + this.getNameWoDiacritics();
    } else {
      result += " " + this.getName();
    }
    this.initLatLon();
    if (latAngle.getDegrees() != 0.0)
      result += " at " + getDMS();
    return result;
  }

  /**
   * get my DMS representation
   * @return
   */
  public String getDMS() {
    String dms = "?";
    if (latAngle.getDegrees() != 0.0) {
      dms = latAngle.toFormattedDMSString() + " "
          + lonAngle.toFormattedDMSString();
    }
    return dms;
  }

  Angle latAngle;
  Angle lonAngle;

  public double getLat() {
    if (latAngle == null) {
      initLatLon();
    }
    return latAngle.degrees;
  }

  public double getLon() {
    if (lonAngle == null) {
      initLatLon();
    }
    return lonAngle.degrees;
  }

  /**
   * initialize the lat and lon values
   */
  private void initLatLon() {
    // 4806N 00937E
    if (coords != null && coords.length() == 12) {
      String latDegreeStr = coords.substring(0, 2);
      String latMinStr=coords.substring(2, 4);
      String ns = coords.substring(4, 5); // N
      String lonDegreeStr = coords.substring(6, 9);
      String lonMinStr=coords.substring(9, 11);
      String ew = coords.substring(11, 12); // E
      try {
        latAngle = Angle.fromDegrees(
            (Double.parseDouble(latDegreeStr)+Double.parseDouble(latMinStr)/60.0) * ("N".equals(ns) ? 1 : -1));
        lonAngle = Angle.fromDegrees(
            (Double.parseDouble(lonDegreeStr)+Double.parseDouble(lonMinStr)/60.0) * ("E".equals(ew) ? 1 : -1));
      } catch (IllegalArgumentException iae) {
        LOGGER.log(Level.INFO,
            "Invalid lat/lon for " + this.name + " at " + coords);
      }
    }
    if (latAngle == null || lonAngle == null) {
      lonAngle = Angle.fromDegrees(0);
      latAngle = Angle.fromDegrees(0);
    }

  }
}
