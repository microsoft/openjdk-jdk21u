/*
 * Copyright (c) 2022, 2023, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @summary Testing Classfile limits.
 * @run junit LimitsTest
 */
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import jdk.internal.classfile.Classfile;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LimitsTest {

    @Test
    void testCPSizeLimit() {
        Classfile.build(ClassDesc.of("BigClass"), cb -> {
            for (int i = 1; i < 65000; i++) {
                cb.withField("field" + i, ConstantDescs.CD_int, fb -> {});
            }
        });
    }

    @Test
    void testCPOverLimit() {
        assertThrows(IllegalArgumentException.class, () -> Classfile.build(ClassDesc.of("BigClass"), cb -> {
            for (int i = 1; i < 66000; i++) {
                cb.withField("field" + i, ConstantDescs.CD_int, fb -> {});
            }
        }));
    }

    @Test
    void testCodeOverLimit() {
        assertThrows(IllegalArgumentException.class, () -> Classfile.build(ClassDesc.of("BigClass"), cb -> cb.withMethodBody(
                "bigMethod", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
                    for (int i = 0; i < 65535; i++) {
                        cob.nop();
                    }
                    cob.return_();
                })));
    }

    @Test
    void testEmptyCode() {
        assertThrows(IllegalArgumentException.class, () -> Classfile.build(ClassDesc.of("EmptyClass"), cb -> cb.withMethodBody(
                "emptyMethod", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {})));
    }
}
