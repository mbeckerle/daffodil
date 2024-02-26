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

package org.apache.daffodil.core.dsom

import org.apache.daffodil.lib.exceptions.Assert

/**
 * When a global schema component is referenced, the "backpointer" is represented by
 * this class. The lexical position is the index within the enclosing construct's lexical parent.
 * For example, if an element ref is referencing a global element decl, then the encloser is
 * the elementRef, and the lexical position is which index, within the sequence/choice that
 * contains that particular elementRef.
 */
case class EnclosingComponentDef(encloser: SchemaComponent, lexicalPosition: Int)

trait NestingLexicalMixin { self: SchemaComponent =>

  /**
   * Schema components lexically contained within this schema component, excluding annotations.
   * So elements, groups, types, but not the annotations on those.
   *
   * This is one-hop, not the transitive closure of this relationship.
   *
   * The inverse of this relationship is the optLexicalParent.
   * @return a sequence of lexically contained schema components
   */
  final lazy val containedNonAnnotationSchemaComponents: Seq[SchemaComponent] = {
    self match {
      case e: ElementDeclMixin => e.immediateType.toSeq
      case ct: ComplexTypeBase => Seq(ct.modelGroup)
      case st: SimpleTypeDefBase => st.optUnion.map{ u: Union => u.directMemberTypes }.getOrElse(Nil)
      case mg: ModelGroup => mg.groupMembers
      case _ => Nil
    }
  }

  /**
   * Schema components referenced by name from this schema component, excluding
   * references from or to annotations.
   * Includes elements references to type defs, element refs to elements,
   * group refs to group defs, type refs to type defs.
   *
   * Also includes dfdl:prefixLengthType, as a special kind of type reference from
   * element decls.
   *
   * Also includes dfdl:repType as a special kind of type reference from
   * simple type defs.
   *
   * This is one-hop, not the transitive closure of this relationship.
   * @return a sequence of the schema components referenced from this one.
   */
  final lazy val referencedNonAnnotationSchemaComponents: Seq[SchemaComponent] = LV('referencedSchemaComponents){
    self match {
      case er: AbstractElementRef => Seq(er.referencedElement)
      //
      // Element declarations can reference types
      //
      case e: ElementDeclMixin => {
        val eb = e.asInstanceOf[ElementBase]
        val typeRefs = e.namedType match{
          case Some(std: SimpleTypeDefBase) => Some(std.asInstanceOf[SchemaComponent]).toSeq
          case Some(ct: ComplexTypeBase) => Some(ct.asInstanceOf[SchemaComponent]).toSeq
          case _ => Nil
        }
        typeRefs ++ eb.optPrefixLengthElementDecl ++ eb.optRepTypeElementDecl
      }
      //
      // No special case for hidden group ref. Those have already been replaced by
      // either a sequence group ref or choice group ref with the isHidden flag.
      //
      case gr: GroupRef => Seq(gr.groupDef)
      case s: SequenceDefMixin if s.hiddenGroupRefOption.isDefined =>
        Assert.invariantFailed("Sequence with hidden group ref should already have been replaced by a GroupRef object.")
      case std: SimpleTypeDefBase => {
        // a restriction always references a base type by name, we just want to exclude bases that are primitives
        val restrictionRef = std.optRestriction.flatMap{ _.optBaseTypeDef }
        // a union can reference member types by name.
        val unionRefs = std.optUnion.map{ _.namedTypes }.getOrElse(Nil)
        unionRefs ++ restrictionRef
      }.toSeq
      case _ => Nil
    }
  }.value

  /** The lexically enclosing schema component */
  def optLexicalParent: Option[SchemaComponent]

  def shortSchemaComponentDesignator: String

  private def usageErr(sc: SchemaComponent) =
    Assert.usageError("Not to be called on " + sc)

  /**
   * Provides a sequence of the enclosing components. For shared global components, this
   * includes all the referring components across the schema.
   *
   * The enclosing components are identified by the object, and the position of this
   * object within that enclosing component by index.
   */
  final lazy val enclosingComponents: Seq[EnclosingComponentDef] = { // Public for tests
    //
    // enclosing components is not defined for some top-level schema component objects
    //
    this match {
      case sd: SchemaDocument => usageErr(sd)
      case ss: SchemaSet => usageErr(ss)
      case s: Schema => usageErr(s)
      case _ => // ok
    }
    val result =
      optLexicalParent.toSeq.flatMap { lp =>
        //
        // If this is a term, then it has a position within its parent.
        // otherwise just use 1.
        //
        val pos = this match {
          case t: Term => t.position
          case _ => 1
        }
        val components =
          lp match {
            case sd: SchemaDocument => {
              // enclosing lexical component is schema document, so
              // that means this is a global component, and we
              // we need to determine the list of components that reference this global component
              this match {
                case r: Root => Nil
                case gedecl: GlobalElementDecl
                    if (schemaSet.root.referencedElement eq gedecl) => {
                  // this is the global root element's declaration
                  // in this case the enclosing component is the root, which is a
                  // special kind of element reference.
                  Seq(EnclosingComponentDef(schemaSet.root, 1))
                }
                case gc: GlobalComponent => {
                  //
                  // The root object builds up this refMap which contains all the
                  // relationships of referencer to referencee.
                  //
                  val optRefList = schemaSet.root.refMap.get(gc)
                  val refList =
                    optRefList.toSeq.flatMap { pairList =>
                      pairList.flatMap { pair =>
                        pair match {
                          case (scid, seqRefSpec) => seqRefSpec
                        }
                      }
                    }
                  val componentList = refList.map { refSpec =>
                    val frm = refSpec.from
                    //
                    // Do checking that the referenced relationship is one we expect
                    // to find. We only expect to deal with ElementRefs to GlobalElementDecls
                    // GroupRefs to Global Group Definitions, or Elements referring to
                    // Global type definitions.
                    //
                    // The other kinds of references (to escape schemes, to named formats, or
                    // from derived types to their base types) shouldn't be encountered here.
                    //
                    frm match {
                      case er: ElementRef => // ok
                      case gr: GroupRef => // ok
                      case ed: ElementDeclMixin if (ed.namedTypeQName.isDefined) => // ok
                      case _ =>
                        Assert.invariantFailed(
                          "referring component is not a ref object: " + frm,
                        )
                    }
                    EnclosingComponentDef(refSpec.from, pos)
                  }
                  componentList
                } // end case of this being a global component
                //
                // we don't care about these other components for our purposes here
                //
                case es: DFDLEscapeScheme => Nil
                case f: DFDLFormat => Nil
                case p: DFDLProperty => Nil
                case _ =>
                  Assert.invariantFailed(
                    "non-global component has schema document as lexical parent: " + this,
                  )
              } // end match
            } // end case where lexical parent is SchemaDocument
            case ec => {
              //
              // This case for the lexical parent is NOT the schema document. In that case
              // the lexical parent *is* the enclosing component, and there is only one
              // enclosing component.
              //
              Seq(EnclosingComponentDef(ec, pos))
            }
          } // end match
        components
      }
    result.distinct
  }
}
