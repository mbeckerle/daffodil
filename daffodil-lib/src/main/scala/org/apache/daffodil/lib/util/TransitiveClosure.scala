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

package org.apache.daffodil.lib.util

import scala.collection.Map
import scala.collection.mutable

abstract class TransitiveClosure[T] {

  protected def func(sc: T): Seq[T]

  // LinkedHashSet so we get deterministic behavior.
  private val processed = new mutable.LinkedHashSet[T]
  private val items = new mutable.Queue[T]

  def apply(start: Seq[T]): mutable.LinkedHashSet[T] = {
    start.foreach {
      items.enqueue(_)
    }
    tclose()
  }

  /**
   * Breadth first traversal.
   */
  private def tclose() = {
    while (!items.isEmpty) {
      val hd = items.dequeue
      if (!processed.contains(hd)) {
        processed += hd
        val newOnes = func(hd).filterNot(processed.contains(_)).distinct
        newOnes.foreach {
          items.enqueue(_)
        }
      }
    }
    processed
  }
}

/**
 * Computes transitive closure and detects cycles simultaneously.
 *
 * Traverses the graph depth first.
 * @tparam T the type of the nodes in the graph
 */
abstract class TransitiveClosureWithCycleDetection[T <: AnyRef] {

  def func(node: T): Seq[T]

  def cycleDetected(cycle: Seq[T]): Unit

  def apply(nodes: Seq[T]): Map[T, Seq[T]] = {
    val reachableFrom = mutable.LinkedHashMap[T, mutable.Set[T]]() // for cycle detection
    val orderedReachableFrom =
      mutable.LinkedHashMap[T, Seq[T]]() // to preserve order of the cycle
    val visited = mutable.Set[T]() // so that we get constant time lookups
    val visitedSeq = mutable.ArrayBuffer[T]() // so that we get deterministic order
    val onStack = mutable.Set[T]() // so that we get constant time lookups
    val ordStack = mutable.Stack[T]() // so that we can preserve order
    val closure =
      mutable
        .LinkedHashMap[T, mutable.ArrayBuffer[T]]() // Linked so we get deterministic behavior

    def dfs(node: T): Unit = {
      visited += node
      visitedSeq += node
      onStack += node
      ordStack.push(node)

      func(node).foreach { neighbor =>
        if (reachableFrom(neighbor).contains(node)) {
          // a cycle has been detected.
          cycleDetected(orderedReachableFrom(neighbor) :+ node)
        }
        if (!visited.contains(neighbor)) {
          dfs(neighbor)
        } else if (onStack.contains(neighbor)) {
          cycleDetected(ordStack.reverse :+ neighbor)
        }
      }

      closure.put(node, ordStack.reverse) // Build the closure map
      onStack -= node
      ordStack.pop
    }

    for (node <- nodes) {
      if (!visited.contains(node)) {
        dfs(node)
      }
    }
    closure
    visitedSeq.toSeq
  }

}
