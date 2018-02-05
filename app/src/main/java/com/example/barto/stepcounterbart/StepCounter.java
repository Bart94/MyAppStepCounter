package com.example.barto.stepcounterbart;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class StepCounter {
    String steps;
    private Context c;
    private GoogleApiClient mClient = null;
    private static final int REQUEST_OAUTH = 1;

    public StepCounter(Context context, GoogleApiClient client) {
        this.c = context;
        this.mClient = client;
    }

    public StepCounter(Context context) {
        this.c = context;
        buildFitnessClient();
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

                SharedPreferences mPref = c.getSharedPreferences("Steps", MODE_PRIVATE);
                mPref.edit().putString("step", String.valueOf(steps1)).apply();

                Log.i("Totale stesps: ", mPref.getString("step", " 0 "));
            }
        });
    }

    public void caloriesInBackground() {
        mClient.connect();
        PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_CALORIES_EXPENDED);

        result.setResultCallback(new ResultCallback<DailyTotalResult>() {
            @Override
            public void onResult(@NonNull DailyTotalResult dailyTotalResult) {
                float calories;
                if (dailyTotalResult.getTotal().getDataPoints().isEmpty()) {
                    calories = 0;
                } else {
                    calories = dailyTotalResult.getTotal().getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();
                }
                int tmp = (int) (Math.ceil(calories * Math.pow(10, 0)) / Math.pow(10, 0));

                SharedPreferences calor = c.getSharedPreferences("Calories", MODE_PRIVATE);
                calor.edit().putString("cal", String.valueOf(tmp)).apply();

                Log.i("Totale calorie: ", String.valueOf(tmp));
            }
        });
    }

    //Get steps from last 4 weeks
    public void stepsThisMonth() {
        mClient.connect();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 59);

        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        long startTime = cal.getTimeInMillis();


        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();

        //Check how many steps were walked and recorded in the last 7 days
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        PendingResult<DataReadResult> result = Fitness.HistoryApi.readData(mClient, readRequest);
        result.setResultCallback(new ResultCallback<DataReadResult>() {
            @Override
            public void onResult(@NonNull DataReadResult dataReadResult) {
                int total = 0;
                ArrayList<String> array1 = new ArrayList<>();
                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSetx = bucket.getDataSets();
                    for (DataSet dataSet : dataSetx) {
                        if (dataSet.getDataType().getName().equals("com.google.step_count.delta")) {
                            if (dataSet.getDataPoints().size() > 0) {
                                // total steps
                                array1.add(String.valueOf(dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt()));
                                total += dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                            }
                        }
                    }
                }
                saveUpdateArray(array1, "monthSteps");
                SharedPreferences monstep = c.getSharedPreferences("MonStep", MODE_PRIVATE);
                monstep.edit().putString("month", String.valueOf(total)).apply();
            }
        });
    }

    //Get steps from last 1 weeks
    public void stepsThisWeek() {
        mClient.connect();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 59);

        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long startTime = cal.getTimeInMillis();

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();

        //Check how many steps were walked and recorded in the last 7 days
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        PendingResult<DataReadResult> result = Fitness.HistoryApi.readData(mClient, readRequest);
        result.setResultCallback(new ResultCallback<DataReadResult>() {
            @Override
            public void onResult(@NonNull DataReadResult dataReadResult) {
                ArrayList<String> array = new ArrayList<>();
                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSetx = bucket.getDataSets();
                    for (DataSet dataSet : dataSetx) {
                        if (dataSet.getDataType().getName().equals("com.google.step_count.delta")) {
                            if (dataSet.getDataPoints().size() > 0) {
                                // total steps
                                array.add(String.valueOf(dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt()));
                            }
                        }
                    }
                }
                saveUpdateArray(array, "weekSteps");
            }
        });
    }

    //Get steps from today
    public void stepsThisDay() {
        mClient.connect();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 59);

        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();

        //Check how many steps were walked and recorded today
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        PendingResult<DataReadResult> result = Fitness.HistoryApi.readData(mClient, readRequest);
        result.setResultCallback(new ResultCallback<DataReadResult>() {
            @Override
            public void onResult(@NonNull DataReadResult dataReadResult) {
                ArrayList<String> array = new ArrayList<>();
                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSetx = bucket.getDataSets();
                    for (DataSet dataSet : dataSetx) {
                        if (dataSet.getDataType().getName().equals("com.google.step_count.delta")) {
                            if (dataSet.getDataPoints().size() > 0) {
                                // total steps
                                array.add(String.valueOf(dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt()));
                                Log.e("daily",String.valueOf(dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt()) );
                            }
                        }
                    }
                }
                saveUpdateArray(array, "dailySteps");
            }
        });
    }

    public void saveUpdateArray(ArrayList<String> array, String name) {
        SharedPreferences tmpArray = c.getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor edit = tmpArray.edit();
        int i;

        for (i = 0; i < array.size(); i++) {
            edit.putString("item" + i, array.get(i));
            edit.apply();
        }
    }

    //Delta ultime due settimane
    public void stepsDailyDelta() {
        mClient.connect();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 59);

        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -2);
        long startTime = cal.getTimeInMillis();

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();

        //Check how many steps were walked and recorded in the last 7 days
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        PendingResult<DataReadResult> result = Fitness.HistoryApi.readData(mClient, readRequest);
        result.setResultCallback(new ResultCallback<DataReadResult>() {
            @Override
            public void onResult(@NonNull DataReadResult dataReadResult) {
                ArrayList<Integer> array = new ArrayList<>();
                int i = 0;
                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSetx = bucket.getDataSets();
                    for (DataSet dataSet : dataSetx) {
                        if (dataSet.getDataType().getName().equals("com.google.step_count.delta")) {
                            if (dataSet.getDataPoints().size() > 0) {
                                //Log.e("DELTA" + i, String.valueOf(dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt()));
                                array.add(dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt());
                                i++;
                            }
                        }
                    }
                }
                if (!array.isEmpty()) {
                    if (array.get(array.size() - 1) != 0) {
                        Double tmp = ((double) (array.get(array.size() - 1) - array.get(0)) / (double) array.get(0)) * 100;
                        int deltavalue = tmp.intValue();
                        SharedPreferences delta = c.getSharedPreferences("DeltaStep", MODE_PRIVATE);
                        delta.edit().putString("delta", String.valueOf(deltavalue)).apply();
                    }
                }
            }
        });
    }

    public void customSteps(Date start, Date end){
        mClient.connect();
        Calendar cal = Calendar.getInstance();

        cal.setTime(end);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 59);

        long endTime = cal.getTimeInMillis();

        cal.setTime(start);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 59);

        long startTime = cal.getTimeInMillis();

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();

        //Check how many steps were walked and recorded in the last 7 days
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        PendingResult<DataReadResult> result = Fitness.HistoryApi.readData(mClient, readRequest);
        result.setResultCallback(new ResultCallback<DataReadResult>() {
            @Override
            public void onResult(@NonNull DataReadResult dataReadResult) {
                int total = 0;
                ArrayList<String> array1 = new ArrayList<>();
                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSetx = bucket.getDataSets();
                    for (DataSet dataSet : dataSetx) {
                        if (dataSet.getDataType().getName().equals("com.google.step_count.delta")) {
                            if (dataSet.getDataPoints().size() > 0) {
                                // total steps
                                array1.add(String.valueOf(dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt()));
                                total = total + dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                            }
                        }
                    }
                }
                saveUpdateArray(array1, "customSteps");
            }
        });
    }

    public void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(c)
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
                                doInBackground();
                                caloriesInBackground();
                                stepsThisMonth();
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
                                try {
                                    Log.i("TAG", "Attempting to resolve failed connection");
                                    result.startResolutionForResult((Activity) c, REQUEST_OAUTH);
                                } catch (IntentSender.SendIntentException e) {
                                    Log.e("TAG", "Exception while starting resolution activity", e);
                                }
                            }
                        })
                .build();
    }

}
