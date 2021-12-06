package org.apache.daffodil.runtime1

import org.apache.daffodil.compiler.ForUnparser
import org.apache.daffodil.grammar.SeqComp
import GramRuntime1._
import org.apache.daffodil.processors.parsers.NadaParser
import org.apache.daffodil.processors.parsers.SeqCompParser // for implicit conversion of Gram to GramRuntime1

class SeqCompRuntime1(sc: SeqComp)
  extends GramRuntime1(sc){

  lazy val parserChildren = sc.children.filter(_.forWhat != ForUnparser).map { _.parser }.filterNot { _.isInstanceOf[NadaParser] }

  final override lazy val parser = {
    if (parserChildren.isEmpty) new NadaParser(sc.context.runtimeData)
    else if (parserChildren.length == 1) parserChildren.head
    else new SeqCompParser(sc.context.runtimeData, parserChildren.toVector)
  }

  lazy val unparserChildren = {
    val unparserKeptChildren =
      children.filter(
        x =>
          !x.isEmpty && (x.forWhat != ForParser))
    val unparsers =
      unparserKeptChildren.map { x =>
        x.unparser
      }.filterNot { _.isInstanceOf[NadaUnparser] }
    unparsers
  }

  final override lazy val unparser = {
    if (unparserChildren.isEmpty) new NadaUnparser(context.runtimeData)
    else if (unparserChildren.length == 1) unparserChildren.head
    else new SeqCompUnparser(context.runtimeData, unparserChildren.toVector)
  }
}
