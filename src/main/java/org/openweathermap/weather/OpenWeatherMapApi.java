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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.bitplan.util.JsonUtil;
import com.google.gson.Gson;

/**
 * base class for open weather map api calls 
 * @author wf
 *
 */
public class OpenWeatherMapApi {
  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("org.openweathermap.weather");
  
  // set to true to debug
  public static boolean debug=false;

  public static final String DEMO_URL="https://samples.openweathermap.org"; // (example mode)
  // base URL of the weather API service 
  protected static String baseurl=DEMO_URL;

  // appid - sample mode
  // you might want to call enableProduction to use the production/non - sample version of the api
  public static final String DEMO_ID="b6907d289e10d714a6e88b30761fae22";
  public static String appid=DEMO_ID;
  
  // units - in sample mode this parameter is empty
  public static String units="";
  
  /**
   * enable production with the given appid
   * @param pAppid
   */
  public static void enableProduction(String pAppid) {
    appid=pAppid;
    baseurl="https://api.openweathermap.org";
    units="&units=metric";
  }
  
  public static void enableDemo() {
    baseurl=DEMO_URL;
    appid=DEMO_ID;
    units="";
  }

  /**
   * get the type of weather API result for the given location
   * @param location - including the id
   * @param type - weather current or history
   * @param params - mostly empty has e.g. type and start and end
   * @param clazz - corresponding org.openweathermap.weather class for which and instance is to be returned
   * e.g. WeatherReport, WeatherForecast or WeatherHistory
   * @return - the result
   */
  public static OpenWeatherMapApi getByLocation(Location location, String type, String params,Class<? extends OpenWeatherMapApi> clazz) {
    long id = location.getId();
    String url = String.format(
        "%s/data/2.5/%s?id=%d&appid=%s%s%s",
        baseurl,type,id, appid,units,params);
    try {
      String json = JsonUtil.read(url);
      if (debug)
        LOGGER.log(Level.INFO, url+"="+json);
      Gson gson = new Gson();
      OpenWeatherMapApi result = gson.fromJson(json, clazz);
      return result;
    } catch (Throwable th) {
      String msg=th.getMessage();
      LOGGER.log(Level.SEVERE, msg, th);
      return null;
    }
  }
}
