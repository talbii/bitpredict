package com.talbii.bitpredict;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
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

    @SuppressLint("DefaultLocale")
    private String formatDouble(double d) {
        assert d > 0;
        var magnitude = Math.log10(d);
        if(magnitude < -5) return String.format("%." + magnitude + "f", d);
        if(magnitude >= 0) return String.format("%.3f", d);
        return String.format("%.5f", d);
    }

    private ArrayList<Double> getHistoricalData(@NonNull final String s) {
        DocumentReference d = FirebaseFirestore.getInstance().document(s);
        try {
            return (ArrayList<Double>) Tasks.await(d.get()).get("data");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);

        final var t = findViewById(R.id.activity_coin_root);

        var i = getIntent();

        /*Load Symbol*/
        var image = (ImageView) t.findViewWithTag("coin_symbol");
        Glide.with(this)
                .load(FirebaseStorage.getInstance().getReference(i.getStringExtra("symbol")))
                .into(image);

        ((TextView) t.findViewWithTag("coin_name")).setText(i.getStringExtra("name"));
        ((TextView) t.findViewWithTag("coin_quote_latest")).setText(formatDouble(i.getDoubleExtra("latest_quote", -1)));

        var lineChart = (LineChartView) findViewById(R.id.lineChart);
        final var l = getHistoricalData(i.getStringExtra("historical"));
        final var lp = new ArrayList<Pair<String, Float>>();
        for(var j = 0; j < l.size(); j++) {
            lp.add(new Pair<>("label"+j, l.get(j).floatValue()));
        }

        lineChart.getAnimation().setDuration(1000L);
        lineChart.animate(lp);
    }
}