package deque;

import org.junit.Test;

import java.util.Comparator;

public class TestMaxArrayDeque {
    private class NameComparator implements Comparator<String> {
        public int compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }

    public Comparator<String> getNameComparator() {
        return new NameComparator();
    }

    private class CountComparator implements Comparator<String> {
        public int compare(String s1, String s2) {
            int s1len = s1.length();
            int s2len = s2.length();
            return s1len - s2len;
        }
    }

    public Comparator<String> countComparator() {
        return new CountComparator();
    }
    @Test
    public void addIsEmptySizeTest() {
        Comparator<String> c = countComparator();
        String maxItem;
        MaxArrayDeque<String> lld1 = new MaxArrayDeque<String>(c);

        lld1.addFirst("front");

        lld1.addLast("middle");

        lld1.addLast("back");
        maxItem = lld1.max(c);
        System.out.println(maxItem);

    }
}
