package org.graalvm.collections.list.primitives;

import java.util.Arrays;

public final class SimpleIntSpecifiedArrayList {

    // CONSTTANTS
    private final static int INITIAL_CAPACITY = 2; // Used on first insertion
    private final static int NEXT_CAPACITY = 16; // Capacity after first grow

    private final static int[] EMPTY_INTS = {};

    // Fields
    private transient int[] elementData;
    private int size = 0;

    public SimpleIntSpecifiedArrayList() {
        elementData = EMPTY_INTS;
    }

    public SimpleIntSpecifiedArrayList(final int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IndexOutOfBoundsException("Negative size: " + initialCapacity);
        } else if (initialCapacity == 0) {
            elementData = EMPTY_INTS;
        } else {
            elementData = new int[initialCapacity];
        }

    }

    public SimpleIntSpecifiedArrayList(final int[] collection) {
        this.size = collection.length;
        if (size != 0) {
            elementData = collection;
        } else {
            elementData = EMPTY_INTS;
        }
    }

    public final int size() {
        return size;
    }

    public final boolean isEmpty() {
        return size == 0;
    }

    public final boolean contains(int e) {
        return indexOf(e) >= 0;
    }

    public final int[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    public final boolean add(int e) {
        ensureCapacity(size + 1);
        elementData[size++] = e;
        return true;
    }

    public final void add(int index, int element) {
        checkBoundsForAdd(index);
        ensureCapacity(size + 1);

        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;

    }

// public int removeIdx(int index) {
// checkBoundaries(index);
// final int oldElem = elementData[index];
// if (size != index)
// System.arraycopy(elementData, index + 1, elementData, index, size - index - 1);
// return oldElem;
// }

    // TODO remove this
    public final boolean remove(int e) {
        for (int i = 0; i < size; i++) {
            if (e == elementData[i]) {
                fastRemove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the Object at given index without any checks.
     *
     * @param index index of object to be removed
     */
    private final void fastRemove(final int index) {
        System.arraycopy(elementData, index + 1, elementData, index, size - index - 1);
        size--;
    }

    public final void clear() {
        size = 0;
        elementData = EMPTY_INTS;
    }

    public final int get(int index) {
        checkBoundaries(index);
        return elementData[index];
    }

    public final int set(int index, int element) {
        checkBoundaries(index);
        final int oldVal = elementData[index];
        elementData[index] = element;
        return oldVal;
    }

    public final int indexOf(int e) {
        for (int i = 0; i < size; i++)
            if (e == elementData[i])
                return i;

        return -1;
    }

    public final int lastIndexOf(int e) {
        for (int i = size - 1; i > -1; i--)
            if (e == elementData[i])
                return i;

        return -1;
    }

    public final void ensureCapacity(final int capacity) {
        if (elementData.length < capacity) {
            grow(capacity);
        }

    }

    private void grow(int minCapacity) {
        final int curCapacity = elementData.length;
        if (elementData == EMPTY_INTS) {
            elementData = new int[Math.max(INITIAL_CAPACITY, minCapacity)];
        } else if (curCapacity <= NEXT_CAPACITY) {
            elementData = Arrays.copyOf(elementData, Math.max(NEXT_CAPACITY, minCapacity));
        } else {
            // final int nextCapacity = curCapacity + (curCapacity >> 1);
            // final int nextCapacity = curCapacity + NEXT_CAPACITY;

// if (curCapacity < 12) {
// newLength = curCapacity + 4;
// } else {
            final int newLength = curCapacity << 1; // Times 2
            // }
            elementData = Arrays.copyOf(elementData, Math.max(newLength, minCapacity));
        }
    }

    private void checkBoundaries(final int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size " + size);
    }

    private void checkBoundsForAdd(final int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size " + size);
    }

}