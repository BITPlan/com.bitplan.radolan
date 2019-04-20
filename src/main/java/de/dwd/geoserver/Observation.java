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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import de.dwd.geoserver.WFS.Feature;
import de.dwd.geoserver.WFS.Property;
import de.dwd.geoserver.WFS.WFSResponse;

/**
 * a weather observation
 * @author wf
 *
 */
public class Observation {

  public static boolean debug=true;
  static DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  
  Station station; // the station this Observation was made
  Date date; // date time of the observation
  String name;  // the name of the observation
  Double value; // the value of the observation
  
  /**
   * get the Observations for the given station manager and wfs type
   * @param sm
   * @param wfsType
   * @throws Exception 
   */
  public static void getObservations(StationManager sm,WFS.WFSType wfsType) throws Exception {
    WFSResponse wfsResponse=WFS.getResponseForBox(wfsType, sm.getNorthWest(), sm.getSouthEast());
    for (Feature feature:wfsResponse.features) {
      Observation observation=new Observation();
      Property props = feature.properties;
      if (debug)
        System.out.println(props.toString());
      Station station=sm.byId(props.ID);
      observation.station=station;
      observation.date=isoDateFormat.parse(props.M_DATE);
      if (props.EVAPORATION!=null) {
        observation.name="evaporation";
        observation.value=props.EVAPORATION;
      }
      sm.add(observation);
    }
      
  }

  /**
   * add the given vertex
   * @param oVertex
   */
  public void toVertex(Vertex oVertex) {
    oVertex.property("name",name);
    oVertex.property("value",value);
    oVertex.property("date",date);
  }
  
  public String toString() {
    String text=String.format("%s: %s=%5.1f", isoDateFormat.format(date),name,value);
    return text;
  }
}