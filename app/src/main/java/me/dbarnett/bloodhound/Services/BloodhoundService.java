package me.dbarnett.bloodhound.Services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.dbarnett.bloodhound.DB.TrackingEvent;
import me.dbarnett.bloodhound.DB.TrackingEventCollection;
import me.dbarnett.bloodhound.Nextcloud.NextcloudActions;

/**
 * The type Bloodhound service.
 */
public class BloodhoundService extends Service{
    /**
     * The constant isRunning.
     */
    public static boolean isRunning = false;

    /**
     * The Use alarm.
     */
    boolean useAlarm;
    /**
     * The Use camera.
     */
    boolean useCamera;
    /**
     * The Use mic.
     */
    boolean useMic;
    /**
     * The Use location.
     */

    String timeStamp;
    boolean useLocation;
    /**
     * The constant r.
     */
    public static Ringtone r;
    /**
     * The Phone number.
     */
    static String phoneNumber;

    /**
     * The constant camera.
     */
    public static Camera camera;

    /**
     * The Tracking event collection.
     */
    TrackingEventCollection trackingEventCollection;

    /**
     * The M context.
     */
    static Context mContext;
    private LocationManager mLocationManager = null;
    /**
     * The Am.
     */
    AudioManager am;

    /**
     * The Recorder.
     */
    MediaRecorder recorder;
    /**
     * The Alarm timer.
     */
    Timer alarmTimer;

    /**
     * The Mic timer.
     */
    Timer micTimer;
    /**
     * The Audio file timer.
     */
    Timer audioFileTimer;

    /**
     * The Start time.
     */
    String startTime;
    /**
     * The End time.
     */
    String endTime;
    /**
     * The Tracking type.
     */
    String trackingType;

    /**
     * The Use sms.
     */
    boolean useSms = false;

    /**
     * The constant nextcloudActions.
     */
    public static NextcloudActions nextcloudActions;
    /**
     * The Location manager.
     */
    public LocationManager locationManager;


    /**
     * The Sms.
     */
    static SmsManager sms;

    private static final String TAG = "BloodhoundService";

    /**
     * The M binder.
     */
    IBinder mBinder;

    /**
     * The Location listener.
     */
    LocationListener locationListener;
    /**
     * The M allow rebind.
     */
    boolean mAllowRebind;

    @Override
    public void onCreate() {

    }

    public static Context getBloodhoundContext(){
        return mContext;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mContext = getApplicationContext();
        isRunning = true;

        resetBloodhound();


        trackingEventCollection = new TrackingEventCollection(mContext);
        startTime = new SimpleDateFormat("MM/dd/yyyy - HH:mm:ss").format(new Date());
        Log.i("tracking_start_time", startTime);
        if (intent != null){

            trackingType = intent.getStringExtra("trackingType");
            useAlarm = intent.getBooleanExtra("useAlarm", false);
            useCamera = intent.getBooleanExtra("useCamera", false);
            useMic = intent.getBooleanExtra("useMic", false);
            useLocation = intent.getBooleanExtra("useLocation", false);
            nextcloud();

            phoneNumber = intent.getStringExtra("phoneNumber");
            if (useLocation){
                useLocation();
            }
            if (useAlarm) {
                playAlarm();
            }

            if (useCamera) {
                takePhoto(this, 0);
            }

            if (useMic){
                startRecording();
            }

            if (phoneNumber != null){
                sms  = SmsManager.getDefault();
                useSms = true;
            }
        }

        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {

    }

    @Override
    public void onDestroy() {

        endBloodhound();

        super.onDestroy();

    }

    public void endBloodhound(){
        endTime = new SimpleDateFormat("MM/dd/yyyy - HH:mm:ss").format(new Date());

        TrackingEvent trackingEvent = new TrackingEvent();
        trackingEvent.setStartTime(startTime);
        trackingEvent.setEndTime(endTime);
        trackingEvent.setTrackingType(trackingType);

        trackingEventCollection.addTrackingEvent(trackingEvent);

        if (audioFileTimer != null){
            audioFileTimer.cancel();
            File audiofileDir = new File(mContext.getFilesDir() + "/Recordings");
            final File audiofile = new File(mContext.getFilesDir() + "/Recordings/" + timeStamp + ".mp3");
            audiofileDir.mkdirs();
            try {
                audiofile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            recorder.stop();
            recorder.release();
            nextcloudActions.startUpload(audiofile, "Bloodhound/Recordings/" + audiofile.getName(), "audio/mpeg");
        }
        isRunning = false;
        resetBloodhound();
        Log.i(TAG, "Bloodhound has stopped");

        stopSelf();
    }


    /**
     * Reset bloodhound.
     */
    protected void resetBloodhound(){
        if (alarmTimer != null){
            alarmTimer.cancel();
            alarmTimer = null;
        }

        if (micTimer != null){
            micTimer.cancel();
            micTimer = null;
        }
        
        trackingEventCollection = null;

        if (locationManager != null){
            locationManager.removeUpdates(locationListener);
            locationManager = null;
        }

        if (r != null) {
            if (r.isPlaying()) {
                r.stop();
            }
        }

    }

    /**
     * Save location.
     *
     * @param location the location
     */
    public void saveLocation(Location location){
        JSONObject locationInfo = new JSONObject();
        Log.i("location", "saving location");

        try {
            locationInfo.put("Time", location.getTime());
            locationInfo.put("Lat", location.getLatitude());
            locationInfo.put("Lon", location.getLongitude());
            locationInfo.put("Provider", location.getProvider());
            locationInfo.put("Altitude", location.getAltitude());
            locationInfo.put("Speed", location.getSpeed());
            locationInfo.put("Bearing", location.getBearing());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File locationDir = new File(mContext.getFilesDir().getAbsolutePath() + "/Location");

        try {
            locationDir.mkdir();
            File locationFile = new File(locationDir.getAbsolutePath() + File.separator + timeStamp + ".json");
            locationFile.createNewFile();

            FileWriter out = new FileWriter(locationFile);
            out.write(locationInfo.toString());
            out.close();

            nextcloudActions.startUpload(locationFile, "Bloodhound/Location/" + locationFile.getName(), "application/json");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Use location.
     */
    protected void useLocation(){
        try {
            mLocationManager = null;
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);


            String bestProvider = LocationManager.GPS_PROVIDER.toString();
            String okayProvider = LocationManager.NETWORK_PROVIDER.toString();
            try {
                locationListener = new BloodhoundLocationListener(bestProvider);
                mLocationManager.requestLocationUpdates(bestProvider, 300 * 1000, 2, locationListener);
                return;
            }catch (SecurityException e){
                Log.e(TAG, e.getMessage());
            }
            try {
                locationListener = new BloodhoundLocationListener(okayProvider);
                mLocationManager.requestLocationUpdates(bestProvider, 300 * 1000, 2, locationListener);
            }catch (SecurityException e){
                Log.e(TAG, e.getMessage());
            }

            Log.i(TAG, "using location");
        }catch (SecurityException e){
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Play alarm.
     */
    protected void playAlarm(){
        alarmTimer = new Timer();
        System.out.println("Playing alarm");
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        r = null;
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        System.out.println(notification);
        alarmTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (r != null) {
                    try {
                        am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                        if (r.isPlaying()) {
                            r.stop();
                        }
                        r.play();
                    } catch (Exception e) {
                        Log.e("alarm", e.getMessage());
                    }
                }

            }
        }, 0, 5*1000);
    }

    /**
     * Nextcloud.
     */
    public void nextcloud(){
        nextcloudActions = new NextcloudActions(getApplicationContext());
    }

    @SuppressWarnings("deprecation")
    private static void takePhoto(final Context context, final int cameraType) {
        final SurfaceView surfaceView = new SurfaceView(context);
        SurfaceHolder holder = surfaceView.getHolder();
        final WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                camera = null;

                try {
                    if (cameraType == 1) {
                        camera = openFrontFacingCamera();
                    }else{
                        camera = Camera.open();
                    }
                    
                    Camera.Parameters cameraParameters = camera.getParameters();

                    List<Camera.Size> points = cameraParameters.getSupportedPictureSizes();
                    
                    System.out.println(points.toString());
                    cameraParameters.setPictureSize(points.get(1).width, points.get(1).height);
                    if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        cameraParameters.set("orientation", "portrait");
                        cameraParameters.set("rotation", 270);

                    } else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        cameraParameters.set("orientation", "landscape");
                        cameraParameters.set("rotation", 270);
                    }

                    camera.setParameters(cameraParameters);
                    

                    try {
                        camera.setPreviewDisplay(holder);
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                        throw new RuntimeException(e);
                    }

                    camera.startPreview();
                    camera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, final Camera camera) {
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {


                                    camera.takePicture(null, null, new Camera.PictureCallback() {

                                        @Override
                                        public void onPictureTaken(byte[] data, Camera camera) {

                                            File pictureFile = getPictureFile();
                                            if (pictureFile == null) {
                                                return;
                                            }

                                            try {
                                                FileOutputStream fos = new FileOutputStream(pictureFile);
                                                fos.write(data);
                                                fos.close();


                                            } catch (FileNotFoundException e) {
                                                Log.e(TAG, e.getMessage());

                                            } catch (IOException e) {

                                            }

                                            camera.release();
                                            windowManager.removeView(surfaceView);

                                            nextcloudActions.startUpload(pictureFile, "Bloodhound/Pictures/" + pictureFile.getName(), "image/jpg");
                                            
                                            if (cameraType == 0) {
                                                takePhoto(context, 1);
                                            }
                                        }
                                    });

                                }
                            }, 1000);

                        }
                    });


                } catch (Exception e) {
                    if (camera != null)
                        camera.release();
                    throw new RuntimeException(e);
                }
            }

            @Override public void surfaceDestroyed(SurfaceHolder holder) {}
            @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
        });


        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.TRANSLUCENT);
        windowManager.addView(surfaceView, layoutParams);
    }


    @SuppressWarnings("deprecation")
    private static Camera openFrontFacingCamera()
    {
        int cameraCount;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for ( int i = 0; i < cameraCount; i++ ) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(i);
                } catch (RuntimeException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return cam;
    }

    private static File getPictureFile(){

        File pictureDir = new File(mContext.getFilesDir().getAbsolutePath(), "Pictures");
        
        if (!pictureDir.exists()){
            if (!pictureDir.mkdirs()){
                Log.d("Bloodhound Pictures", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File picFile = new File(pictureDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        
        return picFile;
    }

    /**
     * Start recording.
     */
    public void startRecording(){
        micTimer = new Timer();
        micTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                recordAudio();

            }
        }, 0, 30*1010);
    }

    /**
     * Record audio.
     */
    public void recordAudio() {
        recorder = new MediaRecorder();
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        ContentValues values = new ContentValues(3);
        File audiofileDir = new File(mContext.getFilesDir() + "/Recordings");
        final File audiofile = new File(mContext.getFilesDir() + "/Recordings/" + timeStamp + ".mp3");
        audiofileDir.mkdirs();
        try {
            audiofile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        values.put(MediaStore.MediaColumns.TITLE, timeStamp + ".mp3");
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(audiofile.getAbsolutePath());
        try {
            recorder.prepare();
        } catch (Exception e){
            Log.e(TAG, e.getMessage());
        }

        audioFileTimer = new Timer();
        recorder.start();
        audioFileTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                recorder.stop();
                recorder.release();
                nextcloudActions.startUpload(audiofile, "Bloodhound/Recordings/" + audiofile.getName(), "audio/mpeg");
            }
        }, 30000);

    }

    /**
     * Gps string string.
     *
     * @param lat the lat
     * @param lon the lon
     * @return the string
     */
    protected String gpsString(double lat, double lon){
        return "Bloodhound Tracking \ngeo:" + lat + "," + lon + "?q=" + lat + "," + lon;
    }

    private class BloodhoundLocationListener implements android.location.LocationListener {
        /**
         * The M last location.
         */
        Location mLastLocation;

        /**
         * Instantiates a new Bloodhound location listener.
         *
         * @param provider the provider
         */
        public BloodhoundLocationListener(String provider)
        {
            Log.i(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            if (useSms){

                sms.sendTextMessage(phoneNumber, null, gpsString(location.getLatitude(), location.getLongitude()), null, null);

            }else{
                saveLocation(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
          
        }

        @Override
        public void onProviderDisabled(String provider) {
          
        }
    }
}
