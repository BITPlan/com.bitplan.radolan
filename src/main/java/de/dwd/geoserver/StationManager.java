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

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

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
    GraphTraversalSource g = graph.traversal();
    GraphTraversal<Vertex, Vertex> stationTraversal = g.V().has("id",station.id);
    Vertex stationVertex;
    if (stationTraversal.hasNext()) {
      stationVertex=stationTraversal.next();
    } else {
      stationVertex= graph.addVertex();
    }
    stationVertex.property("id",station.id);
    stationVertex.property("name",station.name);
  }
  
  public GraphTraversalSource g() {
    return graph.traversal();
  }

  public static void reset() {
    instance=null;
  }
}
