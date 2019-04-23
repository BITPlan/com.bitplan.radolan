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
package de.dwd.geoserver;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openweathermap.weather.Coord;

import com.bitplan.util.CachedUrl;

import de.dwd.geoserver.WFS.Feature;
import de.dwd.geoserver.WFS.WFSResponse;
import de.dwd.geoserver.WFS.WFSType;

/**
 * a weather station (e.g. of Deutscher Wetterdienst)
 * 
 * @author wf
 *
 */
public class Station {
  public static boolean debug=false;
  
  String name;
  public String id;
  Coord coord;
  private Double distance;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Double getDistance() {
    return distance;
  }

  public void setDistance(Double distance) {
    this.distance = distance;
  }

  public Coord getCoord() {
    return coord;
  }

  public void setCoord(Coord coord) {
    this.coord = coord;
  }

  /**
   * Construct a station of Deutscher Wetterdienst
   * 
   * @param id
   * @param name
   * @param coord
   * @param distance
   */
  public Station(String id, String name, Coord coord, double distance) {
    this.id = id;
    this.name = name;
    this.coord = coord;
    this.setDistance(distance);
  }

  /**
   * construct me from a feature with the given distance
   * 
   * @param feature
   * @param distance
   */
  public Station(Feature feature, double distance) {
    this(feature.properties.ID, feature.properties.NAME,
        feature.geometry.getCoord(), distance);
  }

  /**
   * construct me from a feature
   * 
   * @param feature
   */
  public Station(Feature feature) {
    this(feature, 0.0);
  }

  /**
   * default constructor
   */
  public Station() {
  }

  public String toString() {
    String text = String.format(Locale.GERMAN, "%s(%s) - %.1f km %s", name, id,
        getDistance(), coord.toString());
    return text;
  }

  /**
   * get a map of all DWD Stations
   * 
   * @return the map of DWD Stations by ID
   * @throws Exception
   */
  public static Map<String, Station> getAllStations() throws Exception {
    Map<String, Station> stations = new HashMap<String, Station>();
    Coord nw = new Coord(47.3, 5.9);
    Coord se = new Coord(55.0, 15.1);
    WFSResponse wfsresponse = WFS.getResponseForBox(WFSType.VPGB, nw, se);
    for (Feature feature : wfsresponse.features) {
      if (!stations.containsKey(feature.properties.ID)) {
        Station station = new Station(feature);
        stations.put(station.id, station);
      }
    }
    return stations;
  }
  
  /**
   * get all stations from the CDC soil area
   * @return the map of stations
   * @throws Exception
   */
  public static Map<String, Station> getAllSoilStations() throws Exception {
    Map<String, Station> stations = new HashMap<String, Station>();
    boolean useCache=true;
    String url="ftp://ftp-cdc.dwd.de/pub/CDC/derived_germany/soil/daily/recent/derived_germany_soil_daily_recent_stations_list.txt";
    String csv = CachedUrl.readString(url, useCache,"ISO-8859-1");
    StringReader csvReader = new StringReader(csv);
    CSVParser parser = new CSVParser(csvReader, CSVFormat.newFormat(';').withHeader());
    for (final CSVRecord record : parser) {
      if (debug)
        System.out.println(record.toString());
      Station station=new Station();
      station.id=record.get("Stationsindex");
      station.name=record.get(4).trim();
      stations.put(station.id, station);
      double lat=Double.parseDouble(record.get(2).trim());
      double lon=Double.parseDouble(record.get(3).trim());
      station.coord=new Coord(lat,lon);
    }
    parser.close();
 
    return stations;
  }

  /**
   * get the given station from the given station Vertex
   * 
   * @param station
   * @param stationVertex
   */
  public void fromVertex(Vertex stationVertex) {
    id = (String) stationVertex.property("stationid").value();
    name = (String) stationVertex.property("name").value();
    if ((stationVertex.property("lat").isPresent())
        && (stationVertex.property("lon").isPresent())) {
      double lat = (double) stationVertex.property("lat").value();
      double lon = (double) stationVertex.property("lon").value();
      coord = new Coord(lat, lon);
    }
  }

  public String getShortName() {
    String shortName=this.name.replaceAll("-.*","");
    shortName=shortName.replaceAll("/.*","");
    shortName=shortName.replaceAll("\\(.*","");
    shortName=shortName.replaceAll("\\,.*","");
    return shortName;
  }

}