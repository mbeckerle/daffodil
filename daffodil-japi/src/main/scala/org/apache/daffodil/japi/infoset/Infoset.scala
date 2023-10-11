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

package org.apache.daffodil.japi.infoset

import org.apache.daffodil.lib.exceptions.Assert
import org.apache.daffodil.lib.util.MaybeBoolean
import org.apache.daffodil.runtime1.dpath.NodeInfo
import org.apache.daffodil.runtime1.infoset.InfosetInputterEventType
import org.apache.daffodil.runtime1.infoset.{ InfosetInputter => SInfosetInputter }
import org.apache.daffodil.runtime1.infoset.{ JDOMInfosetInputter => SJDOMInfosetInputter }
import org.apache.daffodil.runtime1.infoset.{ JsonInfosetInputter => SJsonInfosetInputter }
import org.apache.daffodil.runtime1.infoset.{
  ScalaXMLInfosetInputter => SScalaXMLInfosetInputter,
}
import org.apache.daffodil.runtime1.infoset.{ W3CDOMInfosetInputter => SW3CDOMInfosetInputter }
import org.apache.daffodil.runtime1.infoset.{
  XMLTextInfosetInputter => SXMLTextInfosetInputter,
}

/**
 * Abstract class used to determine how the infoset representation should be
 * input from a call to DataProcessor#unparse. This uses a Cursor API, such
 * that each call to advance/inspect must update a cursor value, minimizing
 * allocations. Callers of advance/inspect are expected to copy out any
 * information from advanceAccessor and inspectAccessor if they need to retain
 * the information after a call to advance/inspect.
 **/
abstract class InfosetInputter extends SInfosetInputter {

  /**
   * Return the current infoset inputter event type
   */
  def getEventType(): InfosetInputterEventType

  /**
   * Get the local name of the current event. This will only be called when the
   * current event type is StartElement.
   */
  def getLocalName(): String

  /**
   * Get the namespace of the current event. This will only be called when the
   * current event type is StartElement. If the InfosetInputter does not
   * support namespaces, this shoud return null. This may return null to
   * represent no namespaces.
   */
  def getNamespaceURI(): String

  /**
   * Get the content of a simple type. This will only be called when the
   * current event type is StartElement and the element is a simple type. If
   * the event contains complex data, it is an error and should throw
   * NonTextFoundInSimpleContentException. If the element does not have any
   * simple content, this should return either null or the empty string.
   */
  def getSimpleText(
    primType: NodeInfo.Kind,
    runtimeProperties: java.util.Map[String, String],
  ): String =
    getSimpleText(primType)

  /**
   * See getSimpleText(primType, runtimeProperties), which has a default
   * implementation to call this function without the runtimeProperties Map
   */
  def getSimpleText(primType: NodeInfo.Kind): String

  /**
   * Determine if the current event is nilled. This will only be called when
   * the current event type is StartElement. Return MaybeBoolean.Nope if no
   * nil property is set, which implies the element is not nilled. Return
   * MaybeBoolean(false) if the nil property is set, but it is set to false.
   * Return MaybeBoolean(true) if the nil property is set to true.
   */
  def isNilled(): MaybeBoolean

  /**
   * Return true if there are remaining events. False otherwise.
   */
  def hasNext(): Boolean

  /**
   * Move the internal state to the next event.
   */
  def next(): Unit
}

/**
 * The below classes are all just proxies of the classes implemented in the
 * core daffodil code. The reason for this is purely for documentation. When
 * only generate javadoc on the daffodil-japi subproject. By having this proxy
 * classes, we can document these classes and have a small and clean javadoc.
 *
 * FIXME: Restore these after refactoring is complete.
 */

/**
 * [[InfosetInputter]] to read an infoset represented as a scala.xml.Node
 *
 * @param node the scala.xml.Node infoset
 */
class ScalaXMLInfosetInputter(node: scala.xml.Node) extends InfosetInputterProxy {

  override val infosetInputter = new SScalaXMLInfosetInputter(node)
}

/**
 * [[InfosetInputter]] to read an infoset represented as XML from a java.io.InputStream
 */
class XMLTextInfosetInputter private (inputter: SXMLTextInfosetInputter)
  extends InfosetInputterProxy {

  /**
   * Read in an infoset in the form of XML text from a java.io.InputStream
   *
   * @param is the java.io.InputStream to read the XML text from
   */
  def this(is: java.io.InputStream) = this(new SXMLTextInfosetInputter(is))

  override val infosetInputter = inputter
}

/**
 * [[InfosetInputter]] to read an infoset represented as JSON from a java.io.InputStream
 */
class JsonInfosetInputter private (inputter: SJsonInfosetInputter)
  extends InfosetInputterProxy {

  /**
   * Read in an infoset in the form of json text from a java.io.InputStream
   *
   * @param is the java.io.InputStream to read the json text from
   */
  def this(is: java.io.InputStream) = this(new SJsonInfosetInputter(is))

  override val infosetInputter = inputter
}

/**
 * [[InfosetInputter]] to read an infoset represented as an org.jdom2.Document
 *
 * @param document the org.jdom2.Document infoset
 */
class JDOMInfosetInputter(document: org.jdom2.Document) extends InfosetInputterProxy {

  override val infosetInputter = new SJDOMInfosetInputter(document)
}

/**
 * [[InfosetInputter]] to read an infoset represented as an org.w3c.dom.Document
 *
 * @param document the org.w3c.dom.Document infoset. Note that w3c
 *                 Documents are not guaranteed to be thread-safe, even if all
 *                 users only read/traverse it. It is up to the user to ensure
 *                 that the Document passed into the W3CDOMInfosetInputter is
 *                 not read or written by other threads while the
 *                 W3CDOMInfosetInputter has access to it.
 */
class W3CDOMInfosetInputter(document: org.w3c.dom.Document) extends InfosetInputterProxy {

  override val infosetInputter = new SW3CDOMInfosetInputter(document)
}

/**
 * A proxy for InfosetInputters that are internal to Daffodil
 */
abstract class InfosetInputterProxy extends InfosetInputter {

  /**
   * The InfosetInputter to proxy infoset events to
   */
  protected val infosetInputter: SInfosetInputter

  override def getEventType() = infosetInputter.getEventType()
  override def getLocalName() = infosetInputter.getLocalName()
  override def getNamespaceURI() = infosetInputter.getNamespaceURI()
  override def getSimpleText(
    primType: NodeInfo.Kind,
    runtimeProperties: java.util.Map[String, String],
  ): String = {
    infosetInputter.getSimpleText(primType, runtimeProperties)
  }
  override def getSimpleText(primType: NodeInfo.Kind) = {
    // $COVERAGE-OFF$
    Assert.impossible()
    // $COVERAGE-ON$
  }
  override def hasNext() = infosetInputter.hasNext()
  override def isNilled() = infosetInputter.isNilled()
  override def next() = infosetInputter.next()
  override lazy val supportsNamespaces = infosetInputter.supportsNamespaces

  override def fini = infosetInputter.fini
}

/**
 * A proxy for InfosetOutputters that are internal to Daffodil
 */
//abstract class InfosetOutputterProxy extends InfosetOutputter {
//
//  /**
//   * The InfosetOutputter to proxy infoset events to
//   */
//  protected val infosetOutputter: SInfosetOutputter
//
//  override def reset(): Unit = infosetOutputter.reset()
//  override def startDocument(): Unit = infosetOutputter.startDocument()
//  override def endDocument(): Unit = infosetOutputter.endDocument()
//  override def startSimple(diSimple: DISimple): Unit = infosetOutputter.startSimple(diSimple)
//  override def endSimple(diSimple: DISimple): Unit = infosetOutputter.endSimple(diSimple)
//  override def startComplex(diComplex: DIComplex): Unit =
//    infosetOutputter.startComplex(diComplex)
//  override def endComplex(diComplex: DIComplex): Unit = infosetOutputter.endComplex(diComplex)
//  override def startArray(diArray: DIArray): Unit = infosetOutputter.startArray(diArray)
//  override def endArray(diArray: DIArray): Unit = infosetOutputter.endArray(diArray)
//}
