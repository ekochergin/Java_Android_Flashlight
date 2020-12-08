package com.smalltastygames.flashlight;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class FlashlightActivity extends AppCompatActivity {

    private String debugID = "FlashlightActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(debugID, "onCreate starts");

        Intent serviceIntent = new Intent();
        //serviceIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        serviceIntent.setClass(getApplicationContext(), FlashlightService.class);

        startService(serviceIntent);

        finish();
    }
}
