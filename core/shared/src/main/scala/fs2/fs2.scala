/*
 * Copyright (c) 2013 Functional Streams for Scala
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package object fs2 {

  /**
    * A stream transformation represented as a function from stream to stream.
    *
    * Pipes are typically applied with the `through` operation on `Stream`.
    */
  type Pipe[F[_], -I, +O] = Stream[F, I] => Stream[F, O]

  /**
    * A stream transformation that combines two streams in to a single stream,
    * represented as a function from two streams to a single stream.
    *
    * `Pipe2`s are typically applied with the `through2` operation on `Stream`.
    */
  type Pipe2[F[_], -I, -I2, +O] = (Stream[F, I], Stream[F, I2]) => Stream[F, O]

  /**
    * A pipe that converts a stream to a `Stream[F,Unit]`.
    *
    * Sinks are typically applied with the `to` operation on `Stream`.
    */
  @deprecated("Use Pipe[F, I, Unit] instead", "1.0.2")
  type Sink[F[_], -I] = Pipe[F, I, Unit]

  /**
    * Indicates that a stream evaluates no effects.
    *
    * A `Stream[Pure,O]` can be safely converted to a `Stream[F,O]` for all `F`.
    */
  type Pure[A] <: Nothing

  /**
    * Alias for `Nothing` which works better with type inference.
    */
  type INothing <: Nothing
}
