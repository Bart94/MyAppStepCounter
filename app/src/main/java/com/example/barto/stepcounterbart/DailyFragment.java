package com.example.barto.stepcounterbart;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DailyFragment extends Fragment {
    TextView title;
    ListView listView;
    TextView textView;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.first_frag, container, false);

        title = v.findViewById(R.id.textViewFirst);
        listView = v.findViewById(R.id.listViewFirst);
        textView = v.findViewById(R.id.positionFirst);

        ImageView img = v.findViewById(R.id.arrow_daily);
        img.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getContext(), MainActivity.class);
                startActivity(i);
            }
        });

        mSwipeRefreshLayout = v.findViewById(R.id.swiperefreshdailyfragment);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FirebaseDb(getContext(), listView, textView).dbGeneral("passi");
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        title.setText("Classifica Giornaliera");

        Date currentLocalTime = Calendar.getInstance().getTime();
        DateFormat date = new SimpleDateFormat("HH:mm");
        String localTime = date.format(currentLocalTime);

        if (haveNetworkConnection()) {
            /*TextView text = v.findViewById(R.id.lastUpdateFirst);
            String s = "Last Update: " + localTime;
            text.setText(s);*/
            new FirebaseDb(getContext(), listView, textView).dbGeneral("passi");
        }
        return v;
    }

    public static DailyFragment newInstance(String text1) {

        DailyFragment f = new DailyFragment();
        Bundle b = new Bundle();
        b.putString("title", text1);

        f.setArguments(b);

        return f;
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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
            Toast.makeText(getActivity(), "Enable Connection For Accurate Performance!", Toast.LENGTH_SHORT).show();
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
