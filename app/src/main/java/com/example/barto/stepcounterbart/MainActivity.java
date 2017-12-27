package com.example.barto.stepcounterbart;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.thunderrise.animations.FlipAnimation;
import com.thunderrise.animations.RotateAnimation;
import com.thunderrise.animations.ShakeAnimation;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_OAUTH = 1;
    private static final String TAG = "Tag";
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mClient = null;
    TextView tmp;

    //OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        drawer.setScrimColor(Color.TRANSPARENT);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences firstStart = getSharedPreferences("FirstStart", MODE_PRIVATE);

        if (firstStart.getString("boolean", "false").equalsIgnoreCase("false")) {
            Intent i = new Intent(this, Settings.class);
            startActivity(i);
        } else {
            //Connessione a Google Fit
            buildFitnessClient();

            //Setto nome all'Header
            View header = navigationView.getHeaderView(0);
            TextView name = header.findViewById(R.id.header_textView);
            Person p = retrieveUserData();
            String tmp = p.getName() + " " + p.getSurname();
            name.setText(tmp);

            //Verifica che GoogleFit sia installato. Se non lo è, vieni reindirizzato al PlayStore
            if (!IsGoogleFitInstalled()) {
                AlertDialog.Builder miaAlert = new AlertDialog.Builder(this);
                miaAlert.setTitle("Attenzione!");
                miaAlert.setMessage("E' necessario installare Google Fit!");
                miaAlert.setCancelable(false);
                miaAlert.setPositiveButton("Vai al PlayStore", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.fitness")));
                    }
                });
                AlertDialog alert = miaAlert.create();
                alert.show();
            }
            optionMenu();
        }
    }

    @Override
    protected void onStart() {
        //Connessione a Google Fit
        buildFitnessClient();
        // Connect to the Fitness API
        Log.i(TAG, "Connecting...");
        mClient.connect();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Intent i1 = new Intent(this, GpsService.class);
            startService(i1);
        }

        animationIcon();

        Handler m = new Handler();
        m.postDelayed(runnable, 4500);
        super.onStart();
    }

    @Override
    protected void onPause() {
        SharedPreferences firstStart = getSharedPreferences("FirstStart", MODE_PRIVATE);

        if (!firstStart.getString("boolean", "false").equalsIgnoreCase("false")) {
            doOnStart();
            scheduleAlarm();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        Handler m = new Handler();
        m.postDelayed(runnable, 4500);
        SharedPreferences firstStart = getSharedPreferences("FirstStart", MODE_PRIVATE);
        if (!firstStart.getString("boolean", "false").equalsIgnoreCase("false")) {
            SharedPreferences mPref = getSharedPreferences("Steps", MODE_PRIVATE);
            String steps = mPref.getString("step", "0");
            Person p = retrieveUserData();
            p.setSteps(steps);
            new FirebaseDb(this).setNode(FirebaseDatabase.getInstance().getReference(), p);
        }
        super.onResume();
    }

    @Override
    protected void onStop() {

        if (mClient != null) {
            mClient.disconnect();
            Intent i = new Intent(this, Steps.class);
            startService(i);
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, Settings.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Intent i;

        int id = item.getItemId();

        if (id == R.id.stats) {
            // Handle the camera action
            i = new Intent(this, Stats.class);
            startActivity(i);
        } else if (id == R.id.rank) {
            i = new Intent(this, RankHandler.class);
            startActivity(i);
        } else if (id == R.id.tracking) {
            i = new Intent(this, MapsActivity.class);
            startActivity(i);
        } else if (id == R.id.setting) {
            i = new Intent(this, Settings.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
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
                                Log.i(TAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.
                                // Put application specific code here.
                                //doOnStart();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: BackService Disconnected");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult result) {
                                Log.i(TAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                try {
                                    Log.i(TAG, "Attempting to resolve failed connection");
                                    result.startResolutionForResult(MainActivity.this, REQUEST_OAUTH);
                                } catch (IntentSender.SendIntentException e) {
                                    Log.e(TAG, "Exception while starting resolution activity", e);
                                }
                            }
                        })
                .build();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void doOnStart() {
        SharedPreferences mPref = getSharedPreferences("Steps", MODE_PRIVATE);
        String steps = mPref.getString("step", "0");
        TextView text = findViewById(R.id.step_update);
        Person user = retrieveUserData();

        animationText(text);

        if (steps.equalsIgnoreCase("0")) {
            text.setText(String.valueOf(steps));
        } else {
            text.setText(String.valueOf(steps));
            user.setSteps(steps);
            saveUpdate(user);
        }

        //TimeElapsed
        long millis = Integer.parseInt(steps) * 746;
        @SuppressLint("DefaultLocale")
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        TextView time = findViewById(R.id.elapsed_time);
        animationText(time);
        time.setText(hms);

        //Km walked
        TextView text1 = findViewById(R.id.chilometers);
        double km = Integer.parseInt(steps) * 0.27 / 1000;
        float result = (float) (Math.ceil(km * Math.pow(10, 2)) / Math.pow(10, 2));
        animationText(text1);
        text1.setText(String.valueOf(result));

        //Calories Burned
        SharedPreferences calor = getSharedPreferences("Calories", MODE_PRIVATE);
        String cal = calor.getString("cal", "0");
        TextView text2 = findViewById(R.id.calories);
        animationText(text2);
        if (cal.equalsIgnoreCase("0")) {
            text2.setText(String.valueOf(cal));
        } else {
            text2.setText(String.valueOf(cal));
        }
    }

    private final Runnable runnable = new Runnable() {
        public void run() {
            //new FirebaseDb(MainActivity.this).uptodb();
            new StepCounter(MainActivity.this, mClient).doInBackground();
            new StepCounter(MainActivity.this, mClient).caloriesInBackground();
            new StepCounter(MainActivity.this, mClient).stepsThisMonth();
            new StepCounter(MainActivity.this, mClient).stepsDailyDelta();
            new StepCounter(MainActivity.this, mClient).stepsThisWeek();
            doOnStart();
        }
    };

    public void saveUpdate(Person p) {
        SharedPreferences tmpPerson = getSharedPreferences("tmp", MODE_PRIVATE);
        SharedPreferences.Editor edit = tmpPerson.edit();
        Gson gson = new Gson();
        String json = gson.toJson(p);
        edit.putString("Person", json);
        edit.apply();
    }

    public Person retrieveUserData() {
        Gson gson = new Gson();
        SharedPreferences tmpPerson = getSharedPreferences("tmp", MODE_PRIVATE);
        String json = tmpPerson.getString("Person", "");
        if (json.equalsIgnoreCase("")) {
            Person p = new Person("", "", "", "", "", "", "");
            return p;
        } else {
            Person obj = gson.fromJson(json, Person.class);
            Log.i("MainActivity", obj.toString());
            return obj;
        }
    }

    public void scheduleAlarm() {
        Calendar calendar = Calendar.getInstance();

        //Setto l'intent che dovrà passare alla classe Reminder
        Intent intentAlarm = new Intent(this, FirebaseBackUpdate.class);

        //Faccio partire il conteggio
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_HOUR,
                PendingIntent.getBroadcast(this, 0, intentAlarm, 0));
    }

    public void animationIcon() {
        ShakeAnimation.create().with(findViewById(R.id.shoes))
                .setDuration(2000)
                .setRepeatCount(1)
                .start();

        ShakeAnimation.create().with(findViewById(R.id.imageView4))
                .setDuration(2000)
                .setRepeatCount(1)
                .start();

        RotateAnimation.create().with(findViewById(R.id.imageView3))
                .setRepeatCount(1)
                .setDuration(2000)
                .start();

        FlipAnimation.create().with(findViewById(R.id.distance))
                .setDuration(2000)
                .setRepeatCount(1)
                .start();
    }

    public void animationText(TextView text) {
        tmp = text;
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                fadeOut.setDuration(1500);
                AnimationSet animation = new AnimationSet(false); //change to false

                animation.addAnimation(fadeOut);
                tmp.setAnimation(animation);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                fadeIn.setStartOffset(500);
                fadeIn.setDuration(1500);

                AnimationSet animation = new AnimationSet(false); //change to false

                animation.addAnimation(fadeIn);
                tmp.setAnimation(animation);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        tmp.addTextChangedListener(textWatcher);
    }

    public boolean IsGoogleFitInstalled() {
        String string = "com.google.android.apps.fitness";
        boolean checkIsInstalled = false;
        List<PackageInfo> apps = getPackageManager().getInstalledPackages(0);

        //ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).packageName.equalsIgnoreCase(string)) {
                checkIsInstalled = true;
            }
            //Log.e("APP NAME", apps.get(i).applicationInfo.loadLabel(getPackageManager()).toString());
            //Log.e("PACKAGE", apps.get(i).packageName);
        }
        return checkIsInstalled;
    }

    public void optionMenu() {
        //Create Options Menu
        ImageView icon = new ImageView(this); // Create an icon
        icon.setImageDrawable(getDrawable(R.drawable.ic_plus_button));
        icon.setColorFilter(Color.parseColor("#03A9F4"));

        FloatingActionButton actionButton = new FloatingActionButton.Builder(this)
                .setContentView(icon)
                .build();

        final SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);

        // repeat many times:
        ImageView itemIcon = new ImageView(this);
        itemIcon.setImageDrawable(getDrawable(R.drawable.ic_tape_icon));
        SubActionButton button1 = itemBuilder.setContentView(itemIcon).build();

        ImageView itemIcon1 = new ImageView(this);
        itemIcon1.setImageDrawable(getDrawable(R.drawable.ic_weight));
        SubActionButton button2 = itemBuilder.setContentView(itemIcon1).build();

        ImageView itemIcon2 = new ImageView(this);
        itemIcon2.setImageDrawable(getDrawable(R.drawable.ic_target));
        SubActionButton button3 = itemBuilder.setContentView(itemIcon2).build();

        final FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(button1)
                .addSubActionView(button2)
                .addSubActionView(button3)
                .attachTo(actionButton)
                .build();

        itemIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LovelyTextInputDialog(MainActivity.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setTitle("Inserisci Altezza:")
                        .setIcon(R.drawable.ic_tape_white)
                        .setInputFilter("Error! Inserisci un valore in cm.", new LovelyTextInputDialog.TextFilter() {
                            @Override
                            public boolean check(String text) {
                                return (text.matches("[0-9]+") && text.length() > 1 && text.length() < 4);
                            }
                        })
                        .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                            @Override
                            public void onTextInputConfirmed(String text) {
                                Person p = retrieveUserData();
                                p.setHeight(text);
                                saveUpdate(p);
                                new FirebaseDb(getApplicationContext()).setNode(FirebaseDatabase.getInstance().getReference(), p);
                            }
                        })
                        .show();
            }
        });

        itemIcon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LovelyTextInputDialog(MainActivity.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setTitle("Inserisci Peso:")
                        .setIcon(R.drawable.ic_weight_white)
                        .setInputFilter("Error! Inserisci un valore in Kg.", new LovelyTextInputDialog.TextFilter() {
                            @Override
                            public boolean check(String text) {
                                return (text.matches("[0-9]+") && text.length() > 0 && text.length() < 4);
                            }
                        })
                        .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                            @Override
                            public void onTextInputConfirmed(String text) {
                                Person p = retrieveUserData();
                                p.setWeight(text);
                                saveUpdate(p);
                                new FirebaseDb(getApplicationContext()).setNode(FirebaseDatabase.getInstance().getReference(), p);
                            }
                        })
                        .show();
            }
        });

        itemIcon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LovelyTextInputDialog(MainActivity.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setTitle("Inserisci Obiettivo:")
                        .setIcon(R.drawable.ic_target_white)
                        .setInputFilter("Error! Inserisci un numero valido.", new LovelyTextInputDialog.TextFilter() {
                            @Override
                            public boolean check(String text) {
                                return text.matches("[0-9]+") && text.length() > 1;
                            }
                        })
                        .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                            @Override
                            public void onTextInputConfirmed(String text) {
                                SharedPreferences mPref = getApplicationContext().getSharedPreferences("TargetSteps", MODE_PRIVATE);
                                mPref.edit().putString("target", text).apply();
                            }
                        })
                        .show();
            }
        });
    }
}