package org.apache.daffodil.runtime1.processors

import org.apache.daffodil.lib.exceptions.Assert
import org.apache.daffodil.runtime1.api.MetadataHandler

/**
 * Walks the schema, but not the DSOM schema, it walks the RuntimeData objects that
 * represent the DFDL schema at runtime.
 *
 * @param dp
 */
class MetadataWalker(private val dp: DataProcessor) {

  private lazy val rootERD = dp.ssrd.elementRuntimeData

  def walk(handler: MetadataHandler): Unit = {
    walkTerm(handler, rootERD)
  }

  private def walkTerm(handler: MetadataHandler, trd: TermRuntimeData): Unit = {
    trd match {
      case err: ErrorERD => Assert.invariantFailed("should not get ErrorERDs")
      case erd: ElementRuntimeData => walkElement(handler, erd)
      case srd: SequenceRuntimeData => walkSequence(handler, srd)
      case crd: ChoiceRuntimeData => walkChoice(handler, crd)
      case _ => Assert.invariantFailed(s"unrecognized TermRuntimeData subtype: $trd")
    }
  }

  private def walkElement(handler: MetadataHandler, erd: ElementRuntimeData): Unit = {
    if (erd.optComplexTypeModelGroupRuntimeData.isDefined)
      walkComplexElement(handler, erd)
    else
      walkSimpleElement(handler, erd)
  }

  private def walkComplexElement(
    handler: MetadataHandler,
    erd: ElementRuntimeData,
  ): Unit = {
    val mgrd = erd.optComplexTypeModelGroupRuntimeData.getOrElse {
      Assert.invariantFailed("not a complex type element")
    }
    handler.startElementComplex(erd)
    walkTerm(handler, mgrd)
    handler.endElementComplex(erd)
  }

  private def walkSimpleElement(
    handler: MetadataHandler,
    erd: ElementRuntimeData,
  ): Unit = {
    handler.elementSimple(erd)
  }

  private def walkSequence(handler: MetadataHandler, srd: SequenceRuntimeData): Unit = {
    // handler.startSequence()
    srd.groupMembers.map { trd =>
      walkTerm(handler, trd)
    }
    // handler.endSequence()
  }

  private def walkChoice(handler: MetadataHandler, crd: ChoiceRuntimeData): Unit = {
    // handler.startChoice()
    crd.groupMembers.map { trd =>
      walkTerm(handler, trd)
    }
    // handler.endChoice()
  }

}
