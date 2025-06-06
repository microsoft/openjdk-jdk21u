/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.bench.java.lang.invoke;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import sun.invoke.util.Wrapper;

/**
 * Test sun.invoke.util.Wrapper accessors
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 3, jvmArgs = "--add-exports=java.base/sun.invoke.util=ALL-UNNAMED")
public class Wrappers {

    public static Class<?>[] PRIM_CLASSES = {
        int.class,
        float.class,
        short.class,
        double.class,
        void.class,
        boolean.class,
        byte.class,
        char.class,
        long.class };

    public static Class<?>[] WRAP_CLASSES = {
        Integer.class,
        Float.class,
        Short.class,
        Double.class,
        Void.class,
        Boolean.class,
        Byte.class,
        Character.class,
        Long.class,
        Object.class };
    public static char[] BASIC_TYPES = {
        'I',
        'J',
        'S',
        'B',
        'C',
        'F',
        'D',
        'Z',
        'V',
        'L' };
    public static char[] PRIM_TYPES = {
        'I',
        'J',
        'S',
        'B',
        'C',
        'F',
        'D',
        'Z',
        'V' };

    @Benchmark
    public void forPrimitive(Blackhole bh) throws Throwable {
        for (Class<?> c : PRIM_CLASSES) {
            bh.consume(Wrapper.forPrimitiveType(c));
        }
    }

    @Benchmark
    public void forWrapper(Blackhole bh) throws Throwable {
        for (Class<?> c : WRAP_CLASSES) {
            bh.consume(Wrapper.forWrapperType(c));
        }
    }

    @Benchmark
    public void forBasicType(Blackhole bh) throws Throwable {
        for (char c : BASIC_TYPES) {
            bh.consume(Wrapper.forBasicType(c));
        }
    }

    @Benchmark
    public void forPrimitiveType(Blackhole bh) throws Throwable {
        for (char c : PRIM_TYPES) {
            bh.consume(Wrapper.forPrimitiveType(c));
        }
    }
}
