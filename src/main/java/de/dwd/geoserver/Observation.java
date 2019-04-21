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

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import de.dwd.geoserver.WFS.Feature;
import de.dwd.geoserver.WFS.Property;
import de.dwd.geoserver.WFS.WFSResponse;

/**
 * a weather observation
 * 
 * @author wf
 *
 */
public class Observation {

  public static boolean debug = true;
  static DateFormat isoDateFormat = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss'Z'");
  static DateFormat shortIsoDateFormat = new SimpleDateFormat(
      "yyyy-MM-dd");
  static DateFormat defaultDateFormatter =new SimpleDateFormat(
      "EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

  private Station station; // the station this Observation was made e.g. 1078/Düsseldorf
  private String stationid; // e.g. 1078 - this is redundant but simpler
  String date; // date  of the observation as yyyy-MM-DD eg. 2019-04-20
  String name; // the name of the observation e.g. evaporation
  Double value; // the value of the observation e.g. 5.2

  public String getStationid() {
    return stationid;
  }

  public void setStationid(String stationid) {
    this.stationid = stationid;
  }

  public Station getStation() {
    return station;
  }

  public void setStation(Station station) {
    this.station = station;
  }

  /**
   * get the Observations for the given station manager and WFSType
   * 
   * @param sm
   *          - the station manager to be used
   * @param wfsType
   * @throws Exception
   */
  public static void getObservations(StationManager sm, WFS.WFSType wfsType)
      throws Exception {
    WFSResponse wfsResponse = WFS.getResponseForBox(wfsType, sm.getNorthWest(),
        sm.getSouthEast());
    getObservations(sm, wfsResponse);
  }

  /**
   * get observations from the given station manager and WFSResponse
   * 
   * @param sm
   * @param wfsResponse
   * @throws Exception
   */
  public static void getObservations(StationManager sm, WFSResponse wfsResponse)
      throws Exception {
    for (Feature feature : wfsResponse.features) {
      Observation observation = new Observation();
      Property props = feature.properties;
      if (debug)
        System.out.println(props.toString());
      observation.setStationid(props.ID);
      Station station = sm.byId(observation.getStationid());
      observation.setStation(station);
      Date observation_date = isoDateFormat.parse(props.M_DATE);
      observation.date=shortIsoDateFormat.format(observation_date);
      if (props.EVAPORATION != null) {
        observation.name = "evaporation";
        observation.value = props.EVAPORATION;
      }
      sm.add(observation);
    }
  }
  
  /**
   * get observations from the given stationManager and json files in the given jsonPath
   * @param sm
   * @param jsonPath
   * @throws Exception
   */
  public static void getObservations(StationManager sm,File jsonPath) throws Exception {
    File[] jsonFiles = jsonPath.listFiles(new FileFilter() {

      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".json");
      }});
    for (File jsonFile:jsonFiles) {
      String json=FileUtils.readFileToString(jsonFile, "UTF-8");
      WFSResponse wfsResponse=WFS.fromJson(json);
      getObservations(sm,wfsResponse);
    }
  }

  /**
   * add the given vertex
   * 
   * @param oVertex
   */
  public void toVertex(Vertex oVertex) {
    oVertex.property("name", name);
    oVertex.property("value", value);
    oVertex.property("date", date);
    if (getStation()!=null) {
      oVertex.property("stationid",getStation().id);
    }
  }
  
  /**
   * get an Observation from the given vertex
   * @param oVertex
   * @return the Observation
   * @throws Exception
   */
  public static Observation from(Vertex oVertex) throws Exception {
    Observation o=new Observation();
    o.name=oVertex.property("name").value().toString();
    o.value=(Double) oVertex.property("value").value();
    o.date=oVertex.property("date").value().toString();
    return o;
  }

  /**
   * convert me to a human readable string
   */
  public String toString() {
    String text = String.format("%s: %s -> %s=%5.1f", getStation()!=null?getStation().name:getStationid(),date,
        name, value);
    return text;
  }
}
