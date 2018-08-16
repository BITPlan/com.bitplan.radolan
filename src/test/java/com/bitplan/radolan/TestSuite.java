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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cs.fau.de.since.radolan.TestConversion;
import cs.fau.de.since.radolan.TestData;
import cs.fau.de.since.radolan.TestHeader;
import cs.fau.de.since.radolan.TestRadolan;
import cs.fau.de.since.radolan.TestTranslate;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestUnLocodeManager.class,TestTranslate.class, TestConversion.class,TestData.class,TestHeader.class,TestRadolan.class })
/**
 * TestSuite
 * 
 * @author wf
 *
 *         no content necessary - annotation has info
 */
public class TestSuite {
  public static boolean debug = false;
}