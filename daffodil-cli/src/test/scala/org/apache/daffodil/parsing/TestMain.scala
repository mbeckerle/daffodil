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

package org.apache.daffodil.parsing
//
//import org.apache.daffodil.CLI.Util
//import org.apache.daffodil.Main
//// import org.junit.Test
//
class TestMain {
//
//
// FIXME: I needed this to breakpoint debug an integration test
// It would be good if the CLI.Util object could be visible to both
// the src/it and src/test directories.
//
// But we also need a way to capture stderr and stdout from a
// invocation of Main.main so that we can scrutinize output without having to
// stage Daffodil and run in a separate process/shell.
//
//  // @Test
//  def testParsingMain1(): Unit = {
//    val schemaFile = Util.daffodilPath("daffodil-cli/src/it/resources/org/apache/daffodil/CLI/ABC_IBM_invalid.dfdl.xsd")
//    val testSchemaFile =  Util.cmdConvert(schemaFile)
//    //
//    // We need a way to capture stdout and stderr from this
//    // without having to run a staged daffodil to exercise
//    // the CLI.
//    //
//    Main.main( Array[String]( "parse", "-s", testSchemaFile, "-r", "ABC"))
//    //
//    // We're expecting to see a SAXParseException reported about
//    // fixed not a valid value for maxOccurs, not a valid value for
//    // type nonNegativeInteger.
//    //
//  }
}
