package org.apache.daffodil.runtime1.api

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Defines the interface for InfosetOutputters.
 *
 * Note that these functions all throw the generic java.lang.Exception to
 * indicate error. Part of the reason to do this instead of a custom exception
 * (e.g. InfosetOutputterException) is to simplify implementations. If an
 * implementation already throws an exception when there is an error, there is
 * no need to catch it and wrap it in a Daffodil specific exception. This is
 * especially true considering Daffodil will just unwrap the exception and
 * convert it to a SDE. Additionally, because Scala does not have checked
 * exceptions, it can be difficult to ensure all expected exceptions are caught
 * by implementations. This does mean some exceptions that you might normally
 * expect to bubble up and will not, and will instead be turned into an SDE.
 */
trait InfosetOutputter extends BlobMethodsMixin {

  import Status._

  /**
   * Reset the internal state of this InfosetOutputter. This should be called
   * inbetween calls to the parse method.
   */
  def reset(): Unit

  /**
   * Called by Daffodil internals to signify the beginning of the infoset.
   *
   * Throws java.lang.Exception if there was an error and Daffodil should stop parsing
   */
  @throws[Exception]
  def startDocument(): Unit

  /**
   * Called by Daffodil internals to signify the end of the infoset.
   *
   * Throws java.lang.Exception if there was an error and Daffodil should stop parsing
   */
  @throws[Exception]
  def endDocument(): Unit

  /**
   * Called by Daffodil internals to signify the beginning of a simple element.
   *
   * Throws java.lang.Exception if there was an error and Daffodil should stop parsing
   *
   * @param diSimple the simple element that is started. Various fields of
   *                 DISimple can be accessed to determine things like the
   *                 value, nil, name, namespace, etc.
   */
  @throws[Exception]
  def startSimple(diSimple: InfosetSimpleElement): Unit

  /**
   * Called by Daffodil internals to signify the end of a simple element.
   *
   * Throws java.lang.Exception if there was an error and Daffodil should stop parsing
   *
   * @param diSimple the simple element that is ended. Various fields of
   *                 DISimple can be accessed to determine things like the
   *                 value, nil, name, namespace, etc.
   */
  @throws[Exception]
  def endSimple(diSimple: InfosetSimpleElement): Unit

  /**
   * Called by Daffodil internals to signify the beginning of a complex element.
   *
   * Throws java.lang.Exception if there was an error and Daffodil should stop parsing
   *
   * @param diComplex the complex element that is started. Various fields of
   *                  DIComplex can be accessed to determine things like the
   *                  nil, name, namespace, etc.
   */
  @throws[Exception]
  def startComplex(diComplex: InfosetComplexElement): Unit

  /**
   * Called by Daffodil internals to signify the end of a complex element.
   *
   * Throws java.lang.Exception if there was an error and Daffodil should stop parsing
   *
   * @param diComplex the complex element that is ended. Various fields of
   *                  DIComplex can be accessed to determine things like the
   *                  nil, name, namespace, etc.
   */
  @throws[Exception]
  def endComplex(diComplex: InfosetComplexElement): Unit

  /**
   * Called by Daffodil internals to signify the beginning of an array of elements.
   *
   * Throws java.lang.Exception if there was an error and Daffodil should stop parsing
   *
   * @param diArray the array that is started. Various fields of
   *                  DIArray can be accessed to determine things like the
   *                  name, namespace, etc.
   */
  @throws[Exception]
  def startArray(diArray: InfosetArray): Unit

  /**
   * Called by Daffodil internals to signify the end of an array of elements.
   *
   * Throws java.lang.Exception if there was an error and Daffodil should stop parsing
   *
   * @param diArray the array that is ended. Various fields of
   *                  DIArray can be accessed to determine things like the
   *                  name, namespace, etc.
   */
  @throws[Exception]
  def endArray(diArray: InfosetArray): Unit

  def getStatus(): Status
}

/**
 * Methods that provide Blob (Binary Large Object) support.
 *
 * FIXME: Scaladoc
 */
trait BlobMethodsMixin {

  def setBlobAttributes(blobDir: Path, blobPrefix: String, blobSuffix: String): Unit
  def setBlobPaths(empty: Seq[Path]): Unit
  def getBlobDirectory(): Path
  def getBlobPrefix(): String
  def getBlobSuffix(): String

}

/**
 * An available basic implementation of the BLOB methods.
 * Stores blobs in files in directory identified by Java system property
 * `java.io.tempdir`.
 *
 * FIXME: Unclear if Daffodil implements blob copying based on these
 *   alone or not, and what else people have to do for BLOB support
 *   than mixin this to their InfosetOutputter. 
 *
 * FIXME: Scaladoc
 */
abstract class BlobMethodsImpl() extends BlobMethodsMixin {

  /**
   * Set the attributes for how to create blob files.
   *
   * @param dir the Path the the directory to create files. If the directory
   *            does not exist, Daffodil will attempt to create it before
   *            writing a blob.
   * @param prefix the prefix string to be used in generating a blob file name
   * @param suffix the suffix string to be used in generating a blob file name
   */
  final def setBlobAttributes(dir: Path, prefix: String, suffix: String): Unit = {
    blobDirectory = dir
    blobPrefix = prefix
    blobSuffix = suffix
  }

  /**
   * Get the list of blob paths that were output in the infoset.
   *
   * This is the same as what would be found by iterating over the infoset.
   */
  final def getBlobPaths(): Seq[Path] = blobPaths
  final def getBlobDirectory(): Path = blobDirectory
  final def getBlobPrefix(): String = blobPrefix
  final def getBlobSuffix(): String = blobSuffix
  final def setBlobPaths(paths: Seq[Path]): Unit = blobPaths = paths
  private var blobDirectory: Path = Paths.get(System.getProperty("java.io.tmpdir"))
  private var blobPrefix: String = "daffodil-"
  private var blobSuffix: String = ".blob"
  private var blobPaths: Seq[Path] = Seq.empty
}
