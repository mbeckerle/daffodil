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

package org.apache.daffodil.core.layers

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import scala.util.Random

import org.apache.daffodil.core.util.TestUtils
import org.apache.daffodil.lib.Implicits.intercept
import org.apache.daffodil.lib.util._
import org.apache.daffodil.lib.xml.XMLUtils
import org.apache.daffodil.runtime1.processors.parsers.ParseError

import org.apache.commons.io.IOUtils
import org.junit.Test

class TestGzipErrors {

  val example = XMLUtils.EXAMPLE_NAMESPACE

  val text =
    """This is just some made up text that is intended to be
a few lines long. If this had been real text, it would not have been quite
so boring to read. Use of famous quotes or song lyrics or anything like that
introduces copyright notice issues, so it is easier to simply make up
a few lines of pointless text like this.""".replace("\r\n", "\n").replace("\n", " ")

  val GZIPLayer1Schema =
    SchemaUtils.dfdlTestSchema(
      <xs:include schemaLocation="/org/apache/daffodil/xsd/DFDLGeneralFormat.dfdl.xsd"/>
          <xs:import namespace="urn:org.apache.daffodil.layers.gzip"
                     schemaLocation="/org/apache/daffodil/layers/xsd/gzipLayer.dfdl.xsd"/>,
      <dfdl:format ref="tns:GeneralFormat" representation="binary"/>,
      <xs:element name="root">
        <xs:complexType>
          <xs:choice>
            <xs:element name="e1" dfdl:lengthKind="implicit"
                        xmlns:gz="urn:org.apache.daffodil.layers.gzip">
              <xs:complexType>
                <xs:sequence>
                  <xs:element name="len" type="xs:int" dfdl:lengthKind="explicit" dfdl:length="4"
                              dfdl:outputValueCalc="{ dfdl:contentLength(../x1/data, 'bytes') }"/>
                  <xs:element name="x1" dfdl:lengthKind="explicit" dfdl:length="{ ../len }">
                    <xs:complexType>
                      <xs:sequence>
                        <xs:element name="data">
                          <xs:complexType>
                            <xs:sequence dfdlx:layer="gz:gzip">
                              <xs:element name="s1" type="xs:string" dfdl:lengthKind="delimited"/>
                            </xs:sequence>
                          </xs:complexType>
                        </xs:element>
                      </xs:sequence>
                    </xs:complexType>
                  </xs:element>
                  <xs:element name="s2" type="xs:string" dfdl:lengthKind="delimited"/>
                </xs:sequence>
              </xs:complexType>
            </xs:element>
            <xs:element name="hex" type="xs:hexBinary" dfdl:lengthKind="delimited" dfdl:encoding="iso-8859-1"/>
          </xs:choice>
        </xs:complexType>
      </xs:element>,
      elementFormDefault = "unqualified",
    )

  def makeGZIPData(text: String) = {
    val baos = new ByteArrayOutputStream()
    val gzos = new java.util.zip.GZIPOutputStream(baos)
    IOUtils.write(text, gzos, StandardCharsets.UTF_8)
    gzos.close()
    val data = baos.toByteArray()
    // Java 16+ sets the 9th byte to 0xFF, but previous Java versions set the
    // value to 0x00. Daffodil always unparses with 0xFF regardless of Java
    // version, so force the gzip data to 0xFF to make sure tests round trip
    data(9) = 0xff.toByte
    data
  }

  def makeGZIPLayer1Data() = {
    val gzipData = makeGZIPData(text)
    val dataLength = gzipData.length
    val baos = new ByteArrayOutputStream()
    val dos = new java.io.DataOutputStream(baos)
    dos.writeInt(dataLength)
    dos.write(gzipData)
    dos.write("afterGzip".getBytes(StandardCharsets.UTF_8))
    dos.close()
    val data = baos.toByteArray()
    (data, dataLength)
  }

  @Test def testGZIPLayerOk1(): Unit = {
    val sch = GZIPLayer1Schema
    val (data, _) = makeGZIPLayer1Data()
    val infoset =
      <ex:root xmlns:ex={example}>
      <e1>
        <len>{212}</len>
        <x1>
        <data>
          <s1>{text}</s1>
        </data>
      </x1>
        <s2>afterGzip</s2>
      </e1>
    </ex:root>
    val (_, actual) = TestUtils.testBinary(sch, data, areTracing = false)
    TestUtils.assertEqualsXMLElements(infoset, actual)
  }

  @Test def testGZIPLayerErr1(): Unit = {
    val sch = GZIPLayer1Schema
    val (data, _) = makeGZIPLayer1Data()
    // clobber half the data
    ((data.length / 2) to data.length - 1).foreach { i => data(i) = 0 }
    // This causes the gzip input stream to throw an IOException
    val infoset =
      <ex:root xmlns:ex={example}>
        <hex xmlns=""><![CDATA[000000D41F8B08000000000000FF4D904176C3200C44AF3207C8F33DBA6F0F40CCD85683918B44D3DC3EC2C9A2EFB1013EF3357C6E6288F5DDCD61BA137BCA443FE0FC73F8967C5C4B75D6CC0C575C8984857714A93414ADEB848F25D800B794036045632A67C605E2B86B2F19553D800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000]]></hex>
      </ex:root>
    val (_, actual) = TestUtils.testBinary(sch, data, areTracing = false)
    TestUtils.assertEqualsXMLElements(infoset, actual)
  }

  @Test def testGZIPLayerErr2(): Unit = {
    val sch = GZIPLayer1Schema
    val (data, _) = makeGZIPLayer1Data()
    // clobber last 10% of the data with 0xFF bytes.
    ((data.length - (data.length / 10)) to data.length - 1).foreach { i => data(i) = -1 }
    // This causes the gzip input stream to throw an IOException
    val infoset =
      <ex:root xmlns:ex={example}>
        <hex xmlns=""><![CDATA[000000D41F8B08000000000000FF4D904176C3200C44AF3207C8F33DBA6F0F40CCD85683918B44D3DC3EC2C9A2EFB1013EF3357C6E6288F5DDCD61BA137BCA443FE0FC73F8967C5C4B75D6CC0C575C8984857714A93414ADEB848F25D800B794036045632A67C605E2B86B2F19553D805FBE889F2ECE70E2AA4DEA3AA2E3519EF065842E58D2AEDD02530F8DB640832A8F26F3B94DF511CA712437BE27ADDE34F739F8598F20D7CD875566460BEBB4CB10CAD989C9846D684DF6A33CA2F9ED6CFEBF5DCC7168C4169ABDBEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF]]></hex>
      </ex:root>
    val (_, actual) = TestUtils.testBinary(sch, data, areTracing = false)
    TestUtils.assertEqualsXMLElements(infoset, actual)
  }

  val GZIPLayerErrSchema =
    SchemaUtils.dfdlTestSchema(
      <xs:include schemaLocation="/org/apache/daffodil/xsd/DFDLGeneralFormat.dfdl.xsd"/>
          <xs:import namespace="urn:org.apache.daffodil.layers.gzip"
                     schemaLocation="/org/apache/daffodil/layers/xsd/gzipLayer.dfdl.xsd"/>,
      <dfdl:format ref="tns:GeneralFormat" representation="binary"/>,
      <xs:element name="root">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="e1" dfdl:lengthKind="implicit"
                        xmlns:gz="urn:org.apache.daffodil.layers.gzip">
              <xs:complexType>
                <xs:sequence>
                  <xs:element name="len" type="xs:unsignedInt" dfdl:lengthKind="explicit" dfdl:length="4"
                              dfdl:outputValueCalc="{ dfdl:contentLength(../x1/data, 'bytes') }"/>
                  <xs:element name="x1" dfdl:lengthKind="explicit" dfdl:length="{ ../len }">
                    <xs:complexType>
                      <xs:sequence>
                        <xs:element name="data">
                          <xs:complexType>
                            <xs:sequence dfdlx:layer="gz:gzip">
                              <xs:element name="s1" type="xs:string" dfdl:lengthKind="delimited"/>
                            </xs:sequence>
                          </xs:complexType>
                        </xs:element>
                      </xs:sequence>
                    </xs:complexType>
                  </xs:element>
                  <xs:element name="s2" type="xs:string" dfdl:lengthKind="delimited"/>
                </xs:sequence>
              </xs:complexType>
            </xs:element>
          </xs:sequence>
        </xs:complexType>
      </xs:element>,
      elementFormDefault = "unqualified",
    )

  /**
   * These are parse errors due to IOExceptions that occur.
   */
  val excludes = Seq(
    "EOFException",
    "Corrupt GZIP trailer",
    "Unexpected end of ZLIB",
    "invalid distance too far back",
    "Not in GZIP format",
    "invalid literal/lengths set",
    "invalid bit length repeat",
    "invalid code lengths set",
    "invalid code -- missing end-of-block",
    "invalid distances set",
    "Unsupported compression method",
    "Insufficient bits in data",
    "too many length or distance symbols",
    "invalid stored block lengths",
    "invalid block type",
  )

  @Test def testGZIPLayerFuzz1(): Unit = fuzz1(2) // fuzz1(1000)

  def fuzz1(nTrials: Int): Unit = {
    val seed = 987654321
    val sch = GZIPLayerErrSchema
    val r = new Random(seed)
    (1 to nTrials).foreach { i =>
      val (data, _) = makeGZIPLayer1Data()
      // clobber last section of the data (up to 95% of it) with random bytes.
      ((data.length - (data.length / (r.nextInt(19) + 1))) to (data.length - 1)).foreach { i =>
        data(i) = (r.nextInt(256) & 0xff).toByte
      }
      val e = intercept[ParseError] {
        TestUtils.testBinary(sch, data, areTracing = false)
      }
      val m = TestUtils.getAMessage(e)
      if (excludes.forall(ex => !m.contains(ex)))
        println(TestUtils.getAMessage(e))
    }
  }

  @Test def testGZIPLayerFuzz2(): Unit = fuzz2(2) // fuzz2(1000)

  def fuzz2(nTrials: Int): Unit = {
    val expected = <root xmlns="http://example.com">
      <e1 xmlns="">
        <len>212</len> <x1>
        <data>
          <s1>This is just some made up text that is intended to be a few lines long. If this had been real text, it would not have been quite so boring to read. Use of famous quotes or song lyrics or anything like that introduces copyright notice issues, so it is easier to simply make up a few lines of pointless text like this.</s1>
        </data>
      </x1> <s2>afterGzip</s2>
      </e1>
    </root>
    val seed = 123456789
    val sch = GZIPLayerErrSchema
    val r = new Random(seed)
    var index = 0
    var current: Byte = 0
    var newValue: Byte = 0
    (1 to nTrials).foreach { i =>
      print(i + ".")
      val (data, len) = makeGZIPLayer1Data()
      // clobber one randomly chosen byte with a random byte.
      index =
        math.max(4, r.nextInt(len)) // at least 4 bytes in so we don't hammer the length field.
      current = data(index)
      newValue = current
      // make sure they are different so we're definitely changing a byte value
      while (current == newValue) {
        newValue = ((current ^ r.nextInt(256)) & 0xff).toByte
      }
      data(index) = newValue
      val (pr, node) =
        try {
          TestUtils.testBinary(sch, data, areTracing = false)
        } catch {
          case e: ParseError => {
            val m = TestUtils.getAMessage(e)
            if (excludes.forall(ex => !m.contains(ex))) {
              println(TestUtils.getAMessage(e))
            }
            (null, null)
          }
        }
      if (pr ne null) {
        TestUtils.assertEqualsXMLElements(expected, node)
        // println(s"parsed without error despite byte at index $index modified from $current to $newValue.\n Infoset: ${node.toString()}")
      }
    }
  }
}
