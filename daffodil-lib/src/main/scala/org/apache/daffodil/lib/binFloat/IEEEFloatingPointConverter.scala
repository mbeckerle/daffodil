package org.apache.daffodil.lib.binFloat

/**
 * Representing IEEE floating point in base 10 such that one can
 * convert to/from base 10 without loss of precision, yet one can also
 * manipulate the value.
 *
 * NaNs and Infinities are represented by special markers.
 *
 * Numbers that perfectly convert between base 10 and base 2 are represented
 * as base 10 text in the usual notation like 2.5E-10.
 *
 * A number, D, that does not perfectly convert between base 10 and base 2 is
 * represented as an object which provides the normal base 10 notation as a string,
 * which is known to be an approximation. Converting this object toDouble
 * provides the exact binary representation. These objects are immutable.
 *
 * Serialization of these objects in human-readable form requires storing both
 * the normal representation (e.g., 2.5E-10) but also the original value (8 bytes)
 * as a java.lang.long integer.
 * When deserializing these back, if the incoming base 10 string is not the
 * correct string corresponding to the original value, then the original value is
 * dropped, and a new original value is computed from the string.
 * If, however, the incoming base 10 string is the correct approximation for the
 * original value, then the original value is preserved.
 *
 * but also retains the binary representation as a raw java.lang.long value.
 * These support a changeValue method which takes a new value either as a string
 * or as a double.
 */

/**
 * Object for converting IEEE 754 double-precision floating-point numbers
 * to and from a pair of integers (X, Y) where D = X * (2 ^ Y).
 * This implementation uses only shifting and masking for calculations.
 */
object IEEEFloatingPointConverter {

  private val SIGN_MASK: Long = 0x8000000000000000L
  private val EXPONENT_MASK: Long = 0x7ff0000000000000L
  private val MANTISSA_MASK: Long = 0x000fffffffffffffL
  private val EXPONENT_BIAS: Int = 1023
  private val MANTISSA_BITS: Int = 52

  /**
   *
   * Converts a double-precision floating-point number, D, into a pair of integers (X, Y).
   * D = X * 2^Y, preserving every bit of D.
   *
   * @param value the IEEE 754 double-precision floating-point number.
   * @return a tuple (X, Y) such that D = X * 2^Y.
   */
  def doubleToIntegerPair(value: Double): (Long, Int) = {
    if (value == 0.0) return (0L, 0)

    val bits = java.lang.Double.doubleToRawLongBits(value)

    // Extract sign, exponent, and mantissa
    val sign = if ((bits & SIGN_MASK) != 0) -1 else 1
    val rawExponent = ((bits & EXPONENT_MASK) >>> MANTISSA_BITS).toInt
    val mantissa = bits & MANTISSA_MASK
    val isSubnormal = rawExponent == 0

    // Compute unbiased binary exponent
    val binaryExponent =
      if (isSubnormal) -EXPONENT_BIAS + 1
      else rawExponent - EXPONENT_BIAS

    // Normalize mantissa
    val normalizedMantissa: Long =
      if (isSubnormal) mantissa
      else mantissa | (1L << MANTISSA_BITS)

    val trailingZeroBits = java.lang.Long.numberOfTrailingZeros(normalizedMantissa)
    assert(trailingZeroBits <= 52)

    val adjustedExponent = binaryExponent - 52


    // Combine sign and return
    val x = normalizedMantissa * sign
    (x, adjustedExponent)
  }

  /**
   * Converts a pair (X, Y) back into a double-precision floating-point number.
   * D = X * 2^Y.
   *
   * @param x the signed integer X.
   * @param y the signed base-2 exponent Y.
   * @return the reconstructed IEEE 754 double-precision floating-point number.
   */
  def integerPairToDouble(x: Long, y: Int): Double = {
    if (x == 0L) return 0.0
    x.toDouble * Math.pow(2, y)
  }
}
