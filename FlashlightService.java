package com.smalltastygames.flashlight;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

public class FlashlightService extends Service {

    private CameraManager cameraManager;
    private boolean isOn = false;
    private String debugID = "toggleFlash";
    private String channelID = "Flashlight_chnl";
    private NotificationManagerCompat notificationManager;
    private Toast camAccessErr;
    private Intent tapIntent;
    private Intent swipeIntent;
    private PendingIntent deletePendingIntent;
    private PendingIntent tapPendingIntent;
    private NotificationCompat.Builder builder;

    private final int NOTIFICATION_SWIPED = 1;
    private final int NOTIFICATION_TAPPED = 2;


    @Override
    public void onCreate() {
        super.onCreate();
        this.cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        this.notificationManager = NotificationManagerCompat.from(this);
        this.camAccessErr = Toast.makeText(this, getText(R.string.camera_access_err), Toast.LENGTH_SHORT);

        //sets up the intent to handle swiping the notification out
        this.swipeIntent = new Intent();
        this.swipeIntent.setClass(getApplicationContext(), FlashlightService.class);
        this.swipeIntent.putExtra("disable", 1);
        this.swipeIntent.putExtra("justSwitch", 0);

        this.deletePendingIntent = PendingIntent.getService(this,
                NOTIFICATION_SWIPED,
                this.swipeIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        // sets up the intent to handle notification button
        this.tapIntent = new Intent();
        //this.tapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.tapIntent.setClass(getApplicationContext(), FlashlightService.class);
        this.tapIntent.putExtra("disable", 0);
        this.tapIntent.putExtra("justSwitch", 1);

        this.tapPendingIntent = PendingIntent.getService(this,
                NOTIFICATION_TAPPED,
                this.tapIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        //sets up the notification
        //notification will appear when screen is locked. Swiping it away will turn flashlight off
        this.builder = new NotificationCompat.Builder(this, channelID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.swipe_2_off))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDeleteIntent(this.deletePendingIntent) // this string handles swiping off event handling
                .setColor(0xff358f9c)
        ;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel(); //this is for android 8 and higher
        isOn = !isOn;

        int disable = intent.getIntExtra("disable", 0); // 1 - user swiped app
        int justSwitch = intent.getIntExtra("justSwitch", 0); // 1 - user tapped on notification
        Log.d(debugID, " disable: " + disable);
        Log.d(debugID, " justSwitch: " + justSwitch);

        if((disable == 1)||(justSwitch == 0 && !isOn)) {//user swiped notification or turned it off via tap on icon -> kill service
            Log.d(debugID, "turning off");
            this.stopSelf(); // will call onDestroy
        }else{
            switchLight();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        notificationManager.cancelAll(); //remove notification
        // turn off camera explicitly
        try{
            cameraManager.setTorchMode(cameraManager.getCameraIdList()[0], false);
            this.notificationManager.cancelAll();//remove notification
        }catch(CameraAccessException cae){
            this.camAccessErr.show();
            Log.d(debugID, "On destroy. No camera access");
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void switchLight(){
        try {
            this.builder.mActions.clear(); // wipes notifications in order update action button later
            if (isOn){
                this.builder.addAction(R.drawable.ic_launcher_foreground, getString(R.string.tap_2_off), this.tapPendingIntent);
            }else{
                this.builder.addAction(R.drawable.ic_launcher_foreground, getString(R.string.tap_2_on), this.tapPendingIntent);
            }
            cameraManager.setTorchMode(cameraManager.getCameraIdList()[0], isOn);
            this.notificationManager.notify(1, this.builder.build()); //displays updated notification
        }catch (CameraAccessException cae){
            this.camAccessErr.show();
            Log.d(debugID, "switchLight. No camera access");
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.swipe_2_off);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
