package com.talbii.bitpredict;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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
    private ExecutorService executor = null;
    private ArrayList<Double> historical;
    private SharedPreferences sp;
    private int N;

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
                    Log.d("CoinActivity/getHistoricalData/Runnable", "Got array! ");
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
  
    private List<Future<BigDecimal>> requestPredictions(List<Callable<BigDecimal>> actions) {
        assert executor == null || executor.isTerminated();
        executor = Executors.newFixedThreadPool(actions.size());

        var futures = new ArrayList<Future<BigDecimal>>(actions.size());

        for(var callable : actions) {
            futures.add(executor.submit(callable));
        }

        return futures;
    }

    @SuppressLint("SetTextI18n")
    private void displayHistoricalData(@NonNull final String s) {
        var lineChart = (LineChartView) findViewById(R.id.lineChart);
        historical = getHistoricalData(s);
        assert historical != null;

        var actions = new ArrayList<Callable<BigDecimal>>();

        actions.add(() -> {
            var sliceSize = Math.min(historical.size(), N);
            var slicedList = historical.subList(historical.size() - sliceSize, historical.size());
            var pairList = new ArrayList<Pair<Double, Double>>(historical.size());

            var i = (double)historical.size();
            for (var d : slicedList) pairList.add(new Pair<>(i++, d));

            return Polynomial.neville_interpolation(pairList, historical.size() + 1);
        });

        actions.add(() -> {
            var l = new ArrayList<BigDecimal>(historical.size());
            for(var x : historical) l.add(new BigDecimal(x));
            return MarkovChain.predictNext(l);
        });
  
        var predictions = requestPredictions(actions);

        final var lp = new ArrayList<Pair<String, Float>>();
        for (var x : historical) lp.add(new Pair<>("label", x.floatValue()));

        lineChart.getAnimation().setDuration(1000L);
        lineChart.animate(lp);

        var nevilleTextView = (TextView)findViewById(R.id.neville_prediction);
        var markovTextView = (TextView)findViewById(R.id.markov_prediction);

        try {
            nevilleTextView.setText(Utilities.formatBigDecimal(predictions.get(0).get()));
            markovTextView.setText(Utilities.formatBigDecimal(predictions.get(1).get()));
        } catch (ExecutionException | InterruptedException e) {
            nevilleTextView.setText("--");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);
        var i = getIntent();
        sp = getSharedPreferences("neville_preferences", Context.MODE_PRIVATE);
        final var t = findViewById(R.id.activity_coin_root);

        N = sp.getInt("neville_N",15);

        /*Load Symbol - this is probably cached so we can proceed with the request.*/
        var image = (ImageView) t.findViewWithTag("coin_symbol");
        Glide.with(this)
                .load(FirebaseStorage.getInstance().getReference(i.getStringExtra("symbol")))
                .into(image);

        ((TextView) t.findViewWithTag("coin_name")).setText(i.getStringExtra("name"));
        ((TextView) t.findViewWithTag("coin_quote_latest")).setText(Utilities.formatDouble(i.getDoubleExtra("latest_quote", -1)));
        if(MainActivity.currentNetworkState) displayHistoricalData(i.getStringExtra("historical"));
        else {
            Log.d("CoinActivity", "No internet; skipping request of historical for now.");
            Toast.makeText(this, "No internet!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        var edit = sp.edit();
        edit.putInt("neville_N", N);
        edit.apply();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        var inflater = getMenuInflater();
        inflater.inflate(R.menu.coin_activity_menu, menu);
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void evaluateNeville() {
        var executor = Executors.newSingleThreadExecutor();
        var prediction = executor.submit(() -> {
            var sliceSize = Math.min(historical.size(), N);
            var slicedList = historical.subList(historical.size() - sliceSize, historical.size());
            var pairList = new ArrayList<Pair<Double, Double>>(historical.size());

            var i = (double)historical.size();
            for (var d : slicedList) pairList.add(new Pair<>(i++, d));

            return Polynomial.neville_interpolation(pairList, historical.size() + 1);
        });

        var nevilleTextView = (TextView)findViewById(R.id.neville_prediction);

        try {
            Log.d("CoinActivity/evaluateNeville", "Updating nevilleTextView");
            nevilleTextView.setText(prediction.get().toString());
            Log.d("CoinActivity/evaluateNeville", "New value: " + prediction.get().toString());
         } catch (ExecutionException | InterruptedException e) {
            nevilleTextView.setText("Failed to predict :-(");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.select_neville_degree:
                var builder= new AlertDialog.Builder(this);
                builder.setTitle("Select N:")
                        .setItems(R.array.n_array, (dialogInterface, i) -> {
                            N = i + 5;
                            evaluateNeville();
                        })
                .create().show();
                break;
            default:
                Log.d("CoinActivity/onOptionsItemSelected", "this should not enter!");
        }
        return true;
    }
}