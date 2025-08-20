package tfc.glsl.meta.enums;

public enum StorageQualifier {
    UNIFORM("uniform"),
    STRUCT("struct"),
    BUFFER("buffer"),
    IN("in"),
    OUT("out"),
    VARYING("varying"),
    ;

    private final String typeName;

    StorageQualifier(String name) {
        this.typeName = name;
    }

    public String getTypeName() {
        return typeName;
    }
}
