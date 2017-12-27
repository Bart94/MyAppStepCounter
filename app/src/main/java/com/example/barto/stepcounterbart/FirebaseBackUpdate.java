package com.example.barto.stepcounterbart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import static android.content.Context.MODE_PRIVATE;

public class FirebaseBackUpdate extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        final Context c = context;

        StepCounter step = new StepCounter(context);
        step.doInBackground();
        step.stepsDailyDelta();
        step.stepsThisMonth();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

                SharedPreferences firstStart = c.getSharedPreferences("FirstStart", MODE_PRIVATE);
                SharedPreferences mPref = c.getSharedPreferences("Steps", MODE_PRIVATE);
                SharedPreferences monstep = c.getSharedPreferences("MonStep", MODE_PRIVATE);
                SharedPreferences identifier = c.getSharedPreferences("Id", MODE_PRIVATE);
                SharedPreferences delta = c.getSharedPreferences("DeltaStep", MODE_PRIVATE);

                float tmp = Float.parseFloat(mPref.getString("step", "0"));
                float  division = ((tmp / (float) 10000) * 100);

                if (!firstStart.getString("boolean", "false").equalsIgnoreCase("false")) {
                    Gson gson = new Gson();
                    SharedPreferences tmpPerson = c.getSharedPreferences("tmp", MODE_PRIVATE);
                    String json = tmpPerson.getString("Person", "");
                    Person obj = gson.fromJson(json, Person.class);

                    String steps = mPref.getString("step", "0");

                    mRef.child("users").child(identifier.getString("id", "0")).child("delta").setValue(delta.getString("delta", "0"));
                    mRef.child("users").child(identifier.getString("id", "0")).child("monstep").setValue(monstep.getString("month", "0"));

                    obj.setSteps(steps);
                    FirebaseDb fdb = new FirebaseDb(c);
                    fdb.setNode(mRef, obj);
                }

                //Create Notification
                /*NotificationCompat.Builder notify_builder = new NotificationCompat.Builder(c);

                Intent intent1 = new Intent(c, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent1, 0);

                Date currentLocalTime = Calendar.getInstance().getTime();

                DateFormat date = new SimpleDateFormat("HH:mm");
                String localTime = date.format(currentLocalTime);

                notify_builder.setContentIntent(pendingIntent);
                notify_builder.setSmallIcon(R.mipmap.ic_launcher);

                if(!haveNetworkConnection(c) && (Calendar.getInstance().get(Calendar.HOUR) == 0)){
                    notify_builder.setContentTitle("Passi Totali: " + 0);
                    notify_builder.setContentText("Alle " + localTime + " eri al " + 0 + "% del tuo obiettivo giornaliero!");
                }else{
                    notify_builder.setContentTitle("Passi Totali: " + mPref.getString("step", "0"));
                    notify_builder.setContentText("Alle " + localTime + " eri al " + Math.round(division) + "% del tuo obiettivo giornaliero!");
                }

                notify_builder.setOngoing(true);

                NotificationManager notification = (NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);

                notification.notify(001, notify_builder.build());*/
            }
        }, 1000 * 60);
    }

    private boolean haveNetworkConnection(Context c) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
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
            Toast.makeText(c, "Enable Connection For Accurate Performance!", Toast.LENGTH_SHORT).show();
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
