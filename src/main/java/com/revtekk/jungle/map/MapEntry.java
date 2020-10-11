package com.revtekk.jungle.map;

import java.util.Map;

final class MapEntry<K, V> implements Map.Entry<K, V>
{
    private final K key;
    private V value;
    private boolean deleted;

    public MapEntry(K key, V value)
    {
        this.key = key;
        this.value = value;
        this.deleted = false;
    }

    @Override
    public K getKey()
    {
        return key;
    }

    @Override
    public V getValue()
    {
        return value;
    }

    @Override
    public V setValue(V value)
    {
        V prev = this.value;
        this.value = value;

        return prev;
    }

    /**
     * Mark the entry as deleted
     */
    public void markAsDeleted()
    {
        deleted = true;
    }

    /**
     * Check the delete status of the entry
     * @return true if deleted, false otherwise
     */
    public boolean isDeleted()
    {
        return deleted;
    }
}
