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

/**
 * Weather Forecast from openweathermap.org
 * 
 * example:
 * https://samples.openweathermap.org/data/2.5/forecast?id=524901&appid=b6907d289e10d714a6e88b30761fae22
 * 
 * @author wf
 *
 */
public class WeatherForecast extends OpenWeatherMapApi {
  // prepare a LOGGER
  protected static Logger LOGGER = Logger
      .getLogger("org.openweathermap.weather");
  
  /**
   * members of the Weather forecast
   */
  public String message;
  public long cod;
  public int cnt;
  public Location city;
  public Forecast[] list;

  /**
   * get the total rain forecast for the given number of hours
   * 
   * @param hours
   * @return - the total mm of rain/snow
   */
  public double totalPrecipitation(int hours) {
    if (hours > list.length * 3) {
      String msg=String.format(
          "%2d hours is out of range for totalRain calculation max hours for forecast is %2d hours - result is limited",
          hours, list.length * 3);
      LOGGER.log(Level.WARNING,msg);
    }
    double total = 0;
    // loop over the list
    for (Forecast forecast : list) {
      total+=forecast.getPrecipitation();
      hours-=3;
      if (hours<=0)
        break;
    }
    return total;
  }

  /**
   * get a Weather forecast by location
   * 
   * @param location
   * @return - the weather forecast
   */
  public static WeatherForecast getByLocation(Location location) {
    WeatherForecast forecast = (WeatherForecast) OpenWeatherMapApi
        .getByLocation(location, "forecast","", WeatherForecast.class);
    return forecast;
  }
}
