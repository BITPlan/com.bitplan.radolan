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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.bitplan.json.JsonAble;
import com.bitplan.json.JsonManagerImpl;

import cs.fau.de.since.radolan.Translate;

/**
 * manage a list of united nations location codes
 * @author wf
 *
 */
public class UnLocodeManager implements JsonAble {
  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("com.bitplan.geo");
  
  public static boolean debug=false;
  
  public static final transient String RESOURCE_NAME="unlocode_de.json";
  public List<UnLocode> unLocodes=new ArrayList<UnLocode>();
  public transient Map<String,UnLocode> unLocodeByName=new TreeMap<String,UnLocode>();
  public transient Map<Integer,UnLocode> unLocodesByGridNumber=new TreeMap<Integer,UnLocode>();
  
  private static UnLocodeManager instance;
  private UnLocodeManager() {};
  /**
   * singleton access
   * @return
   */
  public static UnLocodeManager getInstance() {
    if (instance==null) {
      // https://stackoverflow.com/a/21337734/1497139
      try {
        String json=IOUtils.toString(UnLocodeManager.class.getClassLoader().getResource(RESOURCE_NAME), "UTF-8");
        JsonManagerImpl<UnLocodeManager> m=new JsonManagerImpl<UnLocodeManager>(UnLocodeManager.class);
        instance=m.fromJson(json);
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "could not get "+RESOURCE_NAME+" from classpath/jar");
      }
    }
    return instance;
  }
  
  /**
   * reinit me
   */
  public void reinit() {
    for (UnLocode code:this.unLocodes) {
      if (code.coords!=null) {
        unLocodeByName.put(code.name, code);
      }
    }
  }
  
  public static UnLocodeManager getEmpty() {
    return new UnLocodeManager();
  }
  
  /**
   * find locations close to the given latitude and longitude
   * as a sorted map by distance in km
   * @param lat
   * @param lon
   * @return the map of cities
   */
  public Map<Double,UnLocode> lookup(double lat, double lon, double maxDist) {
    Map<Double,UnLocode> cities=new TreeMap<Double,UnLocode>();
    long startTime = System.nanoTime();
 
    // https://stackoverflow.com/questions/1260572/fastest-way-to-find-the-locationzip-city-state-given-latitude-longitude
    for (UnLocode city:this.unLocodes) {
      if (city.coords!=null) {
        double citylat = city.getLat();
        double citylon = city.getLon();
        double dist=Translate.haversine(lat, lon, citylat, citylon);
        if (dist<=maxDist) {
          cities.put(dist, city);
        }
      }
    }
    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 100000;
    if (debug)
      LOGGER.log(Level.INFO,
          String.format("city lookup took %d msecs and returned %d results",
              duration, cities.size()));
    return cities;  
  }
  
  /**
   * lookup the given location
   * @param name
   * @return the location
   */
  public UnLocode lookup(String name) {
    UnLocode code=this.unLocodeByName.get(name);
    return code;
  }

}
