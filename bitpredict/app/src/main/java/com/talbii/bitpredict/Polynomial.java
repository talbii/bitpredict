package com.talbii.bitpredict;

import android.util.Log;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import kotlin.Pair;

public class Polynomial {
    /*
    * Given a list of points (x_i, y_i) ∈ X * Y, and a point x_j,
    * */

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

        for(var i = 0; i < n; i++) poly[i][i] = BigDecimal.valueOf(data.get(i).getSecond());

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

                poly[i][j] = numerator.divide(denominator,2,BigDecimal.ROUND_HALF_UP);
                //poly[i][j] = ((x - x_j) * poly[i][j-1] - (x - x_i) * poly[i+1][j])/(x_i - x_j);
                /*if(Double.isNaN(poly[i][j])) {
                    final String TAG = "Polynomial/neville_interpolation";
                    Log.d(TAG, "NaN at " + i + "," + j);
                    Log.d(TAG, "x_i=" + x_i + " x_j=" + x_j);
                    Log.d(TAG, "poly[i][j-1]=" + poly[i][j-1] + " poly[i+1][j]=" + poly[i+1][j]);
                    assert !Double.isNaN(poly[i][j]);
                }*/
                i++; j++;
            }
        }

        return poly[0][n - 1];
    }


}
