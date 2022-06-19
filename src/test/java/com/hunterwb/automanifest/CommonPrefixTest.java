package com.hunterwb.automanifest;

import java.util.Arrays;

public final class CommonPrefixTest {

    public void test1() {
        CommonPrefix<Integer> cp = new CommonPrefix<Integer>();
        cp.apply(0);
        check(cp, 0);
    }

    public void test2() {
        CommonPrefix<Integer> cp = new CommonPrefix<Integer>();
        cp.apply(0);
        cp.apply(1);
        check(cp);
    }

    public void test3() {
        CommonPrefix<Integer> cp = new CommonPrefix<Integer>();
        cp.apply(0, 0);
        cp.apply(0, 1);
        check(cp, 0);
    }

    public void test4() {
        CommonPrefix<Integer> cp = new CommonPrefix<Integer>();
        cp.apply(0);
        cp.apply(0, 1);
        check(cp, 0);
    }

    public void test5() {
        CommonPrefix<Integer> cp = new CommonPrefix<Integer>();
        check(cp);
    }

    public void test6() {
        CommonPrefix<Integer> cp = new CommonPrefix<Integer>();
        cp.apply();
        check(cp);
    }

    public void test7() {
        CommonPrefix<Integer> cp = new CommonPrefix<Integer>();
        cp.apply();
        cp.apply(0);
        cp.apply(1);
        check(cp);
    }

    public void test8() {
        CommonPrefix<Integer> cp = new CommonPrefix<Integer>();
        cp.apply(0, 1);
        cp.apply(0);
        cp.apply();
        check(cp);
    }

    public void test9() {
        CommonPrefix<Integer> cp = new CommonPrefix<Integer>();
        cp.apply(0, 1, 2);
        cp.apply(0, 1, 2);
        cp.apply(0, 1, 2, 3);
        check(cp, 0, 1, 2);
    }

    public void test10() {
        CommonPrefix<Integer> cp = new CommonPrefix<Integer>();
        cp.apply(0, 1, 2);
        check(cp, 0, 1, 2);
    }

    private static <T> void check(CommonPrefix<T> commonPrefix, T... expected) {
        assert Arrays.equals(commonPrefix.components, expected) : Arrays.toString(commonPrefix.components);
    }
}
