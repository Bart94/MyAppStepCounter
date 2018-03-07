package com.example.barto.stepcounterbart;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

public class GpsService extends Service {

    private final GpsService.LocalBinder mBinder = new LocalBinder();
    List<LatLng> points = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationClient;
    Position pos = new Position();
    private LocationCallback mLocationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Timer t = new Timer();
        t.schedule(looper(), 100, 1000 * 10);
        return START_STICKY;
    }

    public class LocalBinder extends Binder {
        public GpsService getService() {
            return GpsService.this;
        }
    }

    public void saveData(Position p) {
        SharedPreferences tmpPerson = getSharedPreferences("Temp", MODE_PRIVATE);
        SharedPreferences.Editor edit = tmpPerson.edit();
        Gson gson = new Gson();
        String json = gson.toJson(p);
        edit.putString("Position", json);
        edit.apply();
    }

    public Position retrieveData() {
        Gson gson = new Gson();
        SharedPreferences tmpPerson = getSharedPreferences("Temp", MODE_PRIVATE);
        tmpPerson.edit().clear().apply();

        String json = tmpPerson.getString("Position", "");
        if (json.equalsIgnoreCase("")) {
            Position p = new Position(createArray());
            return p;
        } else {
            Position obj = gson.fromJson(json, Position.class);
            return obj;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double dist = earthRadius * c;

        return dist; // output distance, in MILES
    }

    public List<LatLng> createArray() {
        List<LatLng> array = new List<LatLng>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<LatLng> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] a) {
                return null;
            }

            @Override
            public boolean add(LatLng latLng) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends LatLng> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, @NonNull Collection<? extends LatLng> c) {
                return false;
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public LatLng get(int index) {
                return null;
            }

            @Override
            public LatLng set(int index, LatLng element) {
                return null;
            }

            @Override
            public void add(int index, LatLng element) {

            }

            @Override
            public LatLng remove(int index) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @NonNull
            @Override
            public ListIterator<LatLng> listIterator() {
                return null;
            }

            @NonNull
            @Override
            public ListIterator<LatLng> listIterator(int index) {
                return null;
            }

            @NonNull
            @Override
            public List<LatLng> subList(int fromIndex, int toIndex) {
                return null;
            }
        };
        return array;
    }

    /*public void isNewDay() {
        Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.HOUR_OF_DAY);
        //Log.e("Hours", String.valueOf(hours));
        int minutes = c.get(Calendar.MINUTE);
        //Log.e("Hours", String.valueOf(minutes));
        if (hours < 1 && minutes < 7) {
            SharedPreferences tmpPerson = getSharedPreferences("Temp", MODE_PRIVATE);
            tmpPerson.edit().clear().apply();
        }
    }*/

    public boolean CheckGpsStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public TimerTask looper() {
        TimerTask timer = new TimerTask() {
            @Override
            public void run() {
                //isNewDay();
                if (CheckGpsStatus()) {

                    if (ActivityCompat.checkSelfPermission(GpsService.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(GpsService.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    }

                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(GpsService.this);

                    mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());
                                Position tmpPos = retrieveData();
                                List<LatLng> tmp = tmpPos.getList();
                                double tmplatitude;
                                double tmplongitude;

                                //Log.e("Gelel", "sahsa");

                                if (tmp.size() >= 1) {
                                    tmplatitude = tmp.get(tmp.size() - 1).latitude;
                                    tmplongitude = tmp.get(tmp.size() - 1).longitude;

                                    // if distance > 0.003 miles (4 meters) we take locations
                                    float[] distance = new float[1];
                                    Location.distanceBetween(tmplatitude, tmplongitude, location.getLatitude(), location.getLongitude(), distance);
                                    //distance(tmplatitude, tmplongitude, location.getLatitude(), location.getLongitude())
                                    if ((distance[0] > 0.003) || (distance(tmplatitude, tmplongitude, location.getLatitude(), location.getLongitude()) > 0.2)) {
                                        points.add(myPosition);
                                        //Log.e("myPos", myPosition.toString());
                                        //Log.e("arraySize", String.valueOf(points.size()));
                                        pos.setList(points);
                                        saveData(pos);
                                    }
                                }else {
                                    points.add(myPosition);
                                    //Log.e("myPos", myPosition.toString());
                                    pos.setList(points);
                                    saveData(pos);
                                }
                            }
                        }
                    });
                }
            }
        };
        return timer;
    }
}
