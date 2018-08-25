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
import java.time.Month;

/**
 * handle known urls
 * 
 * @author wf
 *
 */
public class KnownUrl {
	public static int OPEN_DATA = 0;
	public static int GRIDS = 1;
	public static String knownUrls[] = { "https://opendata.dwd.de/weather/radar/radolan/",
			"ftp://ftp-cdc.dwd.de/pub/CDC/grids_germany/daily/radolan" };
	public static final String RADOLAN_HISTORY = knownUrls[GRIDS];

	/**
	 * get the recent SF data url for the given local date
	 * @param date
	 * @return - the url
	 */
	public static String getSFRecent(LocalDate date) {
		return getSFRecent(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
	}

	/**
	 * get the recent SF data url for the given parameters
	 * @param year
	 * @param month
	 * @param dayOfMonth
	 * @return - the url
	 */
	private static String getSFRecent(int year, int month, int dayOfMonth) {
		String url = String.format(RADOLAN_HISTORY + "recent/raa01-sf_10000-%02d%02d%02d1650-dwd---bin.gz", year % 2000,
				month, dayOfMonth);
		return url;
	}

}
