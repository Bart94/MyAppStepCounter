package com.example.barto.stepcounterbart;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DeltaFragment extends Fragment {
    TextView title;
    ListView listView;
    TextView textView;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.third_frag, container, false);

        TextView title = v.findViewById(R.id.textViewThird);
        listView = v.findViewById(R.id.listViewThird);
        textView = v.findViewById(R.id.positionThird);

        title.setText("Variazione rispetto a Ieri");

        Date currentLocalTime = Calendar.getInstance().getTime();
        DateFormat date = new SimpleDateFormat("HH:mm");
        String localTime = date.format(currentLocalTime);

        mSwipeRefreshLayout = v.findViewById(R.id.swiperefreshmonthlyfragment);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FirebaseDb(getContext(), listView, textView).deltaStep();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        if (haveNetworkConnection()) {
            /*TextView text = v.findViewById(R.id.lastUpdateThird);
            String s = "Last Update: " + localTime;
            text.setText(s);*/
            new FirebaseDb(getContext(), listView, textView).deltaStep();
        }
        return v;
    }

    public static DeltaFragment newInstance(String text3) {

        DeltaFragment f = new DeltaFragment();
        Bundle b = new Bundle();
        b.putString("title", text3);

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
