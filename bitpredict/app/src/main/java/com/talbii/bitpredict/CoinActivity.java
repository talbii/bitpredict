package com.talbii.bitpredict;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import kotlin.Pair;

public class CoinActivity extends AppCompatActivity {
    private ArrayList<Double> getHistoricalData(@NonNull final String s) {
        Log.d("CoinActivity/getHistoricalData", "Got Path: " + s);
        DocumentReference d = FirebaseFirestore.getInstance().document("/" + s);
        var r = new Runnable() {
            ArrayList<Double> arr;
            @Override
            public void run() {
                try {
                    arr = (ArrayList<Double>) Tasks.await(d.get()).get("historical");
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
            return r.arr;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void displayLineChart(@NonNull final String s) {
        var lineChart = (LineChartView) findViewById(R.id.lineChart);
        var l = getHistoricalData(s);
        assert l != null;

        final var lp = new ArrayList<Pair<String, Float>>();
        for(var j = 0; j < l.size(); j++) {
            var res = 0f;
            var o = (Object) l.get(j);
            if(o instanceof Long) res = ((Long) o).floatValue();
            else res = ((Double) o).floatValue();
            lp.add(new Pair<>("label"+j, res));
        }

        lineChart.getAnimation().setDuration(1000L);
        lineChart.animate(lp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);

        final var t = findViewById(R.id.activity_coin_root);

        var i = getIntent();

        /*Load Symbol - this is probably cached so we can proceed with the request.*/
        var image = (ImageView) t.findViewWithTag("coin_symbol");
        Glide.with(this)
                .load(FirebaseStorage.getInstance().getReference(i.getStringExtra("symbol")))
                .into(image);

        ((TextView) t.findViewWithTag("coin_name")).setText(i.getStringExtra("name"));
        ((TextView) t.findViewWithTag("coin_quote_latest")).setText(Formatting.formatDouble(i.getDoubleExtra("latest_quote", -1)));
        if(MainActivity.currentNetworkState) displayLineChart(i.getStringExtra("historical"));
        else {
            Log.d("CoinActivity", "No internet; skipping request of historical for now.");
        }


    }
}