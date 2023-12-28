package fr.world.nations.assault.damager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixedSizeCache<K, V> {

    private final Map<K, V> list = new HashMap<>();
    private final List<K> keys;

    private final int maxSize;

    public FixedSizeCache(int size) {
        if (size < 1)
            throw new IllegalArgumentException("List size must be at least 1!");
        this.maxSize = size;
        this.keys = new ArrayList<>(size);
    }

    public void add(K key, V value) {
        if (list.containsKey(key))
            return;
        if (keys.size() >= maxSize) {
            list.remove(keys.get(0));
            keys.remove(0);
        }
        list.put(key, value);
        keys.add(key);
    }

    public V get(K key) {
        return list.get(key);
    }

}
