package com.goodsamaritan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootCompleteReceiver extends BroadcastReceiver {
    public OnBootCompleteReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        //Start Services
        context.startService(new Intent(context,LocationService.class));
    }
}
