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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.openweathermap.weather.Coord;

/**
 * handle Stations
 * 
 * @author wf
 *
 */
public class StationManager {
  public static boolean debug = false;
  public static StationManager instance;
  private TinkerGraph graph;

  private Map<String, Station> stationsById = new HashMap<String, Station>();
  public static final Coord Germany_SouthEast = new Coord(55.0, 15.1);
  public static final Coord Germany_NorthWest = new Coord(47.3, 5.9);
  public static String STORE_MODE = IO.graphml;
  public static String STORE_EXTENSION = ".xml";

  public TinkerGraph getGraph() {
    return graph;
  }

  public void setGraph(TinkerGraph graph) {
    this.graph = graph;
  }

  /**
   * get the GraphFile
   * @return - the GraphFile
   */
  public static File getGraphFile() {
    String graphFilePath = System.getProperty("user.home")
        + java.io.File.separator + ".radolan/stations" + STORE_EXTENSION;
    File graphFile = new File(graphFilePath);
    return graphFile;
  }
  
  /**
   * the constructor
   */
  protected StationManager() {
    setGraph(TinkerGraph.open());
    File graphFile=getGraphFile();
    if (graphFile.exists()) {
      read(graphFile);
    }
  }

  /**
   * read my data from the given graphFile
   * 
   * @param graphFile
   */
  public void read(File graphFile) {
    // http://tinkerpop.apache.org/docs/3.4.0/reference/#io-step
    getGraph().traversal().io(graphFile.getPath()).with(IO.reader, STORE_MODE).read()
        .iterate();
  }

  /**
   * write my data to the given graphFile
   * 
   * @param graphFile
   */
  public void write(File graphFile) {
    // http://tinkerpop.apache.org/docs/3.4.0/reference/#io-step
    getGraph().traversal().io(graphFile.getPath()).with(IO.writer, STORE_MODE)
        .write().iterate();
  }

  public void write() {
    File graphFile=getGraphFile();
    if (!graphFile.getParentFile().exists()) {
      graphFile.getParentFile().mkdirs();
    }
    write(graphFile);
  }

  /**
   * get the instance of the StationManager
   * 
   * @return - the instance
   */
  public static StationManager getInstance() {
    if (instance == null) {
      instance = new StationManager();
    }
    return instance;
  }

  /**
   * add the given observation to the graph
   * 
   * @param observation
   */
  public void add(Observation observation) {
    GraphTraversal<Vertex, Vertex> stationTraversal = g().V()
        .hasLabel("observation").has("stationid", observation.getStationid())
        .has("date", observation.date).has("name", observation.name);
    if (stationTraversal.hasNext()) {
      if (debug)
        System.out.println(observation.toString() + " already exists");
    } else {
      Vertex stationVertex = this
          .getStationVertexById(observation.getStationid());
      addDirect(observation, stationVertex);
    }
  }

  public void addDirect(Observation observation, Vertex stationVertex) {
    Vertex oVertex = getGraph().addVertex("observation");
    observation.toVertex(oVertex);
    stationVertex.addEdge("has", oVertex);
  }

  /**
   * add a station to the graph
   * 
   * @param station
   */
  public void add(Station station) {
    Vertex stationVertex = this.getStationVertexById(station.id);
    station.toVertex(stationVertex);
    stationsById.put(station.id, station);
  }

  public void initStationMap() {
    g().V().hasLabel("station").forEachRemaining(v -> {
      Station s = new Station();
      s.fromVertex(v);
      stationsById.put(s.id, s);
    });
  }

  /**
   * get the Station vertex for the given station id
   * 
   * @param stationid
   * @return the Station Vertex
   */
  public Vertex getStationVertexById(String stationid) {
    GraphTraversal<Vertex, Vertex> stationTraversal = g().V()
        .hasLabel("station").has("stationid", stationid);
    Vertex stationVertex;
    if (stationTraversal.hasNext()) {
      stationVertex = stationTraversal.next();
    } else {
      stationVertex = getGraph().addVertex("station");
    }
    return stationVertex;
  }

  /**
   * get the station by it's id
   * 
   * @param id
   * @return the Station
   */
  public Station byId(String id) {
    Station station = new Station();
    station.id = id;
    Vertex stationVertex = this.getStationVertexById(station.id);
    station.fromVertex(stationVertex);
    return station;
  }

  public GraphTraversalSource g() {
    return getGraph().traversal();
  }

  /**
   * initialize the station manager
   * 
   * @return the initialized station manager
   * @throws Exception
   */
  public static StationManager init() throws Exception {
    Map<String, Station> stations = Station.getAllSoilStations(true);
    StationManager sm = StationManager.getInstance();
    for (Station station : stations.values()) {
      sm.add(station);
    }
    sm.write();
    return sm;
  }

  public Coord getSouthEast() {
    return Germany_SouthEast;
  }

  public Coord getNorthWest() {
    return Germany_NorthWest;
  }

  public static void reset() {
    instance = null;
  }

  public int size() {
    return this.stationsById.size();
  }

  public Collection<String> getIds() {
    return stationsById.keySet();
  }

  public Map<String, Station> getStationMap() {
    return this.stationsById;
  }

  /**
   * get a list of stations that are with the given radius
   * 
   * @param c
   * @param radius
   * @return the list of stations
   */
  public List<Station> getStationsWithinRadius(Coord c, double radius) {
    List<Station> stations = new ArrayList<Station>();
    for (Station station : getStationMap().values()) {
      Coord sc = station.getCoord();
      double dist = sc.distance(c);
      if (dist < radius) {
        station.setDistance(dist);
        stations.add(station);
      }
    }
    return stations;
  }

  public static void setStoreMode(String mode) {
    STORE_MODE = mode;
    switch (mode) {
    case IO.graphml:
      STORE_EXTENSION = ".xml";
      break;
    case IO.graphson:
      STORE_EXTENSION = ".json";
      break;
    case IO.gryo:
      STORE_EXTENSION = ".gryo";
      break;
    }
  }

  /**
   * get the evaporation cache
   * @return the new state of the StationManager instance;
   * @throws Exception 
   */
  public static StationManager refreshEvaporationCache() throws Exception {
    reset();
    File graphFile=getGraphFile();
    if (graphFile.exists()) {
      // TODO create backup ?
      graphFile.delete();
    }
    StationManager sm = StationManager.getInstance();
    boolean useCache=false;
    Map<String, Station> smap = Station.getAllSoilStations(useCache);
    for (Station station : smap.values()) {
      sm.add(station);
    }
    Observation.getObservations(sm, useCache);
    sm.write();
    return sm;
  }
  
  /**
   * get the inverse weighted evaporation value for the given coordinate and
   * list of stations
   * 
   * @param c
   *          - the coordinate
   * @param stations
   * @param power
   *          - power adjustment
   * @return - the inverse weighted interpolated value
   */
  public double getInverseWeighted(Coord c, List<Station> stations,
      double power,Function<Station,Double> getValue) {
    double weightSum = 0.;
    double evapSum = 0.;
    for (Station station : stations) {
      double dist = c.distance(station.getCoord());
      Double evap=getValue.apply(station);
      double weight;
      double d = Math.pow(dist, power);
      d = Math.sqrt(d);
      if (d > 0.)
        weight = 1 / d;
      else
        weight = 1.e20; // big value to avoid divison by zero
      if (evap != null) {
        if (debug) {
          station.setDistance(dist);
          System.out.println(String.format("%5.3f %5.1f mm %s", weight,
              evap, station.toString()));
        }
        weightSum += weight;
        evapSum += evap * weight;
      }
    }
    if (debug)
      System.out.println(String.format("%5.3f %5.1f mm %5.1f mm", weightSum,
          evapSum, evapSum / weightSum));
    return evapSum / weightSum;
  }

}
