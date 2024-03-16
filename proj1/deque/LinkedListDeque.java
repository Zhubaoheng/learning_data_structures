package deque;

import java.util.Iterator;


public class LinkedListDeque<T> implements Deque<T>, Iterable<T>{

    private class Node<T> {
        public T item;
        public Node next;
        public Node prev;
        public Node(Node p, T i, Node n) {
            next = n;
            prev = p;
            item = i;
        }
    }

    private Node sentinel;
    private int size = 0;
    public LinkedListDeque() {
        sentinel = new Node(null, 100, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel.prev;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        Node newNode = new Node(sentinel, item, sentinel.next);
        sentinel.next.prev = newNode;
        sentinel.next = newNode;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        Node newNode = new Node(sentinel.prev, item, sentinel);
        sentinel.prev.next = newNode;
        sentinel.prev = newNode;

        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node p = sentinel;
        while (p.next != sentinel) {
            System.out.println(p.next.item);
            p = p.next ;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        T first = (T) sentinel.next.item;
        sentinel.next = sentinel.next.next;
        size -= 1;
        return first;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        T last = (T) sentinel.prev.item;
        sentinel.prev = sentinel.prev.prev;
        size -= 1;
        return last;
    }

    public T get(int index) {
        if (size == 0) {
            return null;
        }
        Node p = sentinel.next;
        for (int i = 0; i < index; i++) {
            p = p.next;
        }
        return (T) p.item;
    }

    public T getRecursive(int index) {
        return (T) getRecursive(index + 1, sentinel);
    }

    private T getRecursive(int index, Node p) {
        if (index == 0){
            return (T) p.item;
        }
        p = p.next;
        return getRecursive(index - 1, p);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Deque)) {
            return false;
        }
        LinkedListDeque<T> others = (LinkedListDeque<T>) o;
        if (others.size() != this.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            if (!others.get(i).equals(this.get(i))) {
                return false;
            }
        }
        return true;
    }

    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    private class LinkedListIterator implements Iterator<T> {
        int wizPos = 0;

        public boolean hasNext() {
            return wizPos < size();
        }
        public T next() {
            T returnItem = get(wizPos);
            wizPos += 1;
            return returnItem;
        }
    }
}
