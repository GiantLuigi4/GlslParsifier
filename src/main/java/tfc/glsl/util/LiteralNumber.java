package tfc.glsl.util;

import java.util.Objects;

public class LiteralNumber extends Number {
    private final String literal;
    private final boolean floatingPoint;
    private final boolean forceLong;
    private final boolean forceDouble;
    private final boolean interpFP;

    public LiteralNumber(String text) {
        this.literal = text;
        this.floatingPoint = text.contains(".");
        this.forceDouble = text.endsWith("d");
        this.forceLong = text.endsWith("l");
        this.interpFP = floatingPoint || text.endsWith("d");
        longValue(); // validate
    }

    public boolean isFloatingPoint() {
        return interpFP;
    }

    public boolean forcedLong() {
        return forceLong;
    }

    public boolean forcedDouble() {
        return forceDouble;
    }

    @Override
    public int intValue() {
        if (!floatingPoint) {
            try {
                return Integer.parseInt(literal);
            } catch (Throwable err) {
            }
        }
        return (int) floatValue();
    }

    @Override
    public long longValue() {
        if (!floatingPoint) {
            try {
                Long.parseLong(literal);
            } catch (Throwable err) {
            }
        }
        return (long) doubleValue();
    }

    @Override
    public float floatValue() {
        if (!forceDouble) {
            try {
                Float.parseFloat(literal);
            } catch (Throwable err) {
            }
        }
        return (float) doubleValue();
    }

    @Override
    public double doubleValue() {
        try {
            return Double.parseDouble(literal);
        } catch (Throwable err) {
        }
        throw new RuntimeException("Not a valid number");
    }

    @Override
    public String toString() {
        return literal;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LiteralNumber that = (LiteralNumber) object;
        return floatingPoint == that.floatingPoint && forceDouble == that.forceDouble && Objects.equals(literal, that.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(literal, floatingPoint, forceDouble);
    }
}
