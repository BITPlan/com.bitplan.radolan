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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import cs.fau.de.since.radolan.Composite;

/**
 * handle composites
 * 
 * @author wf
 *
 */
public class CompositeManager {
  Map<LocalDate, Composite> historyMap = new HashMap<LocalDate, Composite>();

  public Composite getRainSum(LocalDate day) throws Throwable {
    Composite comp = historyMap.get(day);
    if (comp == null) {
      LocalDateTime dateTime = day.atStartOfDay().plusMinutes(23*60+50);
      String url = KnownUrl.getUrlForProduct("sf", dateTime);
      comp = new Composite(url);
    }
    return comp;
  }

  private static CompositeManager instance;

  /**
   * get an instance of the composite Manager
   * 
   * @return - the instance
   */
  public static CompositeManager getInstance() {
    if (instance == null)
      instance = new CompositeManager();
    return instance;
  }
}
