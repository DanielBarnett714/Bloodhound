package me.dbarnett.bloodhound;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import me.dbarnett.bloodhound.Nextcloud.NextcloudLoginActivity;
import me.dbarnett.bloodhound.Services.BloodhoundService;
import me.dbarnett.bloodhound.Services.CheckService;

/**
 * The type Bloodhound activity.
 */
public class BloodhoundActivity extends AppCompatActivity {
    /**
     * The Context.
     */
    Context context;
    /**
     * The Prefs.
     */
    SharedPreferences prefs;

    /**
     * The M layout.
     */
    RelativeLayout mLayout;
    /**
     * The Intent.
     */
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_bloodhound);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        intent = new Intent(context, BloodhoundService.class);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mLayout = (RelativeLayout) findViewById(R.id.bloodhound_layout);
        if (BloodhoundService.isRunning){
            mLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (BloodhoundService.isRunning) {
                    intent = new Intent(BloodhoundService.getBloodhoundContext(), BloodhoundService.class);
                    stopService(intent);
                    Snackbar.make(view, getString(R.string.emergency_stop), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    mLayout.setBackgroundColor(getResources().getColor(R.color.backgroundColor));
                }else {
                    String phoneNumber = prefs.getString("emergency_number", "");
                    if (!phoneNumber.isEmpty()) {
                        Snackbar.make(view, getString(R.string.emergency_start), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        mLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("trackingType", "Emergency");
                        intent.putExtra("useLocation", true);
                        startService(intent);
                    }else{
                        Toast.makeText(getApplicationContext(), getString(R.string.need_phone_number), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (BloodhoundService.isRunning) {
                    intent = new Intent(BloodhoundService.getBloodhoundContext(), BloodhoundService.class);
                    stopService(intent);
                    mLayout.setBackgroundColor(getResources().getColor(R.color.backgroundColor));
                    Snackbar.make(view, getString(R.string.emergency_stop), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }else{
                    CharSequence options[] = new CharSequence[] {"Start Location Tracking.", "Start Full Tracking"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(BloodhoundActivity.this);
                    builder.setTitle("Options");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0){
                                String phoneNumber = prefs.getString("emergency_number", "");
                                if(!phoneNumber.isEmpty()){
                                    intent.putExtra("phoneNumber", phoneNumber);
                                    intent.putExtra("trackingType", "Emergency");
                                    intent.putExtra("useLocation", true);
                                    startService(intent);
                                    Toast.makeText(getApplicationContext(), getString(R.string.emergency_start), Toast.LENGTH_SHORT).show();
                                    mLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                }else{
                                    Toast.makeText(getApplicationContext(), getString(R.string.need_phone_number), Toast.LENGTH_SHORT).show();
                                }
                            }

                            if (which == 1){
                                String username = prefs.getString("username", "");
                                String phoneNumber = prefs.getString("emergency_number", "");
                                if (phoneNumber.isEmpty()){
                                    Toast.makeText(getApplicationContext(), getString(R.string.need_phone_number), Toast.LENGTH_SHORT).show();
                                    return;

                                }else if(!username.isEmpty()){
                                    intent.putExtra("phoneNumber", phoneNumber);
                                    intent.putExtra("trackingType", "Emergency");
                                    intent.putExtra("useAlarm", true);
                                    intent.putExtra("useLocation", true);
                                    intent.putExtra("useCamera", true);
                                    intent.putExtra("useMic", true);
                                    startService(intent);
                                    Toast.makeText(getApplicationContext(), getString(R.string.emergency_full), Toast.LENGTH_SHORT).show();
                                    mLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                }
                            }
                        }
                    });
                    builder.show();
                }

                return true;
            }
        });

        if (permissionsEnabled()){
            Intent serviceIntent = new Intent(context, CheckService.class);
            context.startService(serviceIntent);
        }else{
            Intent intent = new Intent(context, RequestPermissionsActivity.class);
            context.startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (prefs.getString("username", "").isEmpty()) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.bloodhound_menu, menu);
        }else{
            getMenuInflater().inflate(R.menu.bloodhound_menu_nextcloud, menu);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_main_enable_permissions) {
            Intent intent = new Intent(this, RequestPermissionsActivity.class);
            startActivity(intent);
            return true;
        }else if (id == R.id.menu_main_history) {
            Intent intent = new Intent(this, TrackingHistoryActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.menu_main_nextcloud) {
            Intent intent = new Intent(this, NextcloudLoginActivity.class);
            startActivity(intent);
            return true;
        }else if (id == R.id.menu_main_setting) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if (id == R.id.menu_main_log_out) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("username");
            edit.remove("password");
            edit.remove("address");
            edit.commit();
            Intent intent = new Intent(this, NextcloudLoginActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Permissions enabled boolean.
     *
     * @return the boolean
     */
    public boolean permissionsEnabled(){
        if (context.checkCallingOrSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED |
                context.checkCallingOrSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED |
                context.checkCallingOrSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED |
                context.checkCallingOrSelfPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED |
                context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED |
                context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED |
                context.checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED |
                context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){

            return false;
        }
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!notificationManager.isNotificationPolicyAccessGranted()){
                return false;
            }
        }
        String packageName = getApplicationContext().getPackageName();
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!pm.isIgnoringBatteryOptimizations(packageName)){
                return false;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getApplicationContext())){
                return false;
            }
        }
        return true;
    }

}
