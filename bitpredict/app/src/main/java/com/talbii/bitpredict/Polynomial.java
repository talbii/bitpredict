package com.talbii.bitpredict;

import android.util.Log;

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
    public static double neville_interpolation(List<Pair<Double, Double>> data, double x) {
        final var n = data.size();
        var poly = new double[n][n];

        for(var i = 0; i < n; i++) poly[i][i] = data.get(i).getSecond();

        for(var k = 1; k < n; k++) {
            int i = 0, j = i + k;
            while(j < n) {
                var x_i = data.get(i).getFirst();
                var x_j = data.get(j).getFirst();
                poly[i][j] = ((x - x_j) * poly[i][j-1] - (x - x_i) * poly[i+1][j])/(x_i - x_j);
                i++; j++;
            }
        }

        return poly[0][n - 1];
    }


}
