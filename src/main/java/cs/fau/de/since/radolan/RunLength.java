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
 * migrated to Java from https://gitlab.cs.fau.de/since/radolan/blob/master/runlength.go
 * @author wf
 *
 */
public class RunLength {
  
//parseRunlength parses the runlength encoded composite and writes into the
//previously created PlainData field of the composite.
public static void  parseRunlength(Composite c)  {
 /*for i := range c.PlainData {
   line, err := c.readLineRunlength(reader)
   if err != nil {
     return err
   }

   err = c.decodeRunlength(c.PlainData[i], line)
   if err != nil {
     return err
   }
 }

 return nil*/
}
/*
//readLineRunlength reads a line until newline (non inclusive) from the given reader.
//This method is used to get a line of runlenth encoded data.
func (c *Composite) readLineRunlength(rd *bufio.Reader) (line []byte, err error) {
 line, err = rd.ReadBytes('\x0A')
 if err != nil {
   err = newError("readLineRunlength", err.Error())
 }
 length := len(line)
 if length > 0 {
   line = line[:length-1]
 }
 return
}

//decodeRunlength decodes the source line and writes to the given destination.
func (c *Composite) decodeRunlength(dst []float32, line []byte) error {
 // fill destination as runlength encoding will induce gaps
 for i := range dst {
   dst[i] = NaN
 }

 dstpos := 0
 offset := true
 for i, value := range line {
   switch true {
   case i == 0: // skip useless line number
   case offset: // calculate offset
     if value < 16 {
       return newError("decodeRunlength", "invalid offset value")
     }

     dstpos += int(value) - 16 // update offset position
     offset = value == 255     // see if next byte will be also offset
   default:
     // value [XXXX|YYYY] decodes to YYYY repeated XXXX times.
     runlength := int(value >> 4)
     value &= 0x0F

     for j := 0; j < runlength; j++ {
       if dstpos >= len(dst) {
         return newError("decodeRunlength", "destination size exceeded")
       }

       dst[dstpos] = c.rvp6Runlength(value)
       dstpos++
     }
   }
 }

 return nil
}

//rvp6Runlength sets the value of level based composite products to radar
//video processor values (rvp-6).
func (c *Composite) rvp6Runlength(value byte) float32 {
 if value == 0 {
   return NaN
 }
 value--

 if int(value) >= len(c.level) { // border markings
   return NaN
 }
 return c.level[value]
}
*/
}
