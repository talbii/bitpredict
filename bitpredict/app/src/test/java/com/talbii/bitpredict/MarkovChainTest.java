package com.talbii.bitpredict;

import org.junit.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;
import static com.talbii.bitpredict.TestUtilities.*;

public class MarkovChainTest {
    private static final int N = 50;
    private static final Random rnd = new Random();
    private static final BigDecimal ep = new BigDecimal(1e-6);
    private interface Predicate {
        BigDecimal op(int n);
    }
    private static <T> void printMat(T[][] mat) {
        for(var arr : mat) {
            for(var x : arr) System.out.print(x + " ");
            System.out.println();
        }
    }

    private static ArrayList<BigDecimal> fillList(int N, Predicate p) {
        var l = new ArrayList<BigDecimal>(N);
        for(var i = 0; i < N; i++) l.add(p.op(i));
        return l;
    }

    @Test
    public void verify_matrix_only_increasing() {
        var l = fillList(N, (i) -> (d(i)));
        var mat = MarkovChain.generateTransitionMatrix(l);
        printMat(mat);

        assert eqe(mat[2][2], d(1), ep);
    }

    @Test
    public void verify_matrix_only_decreasing() {
        var l = fillList(N, (i) -> d(N - i));
        var mat = MarkovChain.generateTransitionMatrix(l);
        printMat(mat);

        assert eqe(mat[0][0], d(1), ep);
    }

    @Test
    public void verify_matrix_only_equal() {
        var l = fillList(N, (i) -> d(5));
        var mat = MarkovChain.generateTransitionMatrix(l);
        printMat(mat);

        assert eqe(mat[1][1], d(1), ep);
    }

    private double noise() {
        return rnd.nextDouble();
    }

    @Test
    public void verify_matrix_sum_pattern() {
        var l = fillList(N, (i) -> d(i + noise()));
        for(var i = 0; i < N; i++) l.add(d(N - i + noise()));
        for(var i = 0; i < N; i++) l.add(d(i + noise()));

        var mat = MarkovChain.generateTransitionMatrix(l);
        printMat(mat);
    }

    @Test
    public void verify_matrix_sum() {
        var l = fillList(N * N, (i) -> d(1e5 * rnd.nextDouble()));
        //for(var x : l) System.out.print(x + " ");
        var mat = MarkovChain.generateTransitionMatrix(l);
        printMat(mat);

        for(var i = 0; i < 3; i++) {
            var sum = d(0);
            for(var x : mat[i]) sum = sum.add(x);
            assert eqe(sum, d(1), ep);
        }
    }
}
