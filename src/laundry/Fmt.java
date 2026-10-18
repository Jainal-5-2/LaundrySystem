package laundry;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Central place for number and currency formatting.
 * All formatters use Locale.US so grouping is a comma and decimals are a dot,
 * regardless of the JVM's default locale.
 */
public final class Fmt {

    private Fmt() {}

    private static final NumberFormat MONEY;
    private static final NumberFormat INT;

    static {
        MONEY = NumberFormat.getNumberInstance(Locale.US);
        MONEY.setMinimumFractionDigits(2);
        MONEY.setMaximumFractionDigits(2);

        INT = NumberFormat.getNumberInstance(Locale.US);
        INT.setMaximumFractionDigits(0);
    }

    /** "PHP 1,234.56" */
    public static String money(double v) {
        return "PHP " + MONEY.format(round2(v));
    }

    /** "1,234.56" (no currency prefix) */
    public static String amount(double v) {
        return MONEY.format(round2(v));
    }

    /** "1,234" — for counts, order numbers, etc. */
    public static String integer(long n) {
        return INT.format(n);
    }

    /** Formats a quantity: whole numbers as "3", fractions as "3.50". */
    public static String qty(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v)) {
            return INT.format((long) v);
        }
        return String.format(Locale.US, "%.2f", v);
    }

    /** Rounds to 2 decimal places (same logic we had scattered around). */
    public static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}