package com.talbii.bitpredict;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements BroadcastCompatActivity{
    private static final long MINUTE = 60 * 1000;
    private RecyclerView rv;
    private Database d;
    private BroadcastReceiver checkNetworkStatus;
    private List<CoinStruct> l;
    private MainRecyclerAdapter ra;
    static boolean currentNetworkState;
    private boolean firstTime;
    private CoordinatorLayout cl;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ra.notifyDataSetChanged();
        }
    };

    private final RecallableTimer timer = new RecallableTimer();
    private final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            Log.d("MainActivity/TimerTask", "Updating coin prices!");
            l.clear();
            l.addAll(d.getCoins(new ArrayList<>(Database.availableCoins)));
            handler.sendEmptyMessage(1);
        }
    };


    private ArrayList<Comparator<CoinStruct>> spinnerFunctions() {
        var spinnerFunctions = new ArrayList<Comparator<CoinStruct>>();
        spinnerFunctions.add(Comparator.comparing(x -> x.name));
        spinnerFunctions.add((x, y) -> { /*name desc.*/
            return y.name.compareTo(x.name);
        });
        spinnerFunctions.add(Comparator.comparingDouble(x -> x.latest));
        spinnerFunctions.add((x, y) -> Double.compare(y.latest, x.latest));

        return spinnerFunctions;
    }

    @Override
    protected void onStart() {
        registerReceiver(checkNetworkStatus, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cl = findViewById(R.id.mainCoordinatorLayout);
        d = new Database();

        checkNetworkStatus = new NetworkBroadcast(this);
        currentNetworkState = true;
        firstTime = true;
        l = new ArrayList<>();
        //for(CoinStruct cs : l) Log.d("MainActivity", cs.icon.toString());

        ra = new MainRecyclerAdapter(l);
        rv = (RecyclerView) findViewById(R.id.rview);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(ra);

        var spinnerValues = ArrayAdapter.createFromResource(this,
                R.array.sort_by_options, android.R.layout.simple_spinner_item);
        final var spinnerFunctions = spinnerFunctions();

        var spinner = (Spinner) findViewById(R.id.sort_by_spinner);
        spinnerValues.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerValues);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long lo) {
                l.sort(spinnerFunctions.get(i));
                ra.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // wtf?
            }
        });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(checkNetworkStatus);
        super.onDestroy();
    }

    private void makeDismissSnackbar(int stringid, int length) {
        if(firstTime) {
            firstTime = false;
            return;
        }
        var s = Snackbar.make(cl, stringid, length);
        s.setAction("Dismiss", (v) -> s.dismiss());
        s.show();
    }

    @Override
    public void onReceiveBroadcast(int status) {
        boolean b = false;
        if(status == NetworkBroadcast.NO_NETWORK) b = false;
        else if(status == NetworkBroadcast.YES_NETWORK) b = true;
        else {
            Log.w("MainActivity/onReceiveBroadcast:status to boolean", "returning false as default value (got status " + status + " not matching YES/NO constants.");
        }


        if(b == currentNetworkState && !firstTime) return;
        currentNetworkState = b;
        switch(status) {
            case NetworkBroadcast.NO_NETWORK:
                //Toast.makeText(this, "Internet appears to be down.", Toast.LENGTH_LONG).show();
                Log.d("MainActivity/network", "Network is down");
                makeDismissSnackbar(R.string.no_internet, Snackbar.LENGTH_INDEFINITE);
                return;
            case NetworkBroadcast.YES_NETWORK:
                Log.d("MainActivity/network", "Network is up, calling timer:");
                makeDismissSnackbar(R.string.yes_internet, Snackbar.LENGTH_SHORT);
                timer.schedule(timerTask, 0L, 5*MINUTE);
                return;
            default:
                Log.d("MainActivity/onReceiveBroadcast", "Received invalid status code: " + status);
        }
    }
}