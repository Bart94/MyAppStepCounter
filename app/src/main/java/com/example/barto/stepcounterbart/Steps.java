package com.example.barto.stepcounterbart;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Steps extends Service {
    private final LocalBinder mBinder = new LocalBinder();
    String steps;
    private GoogleApiClient mClient = null;
    private static final int REQUEST_OAUTH = 1;

    public class LocalBinder extends Binder {
        public Steps getService() {
            return Steps.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildFitnessClient();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                isNewDay();
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                while (!pm.isInteractive()) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!pm.isDeviceIdleMode()) {
                                Thread.sleep(1000 * 600);
                            } else {
                                Thread.sleep(1000 * 30);
                            }
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                doInBackground();

                Date currentLocalTime = Calendar.getInstance().getTime();

                DateFormat date = new SimpleDateFormat("HH:mm");
                String localTime = date.format(currentLocalTime);

                SharedPreferences mPref = getApplicationContext().getSharedPreferences("StepsService", MODE_PRIVATE);
                SharedPreferences targetSteps = getApplicationContext().getSharedPreferences("TargetSteps", MODE_PRIVATE);

                String target = targetSteps.getString("target", "10000");

                float tmp = Float.parseFloat(mPref.getString("step", "0"));
                float division = ((tmp / Float.parseFloat(target)) * 100);

                NotificationCompat.Builder notify_builder = new NotificationCompat.Builder(getApplicationContext());

                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent1, 0);

                notify_builder.setContentIntent(pendingIntent);
                notify_builder.setSmallIcon(R.mipmap.ic_launcher);

                notify_builder.setContentTitle("Passi Totali: " + mPref.getString("step", "0"));
                notify_builder.setContentText("Sei al " + Math.round(division) + "% del tuo obiettivo giornaliero.");

                Calendar c = Calendar.getInstance();
                int hours = c.get(Calendar.HOUR_OF_DAY);

                if(division >= 90){
                    notify_builder.setContentText("Sei al " + Math.round(division) + "% del tuo obiettivo giornaliero. Ultimo sforzo!");
                }
                if(division >= 100){
                    notify_builder.setContentText("Obiettivo giornaliero raggiunto. Complimenti!");
                }
                if(hours > 10 && division <= 30){
                    notify_builder.setContentText("Sei al " + Math.round(division) + "% del tuo obiettivo giornaliero. Camminiamo?");
                }

                notify_builder.setOngoing(true);
                startForeground(1, notify_builder.build());
            }

        }, 100, 1000 * 15);

        return Service.START_STICKY;
    }

    public void doInBackground() {
        mClient.connect();

        PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_STEP_COUNT_DELTA);

        result.setResultCallback(new ResultCallback<DailyTotalResult>() {
            @Override
            public void onResult(@NonNull DailyTotalResult dailyTotalResult) {
                int steps1;
                if (dailyTotalResult.getTotal().getDataPoints().isEmpty()) {
                    steps1 = 0;
                } else {
                    steps1 = dailyTotalResult.getTotal().getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                }
                steps = String.valueOf(steps);

                SharedPreferences mPref = getApplicationContext().getSharedPreferences("StepsService", MODE_PRIVATE);
                mPref.edit().putString("step", String.valueOf(steps1)).apply();
            }
        });
    }

    public void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i("TAG", "Connected!!!");
                                // Now you can make calls to the Fitness APIs.
                                // Put application specific code here.
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i("TAG", "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i("TAG", "Connection lost.  Reason: BackService Disconnected");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult result) {
                                Log.i("TAG", "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                /*try {
                                    Log.i("TAG", "Attempting to resolve failed connection");
                                    result.startResolutionForResult((Activity) getApplicationContext(), REQUEST_OAUTH);
                                } catch (IntentSender.SendIntentException e) {
                                    Log.e("TAG", "Exception while starting resolution activity", e);
                                }*/
                            }
                        })
                .build();
    }

    public void isNewDay() {
        Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.HOUR_OF_DAY);
        //Log.e("Hours", String.valueOf(hours));
        int minutes = c.get(Calendar.MINUTE);
        //Log.e("Hours", String.valueOf(minutes));
        if (hours < 1 && minutes < 6) {
            SharedPreferences tmpPerson = getSharedPreferences("Temp", MODE_PRIVATE);
            tmpPerson.edit().clear().apply();
        }
    }

}
