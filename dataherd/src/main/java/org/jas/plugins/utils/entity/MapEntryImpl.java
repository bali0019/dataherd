package org.jas.plugins.utils.entity;

import java.util.Map;

/**
 * Created by jabali on 3/5/17.
 */

final public class MapEntryImpl<K, V> implements Map.Entry<K, V>, Comparable<String> {
    private final K key;
    private V value;

    public MapEntryImpl(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }

    @Override
    public int compareTo(String o) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapEntryImpl<?, ?> mapEntry = (MapEntryImpl<?, ?>) o;

        return getKey().equals(mapEntry.getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
}