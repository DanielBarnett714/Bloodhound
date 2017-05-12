package me.dbarnett.bloodhound.Services;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.DownloadRemoteFileOperation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by daniel on 4/23/17.
 */
public class CheckService extends Service {
    /**
     * The Check config.
     */

    private static final String TAG = "CheckService";

    static CheckConfig checkConfig;
    /**
     * The Timer.
     */
    Timer timer;
    /**
     * The M context.
     */
    Context mContext;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mContext = getApplicationContext();


        nextcloud();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (permissionsEnabled()) {

                    checkConfig.startDownload("Bloodhound/Config/check", mContext.getFilesDir() + "/");
                    System.out.println("Checking Bloodhound");
                }
            }
        }, 0, 60*1000);



        return START_STICKY;
    }

    /**
     * Nextcloud.
     */
    public void nextcloud(){
        checkConfig = new CheckConfig();
    }

    /**
     * Permissions enabled boolean.
     *
     * @return the boolean
     */
    public boolean permissionsEnabled(){

        if (mContext.checkCallingOrSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED |
                mContext.checkCallingOrSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED |
                mContext.checkCallingOrSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED |
                mContext.checkCallingOrSelfPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED |
                mContext.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED |
                mContext.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED |
                mContext.checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){

            return false;
        }
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!notificationManager.isNotificationPolicyAccessGranted()){
                return false;
            }

            String packageName = getApplicationContext().getPackageName();
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)){
                return false;
            }
            if (!Settings.canDrawOverlays(getApplicationContext())){
                return false;
            }
        }
        return true;
    }

    private class CheckConfig implements OnRemoteOperationListener, OnDatatransferProgressListener {
        /**
         * The Intent.
         */
        Intent intent;

        private OwnCloudClient mClient;
        private Handler mHandler = new Handler();
        /**
         * The Prefs.
         */
        SharedPreferences prefs;


        /**
         * Instantiates a new Check config.
         */
        public CheckConfig(){
            prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

            String address = prefs.getString("address", "");
            String username = prefs.getString("username", "");
            String password = prefs.getString("password", "");
            System.out.println("login info " + address + " " + username + " " + password);

            intent = new Intent(getApplicationContext(), BloodhoundService.class);

            Uri serverUri = Uri.parse(address);

            mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, getApplicationContext(), true);

            login(username, password);
        }

        /**
         * Start download.
         *
         * @param filePath        the file path
         * @param targetDirectory the target directory
         */
        public void startDownload(String filePath, String targetDirectory) {
            if (mClient.getCredentials().getUsername().equals("")){
                String address = prefs.getString("address", "");
                String username = prefs.getString("username", "");
                String password = prefs.getString("password", "");
                Uri serverUri = Uri.parse(address);
                mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, getApplicationContext(), true);
                System.out.println("login info " + address + " " + username + " " + password);
                login(username, password);
                return;
            }
            DownloadRemoteFileOperation downloadOperation = new DownloadRemoteFileOperation(filePath, targetDirectory);
            downloadOperation.addDatatransferProgressListener(this);
            downloadOperation.execute( mClient, this, mHandler);
        }

        /**
         * Login.
         *
         * @param username the username
         * @param password the password
         */
        public void login(String username, String password){
            mClient.setCredentials(
                    OwnCloudCredentialsFactory.newBasicCredentials(username, password)
            );
        }


        @Override
        public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileName) {
            mHandler.post( new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
            boolean alarm, location, camera, mic;

            if (operation instanceof DownloadRemoteFileOperation) {
                try {
                    if (result.isSuccess()) {

                        File checkFile = new File(mContext.getFilesDir() + "/Bloodhound/Config/check");
                        int check = 0;
                        try {
                            String checkString = new Scanner(checkFile).useDelimiter("\\Z").next();
                            try {
                                check = Integer.valueOf(checkString);
                            } catch (NumberFormatException e) {
                                Log.e("Check file", e.getMessage());
                                return;
                            }

                            File configFile = new File(mContext.getFilesDir() + "/Bloodhound/Config/config.json");
                            if (check == 1) {
                                if (configFile.exists()) {
                                    if (configFile.lastModified() > checkFile.lastModified()) {
                                        if (!BloodhoundService.isRunning) {
                                            try {
                                                String content = new Scanner(configFile).useDelimiter("\\Z").next();
                                                System.out.println(content);
                                                JSONObject jsonConfig = new JSONObject(content);
                                                System.out.println(jsonConfig.toString());
                                                alarm = jsonConfig.getBoolean("alarm");
                                                camera = jsonConfig.getBoolean("camera");
                                                location = jsonConfig.getBoolean("location");
                                                mic = jsonConfig.getBoolean("mic");

                                                intent.putExtra("useAlarm", alarm);
                                                intent.putExtra("useLocation", location);
                                                intent.putExtra("useCamera", camera);
                                                intent.putExtra("useMic", mic);
                                                intent.putExtra("trackingType", "Nextcloud");

                                                startService(intent);

                                                timer.cancel();
                                                timer = new Timer();
                                                timer.scheduleAtFixedRate(new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        if (permissionsEnabled()) {

                                                            checkConfig.startDownload("Bloodhound/Config/check", mContext.getFilesDir() + "/");
                                                            System.out.println("Checking Bloodhound");
                                                        }
                                                    }
                                                }, 0, 10 * 1000);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    } else {
                                        startDownload("Bloodhound/Config/config.json", mContext.getFilesDir() + "/");
                                    }
                                } else {
                                    startDownload("Bloodhound/Config/config.json", mContext.getFilesDir() + "/");
                                }
                            } else if (check == 0 && BloodhoundService.isRunning && BloodhoundService.getBloodhoundContext().getPackageName() == mContext.getPackageName()) {
                                Log.i(TAG, "Stopping bloodhound");
                                stopService(intent);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }catch (NoSuchElementException e){
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }
}
