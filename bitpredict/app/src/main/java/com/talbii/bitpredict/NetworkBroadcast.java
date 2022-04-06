package com.talbii.bitpredict;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

public class NetworkBroadcast extends BroadcastReceiver {
    public static final String TAG = "NetworkBroadcast";
    @Override
    public void onReceive(Context context, Intent intent) {
        final var c = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        var networkStatus = c.getActiveNetwork();
        if(networkStatus == null) {
            // phone has no network!
            Log.d(TAG, "No network!");
            Toast.makeText(context, "No network!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Yes network!");
            Toast.makeText(context, "Yes network!", Toast.LENGTH_SHORT).show();
        }
    }
}
