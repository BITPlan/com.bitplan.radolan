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

import org.junit.Test;

import cs.fau.de.since.radolan.Composite;

public class TestGraph {

	@Test
	public void testRainEventSequence() throws Throwable {
		String url="ftp://ftp-cdc.dwd.de/pub/CDC/grids_germany/daily/radolan/recent/raa01-sf_10000-1808242350-dwd---bin.gz";
		Composite composite=new Composite(url);
	}
}
