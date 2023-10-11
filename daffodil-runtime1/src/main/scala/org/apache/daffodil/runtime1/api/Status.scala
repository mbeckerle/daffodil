package org.apache.daffodil.runtime1.api

object Status extends Enumeration {
  type Status = Value
  val DONE, READY, VISITING = Value
}
