package me.dbarnett.bloodhound.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import me.dbarnett.bloodhound.Services.BloodhoundService;

/**
 * Created by daniel on 4/19/17.
 */
public class SmsReceiver extends BroadcastReceiver {
    /**
     * The Intent.
     */
    Intent intent;
    /**
     * The Phone number.
     */
    String phoneNumber;
    /**
     * The Is running.
     */
    boolean isRunning = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        final SmsManager sms = SmsManager.getDefault();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    Log.i("BloodhoundSMS", "number: "+ senderNum + "; text: " + message);


                    message = message.trim();

                    if (message.equals(prefs.getString("bloodhound_trigger", "EnableBloodhound"))){
                        System.out.println("start bloodhound service");

                        isRunning = true;
                        sms.sendTextMessage(phoneNumber, null, "Bloodhound Location Tracking Enabled \n Reply STOP to disable", null, null);
                        startService(context);
                    }else if (BloodhoundService.isRunning && message.equals("STOP")){
                        sms.sendTextMessage(phoneNumber, null, "Bloodhound Location Tracking Disabled", null, null);
                        stopService(BloodhoundService.getBloodhoundContext());
                    }



                }
            }

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);

        }
    }

    /**
     * Start service.
     *
     * @param context the context
     */
    public void startService(Context context) {
        intent = new Intent(context, BloodhoundService.class);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("trackingType", "SMS");
        intent.putExtra("useLocation", true);
        context.startService(intent);
    }

    /**
     * Stop service.
     *
     * @param context the context
     */
// Method to stop the service
    public void stopService(Context context) {
        intent = new Intent(context, BloodhoundService.class);
        context.stopService(intent);
    }
}
