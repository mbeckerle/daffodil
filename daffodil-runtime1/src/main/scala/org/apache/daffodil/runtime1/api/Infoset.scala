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

package org.apache.daffodil.runtime1.api

import org.apache.daffodil.lib.calendar.DFDLCalendar
import org.apache.daffodil.lib.calendar.DFDLDate
import org.apache.daffodil.lib.calendar.DFDLDateTime
import org.apache.daffodil.lib.calendar.DFDLTime

import java.lang.{Boolean => JBoolean}
import java.lang.{Byte => JByte}
import java.lang.{Double => JDouble}
import java.lang.{Float => JFloat}
import java.lang.{Integer => JInt}
import java.lang.{Long => JLong}
import java.lang.{Number => JNumber}
import java.lang.{Short => JShort}
import java.lang.{String => JString}
import java.math.{BigDecimal => JBigDecimal}
import java.math.{BigInteger => JBigInt}
import java.net.URI

/**
 * API access to array objects in the DFDL Infoset
 */
trait InfosetArray {
  def getOccurrence(occursIndex: Long): InfosetElement
  def length: Long
  final def apply(occursIndex: Long): InfosetElement = getOccurrence(occursIndex)
}

/**
 * API access to elements of the DFDL Infoset
 */
trait InfosetElement extends InfosetItem {
  def parent: InfosetComplexElement
  def isNilled: Boolean
  def isEmpty: Boolean
  def asSimple: InfosetSimpleElement
  def asComplex: InfosetComplexElement

  /*
   * For API users to avoid the need to access RuntimeData
   */
  def metadata: ElementMetadata
  def name = metadata.name
  def namespace = metadata.namespace
}

trait InfosetComplexElement extends InfosetElement {
  override def metadata: ComplexElementMetadata
}

trait InfosetSimpleElement extends InfosetElement {

  override def metadata: SimpleElementMetadata

  /**
   * Caches the string so we're not allocating strings repeatedly
   */
  protected def dataValueAsString: String

  /*
   * These are so that API users don't have to know about our
   * very Scala-oriented DataValue type system.
   */
  def getAnyRef: AnyRef
  def getBigDecimal: JBigDecimal
  def getCalendar: DFDLCalendar
  def getDate: DFDLDate
  def getTime: DFDLTime
  def getDateTime: DFDLDateTime
  def getByteArray: Array[Byte]
  def getBoolean: JBoolean
  def getNumber: JNumber
  def getByte: JByte
  def getShort: JShort
  def getInt: JInt
  def getLong: JLong
  def getDouble: JDouble
  def getFloat: JFloat
  def getBigInt: JBigInt
  def getString: JString
  def getURI: URI
}

trait InfosetDocument extends InfosetItem {}

trait InfosetItem {

  /**
   * The totalElementCount is the total count of how many elements this InfosetItem contains.
   *
   * (Used to call this 'size', but size is often a length-like thing, so changed name
   * to be more distinctive)
   */
  def totalElementCount: Long
}
