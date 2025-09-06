package tfc.glsl.util;

public final class Pair<T, V> {
    private final T t;
    private final V v;

    public Pair(T t, V v) {
        this.t = t;
        this.v = v;
    }

    public static <T, V> Pair<T, V> of(T first, V second) {
        return new Pair<>(first, second);
    }

    public T getFirst() {
        return t;
    }

    public V getSecond() {
        return v;
    }
}
