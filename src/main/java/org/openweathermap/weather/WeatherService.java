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

/**
 * access to openweathermap api for a given location
 * @author wf
 *
 */
public class WeatherService {
  Location location;
  
  /**
   * construct me for the given location
   * @param location
   */
  public WeatherService(Location location) {
    this.location=location;
  }

  /**
   * get my location
   * @return my location
   */
  public Location getLocation() {
    return location;
  }
  
  /**
   * get a weather report for my location
   * @return the weather report for my location
   */
  public WeatherReport getWeatherReport() {
    return WeatherReport.getByLocation(location);
  }
  
  /**
   * get a weather forecast for my location
   * @return the weather forecast for my location
   */
  public WeatherForecast getWeatherForecast() {
    return WeatherForecast.getByLocation(location);
  }

  /**
   * get a weather history for my location
   * @return the weather history for my location
   */
  public WeatherHistory getWeatherHistory() {
    return WeatherHistory.getByLocation(location);
  }

}
