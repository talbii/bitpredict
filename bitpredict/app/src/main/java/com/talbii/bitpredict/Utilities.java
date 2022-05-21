package com.talbii.bitpredict;

import android.annotation.SuppressLint;
import android.util.Log;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class Utilities {
    @SuppressLint("DefaultLocale")
    public static String formatDouble(double d) {
        if(d == -1) return "-1";
        assert d > 0;
        var magnitude = Math.log10(d);
        if(magnitude <= -5) return new DecimalFormat("#.####").format(d);
        if(magnitude >= 0) return new DecimalFormat("#.###").format(d);
        return String.format("%.8f", d);
    }

    public static String formatBigDecimal(BigDecimal d) {
        Log.d("formatBigDouble", d.toPlainString());
        if(d.scale() <= 5) return new DecimalFormat("#.###").format(d);
        if(d.scale() <= 7) return new DecimalFormat("#.#######").format(d);
        if(d.compareTo(BigDecimal.ONE) < 0) return new DecimalFormat("#.##########").format(d);
        return new DecimalFormat("#.###").format(d);
    }

    /*
    * Returns the log (log_e) of x.*/
    public static BigDecimal log(BigDecimal x) {
        // todo: make this more accurate
        return new BigDecimal(Math.log(x.doubleValue()));
    }

    /*
    * Returns the value a ^ b.*/
    public static BigDecimal pow(BigDecimal a, BigDecimal b) {
        // todo: make this more accurate
        return new BigDecimal(Math.pow(a.doubleValue(), b.doubleValue()));
    }

    public static BigDecimal d(double x) {
        return new BigDecimal(x);
    }
}
