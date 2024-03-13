package deque;

public class ArrayDeque<T> implements Deque<T> {
    private int vaildSize = 0;
    public int capacity = 8;
    private T[] items;
    private int nextFirst = 4;
    private int nextLast = 5;

    public ArrayDeque() {
        items = (T[]) new Object[capacity];
        vaildSize = 0;
    }

    @Override
    public void addFirst(T item) {
        if (capacity == vaildSize) {
            resizeBigger(capacity * 2);
        }
        items[nextFirst] = item;
        nextFirst = checkLowerBound(nextFirst - 1);
        vaildSize += 1;
    }

    @Override
    public void addLast(T item) {
        if (capacity == vaildSize) {
            resizeBigger(capacity * 2);
        }
        items[nextLast] = item;
        nextLast = checkUpperBound(nextLast + 1);
        vaildSize += 1;
    }

    private int checkLowerBound(int i) {
        if (i < 0) {
            return capacity - 1;
        }
        return i;
    }

    private int checkUpperBound(int i) {
        if (i > capacity - 1) {
            return 0;
        }
        return i;
    }

    @Override
    public boolean isEmpty() {
        return vaildSize == 0;
    }

    @Override
    public int size() {
        return vaildSize;
    }

    @Override
    public void printDeque() {
        int printNum = nextFirst - 1;
        for (int i = 0; i < vaildSize; i++) {
            System.out.println(printNum);
            printNum = checkUpperBound(printNum + 1);
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (checkResizeSmaller()) {
            resizeSmaller(capacity / 2);
        }

        if (vaildSize == 0) {
            return null;
        }
        T first = items[checkUpperBound(nextFirst + 1)];
        items[checkUpperBound(nextFirst + 1)] = null;
        nextFirst = checkUpperBound(nextFirst + 1);
        vaildSize -= 1;

        return first;
    }

    @Override
    public T removeLast() {
        if (checkResizeSmaller()) {
            resizeSmaller(capacity / 2 );
        }

        if (vaildSize == 0) {
            return null;
        }
        T last = items[checkLowerBound(nextLast - 1)];
        items[checkLowerBound(nextLast - 1)] = null;
        nextLast = checkLowerBound(nextLast - 1);
        vaildSize -= 1;

        return last;
    }

    @Override
    public T get(int index) {
        return items[checkUpperBound(nextFirst + 1 + index)];
    }

    private boolean ifOnePart() {
        return checkLowerBound(nextLast - 1) > checkUpperBound(nextFirst + 1);
    }

    private boolean ifTwoParts() {
        return checkLowerBound(nextLast - 1) < checkUpperBound(nextFirst + 1);
    }

    private boolean checkResizeSmaller() {
        return ((double) vaildSize / capacity <= 0.25) && capacity >= 16;
    }

    public void resizeBigger(int c) {
        T[] a = (T[]) new Object[c];
        if (ifOnePart()) {
            System.arraycopy(items, checkUpperBound(nextFirst + 1), a, checkUpperBound(nextFirst + 1), vaildSize);
            nextLast = vaildSize;
            nextFirst = c - 1;
        }
        else if (ifTwoParts()) {
            //复制前半部分
            System.arraycopy(items, 0, a, 0, nextLast);
            //复制后半部分
            System.arraycopy(items, nextFirst + 1, a, c - (vaildSize - nextLast) , vaildSize - nextLast);
            nextFirst = c - (vaildSize - nextLast) - 1;
        }
        capacity = c;
        items = a;

    }

    public void resizeSmaller(int c) {
        T[] a = (T[]) new Object[c];
        if (ifOnePart()) {
            System.arraycopy(items, nextFirst + 1, a, 0, vaildSize);
            nextFirst = c - 1;
            nextLast = vaildSize;
        }
        else if (ifTwoParts()){
            //复制前半部分
            System.arraycopy(items, 0, a, 0, nextLast);
            //复制后半部分
            System.arraycopy(items, nextFirst + 1, a, c - (vaildSize - nextLast), vaildSize - nextLast);
            nextFirst = c - (vaildSize - nextLast) - 1;
        }
        capacity = c;
        items = a;

    }

}
