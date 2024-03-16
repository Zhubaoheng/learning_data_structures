package deque;
import java.util.Comparator;


public class MaxArrayDeque<T> extends ArrayDeque<T> {

    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        this.comparator = c;
    }
    public T max() {
        return max(comparator);
    }
    public T max(Comparator<T> c) {
        if (this.isEmpty()) {
            return null;
        }
        T maxItem = this.get(0);
        for (int i = 0; i < this.size(); i++) {
            if (c.compare(this.get(i), maxItem) > 0) {
                maxItem = this.get(i);
            }
        }
        return maxItem;
    }

}
