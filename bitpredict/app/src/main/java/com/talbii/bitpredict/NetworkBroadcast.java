package com.talbii.bitpredict;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class NetworkBroadcast extends BroadcastReceiver {
    public static final int NO_NETWORK = 0;
    public static final int YES_NETWORK = 1;
    private final BroadcastCompatActivity main;

    public NetworkBroadcast(BroadcastCompatActivity main) {
        this.main = main;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final var c = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        var networkStatus = c.getActiveNetwork();
        var res = (networkStatus == null) ? NO_NETWORK : YES_NETWORK;
        main.onReceiveBroadcast(res);
    }
}
