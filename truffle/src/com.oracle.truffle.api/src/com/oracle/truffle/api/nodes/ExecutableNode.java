/*
 * Copyright (c) 2012, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package com.oracle.truffle.api.nodes;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * Represents an executable node in a Truffle AST. The executable node represents an AST fragment
 * that can be {@link #execute(VirtualFrame) executed} using a {@link VirtualFrame frame} instance
 * created by the framework.
 *
 * @see TruffleLanguage#parse(com.oracle.truffle.api.TruffleLanguage.InlineParsingRequest)
 * @since 0.31
 */
public abstract class ExecutableNode extends Node {

    final LanguageInfo languageInfo;

    /**
     * Creates new executable node with a given language instance. The language instance is
     * obtainable while {@link TruffleLanguage#parse(InlineParsingRequest)} is executed.
     *
     * @param language the language this executable node is associated with
     * @since 0.31
     */
    protected ExecutableNode(TruffleLanguage<?> language) {
        CompilerAsserts.neverPartOfCompilation();
        if (language != null) {
            this.languageInfo = Node.ACCESSOR.languageSupport().getLanguageInfo(language);
            if (languageInfo == null) {
                throw new IllegalArgumentException("Truffle language instance is not initialized.");
            }
        } else {
            this.languageInfo = null;
        }
    }

    /**
     * Execute this fragment at the place where it was parsed.
     *
     * @param frame the actual frame valid at the parsed location
     * @return the result of the execution
     * @since 0.31
     */
    public abstract Object execute(VirtualFrame frame);

    /**
     * Returns public information about the language. The language can be assumed equal if the
     * instances of the language info instance are the same. To access internal details of the
     * language within the language implementation use {@link #getLanguage(Class)}.
     *
     * @since 0.31
     */
    public final LanguageInfo getLanguageInfo() {
        return languageInfo;
    }

    /**
     * Returns the language instance associated with this executable node. The language instance is
     * intended for internal use in languages and is only accessible if the concrete type of the
     * language is known. Public information about the language can be accessed using
     * {@link #getLanguageInfo()}. The language is <code>null</code> if the executable node is not
     * associated with a <code>null</code> language.
     *
     * @see #getLanguageInfo()
     * @since 0.31
     */
    @SuppressWarnings("rawtypes")
    public final <C extends TruffleLanguage> C getLanguage(Class<C> languageClass) {
        if (languageInfo == null) {
            return null;
        }
        TruffleLanguage<?> language = languageInfo.getSpi();
        if (language.getClass() != languageClass) {
            if (!languageClass.isInstance(language) || languageClass == TruffleLanguage.class || !TruffleLanguage.class.isAssignableFrom(languageClass)) {
                CompilerDirectives.transferToInterpreter();
                throw new ClassCastException("Illegal language class specified. Expected " + language.getClass().getName() + ".");
            }
        }
        return languageClass.cast(language);
    }

}
