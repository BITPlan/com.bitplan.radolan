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
 * Forecast for openweathermap
 * example https://samples.openweathermap.org/data/2.5/forecast?id=524901&appid=b6907d289e10d714a6e88b30761fae22
 * @author wf
 *
 */
public class Forecast {
  public long dt;
  public String dt_txt;
  public Main main;
  public Weather[] weather;
  public Clouds clouds;
  public Snow snow;
  public Rain rain;
  public Wind wind;
  
  /**
   * get the precipitation for this forecast
   * @return the precipitation
   */
  public double getPrecipitation() {
    double result=0.0;
    if (rain!=null)
      result+=rain.mm;
    if (snow!=null)
      result+=snow.mm;
    return result;
  }
}
