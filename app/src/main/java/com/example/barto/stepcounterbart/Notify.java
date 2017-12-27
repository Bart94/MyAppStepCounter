package com.example.barto.stepcounterbart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;


public class Notify extends BroadcastReceiver{
    Context c;
    GoogleApiClient mClient;

    public Notify(Context context, GoogleApiClient client){
        this.c = context;
        this.mClient = client;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
