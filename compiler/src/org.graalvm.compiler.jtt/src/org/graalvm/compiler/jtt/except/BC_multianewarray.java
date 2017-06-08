/*
 * Copyright (c) 2007, 2012, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright (c) 2007, 2012, Oracle and/or its affiliates. All rights reserved.
 */
package org.graalvm.compiler.jtt.except;

import org.junit.Test;

import org.graalvm.compiler.jtt.JTTTest;

/*
 */
public class BC_multianewarray extends JTTTest {

    @SuppressWarnings("unused")
    public static int test(int a, int b) {
        final DummyTestClass[][] v = new DummyTestClass[a][b];
        if (v != null) {
            return a;
        }
        return -1;
    }

    @Test
    public void run0() throws Throwable {
        runTest("test", 0, 0);
    }

    @Test
    public void run1() throws Throwable {
        runTest("test", 1, 1);
    }

    @Test
    public void run2() throws Throwable {
        runTest("test", -1, 0);
    }

    @Test
    public void run3() throws Throwable {
        runTest("test", 0, -1);
    }

}