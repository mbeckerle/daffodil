/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.daffodil.layers.runtime1

import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

import org.apache.daffodil.io.BoundaryMarkInsertingJavaOutputStream
import org.apache.daffodil.io.BoundaryMarkLimitingInputStream
import org.apache.daffodil.lib.exceptions.Assert
import org.apache.daffodil.runtime1.layers.api.Layer

/**
 * A layer which isolates text data by way of a boundary mark string.
 * This is like a terminating delimiter of a DFDL element or sequence/choice group,
 * except that there is no escaping mechanism, so the data cannot in any way
 * contain the boundary mark string, and elements within the layer can have
 * any dfdl:lengthKind, but this does not affect the search for the boundary mark.
 *
 * For example, when using an element with dfdl:lengthKind="delimited" and a dfdl:terminator="END",
 * if that element contains a child element with dfdl:lengthKind="explicit", then the
 * search for the "END" terminator is suspended for the length of the child element, and
 * that search resumes after the child element's length has been parsed.
 *
 * In contrast to this, if a boundary mark layer is used with the boundaryMark variable
 * bound to "END", then the data stream is decoded as characters in the charset encoding
 * given by the layerEncoding variable, and the layer continues until the "END" is found.
 * The dfdl:lengthKind of any child element enclosed within the layer, or even the lengths
 * of other layers found within the scope of this boundary mark layer are not considered and do not
 * disrupt the search for the boundary mark string.
 *
 * This layer does not populate any DFDL variables with results.
 *
 * This layer defines two DFDL variables which provide required parameters.
 */
final class BoundaryMarkLayer
  extends Layer("boundaryMark", "urn:org.apache.daffodil.layers.boundaryMark") {

  private var boundaryMark: String = _
  private var layerEncoding: String = _

  private val maxBoundaryMarkLength: Int = Short.MaxValue

  /**
   * @param boundaryMark  a string which is the boundary marker. Searching for this string is
   *                      done without any notion of escaping. When parsing the data in the
   *                      layer is up to but not including that of the boundary mark string,
   *                      which is removed from the data-stream on parsing, and inserted into
   *                      the data stream on unparsing.
   * @param layerEncoding a string which is the name of the character set encoding used to
   *                      decode characters during the search for the boundary mark, and used
   *                      to encode characters when inserting the boundary mark when unparsing.
   */
  private[layers] def setLayerVariableParameters(
    boundaryMark: String,
    layerEncoding: String,
  ): Unit = {
    this.boundaryMark = boundaryMark
    if (boundaryMark.isEmpty)
      processingError("The boundaryMark variable value may not be empty string.")
    if (boundaryMark.length > maxBoundaryMarkLength)
      processingError(
        s"The boundaryMark string length may not be greater than the limit: $maxBoundaryMarkLength",
      )
    this.layerEncoding = layerEncoding
  }

  private lazy val charset =
    try {
      Charset.forName(layerEncoding)
    } catch {
      // the runtime exceptions thrown by forName want to be
      // caught and turned into regular Exceptions.
      // That's not generally true, but it is for encodings/charsets.
      case re: RuntimeException => {
        processingError(re) // throw new Exception(re)
        Assert.impossible("unreachable")
      }
    }

  override def wrapLayerInput(jis: InputStream): InputStream = {
    new BoundaryMarkLimitingInputStream(jis, boundaryMark, charset)
  }

  override def wrapLayerOutput(jos: OutputStream): OutputStream =
    new BoundaryMarkInsertingJavaOutputStream(jos, boundaryMark, charset)

}
