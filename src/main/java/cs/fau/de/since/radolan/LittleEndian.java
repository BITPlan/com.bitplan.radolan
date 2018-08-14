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
package cs.fau.de.since.radolan;

/**
 * migrated to Java from https://gitlab.cs.fau.de/since/radolan/blob/master/littleendian.go
 * @author wf
 *
 */
public class LittleEndian {

  // parseLittleEndian parses the little endian encoded composite as described
  // in [1] and [3].
  // Result are written into the previously created PlainData field of the
  // composite.
  public static void parseLittleEndian(Composite c) {
    /*
     * last := len(c.PlainData) - 1
     * for i := range c.PlainData {
     * line, err := c.readLineLittleEndian(reader)
     * if err != nil {
     * return err
     * }
     * 
     * err = c.decodeLittleEndian(c.PlainData[last-i], line) // write vertically
     * flipped
     * if err != nil {
     * return err
     * }
     * }
     * 
     * return nil
     */
  }
  /*
   * //readLineLittleEndian reads a line until horizontal limit from the given
   * reader
   * //This method is used to get a line of little endian encoded data.
   * func (c *Composite) readLineLittleEndian(rd *bufio.Reader) (line []byte,
   * err error) {
   * line = make([]byte, c.Dx*2)
   * _, err = io.ReadFull(rd, line)
   * if err != nil {
   * err = newError("readLineLittleEndian", err.Error())
   * }
   * return
   * }
   * 
   * //decodeLittleEndian decodes the source line and writes to the given
   * destination.
   * func (c *Composite) decodeLittleEndian(dst []float32, line []byte) error {
   * if len(line)%2 != 0 || len(dst)*2 != len(line) {
   * return newError("decodeLittleEndian", "wrong destination or source size")
   * }
   * 
   * for i := range dst {
   * tuple := [2]byte{line[2*i], line[2*i+1]}
   * dst[i] = c.rvp6LittleEndian(tuple)
   * }
   * 
   * return nil
   * }
   * 
   * //rvp6LittleEndian converts the raw two byte tuple of little endian encoded
   * composite products
   * //to radar video processor values (rvp-6). NaN may be returned when the
   * no-data flag is set.
   * func (c *Composite) rvp6LittleEndian(tuple [2]byte) float32 {
   * var value int = 0x0F & int(tuple[1])
   * value = (value << 8) + int(tuple[0])
   * 
   * if tuple[1]&(1<<5) != 0 { // error code: no-data
   * return NaN
   * }
   * 
   * if tuple[1]&(1<<6) != 0 { // flag: negative value
   * value *= -1
   * }
   * 
   * conv := c.rvp6Raw(value) // set decimal point
   * 
   * // little endian encoded formats are also used for mm/h
   * if c.DataUnit != Unit_dBZ {
   * return conv
   * }
   * 
   * // Even though this format supports negative values and custom
   * // precision they do not make use of this and we still have to subtract
   * // the bias and scale it (RADVOR FX, dBZ)
   * return toDBZ(conv)
   * }
   */
}