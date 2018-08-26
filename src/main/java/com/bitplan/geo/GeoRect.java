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
package com.bitplan.geo;

/**
 * a Rectangle defined by two edges
 * @see <a href='https://github.com/locationtech/spatial4j/blob/master/src/main/java/org/locationtech/spatial4j/shape/Rectangle.java'> Spatjal4j Rectangle</a>
 * @see <a href='https://lucene.apache.org/core/6_0_0/spatial/org/apache/lucene/spatial/util/GeoRect.html'>Lucene Spatial GeoRect</a>
 * @author wf
 *
 */
public interface GeoRect {
  DPoint getTopLeft();
  DPoint getBottomRight();
}
