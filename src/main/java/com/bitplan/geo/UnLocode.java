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

import com.bitplan.json.JsonAble;

import gov.nasa.worldwind.geom.Angle;

public class UnLocode implements JsonAble {
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
      result += " at " + latAngle.toFormattedDMSString()+" "+lonAngle.toFormattedDMSString();
    return result;
  }
  
  Angle latAngle;
  Angle lonAngle;
  public double getLat() {
    if (latAngle==null) {
      initLatLon();
    }
    return latAngle.degrees;
  }

  public double getLon() {
    if (lonAngle==null) {
      initLatLon();
    }
    return lonAngle.degrees;
  }
  
  private void initLatLon() {
    // 4806N 00937E
    if (coords!=null && coords.length()==12) {
      String latStr=coords.substring(0,2)+" "+coords.substring(2,4)+" "+coords.substring(4,5);
      String lonStr=coords.substring(6,9)+" "+coords.substring(9,11)+" "+coords.substring(11,12);
      latAngle=Angle.fromDMS(latStr);
      lonAngle=Angle.fromDMS(lonStr);
    } else {
      lonAngle=Angle.fromDegrees(0);
      latAngle=Angle.fromDegrees(0);
    }
    
  }
}
