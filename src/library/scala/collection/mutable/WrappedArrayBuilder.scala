/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala
package collection
package mutable

import scala.reflect.ClassTag

/** A builder class for arrays.
 *
 *  This builder can be reused.
 *
 *  @tparam A   type of elements that can be added to this builder.
 *  @param tag  class tag for objects of type `A`.
 *
 *  @since 2.8
 */
class WrappedArrayBuilder[A](tag: ClassTag[A]) extends ReusableBuilder[A, WrappedArray[A]] {

  @deprecated("use tag instead", "2.10.0")
  val manifest: ClassTag[A] = tag

  private var elems: WrappedArray[A] = _
  private var capacity: Int = 0
  private var size: Int = 0

  private def mkArray(size: Int): WrappedArray[A] = {
    if (size == 0) WrappedArrayBuilder.emptyWrappedArray(tag)
    else {
      import java.util.Arrays.copyOf
      val runtimeClass = tag.runtimeClass
      if (runtimeClass.isPrimitive)
        runtimeClass match {
          case java.lang.Integer.TYPE   =>
            val array = if (elems eq null) new Array[Int](size) else copyOf(elems.array.asInstanceOf[Array[Int]], size)
            new WrappedArray.ofInt(array).asInstanceOf[WrappedArray[A]]
          case java.lang.Double.TYPE    =>
            val array = if (elems eq null) new Array[Double](size) else copyOf(elems.array.asInstanceOf[Array[Double]], size)
            new WrappedArray.ofDouble(array).asInstanceOf[WrappedArray[A]]
          case java.lang.Long.TYPE      =>
            val array = if (elems eq null) new Array[Long](size) else copyOf(elems.array.asInstanceOf[Array[Long]], size)
            new WrappedArray.ofLong(array).asInstanceOf[WrappedArray[A]]
          case java.lang.Float.TYPE     =>
            val array = if (elems eq null) new Array[Float](size) else copyOf(elems.array.asInstanceOf[Array[Float]], size)
            new WrappedArray.ofFloat(array).asInstanceOf[WrappedArray[A]]
          case java.lang.Character.TYPE =>
            val array = if (elems eq null) new Array[Char](size) else copyOf(elems.array.asInstanceOf[Array[Char]], size)
            new WrappedArray.ofChar(array).asInstanceOf[WrappedArray[A]]
          case java.lang.Byte.TYPE      =>
            val array = if (elems eq null) new Array[Byte](size) else copyOf(elems.array.asInstanceOf[Array[Byte]], size)
            new WrappedArray.ofByte(array).asInstanceOf[WrappedArray[A]]
          case java.lang.Short.TYPE     =>
            val array = if (elems eq null) new Array[Short](size) else copyOf(elems.array.asInstanceOf[Array[Short]], size)
            new WrappedArray.ofShort(array).asInstanceOf[WrappedArray[A]]
          case java.lang.Boolean.TYPE   =>
            val array = if (elems eq null) new Array[Boolean](size) else copyOf(elems.array.asInstanceOf[Array[Boolean]], size)
            new WrappedArray.ofBoolean(array).asInstanceOf[WrappedArray[A]]
          case java.lang.Void.TYPE      =>
            val array = if (elems eq null) new Array[Unit](size) else copyOf(elems.array.asInstanceOf[Array[AnyRef]], size).asInstanceOf[Array[Unit]]
            new WrappedArray.ofUnit(array).asInstanceOf[WrappedArray[A]]
        }
      else {
        val array = if (elems eq null) new Array[A with AnyRef](size) else copyOf(elems.array.asInstanceOf[Array[A with AnyRef]], size)
        new WrappedArray.ofRef(array).asInstanceOf[WrappedArray[A]]
      }
    }
  }

  private def resize(size: Int) {
    elems = mkArray(size)
    capacity = size
  }

  override def sizeHint(size: Int) {
    if (capacity < size) resize(size)
  }

  private def ensureSize(size: Int) {
    if (capacity < size) {
      var newsize = if (capacity == 0) 16 else capacity * 2
      while (newsize < size) newsize *= 2
      resize(newsize)
    }
  }

  def +=(elem: A): this.type = {
    ensureSize(size + 1)
    elems(size) = elem
    size += 1
    this
  }

  def clear() { size = 0 }

  def result() = {
    if (capacity != 0 && capacity == size) {
      capacity = 0
      elems
    }
    else mkArray(size)
  }

  // todo: add ++=
}
private [mutable] object WrappedArrayBuilder {
  private def emptyWrappedArray[T](tag: ClassTag[T]): mutable.WrappedArray[T] =
    emptyClass.get(tag.runtimeClass).asInstanceOf[WrappedArray[T]]
  private[this] val emptyClass = new ClassValue[WrappedArray[_]] {
    override def computeValue(cls: Class[_]): WrappedArray[_] = {
      if (cls.isPrimitive)
        cls match {
          case java.lang.Integer.TYPE   => new WrappedArray.ofInt(Array.emptyIntArray)
          case java.lang.Double.TYPE    => new WrappedArray.ofDouble(Array.emptyDoubleArray)
          case java.lang.Long.TYPE      => new WrappedArray.ofLong(Array.emptyLongArray)
          case java.lang.Float.TYPE     => new WrappedArray.ofFloat(Array.emptyFloatArray)
          case java.lang.Character.TYPE => new WrappedArray.ofChar(Array.emptyCharArray)
          case java.lang.Byte.TYPE      => new WrappedArray.ofByte(Array.emptyByteArray)
          case java.lang.Short.TYPE     => new WrappedArray.ofShort(Array.emptyShortArray)
          case java.lang.Boolean.TYPE   => new WrappedArray.ofBoolean(Array.emptyBooleanArray)
          case java.lang.Void.TYPE      => new WrappedArray.ofUnit(Array.emptyUnitArray)
        }
      else new WrappedArray.ofRef(Array.empty.asInstanceOf[Array[_ <: AnyRef]])
    }
  }
}
