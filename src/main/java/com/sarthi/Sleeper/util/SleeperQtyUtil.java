package com.sarthi.Sleeper.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Quantity helpers for Sleeper final inspection. Quantities can be fractional
 * (e.g. RMT calls), so they are kept as BigDecimal with a fixed scale instead of int.
 */
public final class SleeperQtyUtil {

    /** Fractional digits stored for inspection quantities (matches DECIMAL(12,2) columns). */
    public static final int QTY_SCALE = 2;

    private SleeperQtyUtil() {
    }

    /** Converts a Number / numeric String to a BigDecimal with QTY_SCALE; null when not numeric. */
    public static BigDecimal toQty(Object value) {
        if (value == null) {
            return null;
        }
        try {
            BigDecimal bd;
            if (value instanceof BigDecimal) {
                bd = (BigDecimal) value;
            } else if (value instanceof Double || value instanceof Float) {
                bd = BigDecimal.valueOf(((Number) value).doubleValue());
            } else if (value instanceof Number) {
                bd = BigDecimal.valueOf(((Number) value).longValue());
            } else {
                String s = value.toString().trim();
                if (s.isEmpty()) {
                    return null;
                }
                bd = new BigDecimal(s);
            }
            return bd.setScale(QTY_SCALE, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Null-safe zero default. */
    public static BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO.setScale(QTY_SCALE);
    }

    /** true when the value is non-null and greater than zero. */
    public static boolean isPositive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    /** Accepted = Offered - Rejected, never negative. Null inputs count as zero. */
    public static BigDecimal acceptedOf(BigDecimal offered, BigDecimal rejected) {
        BigDecimal result = orZero(offered).subtract(orZero(rejected));
        return result.signum() < 0 ? BigDecimal.ZERO.setScale(QTY_SCALE) : result.setScale(QTY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Quantity as plain text without trailing zeros or exponent notation: 4238.25 -> "4238.25", 59490.00 -> "59490".
     * Null when the value is null.
     */
    public static String plain(BigDecimal value) {
        if (value == null) {
            return null;
        }
        BigDecimal stripped = value.stripTrailingZeros();
        if (stripped.scale() < 0) {
            stripped = stripped.setScale(0);
        }
        return stripped.toPlainString();
    }

    /** true when {@code a} is greater than {@code b} (both non-null). */
    public static boolean greaterThan(BigDecimal a, BigDecimal b) {
        return a != null && b != null && a.compareTo(b) > 0;
    }
}
