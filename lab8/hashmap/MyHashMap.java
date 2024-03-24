package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */

    private Set<K> keyset;
    private Collection<Node>[] buckets;
    // You should probably define some more!

    //在哈希表中的元素数量N
    private int N = 0;

    //哈希表的桶数量M
    private int M;
    //负载系数
    private double loadFactor;

    /** Constructors */
    public MyHashMap() {
        this(16, 0.75);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        if (initialSize < 1 || maxLoad <= 0.0) {
            throw new IllegalArgumentException();
        }
        buckets = createTable(initialSize);
        keyset = new HashSet<>();
        M = initialSize;
        loadFactor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */

    protected Collection<Node> createBucket() {
        return new ArrayList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return N;
    }

    @Override
    public void clear() {
        Collection<Node>[] clear = createTable(M);
        buckets = clear;
        N = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return this.get(key) != null;
    }

    private int getHashCode(K key) {
        return Math.floorMod(key.hashCode(), M);
    }
    @Override
    public V get(K key) {
        int hashCode = getHashCode(key);
        if (buckets[hashCode] == null) {
            return null;
        }
        for (Node node : buckets[hashCode]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public void put(K key, V value) {
        double factor = (double)N / M;
        if (factor > loadFactor) {
            resize();
        }
        add(buckets, key, value);
        N += 1;
        keyset.add(key);
    }

    private void add(Collection<Node>[] addBuckets, K key, V value) {
        int hashCode = getHashCode(key);
        if (addBuckets[hashCode] == null) {
            addBuckets[hashCode] = createBucket();
            Node node = createNode(key, value);
            addBuckets[hashCode].add(node);
        }else {
            for (Node node : addBuckets[hashCode]) {
                if (node.key.equals(key)) {
                    N -= 1;
                    if (node.value.equals(value)){
                        return;
                    }else {
                        node.value = value;
                        return;
                    }
                }
            }
            Node node = createNode(key, value);
            addBuckets[hashCode].add(node);
        }
    }
    private void resize() {
        int newM = M * 2;
        Collection<Node>[] newBuckets = createTable(newM);
        for (K key : keyset) {
            add(newBuckets, key, get(key));
        }
        buckets = newBuckets;
        M = newM;
    }

    @Override
    public Set<K> keySet() {
        return keyset;
    }

    @Override
    public Iterator<K> iterator() {
        return keyset.iterator();
    }


}
