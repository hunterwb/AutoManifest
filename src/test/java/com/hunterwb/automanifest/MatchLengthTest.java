package com.hunterwb.automanifest;

public final class MatchLengthTest {

    public void testAll() {
        check(array(), array(), 0);
        check(array(), array(0), 0);
        check(array(), array(0, 1), 0);
        check(array(0), array(0), 1);
        check(array(0), array(1), 0);
        check(array(0), array(0, 1), 1);
        check(array(0, 1), array(0, 1), 2);
    }

    private static <T> T[] array(T... array) {
        return array;
    }

    private static <T> void check(T[] a1, T[] a2, int expected) {
        int actual1 = Env.matchLength(a1, a2);
        assert actual1 == expected : actual1;
        int actual2 = Env.matchLength(a2, a1);
        assert actual2 == expected : actual2;
    }
}
