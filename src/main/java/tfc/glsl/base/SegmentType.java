package tfc.glsl.base;

public enum SegmentType {
    BLOCK_DEF, // in, out, buffer, uniform, and struct blocks
    MEMBER_DEF,
    VAR_DEF,
    CODE,
    ARBITRARY,
    EXTENSION,
}
