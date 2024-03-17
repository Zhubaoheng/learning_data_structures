package flik;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestFlik {
    @Test
    public void testIsSameNumber() {
        int i = 256;
        int j = 256;
        assertTrue(Flik.isSameNumber(i, j));
    }
}
