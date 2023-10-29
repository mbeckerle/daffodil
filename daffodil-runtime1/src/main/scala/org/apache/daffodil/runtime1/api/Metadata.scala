package org.apache.daffodil.runtime1.api

import org.apache.daffodil.lib.exceptions.SchemaFileLocation
import org.apache.daffodil.lib.xml.NS
import org.apache.daffodil.lib.xml.NamedQName
import org.apache.daffodil.runtime1.dpath.NodeInfo.PrimType

/*
 * This is the supportable API for access to the RuntimeData structures
 */
trait Metadata {
  def schemaFileLocation: SchemaFileLocation
  def path: String
  def diagnosticDebugName: String
  override def toString = diagnosticDebugName
}

trait TermMetadata extends Metadata {
  def isArray: Boolean
}

trait ElementMetadata extends TermMetadata {
  def isSimpleType: Boolean
  def isComplexType: Boolean

  def name: String
  def namespace: NS
  def optNamespacePrefix: Option[String]
  def isArray: Boolean
  def isOptional: Boolean
  def namedQName: NamedQName

  def sscd: String
  def isNillable: Boolean

  def runtimeProperties: java.util.Map[String, String]

}

trait ComplexElementMetadata extends ElementMetadata {

  def childMetadata: Seq[ElementMetadata]
}

trait SimpleElementMetadata extends ElementMetadata {
  def primType: PrimType
}

trait ModelGroupMetadata extends TermMetadata {}

trait SequenceMetadata extends ModelGroupMetadata {}

trait ChoiceMetadata extends ModelGroupMetadata {}

/**
 * Base class used by clients who want to walk the runtime schema information.
 */
abstract class MetadataHandler() {

  /**
   * Called for simple type element declarations/references
   */
  def elementSimple(sem: SimpleElementMetadata): Unit

  /**
   * Called for complex type element declarations
   *
   * Subsequent calls will be for the model group making up the content
   * of the element.
   */
  def startElementComplex(cem: ComplexElementMetadata): Unit
  def endElementComplex(cem: ComplexElementMetadata): Unit

  //  def startSequence(): Unit
  //
  //  def endSequence(): Unit
  //
  //  def startChoice(): Unit
  //
  //  def endChoice(): Unit

}
