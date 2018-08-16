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

import cs.fau.de.since.radolan.Composite;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * Display context
 * 
 * @author wf
 *
 */
public class DisplayContext {
  Composite composite;
  Image image;
  Tooltip toolTip;
  Node view;
  String title;
  String borderName;

  /**
   * create a DisplayContext
   * 
   * @param composite
   * @param borderName
   */
  public DisplayContext(Composite composite, String borderName) {
    this.composite = composite;
    this.borderName = borderName;
  }

  /**
   * get the writeAbleImage (if available)
   * @return the writeAbleImage or null if the image is not writeAble
   */
  public WritableImage getWriteableImage() {
    if (image instanceof WritableImage) {
      return (WritableImage) image;
    } else
      return null;
  }
}
