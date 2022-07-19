package com.hunterwb.automanifest;

public final class JoinToStringTest {

    public void testAll() {
        check("", '.');
        check("0", '.', 0);
        check("0.1", '.', 0, 1);
        check("0.1.2", '.', 0, 1, 2);
    }

    private static void check(String expected, char delimiter, Object... array) {
        String actual = Util.joinToString(array, delimiter);
        assert actual.equals(expected) : actual;
    }
}
