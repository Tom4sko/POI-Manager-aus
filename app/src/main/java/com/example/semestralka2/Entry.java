package com.example.semestralka2;

public class Entry<K, T> {
    private final K key;
    private final T data;

    public Entry(K key, T data) {
        this.key = key;
        this.data = data;
    }

    public K getKey() {
        return key;
    }

    public T getData() {
        return data;
    }
}