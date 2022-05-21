package com.talbii.bitpredict;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

public class SimpleLinearRegression {
    public static class LinearFunction implements MathematicalFunction {
        /*
         *  The class LinearFunction represents the linear equation y = alpha + beta * x.
         * */
        BigDecimal alpha;
        BigDecimal beta;

        @Override
        public String toString() {
           return String.format("y = %s + %sx", alpha, beta);
        }

        LinearFunction(BigDecimal alpha, BigDecimal beta) {
            this.alpha = alpha;
            this.beta = beta;
        }

        @Override
        public BigDecimal f(BigDecimal x) {
            return alpha.add(beta.multiply(x));
        }

        @Override
        public double f(double x) {
            return f(new BigDecimal(x)).doubleValue();
        }
    }

    /*
    *  Given n points (x_i, y_i) in R^2, the function regress finds a linear
    * function that fits the best to those points.
    * */
    public static LinearFunction precisionRegress(List<Pair<BigDecimal, BigDecimal>> data) {
        var xavg = BigDecimal.ZERO;
        var yavg = BigDecimal.ZERO;

        for(var p : data) {
            xavg = xavg.add(p.getFirst());
            yavg = yavg.add(p.getSecond());
        }

        {
            var div_by = new BigDecimal(data.size());
            xavg = xavg.divide(div_by);
            yavg = yavg.divide(div_by);
        }

        var beta_numerator = BigDecimal.ZERO;
        var beta_denominator = BigDecimal.ZERO;

        for(var p : data) {
            var xi = p.getFirst();
            var yi = p.getSecond();

            beta_numerator = beta_numerator.add(
                    xi.subtract(xavg).multiply(yi.subtract(yavg))
            );

            beta_denominator = beta_denominator.add(
                    xi.subtract(xavg).pow(2)
            );
        }

        var beta = beta_numerator.divide(beta_denominator);
        var alpha = yavg.subtract(beta.multiply(xavg));

        return new LinearFunction(alpha, beta);
    }

    public static LinearFunction precisionSequenceRegress(List<BigDecimal> data, int initial) {
        var ld = new ArrayList<Pair<BigDecimal, BigDecimal>>(data.size());
        for(var i = 0; i < data.size(); i++) {
            ld.add(new Pair<>(new BigDecimal(initial + i), data.get(i)));
        }

        return precisionRegress(ld);
    }

    public static LinearFunction regress(List<Pair<Double, Double>> data) {
        var dlist = new ArrayList<Pair<BigDecimal, BigDecimal>>(data.size());
        for(var i = 0; i < data.size(); i++) {
            var p = data.get(i);
            dlist.add(new Pair<>(new BigDecimal(p.getFirst()), new BigDecimal(p.getSecond())));
        }

        return precisionRegress(dlist);
    }

    public static LinearFunction regressSequence(List<Double> data, int initial) {
        var ld = new ArrayList<Pair<BigDecimal, BigDecimal>>(data.size());
        for(var i = 0; i < data.size(); i++) {
            ld.add(new Pair<>(new BigDecimal(initial + i), new BigDecimal(data.get(i))));
        }

        return precisionRegress(ld);
    }
}
