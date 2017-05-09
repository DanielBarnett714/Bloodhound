package me.dbarnett.bloodhound.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.dbarnett.bloodhound.Services.CheckService;

/**
 * Created by daniel on 4/23/17.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {
        Intent serviceIntent = new Intent(context, CheckService.class);
        context.startService(serviceIntent);
    }
}
