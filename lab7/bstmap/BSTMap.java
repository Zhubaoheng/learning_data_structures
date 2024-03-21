package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class BSTNode<K extends Comparable, V> {
        private V value;
        private K key;

        private BSTNode left;
        private BSTNode right;
        public BSTNode(K key, V value) {
            this.value = value;
            this.key = key;
            this.right = null;
            this.left = null;
        }
    }
    private int size = 0;
    private BSTNode root;
    public BSTMap() {
        this.size = 0;
    }


    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return get(root, key) != null;
    }

    @Override
    public V get(K key) {
        if (get(root, key) == null) {
            return null;
        }
        return (V) get(root, key).value;
    }

    private BSTNode get(BSTNode node, K sk) {
        if (node == null) {
            return null;
        }
        if (sk.equals(node.key)) {
            return node;
        } else if (sk.compareTo((K) node.key) < 0) {
            return get(node.left, sk);
        } else {
            return get(node.right, sk);
        }
    }



    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (root == null) {
            root = put(root, key, value);
        } else {
            put(root, key, value);
        }

        size += 1;
    }

    private BSTNode put(BSTNode node, K key, V value) {
        if (node == null) {
            return new BSTNode<K, V>(key, value);
        }
        if (key.compareTo((K) node.key) < 0) {
            node.left = put(node.left, key, value);
        }else if (key.compareTo((K) node.key) > 0) {
            node.right = put(node.right, key, value);
        }
        return node;
    }


    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }


    public void printInOrder() {
        inOrder(root);
    }
    public void inOrder(BSTNode node) {
        if (node == null) {
            return;
        }
        inOrder(node.left);
        System.out.println(node.value);
        inOrder(node.right);
    }
    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }
}
