package com.hunterwb.automanifest;

final class CommonPrefix<T> {

    private static final Object[] EMPTY = {};

    @SuppressWarnings("unchecked")
    T[] components = (T[]) EMPTY;

    void apply(T... components2) {
        if (components2 == null) throw new NullPointerException();
        if (components == EMPTY) {
            components = components2;
        } else {
            int n = Env.matchLength(components, components2);
            if (n != components.length) {
                components = Env.withLength(components2, n);
            }
        }
    }
}
