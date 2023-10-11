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

package org.apache.daffodil.runtime1.infoset

import org.apache.daffodil.runtime1.api.BlobMethodsImpl
import org.apache.daffodil.runtime1.api.InfosetOutputter

abstract class InfosetOutputterImpl() extends BlobMethodsImpl with InfosetOutputter {

  import org.apache.daffodil.runtime1.api.Status._

  private def status: Status = READY

  override def getStatus(): Status = {
    // Done, Ready (Not started), Visiting (part way done - can retry to visit more)...
    status
  }

  /**
   * Helper function to determine if an element is nilled or not, taking into
   * account whether or not the nilled state has been set yet.
   *
   * @param diElement the element to check the nilled state of
   * @return true if the nilled state has been set and is true. false if the
   *         nilled state is false or if the nilled state has not been set yet
   *         (e.g. during debugging)
   */
  final def isNilled(diElement: DIElement): Boolean = {
    val maybeIsNilled = diElement.maybeIsNilled
    maybeIsNilled.isDefined && maybeIsNilled.get == true
  }
}


