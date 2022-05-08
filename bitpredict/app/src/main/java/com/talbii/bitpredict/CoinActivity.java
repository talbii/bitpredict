package com.talbii.bitpredict;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.db.williamchart.view.LineChartView;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import kotlin.Pair;

public class CoinActivity extends AppCompatActivity {
    private final FirebaseFirestore fs = FirebaseFirestore.getInstance();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ArrayList<Double> getHistoricalData(@NonNull final String s) {
        Log.d("CoinActivity/getHistoricalData", "Got Path: " + s);
        DocumentReference d = fs.document("/" + s);
        var r = new Runnable() {
            ArrayList<Double> arr;
            boolean validData = true;
            @Override
            public void run() {
                try {
                    ArrayList<Object> res = (ArrayList<Object>) Tasks.await(d.get()).get("historical");
                    arr = new ArrayList<>(res.size());
                    for(var o : res) {
                        if(o instanceof Double) arr.add((Double) o);
                        else if(o instanceof Long) arr.add(((Long) o).doubleValue());
                        else validData = false;
                    }
                    Log.d("CoinActivity/getHistoricalData/Runnable", "Got array! " + String.valueOf(arr));
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        try {
            var t = new Thread(r);
            t.start();
            t.join();
            return (r.validData) ? r.arr : null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
  
    private Future<BigDecimal> requestPredictionForSlice(List<Double> l, int sliceSize) {
        sliceSize = Math.min(l.size(), sliceSize);
        var slicedList = l.subList(l.size() - sliceSize, l.size());
        var pairList = new ArrayList<Pair<Double, Double>>(l.size());

        var i = (double)l.size();
        for (var d : slicedList) pairList.add(new Pair<>(i++, d));

        var future = executor.submit(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return Polynomial.neville_interpolation(pairList, l.size() + 1);
            }
        });

        return future;

    }

    private void displayHistoricalData(@NonNull final String s) {
        var lineChart = (LineChartView) findViewById(R.id.lineChart);
        var l = getHistoricalData(s);
        assert l != null;
  
        var nevillep = requestPredictionForSlice(l, 15);

        final var lp = new ArrayList<Pair<String, Float>>();
        for (var x : l) lp.add(new Pair<>("label", x.floatValue()));

        lineChart.getAnimation().setDuration(1000L);
        lineChart.animate(lp);
  
        var nevilleTextView = (TextView)findViewById(R.id.neville_prediction);

        try {
            nevilleTextView.setText(neville.get().toString());
        } catch (ExecutionException | InterruptedException e) {
            nevilleTextView.setText("Failed to predict :-(");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);
        var i = getIntent();
        final var l = getHistoricalData(i.getStringExtra("historical"));
        assert l != null;

        var neville = requestPredictionForSlice(l, 20);


        final var t = findViewById(R.id.activity_coin_root);



        /*Load Symbol - this is probably cached so we can proceed with the request.*/
        var image = (ImageView) t.findViewWithTag("coin_symbol");
        Glide.with(this)
                .load(FirebaseStorage.getInstance().getReference(i.getStringExtra("symbol")))
                .into(image);

        ((TextView) t.findViewWithTag("coin_name")).setText(i.getStringExtra("name"));
        ((TextView) t.findViewWithTag("coin_quote_latest")).setText(Formatting.formatDouble(i.getDoubleExtra("latest_quote", -1)));
        if(MainActivity.currentNetworkState) displayHistoricalData(i.getStringExtra("historical"));
        else {
            Log.d("CoinActivity", "No internet; skipping request of historical for now.");
        }

    }
}