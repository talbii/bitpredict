package com.talbii.bitpredict;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MarkovChain {
    private static final Random rnd = new Random();

    private static int compare(BigDecimal x, BigDecimal y, BigDecimal epsilon)  {
        /*
        * Compares x, y with error epsilon (i.e., returns x == y if |x - y| < epsilon)*/
        var t = x.subtract(y);
        var sign = t.signum();
        if(sign == 0) return 0;

        var cmp = t.abs().compareTo(epsilon);

        if(cmp <= 0) { // |x-y| <= epsilon
            return 0;
        }
        else {
            return sign;
        }
    }

    private static BigDecimal sum(int[] array) {
        var sum = BigDecimal.ZERO;
        for(var x : array) sum = sum.add(BigDecimal.valueOf(x));
        return sum;
    }

    /*
    * Given n prices in time, generate the transition matrix which describes */
    public static BigDecimal[][] generateTransitionMatrix(List<BigDecimal> prices) {
        assert prices.size() >= 3;
        var mat = new int[3][3];

        /*
        * The transition matrix we are building is 3x3 and of the form:
        *
        *    -     =     +   (the future)
        * -  p1    p2    p3
        *
        * =  p4    p5    p6
        *
        * +  p7    p8    p9
        * (present)
        *
        * So mat[0][2] = [-,+] = Pr(Price decreases in the next 5 min | Price increased in the last 5 min)
        * */

        for(var arr : mat) Arrays.fill(arr, 0);
        BigDecimal epsilon;
        {
            var min = Collections.min(prices);
            var max = Collections.max(prices);
            epsilon = max.subtract(min).multiply(min).divide(new BigDecimal(2*prices.size()), 10, RoundingMode.HALF_EVEN);
        }

        var past = compare(prices.get(1), prices.get(0), epsilon) + 1;
        for(var i = 2; i < prices.size(); i++) {
            var present = compare(prices.get(i), prices.get(i-1), epsilon) + 1;
            mat[present][past]++;
            past = present;
        }

        var transitionMatrix = new BigDecimal[3][3];
        for(var i = 0; i < transitionMatrix.length; i++) {
            var div_by = sum(mat[i]);
            if(div_by.compareTo(BigDecimal.ZERO) == 0) div_by = BigDecimal.ONE;
            for(var j = 0; j < transitionMatrix[0].length; j++) {
                transitionMatrix[i][j] = new BigDecimal(mat[i][j]).divide(div_by, 12, RoundingMode.HALF_EVEN);
            }
        }

        return transitionMatrix;
    }

    private static int randomPick(BigDecimal[] probabilities) {
        /*
        * Picks i with probability probabilities[i]. Assumes sum over probabilities is 1.*/
        var pick = new BigDecimal(rnd.nextDouble());
        for(var i = 0; i < probabilities.length; i++) {
            if(pick.compareTo(probabilities[i]) <= 0) return i;
            pick = pick.subtract(probabilities[i]);
        }
        return probabilities.length - 1;
    }

    public static BigDecimal predictNext(List<BigDecimal> prices) {
        /*
        * Given a list of prices, predict the next element in it. */
        var t = generateTransitionMatrix(prices);

        BigDecimal epsilon;
        {
            var min = Collections.min(prices);
            var max = Collections.max(prices);
            epsilon = max.subtract(min).multiply(min).divide(new BigDecimal(2*prices.size()), 10, RoundingMode.HALF_EVEN);
        }
        var lastElement = prices.get(prices.size() -1);
        var last = compare(lastElement, prices.get(prices.size() - 2), epsilon);

        var prediction = randomPick(t[last]);
        var diff_p = lastElement.subtract(prices.get(prices.size() - 2)).divide(lastElement, 12, RoundingMode.HALF_EVEN);

        if(prediction == 0) { // decrease
            return lastElement.multiply(BigDecimal.ONE.subtract(diff_p));
        } else if(prediction == 1) { // equal
            return lastElement;
        } else { // increase
            return lastElement.multiply(BigDecimal.ONE.add(diff_p));
        }
    }
}
