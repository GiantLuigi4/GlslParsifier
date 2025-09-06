package tfc.glsl.parse;

import tfc.glsl.util.Pair;
import tfc.glsl.util.StringReader;

import java.util.*;

public class PrefixChecker<T> {
    List<Pair<Character, List<Pair<String, T>>>> things = new ArrayList<>();
    Map<Character, List<Pair<String, T>>> chrLookup = new HashMap<>();

    public PrefixChecker() {
    }

    public void add(String key, T value) {
        char chr = key.charAt(0);
        String str = key.substring(1);

        List<Pair<String, T>> pairs = chrLookup.get(chr);
        if (pairs == null) {
            pairs = new ArrayList<>();
            chrLookup.put(chr, pairs);
            things.add(Pair.of(chr, pairs));
        }

        pairs.add(Pair.of(str, value));
    }

    public PrefixChecker<T> prepare() {
        for (Pair<Character, List<Pair<String, T>>> thing : this.things) {
            thing.getSecond().sort(Comparator.comparingInt(a -> -a.getFirst().length()));
        }
        return this;
    }

    public T find(StringReader text) {
        char firstChr = text.charAt(0);
        for (Pair<Character, List<Pair<String, T>>> thing : things) {
            if (firstChr == thing.getFirst()) {
                text.skip(1);
                List<Pair<String, T>> pairs = thing.getSecond();
                for (Pair<String, T> pair : pairs) {
                    if (text.startsWith(pair.getFirst())) {
                        text.skip(-1);
                        return pair.getSecond();
                    }
                }
                text.skip(-1);
            }
        }
        return null;
    }
}
