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

package org.apache.daffodil.util

import org.junit.Test

/**
 * Do we need to use macros to reduce logging overhead when
 * the log-level indicates no logging is required?
 *
 * In scala 12, you must specify compiler options:
 *
 *     -opt:l:inline
 *     -opt:l:method
 *
 * CAUTION: Right now (Scala 2.12.11 2021-04-29), specifying those
 * causes compilation to fail with a bunch of errors like:
 *     [error] Error while emitting PropertyGenerator.scala
 *     [error] Unsupported class file major version 60
 *     [error] Error while emitting PropertyGenerator.scala
 *     [error] Unsupported class file major version 60
 *     ...
 *
 * So given that the inline approach doesn't currently work, yeah, we need the macros.
 *
 * If the test methods in this class generate the same bytecode, then no,
 * we don't need to use macros.
 *
 * If the testInlineWithByNameArgs generates a bunch of by-name passing overhead,
 * that cannot be branched around (by inlining the call to byName, so the if test bypasses such
 * overhead, then yes, we need macros to bypass all that overhead.
 */
class TestDoWeNeedToUseMacros() {
 import DoWeNeedToUseMacros._
  var dontDo = -1
  var doIt = 1

  /**
   * This is the method to look at to see if inlining works.
   *
   * The bytecode wants to load the level and dontDo vars, and
   * then do an IF test and branch around everything.
   *
   * If it doesn't do that, then this isn't sufficiently optimizing.
   */
  @Test def testInlineWithByNameArgs() : Unit = {
    byName(dontDo, "%s %s %s %s", 1, 2, 3, 4)
   }

  /**
   * In scala 12, these next two methods basically produce the same byte code
   * regardless of optimization settings.
   */
  @Test def testIfThenElse() : Unit = {
    if (level <= dontDo)
      regularCall("%s %s %s %s", 1, 2, 3, 4)
  }

  @Test def testViaMacro(): Unit = {
    viaMacro(dontDo, "%s %s %s %s", 1, 2, 3, 4)
  }
}

object DoWeNeedToUseMacros {

  var level: Int = 0

  /**
   * This is the inline, pass-by-name approach.
   * Theoretically, this could optimize into the same thing as the other approaches.
   *
   * Note there is no by-name varargs, so we'd need a bunch of overloads of this with
   * different numbers of arguments.
   */
  @inline
  final def byName(lvl: Int, msg: => String, arg0: => Any, arg1: => Any, arg2: => Any, arg3: => Any ): Unit = {
    if (lvl >= level)
      regularCall(msg, arg0, arg1, arg2, arg3 )
  }

  /**
   * This is the macro approach. Should turn into exactly the if-then equivalent code.
   *
   * The macro is defined in a separate file
   */
  final def viaMacro(lvl: Int, msg: String, args: Any*): Unit = macro Macro4TestMacro.testMacro

  /**
   * All the approaches end up calling this ultimately behind the scenes
   */
  final def regularCall(msg: String, args: Any*) : Unit = {
    throw new Exception(msg.format(args: _*))
  }


}
