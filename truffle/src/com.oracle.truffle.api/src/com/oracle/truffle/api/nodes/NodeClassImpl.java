/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.graalvm.collections.list.SpecifiedArrayListImpl;

import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.Node.Children;

/**
 * Information about a {@link Node} class. A single instance of this class is allocated for every
 * subclass of {@link Node} that is used.
 */
@SuppressWarnings("deprecation")
final class NodeClassImpl extends NodeClass {
    private static final NodeFieldAccessor[] EMPTY_NODE_FIELD_ARRAY = new NodeFieldAccessor[0];

    // The comprehensive list of all fields.
    private final NodeFieldAccessor[] fields;
    private final NodeFieldAccessor parentField;

    private final Class<? extends Node> clazz;

    NodeClassImpl(Class<? extends Node> clazz) {
        super(clazz);
        if (!Node.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException();
        }

        List<NodeFieldAccessor> fieldsList = new ArrayList<>();
        NodeFieldAccessor parentFieldTmp = null;

        try {
            Field field = Node.class.getDeclaredField("parent");
            assert Node.class.isAssignableFrom(field.getType());
            parentFieldTmp = NodeFieldAccessor.create(NodeFieldAccessor.NodeFieldKind.PARENT, field);
        } catch (NoSuchFieldException e) {
            throw new AssertionError("Node field not found", e);
        }

        collectInstanceFields(clazz, fieldsList);

        Collections.sort(fieldsList, new Comparator<NodeFieldAccessor>() {
            public int compare(NodeFieldAccessor o1, NodeFieldAccessor o2) {
                return Integer.compare(order(o1), order(o2));
            }

            private int order(NodeFieldAccessor nodeField) {
                return isChildField(nodeField) ? 0 : (isChildrenField(nodeField) ? 1 : (isCloneableField(nodeField) ? 2 : 3));
            }
        });

        this.fields = fieldsList.toArray(EMPTY_NODE_FIELD_ARRAY);
        this.parentField = parentFieldTmp;
        this.clazz = clazz;
    }

    private static void collectInstanceFields(Class<? extends Object> clazz, List<NodeFieldAccessor> fieldsList) {
        if (clazz.getSuperclass() != null) {
            collectInstanceFields(clazz.getSuperclass(), fieldsList);
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }

            NodeFieldAccessor nodeField;
            if (field.getDeclaringClass() == Node.class && (field.getName().equals("parent") || field.getName().equals("nodeClass"))) {
                continue;
            } else if (field.getAnnotation(Child.class) != null) {
                checkChildField(field);
                nodeField = NodeFieldAccessor.create(NodeFieldAccessor.NodeFieldKind.CHILD, field);
            } else if (field.getAnnotation(Children.class) != null) {
                checkChildrenField(field);
                nodeField = NodeFieldAccessor.create(NodeFieldAccessor.NodeFieldKind.CHILDREN, field);
            } else {
                nodeField = NodeFieldAccessor.create(NodeFieldAccessor.NodeFieldKind.DATA, field);
            }
            fieldsList.add(nodeField);
        }
    }

    private static boolean isNodeType(Class<?> clazz) {
        return Node.class.isAssignableFrom(clazz) || (clazz.isInterface() && NodeInterface.class.isAssignableFrom(clazz));
    }

    private static void checkChildField(Field field) {
        if (!isNodeType(field.getType())) {
            throw new AssertionError("@Child field type must be a subclass of Node or an interface extending NodeInterface (" + field + ")");
        }
        if (Modifier.isFinal(field.getModifiers())) {
            throw new AssertionError("@Child field must not be final (" + field + ")");
        }
    }

    private static void checkChildrenField(Field field) {
        if (!(field.getType().isArray() && isNodeType(field.getType().getComponentType()))) {
            throw new AssertionError("@Children field type must be an array of a subclass of Node or an interface extending NodeInterface (" + field + ")");
        }
    }

    @Override
    public NodeFieldAccessor getParentField() {
        return parentField;
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeClassImpl) {
            NodeClassImpl other = (NodeClassImpl) obj;
            return clazz.equals(other.clazz);
        }
        return false;
    }

    @Override
    public Iterator<Node> makeIterator(Node node) {
        assert clazz.isInstance(node);
        return new NodeIterator(this, node, fields);
    }

    @Override
    public Class<? extends Node> getType() {
        return clazz;
    }

    @Override
    protected Iterable<NodeFieldAccessor> getNodeFields() {
        return getNodeFields(null);
    }

    /**
     * Functional interface equivalent to {@code Predicate<NodeFieldAccessor>}.
     */
    private interface NodeFieldFilter {
        boolean test(NodeFieldAccessor field);
    }

    private Iterable<NodeFieldAccessor> getNodeFields(final NodeFieldFilter filter) {
        return new Iterable<NodeFieldAccessor>() {
            public Iterator<NodeFieldAccessor> iterator() {
                return new Iterator<NodeFieldAccessor>() {
                    private int cursor = -1;
                    {
                        forward();
                    }

                    private void forward() {
                        for (int i = cursor + 1; i < fields.length; i++) {
                            NodeFieldAccessor field = fields[i];
                            if (filter == null || filter.test(field)) {
                                cursor = i;
                                return;
                            }
                        }
                        cursor = fields.length;
                    }

                    public boolean hasNext() {
                        assert cursor >= 0;
                        return cursor < fields.length;
                    }

                    public NodeFieldAccessor next() {
                        if (hasNext()) {
                            NodeFieldAccessor next = fields[cursor];
                            forward();
                            return next;
                        } else {
                            throw new NoSuchElementException();
                        }
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    @Override
    public NodeFieldAccessor[] getFields() {
        return iterableToArray(getNodeFields());
    }

    @Override
    public NodeFieldAccessor[] getChildFields() {
        return iterableToArray(getNodeFields(new NodeFieldFilter() {
            public boolean test(NodeFieldAccessor field) {
                return isChildField(field);
            }
        }));
    }

    @Override
    public NodeFieldAccessor[] getChildrenFields() {
        return iterableToArray(getNodeFields(new NodeFieldFilter() {
            public boolean test(NodeFieldAccessor field) {
                return isChildrenField(field);
            }
        }));
    }

    @Override
    public NodeFieldAccessor[] getCloneableFields() {
        return iterableToArray(getNodeFields(new NodeFieldFilter() {
            public boolean test(NodeFieldAccessor field) {
                return isCloneableField(field);
            }
        }));
    }

    private static NodeFieldAccessor[] iterableToArray(Iterable<NodeFieldAccessor> fields) {
        ArrayList<NodeFieldAccessor> fieldList = new ArrayList<>();
        for (NodeFieldAccessor field : fields) {
            fieldList.add(field);
        }
        return fieldList.toArray(new NodeFieldAccessor[0]);
    }

    @Override
    protected void putFieldObject(Object field, Node receiver, Object value) {
        ((NodeFieldAccessor) field).putObject(receiver, value);
    }

    @Override
    protected Object getFieldObject(Object field, Node receiver) {
        return ((NodeFieldAccessor) field).getObject(receiver);
    }

    @Override
    protected Object getFieldValue(Object field, Node receiver) {
        return ((NodeFieldAccessor) field).loadValue(receiver);
    }

    @Override
    protected Class<?> getFieldType(Object field) {
        return ((NodeFieldAccessor) field).getType();
    }

    @Override
    protected String getFieldName(Object field) {
        return ((NodeFieldAccessor) field).getName();
    }

    @Override
    protected boolean isChildField(Object field) {
        return ((NodeFieldAccessor) field).getKind() == NodeFieldAccessor.NodeFieldKind.CHILD;
    }

    @Override
    protected boolean isChildrenField(Object field) {
        return ((NodeFieldAccessor) field).getKind() == NodeFieldAccessor.NodeFieldKind.CHILDREN;
    }

    @Override
    protected boolean isCloneableField(Object field) {
        return ((NodeFieldAccessor) field).getKind() == NodeFieldAccessor.NodeFieldKind.DATA && NodeCloneable.class.isAssignableFrom(((NodeFieldAccessor) field).getType());
    }

    @Override
    boolean nodeFieldsOrderedByKind() {
        return true;
    }

}
