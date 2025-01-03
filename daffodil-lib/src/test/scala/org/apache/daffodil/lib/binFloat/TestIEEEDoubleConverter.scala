package org.apache.daffodil.lib.binFloat
import org.junit.Assert._
import org.junit.Test

class TestIEEEDoubleConverter {
  import IEEEFloatingPointConverter._

  @Test
  def testZero(): Unit = {
    val value = 0.0
    val (x, y) = doubleToIntegerPair(value)
    val reconstructed = integerPairToDouble(x, y)
    assertEquals("Reconstructed value should match the original", value, reconstructed, 0.0)
  }

  @Test
  def testOne(): Unit = {
    val value = 1.0
    val (x, y) = doubleToIntegerPair(value)
    val reconstructed = integerPairToDouble(x, y)
    assertEquals("Reconstructed value should match the original", value, reconstructed, 0.0)
  }

  @Test
  def testMinusOne(): Unit = {
    val value = -1.0
    val (x, y) = doubleToIntegerPair(value)
    val reconstructed = integerPairToDouble(x, y)
    assertEquals("Reconstructed value should match the original", value, reconstructed, 0.0)
  }

  @Test
  def testPositiveNumber(): Unit = {
    val value = 12345.6789
    val (x, y) = doubleToIntegerPair(value)
    val reconstructed = integerPairToDouble(x, y)
    assertEquals("Reconstructed value should match the original", value, reconstructed, 0.0)
  }

  @Test
  def testNegativeNumber(): Unit = {
    val value = -12345.6789
    val (x, y) = doubleToIntegerPair(value)
    val reconstructed = integerPairToDouble(x, y)
    assertEquals("Reconstructed value should match the original", value, reconstructed, 0.0)
  }

  @Test
  def testSmallestPositiveSubnormal(): Unit = {
    val value = java.lang.Double.MIN_VALUE
    val (x, y) = doubleToIntegerPair(value)
    val reconstructed = integerPairToDouble(x, y)
    assertEquals("Reconstructed value should match the smallest positive subnormal", value, reconstructed, 0.0)
  }

  @Test
  def testLargestPositiveNormal(): Unit = {
    val value = java.lang.Double.MAX_VALUE
    val (x, y) = doubleToIntegerPair(value)
    val reconstructed = integerPairToDouble(x, y)
    assertEquals("Reconstructed value should match the largest positive normal", value, reconstructed, 0.0)
  }

  @Test
  def testSmallestNegativeSubnormal(): Unit = {
    val value = -java.lang.Double.MIN_VALUE
    val (x, y) = doubleToIntegerPair(value)
    val reconstructed = integerPairToDouble(x, y)
    assertEquals("Reconstructed value should match the smallest negative subnormal", value, reconstructed, 0.0)
  }

  @Test
  def testLargestNegativeNormal(): Unit = {
    val value = -java.lang.Double.MAX_VALUE
    val (x, y) = doubleToIntegerPair(value)
    val reconstructed = integerPairToDouble(x, y)
    assertEquals("Reconstructed value should match the largest negative normal", value, reconstructed, 0.0)
  }

  @Test
  def testExactInteger(): Unit = {
    val value = 42.0
    val (x, y) = doubleToIntegerPair(value)
    val reconstructed = integerPairToDouble(x, y)
    assertEquals("Reconstructed value should match the exact integer", value, reconstructed, 0.0)
  }

  @Test
  def testMaxDoubleOddInteger() : Unit = {
    val valueL = 9007199254740991L // largest odd integer perfectly represented by IEEE double.
    assertEquals(valueL, (1L << 53) -1)
    val value = valueL.toDouble
    val (x, y) = doubleToIntegerPair(value)
    val reconstructed = integerPairToDouble(x, y)
    assertEquals("Reconstructed value should match the exact integer", value, reconstructed, 0.0)
  }

  @Test
  def test53rdBit() : Unit = {
    val valueL = (1L << 52) + 1L
    println(f"$valueL%x") // 0x0010 0000 0000 0001
    val value = valueL.toDouble
    val (x, y) = doubleToIntegerPair(value)
    val reconstructed = integerPairToDouble(x, y)
    assertEquals("Reconstructed value should match the exact integer", value, reconstructed, 0.0)
  }
}
