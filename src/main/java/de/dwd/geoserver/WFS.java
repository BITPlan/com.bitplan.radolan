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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.utils.URIBuilder;
import org.openweathermap.weather.Coord;

import com.bitplan.util.JsonUtil;
import com.google.gson.Gson;

/**
 * Access Deutscher Wetterdienst WFS Service
 * https://maps.dwd.de/geoserver/ows?service=wfs&version=2.0.0&request=GetCapabilities
 * 
 * @author wf
 *
 */
public class WFS {
  public static String version = "2.0.0";
  // set to true to debug
  public static boolean debug = false;
  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("de.dwd.geoserver");

  /**
   * reale Evapotranspiration von Gras über sandigem Lehm (AMBAV) VGSL mm
   * potentielle Evapotranspiration vonGras (AMBAV) VPGB mm potentielle
   * Verdunstung über Gras (Haude) VPGH mm FF: Windgeschwindigkeit an RBSN
   * Stationen RH: Relative Feuchte an RBSN Stationen
   */
  public enum WFSType {
    FF, RH, RR, T2m, VPGB
  };

  /**
   * Json WFS response decoding
   * 
   * @author wf
   *
   */
  public class WFSResponse {
    public String type;
    public int totalFeatures;
    public List<Feature> features;

    /**
     * get the DWD Station closest to the given coordinate
     * 
     * @param coord
     * @return - the closest DWD Station
     */
    public Station getClosestStation(Coord coord) {
      Station station = null;
      if (totalFeatures > 0) {
        Map<Double, Station> distanceMap = new TreeMap<Double, Station>();
        for (Feature feature : features) {
          double distance = feature.geometry.getCoord().distance(coord);
          Station fstation = new Station(feature, distance);
          distanceMap.put(distance, fstation);
        }
        Entry<Double, Station> first = distanceMap.entrySet().iterator().next();
        station = first.getValue();
      }
      return station;
    }
  }

  /**
   * a Feature
   * 
   * @author wf
   *
   */
  public class Feature {
    public String type;
    public String id;
    public Geometry geometry;
    public String geometry_name;
    public Property properties;

    public String toString() {
      String text = String.format("type: %s id: %s, geeometry: %s/%s", type, id,
          geometry.toString(), geometry_name);
      return text;
    }
  }

  public class Geometry {
    public String type;
    public Double[] coordinates;
    transient Coord coord;

    public Coord getCoord() {
      if (coord == null)
        coord = new Coord(coordinates[1], coordinates[0]);
      return coord;
    }

    public String toString() {
      String text = String.format("type: %s coordinates: %s", type,
          getCoord().toString());
      return text;
    }
  }

  static final DateFormat isoDateFormat = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss'Z'");

  // a property
  public class Property {
    String ID;
    String NAME;
    public Double PRECIPITATION;
    public Double EVAPORATION;
    public Double SPEED;
    public Double DIRECTION;
    String M_DATE;
    Double[] bbox;

    public String toString() {
      Coord corner1 = new Coord(bbox[1], bbox[0]);
      Coord corner2 = new Coord(bbox[3], bbox[2]);
      String text = String.format(
          "id: %s name: %s precipitation: %5.1f mm, evaporation: %5.1f mm, mdate: %s, bbox: %s-%s",
          ID, NAME, PRECIPITATION, EVAPORATION, M_DATE, corner1.toString(),
          corner2.toString());
      return text;
    }

    public Date getDate() throws ParseException {
      return isoDateFormat.parse(M_DATE);
    }
  }

  /**
   * get the URI builder for the DWD geo service
   * 
   * @return - the URI builder
   */
  public static URIBuilder getGeoServiceURIBuilder(WFSType wfsType) {
    URIBuilder builder = new URIBuilder();
    builder.setScheme("https");
    builder.setHost("maps.dwd.de");
    builder.setPath("geoserver/dwd/ows");
    builder.addParameter("service", "WFS");
    builder.addParameter("version", version);
    builder.addParameter("request", "GetFeature");
    builder.addParameter("typeName", "dwd:RBSN_" + wfsType.toString()); // _RR =
                                                                        // Niederschlag
                                                                        // _FF=Wind
                                                                        // _VPGB
                                                                        // -
                                                                        // potentielle
                                                                        // Verdunstung
    builder.addParameter("outputFormat", "application/json");
    return builder;
  }

  /**
   * get a response for the given box with the given north west and south east
   * corner
   * 
   * @param wfsType
   * @param nw
   *          - north west corner
   * @param se
   *          - south east corner
   * @return - the response
   * @throws Exception
   */
  public static WFSResponse getResponseForBox(WFSType wfsType, Coord nw,
      Coord se) throws Exception {
    URIBuilder builder = getGeoServiceURIBuilder(wfsType);
    builder.addParameter("bbox",
        String.format(Locale.ENGLISH, "%10.5f,%10.5f,%10.5f,%10.5f",
            nw.getLat(), nw.getLon(), se.getLat(), se.getLon()));
    WFSResponse wfsresponse = fromURIBuilder(builder);
    return wfsresponse;
  }

  /**
   * get the response for a given coordinate and WFSType with a given margin
   * 
   * @param coord
   * @param boxMargin
   *          - degrees
   * @return a WFS Response
   * @throws Exception
   */
  public static WFSResponse getResponseAt(WFSType wfsType, Coord coord,
      double boxMargin) throws Exception {
    Coord nw = new Coord(coord.getLat() - boxMargin,
        coord.getLon() - boxMargin);
    Coord se = new Coord(coord.getLat() + boxMargin,
        coord.getLon() + boxMargin);
    return getResponseForBox(wfsType, nw, se);
  }

  /**
   * get a WFS response for the given builder
   * 
   * @param builder
   * @return the WFS response
   * @throws Exception
   */
  public static WFSResponse fromURIBuilder(URIBuilder builder)
      throws Exception {
    String url = builder.build().toString();
    if (debug)
      LOGGER.log(Level.INFO, url);
    String json = JsonUtil.read(url);
    return fromJson(json);
  }

  /**
   * get a WFSResponse from the given json string
   * 
   * @param json
   * @return the WFSResponse
   * @throws Exception
   */
  public static WFSResponse fromJson(String json) throws Exception {
    if (debug) {
      LOGGER.log(Level.INFO, json.replaceAll("\\{", "\n{"));
    }
    Gson gson = new Gson();
    WFSResponse wfsresponse = gson.fromJson(json, WFSResponse.class);
    return wfsresponse;
  }

  /**
   * get the rain history for the given station
   * 
   * @param dwdStation
   * @return - the WFSResponse containing the rain history
   * @throws Exception
   */
  public static WFSResponse getRainHistory(Station dwdStation)
      throws Exception {
    return getResponseAt(WFSType.RR, dwdStation.coord, 0.01);
  }

  /**
   * get the Evaporation History
   * 
   * @param dwdStation
   * @return the evaporation history
   * @throws Exception
   */
  public static WFSResponse getEvaporationHistory(Station dwdStation)
      throws Exception {
    return getResponseAt(WFSType.VPGB, dwdStation.coord, 0.01);
  }
}
