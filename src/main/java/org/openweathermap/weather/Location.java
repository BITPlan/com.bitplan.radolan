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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.bitplan.util.JsonUtil;
import com.google.gson.Gson;

/**
 * https://github.com/BITPlan/com.bitplan.sprinkler/issues/1
 * @author wf
 *
 */
public class Location {
  public static boolean debug=false;
  public static String url="http://bulk.openweathermap.org/sample/city.list.json.gz";
  protected static Map<String,Location> locationsByNameMap=new HashMap<String,Location>();
  protected static Map<Long,Location> locationsByIdMap=new HashMap<Long,Location>();
  protected static Location[] locations;
  
  Long id;
  String name;
  String country;
  Coord coord;

  public Long getId() {
    return id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  
  public String getCountry() {
    return country;
  }
  public void setCountry(String country) {
    this.country = country;
  }
  public Coord getCoord() {
    return coord;
  }
  public void setCoord(Coord coord) {
    this.coord = coord;
  }
  /**
   * get the locations
   * @return the locations
   * @throws MalformedURLException
   * @throws IOException
   */
  public static Location[] getLocations() throws MalformedURLException, IOException {
    if (locations==null) {
      String json=JsonUtil.readGZIP(url);
      if (debug)
        System.out.println(json.substring(0, 280));
      Gson gson=new Gson();
      locations=gson.fromJson(json,Location[].class);
    }
    return locations;
  }
  
  /**
   * get the locations by name map
   * @return the map of locations by name
   * @throws Exception
   */
  public static Map<String,Location> getLocationsByName() throws Exception {
    if (locationsByNameMap.size()==0) {
      for (Location location:getLocations()) {
        String key=location.getName()+"/"+location.country;
        //if (locationsByNameMap.containsKey(key))
        //  System.err.println("Duplicate location key "+key);
        locationsByNameMap.put(key, location);
      }
    }
    return locationsByNameMap;
  }
  
  /**
   * get a location by name
   * @param key - the key (name/country) to lookup
   * @return the location
   * @throws Exception 
   */
  public static Location byName(String key) throws Exception {
    Location location=getLocationsByName().get(key);
    return location;
  }
  
  /**
   * get the locations by Id map
   * @return the map of locations by Id
   * @throws Exception
   */
  public static Map<Long,Location> getLocationsById() throws Exception {
    if (locationsByIdMap.size()==0) {
      for (Location location:getLocations()) {
        locationsByIdMap.put(location.getId(), location);
      }
    }
    return locationsByIdMap;
  }
  
  /**
   * get a location by Id
   * @param Id - the id to look for
   * @return the location
   * @throws Exception 
   */
  public static Location byId(long Id) throws Exception {
    Location location=getLocationsById().get(Id);
    return location;
  }
  
  public String toString() {
    String text=String.format("%s/%s%s", name,country,coord==null?"":":"+coord.toString());
    return text;
  }
}
