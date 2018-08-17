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

import static org.junit.Assert.fail;

import java.util.logging.Level;

/**
 * go migration Testing helper class
 * 
 * @author wf
 *         see e.g. https://golang.org/pkg/fmt/
 */
public class Testing extends BaseTest {


  /**
   * testing.T.Errorf surrogate
   * 
   * @param format
   * @param params
   */
  public void Errorf(String format, Object... params) {
    String msg = String.format(format, params);
    fail(msg);
  }
  
  /**
   * testing.T.Fatalf surrogate
   * @param format
   * @param params
   */
  public void Fatalf(String format, Object... params) {
    Errorf(format,params);
  }

  /**
   * testing.T.Log surrogate
   * 
   * @param format
   * @param params
   */
  public void Logf(String format, Object... params) {
    String msg = String.format(format, params);
    if (debug)
      LOGGER.log(Level.INFO, msg);
  }

}
