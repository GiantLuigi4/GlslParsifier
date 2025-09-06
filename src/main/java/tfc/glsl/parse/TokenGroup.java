package tfc.glsl.parse;

import java.util.HashSet;
import java.util.Set;

public enum TokenGroup {
    STORAGE_TYPE(
            TokenType.BUFFER,
            TokenType.IN,
            TokenType.OUT,
            TokenType.VARYING,
            TokenType.STRUCT,
            TokenType.UNIFORM,
            TokenType.STRUCT,
            TokenType.ATTRIBUTE
    ),
    ATTRIBUTE(
            TokenType.FLAT,
            TokenType.SMOOTH,
            TokenType.NO_PERSPECTIVE,
            TokenType.CENTROID,
            TokenType.CONST
    ),
    TYPE(
            TokenType.FLOAT,
            TokenType.INT,
            TokenType.BOOL,
            TokenType.VOID
    );

    private final Set<TokenType> types = new HashSet<>();

    TokenGroup(TokenType... types) {
        for (TokenType type : types) {
            this.types.add(type);
        }
    }

    public boolean isIn(TokenType type) {
        return types.contains(type);
    }
}
