package com.talbii.bitpredict;

import org.junit.Test;
import static com.talbii.bitpredict.SimpleLinearRegression.*;
import static com.talbii.bitpredict.TestUtilities.*;
import java.math.BigDecimal;
import java.util.ArrayList;

public class SimpleLinearRegressionTest {
    @Test
    public void linear_test_1() {
        var l = new ArrayList<Double>();
        l.add(1.);
        l.add(2.);
        l.add(3.);
        l.add(4.);

        var res = regressSequence(l, 0);

        assert eq(res.alpha, d(1));
        assert eq(res.beta, d(1));
    }

    @Test
    public void linear_test_2() {
        var l = new ArrayList<BigDecimal>();

        l.add(d(5));
        l.add(d(3));
        l.add(d(1));

        var res = precisionSequenceRegress(l, 0);

        assert eq(res.alpha, d(5));
        assert eq(res.beta, d(-2));
    }
}
