package com.epimorphismmc.eunetwork.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumberUtil {
    private static final String[] UNITS = {"", "K", "M", "G", "T", "P"};


    public static String formatBigInteger(BigInteger number) {
        if (number.compareTo(BigInteger.ZERO) < 0) {
            return "-" + formatBigInteger(number.negate());
        }

        int unitIndex = 0;
        BigDecimal temp = new BigDecimal(number, 0);
        while (temp.compareTo(BigDecimal.TEN.pow(3)) >= 0) {
            temp = temp.divide(BigDecimal.TEN.pow(3), 2, RoundingMode.HALF_DOWN);
            unitIndex++;
        }

        DecimalFormat df = new DecimalFormat("#0.00");
        String formattedNumber = df.format(temp.doubleValue());

        if (unitIndex >= UNITS.length) {
            return String.format("%.2e", number.doubleValue());
        }

        return formattedNumber + UNITS[unitIndex];
    }
}
