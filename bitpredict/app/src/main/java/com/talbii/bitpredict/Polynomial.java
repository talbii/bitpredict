package com.talbii.bitpredict;

import static com.talbii.bitpredict.Utilities.d;
import java.math.BigDecimal;
import java.util.List;
import kotlin.Pair;

public class Polynomial {
    /**
     *  Using Neville's Algorithm (https://en.wikipedia.org/wiki/Neville%27s_algorithm), interpolate the points
     *  in the list data.
     * @param data - dataset of n points. assumes data[i].X < data[j].X iff i < j
     * @param x - evaluate the interpolated polynomial of the dataset at point x_j
     * @return the value of the interpolated polynomial of degree data.size() + 1 at the point x_j
     */
    public static BigDecimal neville_interpolation(List<Pair<Double, Double>> data, double x) {
        final var n = data.size();
        var poly = new BigDecimal[n][n];

        for(var i = 0; i < n; i++) for(var j = 0; j < n; j++) poly[i][j] = BigDecimal.ZERO;

        for(var i = 0; i < n; i++) poly[i][i] = d(data.get(i).getSecond());

        for(var k = 1; k < n; k++) {
            int i = 0, j = i + k;
            while(j < n) {
                var x_i = data.get(i).getFirst();
                var x_j = data.get(j).getFirst();
                assert !x_i.equals(x_j);

                // (x - x_j) * poly[i][j-1]
                var first_part = poly[i][j-1].multiply(BigDecimal.valueOf(x - x_j));
                // (x - x_i) * poly[i+1][j]
                var second_part = poly[i+1][j].multiply(BigDecimal.valueOf(x - x_i));

                var numerator = first_part.subtract(second_part);
                var denominator = BigDecimal.valueOf(x_i - x_j);

                poly[i][j] = numerator.divide(denominator,10,BigDecimal.ROUND_HALF_EVEN);
                //poly[i][j] = ((x - x_j) * poly[i][j-1] - (x - x_i) * poly[i+1][j])/(x_i - x_j);
                i++; j++;
            }
        }

        return poly[0][n - 1];
    }
}
