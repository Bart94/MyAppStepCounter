package com.example.barto.stepcounterbart;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Rank extends AppCompatActivity {
    private DatabaseReference mRef;
    private int i, index = 0;
    protected static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rank);

        ListView listView = findViewById(R.id.listView);
        TextView textView = findViewById(R.id.position);
        Date currentLocalTime = Calendar.getInstance().getTime();
        DateFormat date = new SimpleDateFormat("HH:mm");
        String localTime = date.format(currentLocalTime);

        FirebaseDb fdb = new FirebaseDb(this, listView, textView);


        if (haveNetworkConnection()) {
            TextView text = findViewById(R.id.lastUpdate);
            String s = "Last Update: " + localTime;
            text.setText(s);
            fdb.uptodb();
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        if (!haveConnectedMobile && !haveConnectedWifi) {
            Toast.makeText(this, "Enable Connection For Accurate Performance!", Toast.LENGTH_SHORT).show();
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
