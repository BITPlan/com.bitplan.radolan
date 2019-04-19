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
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.openweathermap.weather.Coord;

/**
 * handle Stations
 * @author wf
 *
 */
public class StationManager {

  public static StationManager instance;
  private TinkerGraph graph;
  private File graphFile;
  
  /**
   * the constructor
   */
  protected StationManager() {
    graph = TinkerGraph.open();
    String graphFilePath=System.getProperty("user.home")
    + java.io.File.separator + ".radolan/stations.xml";
    graphFile=new File(graphFilePath);
    if (graphFile.exists()) {
      read(graphFile);
    }
  }
   
  public void read(File graphFile) {
    // http://tinkerpop.apache.org/docs/3.4.0/reference/#io-step
    graph.traversal().io(graphFile.getPath()).with(IO.reader,IO.graphml).read().iterate();   
  }
  
  public void write(File graphFile) {
    // http://tinkerpop.apache.org/docs/3.4.0/reference/#io-step
    graph.traversal().io(graphFile.getPath()).with(IO.writer,IO.graphml).write().iterate();     
  }
  
  public void write() {
    write(graphFile);
  }
  
  /**
   * get the instance of the StationManager
   * @return - the instance
   */
  public static StationManager getInstance() {
    if (instance==null) {
      instance=new StationManager();
    }
    return instance;
  }

  /**
   * add a station to the graph
   * @param station
   */
  public void add(Station station) {
    GraphTraversal<Vertex, Vertex> stationTraversal = g().V().has("id",station.id);
    Vertex stationVertex;
    if (stationTraversal.hasNext()) {
      stationVertex=stationTraversal.next();
    } else {
      stationVertex= graph.addVertex();
    }
    toVertex(station,stationVertex);
  }
  
  /**
   * get the station by it's id
   * @param id
   * @return
   */
  public Station byId(String id) {
    Station station=new Station();
    station.id=id;
    GraphTraversal<Vertex, Vertex> stationTraversal = g().V().has("id",station.id);
    Vertex stationVertex;
    if (stationTraversal.hasNext()) {
      stationVertex=stationTraversal.next();
      fromVertex(station,stationVertex);
    }
    return station;
  }
  
  /**
   * fill the given stationVertex with the given station data
   * @param station
   * @param stationVertex
   */
  private void toVertex(Station station, Vertex stationVertex) {
    stationVertex.property("id",station.id);
    stationVertex.property("name",station.name);
    stationVertex.property("lat",station.coord.getLat());
    stationVertex.property("lon",station.coord.getLon());
  }
  
  /**
   * get the given station from the given station Vertex
   * @param station
   * @param stationVertex
   */
  private void fromVertex(Station station,Vertex stationVertex) {
    station.id=(String) stationVertex.property("id").value();
    station.name=(String) stationVertex.property("name").value();
    double lat=(double) stationVertex.property("lat").value();
    double lon=(double) stationVertex.property("lon").value();
    station.coord=new Coord(lat,lon);
  }

  public GraphTraversalSource g() {
    return graph.traversal();
  }
  
  /**
   * initialize the station manager
   * @return the initialized station manager
   * @throws Exception
   */
  public static StationManager init() throws Exception {
    Map<String, Station> stations = Station.getAllStations();
    StationManager sm = StationManager.getInstance();
    for (Station station:stations.values()) {
      sm.add(station);
    }
    sm.write();
    return sm;
  }

  public static void reset() {
    instance=null;
  }
}
