package com.talbii.bitpredict;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Database {
    public final String TAG = "Database";
    private static final long MEGABYTE = 1024 * 1024;
    private final FirebaseFirestore db;
    private final StorageReference icons;
    public final static Set<String> availableCoins;
    static {
        GetCoinsRunnable g = new GetCoinsRunnable();
        Thread t = new Thread(g);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("(Database) Error in getting list of coins (static block)");
        }

        availableCoins = Collections.unmodifiableSet(new HashSet<>(g.getAvailableCoins()));
        Log.d("Database/static_block", "Got list of available coins: " + availableCoins);
    }

    public Database () {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);
        icons = FirebaseStorage.getInstance().getReference().child("ic");
    }

    /*Gets the historical and latest prices of coin s, and places them in c.
    *  Assumes s is in availableCoins.*/
    private CoinStruct getQuote(@NonNull String s) {

        GetCoinRunnable g = new GetCoinRunnable(db, s);
        Thread t = new Thread(g);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("(Database) Error in getting list of coins (static block)");
        }

        return g.getCoinStruct();
    }

    public CoinStruct getCoin(String s) {
        if(!availableCoins.contains(s)) return null;

        CoinStruct c = getQuote(s);
        c.iconref = icons.child(s + ".png");
        //c.icon = getIcon(s, c.iconref);
        return c;
    }

    public List<CoinStruct> getCoins(List<String> l) {
        var res = Collections.synchronizedList(new ArrayList<CoinStruct>(l.size()));
        var pool = Executors.newCachedThreadPool();
        for(var coin : l) {
            if(!availableCoins.contains(coin)) continue;
            pool.execute(new GetCoinRunnable(db, coin, res));
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    private static class GetCoinsRunnable implements Runnable {
        private ArrayList<String> a;

        public GetCoinsRunnable() {
            a = new ArrayList<>();
        }

        @Override
        public void run() {
            try {
                this.a = (ArrayList<String>) Tasks.await(FirebaseFirestore.getInstance().collection("info").document("coins").get()).get("all");
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("(Database) Error in getting list of coins (static block)");
            }
        }

        public ArrayList<String> getAvailableCoins() {
            return a;
        }
    }

    private static class GetCoinRunnable implements Runnable {
        private final CoinStruct c;
        private final FirebaseFirestore fs;
        private final String s;

        private List<CoinStruct> res;

        public GetCoinRunnable(FirebaseFirestore f, String symbol) {
            c = new CoinStruct();
            fs = f;
            s = symbol;
            res = null;
        }

        public GetCoinRunnable(FirebaseFirestore f, String symbol, List<CoinStruct> writeTo) {
            this(f, symbol);
            res = writeTo;
        }

        @Override
        public void run() {
            try {
                var dref = fs.collection("data").document(s);
                var ds = Tasks.await(dref.get());
                if(ds.exists()) {
                    c.name = (String) ds.get("name");
                    c.latest = (double) ds.get("latest");
                    c.historical = (DocumentReference) ds.get("historical");
                    c.iconref = FirebaseStorage.getInstance().getReference().child("ic").child(s + ".png");
                }

                if(res != null) {
                    res.add(c);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public CoinStruct getCoinStruct() {
            return c;
        }
    }
}