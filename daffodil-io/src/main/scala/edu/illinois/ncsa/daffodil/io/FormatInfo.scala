/*
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
 */

package edu.illinois.ncsa.daffodil.io

import java.nio.charset.CharsetDecoder
import java.nio.charset.CharsetEncoder
import edu.illinois.ncsa.daffodil.schema.annotation.props.gen.BitOrder
import edu.illinois.ncsa.daffodil.schema.annotation.props.gen.ByteOrder
import edu.illinois.ncsa.daffodil.schema.annotation.props.gen.BinaryFloatRep
import edu.illinois.ncsa.daffodil.util.MaybeInt
import edu.illinois.ncsa.daffodil.schema.annotation.props.gen.EncodingErrorPolicy
import edu.illinois.ncsa.daffodil.util.Maybe
import edu.illinois.ncsa.daffodil.schema.annotation.props.gen.UTF16Width

/**
 * Abstract interface to obtain format properties or values derived from
 * properties.
 *
 * This includes anything the I/O layer needs, which includes properties that
 * can be runtime-valued expressions, or that depend on such.
 *
 * By passing in an object that provides quick access to these, we avoid the
 * need to have setters/getters that call setters that change state in the I/O layer.
 */
trait FormatInfo {

  /**
   * Returns a charset encoder for this encoding configured for the
   * `dfdl:encodingErrorPolicy`. This is the same as either the `reportingEncoder`
   * or the `replacingEncoder`.
   */
  def encoder: CharsetEncoder

  /**
   * Returns a charset decoder for this encoding configured for the
   * `dfdl:encodingErrorPolicy`. This is the same as either the `reportingDecoder`
   * or the `replacingDecoder`.
   */
  def decoder: CharsetDecoder

  /**
   * Returns a decoder configured for `dfdl:encodingErrorPolicy="report"`
   * regardless of the value of that property.
   */
  def reportingDecoder: CharsetDecoder

  /**
   * Returns decoder configured for `dfdl:encodingErrorPolicy="replace"`
   * regardless of the value of that property.
   */
  def replacingDecoder: CharsetDecoder

  /**
   * Returns true if encoding is fixed width meaning has the
   * same number of bits in each character representation.
   */
  final def isFixedWidthEncoding: Boolean = maybeCharWidthInBits.isDefined

  /**
   * Returns ByteOrder
   */
  def byteOrder: ByteOrder

  /**
   * Returns ByteOrder as a java.nio.ByteOrder constant.
   */
  final def jByteOrder = byteOrder match {
    case ByteOrder.BigEndian => java.nio.ByteOrder.BIG_ENDIAN
    case _ => java.nio.ByteOrder.LITTLE_ENDIAN
  }

  /**
   * Returns bit order. If text, this is the bit order for the character set
   * encoding. If binary, this is the bitOrder property value.
   */
  def bitOrder: BitOrder

  /**
   * Returns the fillByte value.
   *
   * Note: This has to be obtained from the FormatInfo
   * because the `dfdl:fillByte` property can be specified as a character,
   * which depends then on the `dfdl:encoding` which can be computed via
   * a runtime expression.
   */
  def fillByte: Byte

  /**
   * Returns the BinaryFloatRep
   */
  def binaryFloatRep: BinaryFloatRep

  /**
   * Returns MaybeInt.Nope for variable-width encodings or no encoding defined.
   * Returns MaybeInt(n) for fixed width encodings.
   */
  def maybeCharWidthInBits: MaybeInt

  /**
   * Returns `Nope` if `dfdl:utf16Width` is not defined.
   * Returns `One(w: UTF16Width)` if it is defined.
   */
  def maybeUTF16Width: Maybe[UTF16Width]

  /**
   * Returns 8 if the `dfdl:encoding` is given by a runtime-valued expression.
   * Returns 8 for all ICU-based encodings.
   * Returns 1 to 7 (inclusive) for encodings 1 to 7 bits wide respectively.
   */
  def encodingMandatoryAlignmentInBits: Int

  /**
   * Provides the `dfdl:encodingErrorPolicy` as an EncodingErrorPolicy enum.
   */
  def encodingErrorPolicy: EncodingErrorPolicy
}
