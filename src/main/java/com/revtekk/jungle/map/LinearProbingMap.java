package com.revtekk.jungle.map;

import java.util.*;
import java.util.function.Function;

/**
 * The {@code LinearProbingMap} class implements a hash map with a linear probing, open
 * addressing scheme
 *
 * This map implementation does not currently support removal of elements from any of the
 * views, i.e. keySet(), values(), or entrySet() and doing so will throw an exception.
 *
 * @param <K> the type of keys in the map
 * @param <V> the type of values in the map
 */
public class LinearProbingMap<K, V> extends AbstractMap<K, V>
{
    private static final int INITIAL_CAPACITY = 256;
    private static final float LOAD_CAPACITY = 0.75f;

    private MapEntry<K, V>[] map;
    private int size;

    // Internal magic
    private final InternalKeySet keySet = new InternalKeySet();
    private final InternalEntrySet entrySet = new InternalEntrySet();
    private final InternalValueCollection valueCollection = new InternalValueCollection();

    /**
     * Create a new linear probing hashmap
     */
    public LinearProbingMap()
    {
        this.map = new MapEntry[INITIAL_CAPACITY];
    }

    /**
     * Create a new linear probing hashmap with the given initial capacity
     *
     * As the hashmap is used and starts to get full, the underlying backing array
     * will be resized, so the specified capacity is "initial" and not a bounding capacity
     *
     * @param initialCapacity initial requested capacity
     */
    public LinearProbingMap(int initialCapacity)
    {
        this.map = new MapEntry[initialCapacity];
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public boolean isEmpty()
    {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key)
    {
        Boolean raw = search(key, (pos) -> {
            if(map[pos] == null || map[pos].isDeleted())
                return false;

            return map[pos].getKey().equals(key);
        }, map);

        // Handles edge case where search returns null
        if(raw == null)
            return false;

        return raw;
    }

    @Override
    public boolean containsValue(Object value)
    {
        for(MapEntry<K, V> entry : map)
        {
            if(entry == null || entry.isDeleted())
                continue;

            // Handle null value edge case
            if(entry.getValue() == null)
            {
                if(value == null)
                    return true;
                else
                    continue;
            }

            if(entry.getValue().equals(value))
                return true;
        }

        return false;
    }

    @Override
    public V get(Object key)
    {
        return search(key, (pos) -> {
            if(map[pos] == null || map[pos].isDeleted())
                return null;

            return map[pos].getValue();

        }, map);
    }

    @Override
    public V put(K key, V value)
    {
        // Resize if the map is getting close to being full
        if((float)size / map.length > LOAD_CAPACITY)
        {
            MapEntry<K, V>[] next = new MapEntry[map.length * 2];
            size = 0;

            // Copy over the entries into the new map
            for (MapEntry<K, V> curr : map)
            {
                if(curr == null || curr.isDeleted())
                    continue;

                put(curr.getKey(), curr.getValue(), next);
            }

            map = next;
        }

        return put(key, value, map);
    }

    @Override
    public V remove(Object key)
    {
        return search(key, (pos) -> {
            // Nothing to remove -- return null
            if(map[pos] == null || map[pos].isDeleted())
                return null;

            map[pos].markAsDeleted();
            size--;

            return map[pos].getValue();
        }, map);
    }

    @Override
    public void clear()
    {
        Arrays.fill(map, null);
        size = 0;
    }

    @Override
    public Set<K> keySet()
    {
        return keySet;
    }

    @Override
    public Collection<V> values()
    {
        return valueCollection;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        return entrySet;
    }

    /**
     * Compute the starting index of the object within the map
     *
     * This result will be used to start the search within the map for
     * the given key and is determine by the key's hash and the map's capacity
     *
     * @param key key object
     * @param bound bounding size of the map (usually the length)
     * @return the starting index
     */
    private int computeIndex(Object key, int bound)
    {
        return key.hashCode() % bound;
    }

    /**
     * Perform a search for the given key within the provided map
     *
     * The search will try to find the key within the map:
     *   1. If the key is found, call func(pos), where pos is the position within the map for the key
     *   2. If the key isn't found, call func(pos) on the first null entry in the map
     *
     * FIXME: there is a subtle bug! When func() is a get, the search exists prematurely when it encounters
     *  a deleted entry returning null; however, it should just skip deleted entries, which is a different use-case
     *  than with put() which should overwrite a deleted entry
     *
     * Users of this method can perform whatever action on the map entry and return whatever value, as needed.
     *
     * @param key key object
     * @param func function to call on the located map entry
     * @param map map to use
     * @return whatever the parameter func returns (input dependent)
     */
    private <R> R search(Object key, Function<Integer, R> func, MapEntry<K, V>[] map)
    {
        int pos = computeIndex(key, map.length);

        for(int i = pos; i < map.length; i++)
        {
            if(map[i] == null || map[i].isDeleted())
                return func.apply(i);

            if(!map[i].getKey().equals(key))
                continue;

            return func.apply(i);
        }

        for(int i = 0; i < pos; i++)
        {
            if(map[i] == null || map[i].isDeleted())
                return func.apply(i);

            if(!map[i].getKey().equals(key))
                continue;

            return func.apply(i);
        }

        return null;
    }

    /**
     * Put the (key, value) pair in the provided map
     *
     * This method takes in a map as a parameter to allow for reuse when the map
     * needs to be resized. This way, the new map can be used as a target for inserting
     * all the old keys before the old map is discarded and replaced with the new map.
     *
     * @param key key object
     * @param value value object
     * @param map map to insert into
     * @return the old value if it exists, otherwise null
     */
    private V put(K key, V value, MapEntry<K, V>[] map)
    {
        return search(key, (pos) -> {
            if(map[pos] == null || map[pos].isDeleted())
            {
                map[pos] = new MapEntry<>(key, value);
                size++;

                return null;
            }
            else
            {
                return map[pos].setValue(value);
            }

        }, map);
    }

    /**
     * The {@code MapEntry} class implements a key-value pair entry which resides within
     * the {@code LinearProbingMap} implementation
     *
     * The instances of this class also contain a marker for deletion, which is a required property
     * for correctness within the linear probing, open addressing hashmap scheme.
     *
     * When the 'deleted' flag is set, the entry is to be considered "effectively null", i.e. can be
     * safely overwritten by a put() or skipped by a get()
     *
     * @param <K> key type
     * @param <V> value type
     */
    private static final class MapEntry<K, V> implements Map.Entry<K, V>
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

    /**
     * Internal key set representation
     *
     * This set is special in that it is linked internally to the underlying
     * map entries and so modifications to the map are reflected within the set.
     *
     * The actual "magic" that makes everything work is via the implementation of
     * the iterator, which does the map traversal (internally)
     */
    private class InternalKeySet extends AbstractSet<K>
    {
        @Override
        public Iterator<K> iterator()
        {
            return new InternalKeySetIterator();
        }

        @Override
        public int size()
        {
            return LinearProbingMap.this.size();
        }

        @Override
        public boolean contains(Object o)
        {
            return LinearProbingMap.this.containsKey(o);
        }
    }

    /**
     * Internal key set iterator
     *
     * This iterator implements the required logic to iterate (linearly) over the entire
     * map and return one-by-one through calls to next() the keys within the map, until all
     * are exhausted and hasNext() returns false
     */
    private class InternalKeySetIterator implements Iterator<K>
    {
        private int pos = 0;

        public InternalKeySetIterator()
        {
            // Setup initial position
            computeNext();
        }

        @Override
        public boolean hasNext()
        {
            return pos < map.length;
        }

        @Override
        public K next()
        {
            if(!hasNext())
                throw new NoSuchElementException("end of map");

            K key = map[pos].getKey();

            pos++;
            computeNext();

            return key;
        }

        /**
         * Computes the next valid key-value pair entry within the map
         */
        private void computeNext()
        {
            // Move pos to the next valid position
            while(pos < map.length && (map[pos] == null || map[pos].isDeleted()))
                pos++;
        }
    }

    /**
     * Internal entry set representation
     *
     * This set is special in that it is linked internally to the underlying
     * map entries and so modifications to the map are reflected within the set.
     *
     * The actual "magic" that makes everything work is via the implementation of
     * the iterator, which does the map traversal (internally)
     */
    private class InternalEntrySet extends AbstractSet<Map.Entry<K, V>>
    {
        @Override
        public Iterator<Entry<K, V>> iterator()
        {
            return new InternalEntrySetIterator();
        }

        @Override
        public int size()
        {
            return LinearProbingMap.this.size();
        }

        @Override
        public boolean contains(Object o)
        {
            // Give up and do a iterator based equality
            if(!(o instanceof MapEntry))
                return super.contains(o);

            MapEntry<K, V> entry = (MapEntry<K,V>)o;
            V value = LinearProbingMap.this.get(entry.getKey());

            if(value == null)
                return entry.getValue() == null;

            return value.equals(entry.getValue());
        }
    }

    /**
     * Internal entry set iterator
     *
     * This iterator implements the required logic to iterate (linearly) over the entire
     * map and return one-by-one through calls to next() the entries within the map, until all
     * are exhausted and hasNext() returns false
     */
    private class InternalEntrySetIterator implements Iterator<Map.Entry<K, V>>
    {
        private int pos = 0;

        public InternalEntrySetIterator()
        {
            computeNext();
        }

        @Override
        public boolean hasNext()
        {
            return pos < map.length;
        }

        @Override
        public Map.Entry<K, V> next()
        {
            if(!hasNext())
                throw new NoSuchElementException("end of map");

            MapEntry<K, V> entry = map[pos];

            pos++;
            computeNext();

            return entry;
        }

        /**
         * Computes the next valid key-value pair entry within the map
         */
        private void computeNext()
        {
            // Move pos to the next valid position
            while(pos < map.length && (map[pos] == null || map[pos].isDeleted()))
                pos++;
        }
    }

    /**
     * Internal value collection representation
     *
     * This collection is special in that it is linked internally to the underlying
     * map entries and so modifications to the map are reflected within the collection.
     *
     * The actual "magic" that makes everything work is via the implementation of
     * the iterator, which does the map traversal (internally)
     */
    private class InternalValueCollection extends AbstractCollection<V>
    {
        @Override
        public Iterator<V> iterator()
        {
            return new InternalValueCollectionIterator();
        }

        @Override
        public int size()
        {
            return LinearProbingMap.this.size();
        }
    }

    /**
     * Internal value collection iterator
     *
     * This iterator implements the required logic to iterate (linearly) over the entire
     * map and return one-by-one through calls to next() the values within the map, until all
     * are exhausted and hasNext() returns false
     */
    private class InternalValueCollectionIterator implements Iterator<V>
    {
        private int pos = 0;

        public InternalValueCollectionIterator()
        {
            computeNext();
        }

        @Override
        public boolean hasNext()
        {
            return pos < map.length;
        }

        @Override
        public V next()
        {
            if(!hasNext())
                throw new NoSuchElementException("end of map");

            V value = map[pos].getValue();

            pos++;
            computeNext();

            return value;
        }

        /**
         * Computes the next valid key-value pair entry within the map
         */
        private void computeNext()
        {
            // Move pos to the next valid position
            while(pos < map.length && (map[pos] == null || map[pos].isDeleted()))
                pos++;
        }
    }
}
