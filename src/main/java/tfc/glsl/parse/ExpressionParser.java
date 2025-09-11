package tfc.glsl.parse;

import tfc.glsl.base.GlslValue;
import tfc.glsl.value.OperationValue;

import java.util.*;
import java.util.function.Supplier;

public class ExpressionParser {
    private static final List<String[]> precedences = new ArrayList<>();
    private static final Set<String> allOps = new HashSet<>();
    private static final HashMap<String, Integer> opToPriority = new HashMap<>();

    static {
        precedences.add(new String[]{"*", "/", "%"});
        precedences.add(new String[]{"+", "-"});
        precedences.add(new String[]{"<<", ">>"});
        precedences.add(new String[]{"<", ">", "<=", ">="});
        precedences.add(new String[]{"==", "!="});
        precedences.add(new String[]{"&"});
        precedences.add(new String[]{"^"});
        precedences.add(new String[]{"|"});
        precedences.add(new String[]{"&&", "||"});

//        Collections.reverse(precedences);

        for (int i = 0; i < precedences.size(); i++) {
            String[] strs = precedences.get(i);
            for (String str : strs) {
                allOps.add(str);
                opToPriority.put(str, i);
            }
        }
    }

    public static GlslValue doParse(
            TokenStreamer streamer,
            Supplier<GlslValue> nextValue
    ) {
        List<GlslValue> values = new ArrayList<>();
        List<GlslToken> ops = new ArrayList<>();

        while (true) {
            values.add(nextValue.get());
            if (allOps.contains(streamer.current().string())) {
                ops.add(streamer.current());
                streamer.advance();
            } else {
                break;
            }
        }

        if (ops.isEmpty())
            return values.get(0);

        // TODO: this is horribly unoptimized!
        for (int i = 0; i < precedences.size(); i++) {
            for (int i1 = 0; i1 < ops.size(); i1++) {
                GlslToken token = ops.get(i1);
                if (opToPriority.get(token.string()) == i) {
                    GlslValue value0 = values.remove(i1);
                    GlslValue value1 = values.get(i1);
                    ops.remove(i1);
                    values.set(i1, new OperationValue(
                            value0,
                            token.string(),
                            value1
                    ));
                    i1--;
                }
            }
        }

        return values.get(0);
    }
}
