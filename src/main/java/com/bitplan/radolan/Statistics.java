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
package com.bitplan.radolan;

import java.util.Locale;

/**
 * base statistic for composite data
 * 
 * @author wf
 *
 */
public class Statistics {

  float min;
  float max;
  double sum;
  int count;
  int countNaN;
  private int total;

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public Statistics() {
    clear();
  }

  /**
   * clear the statistics
   */
  public void clear() {
    sum = 0;
    count = 0;
    countNaN=0;
    setTotal(0);
    min = Float.MAX_VALUE;
    max = Float.MIN_VALUE;
  }

  /**
   * get the average
   * 
   * @return the average
   */
  public double getAverage() {
    return sum / count;
  }

  /**
   * add a value to the statistics
   * 
   * @param value
   */
  public void add(float value) {
    setTotal(getTotal() + 1);
    if (Float.isNaN(value)) {
      countNaN++;
      return;
    }
    count++;
    sum += value;
    if (value < min)
      min = value;
    if (value > max)
      max = value;
  }

  public String toString() {
    String text = String.format(Locale.ENGLISH,
        "min: %.1f max: %5.1f avg:%4.1f NaN: %7d count: %7d total: %7d", min, max,
        getAverage(), countNaN,count,count+countNaN);
    return text;
  }
}
