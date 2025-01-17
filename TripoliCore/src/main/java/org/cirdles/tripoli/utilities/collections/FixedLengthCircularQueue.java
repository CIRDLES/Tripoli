package org.cirdles.tripoli.utilities.collections;

public class FixedLengthCircularQueue<T> {
    private final T[] array;

    public FixedLengthCircularQueue(T[] values) {
        this.array = values;
    }

    public T get(int index) {
        return this.array[index % this.array.length];
    }

    public void put(int index, T val) {
        this.array[index % this.array.length] = val;
    }

    public int length() {
        return array.length;
    }
}
