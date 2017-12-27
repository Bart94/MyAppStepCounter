package com.example.barto.stepcounterbart;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class FirebaseDb {
    Context c;
    String s;
    DatabaseReference mRef;
    Person p;
    ArrayList<String> array;
    ArrayList<String> values;
    ListView listView;
    TextView textView;

    public FirebaseDb(Context context, ListView list, TextView text) {
        this.c = context;
        this.listView = list;
        this.textView = text;
    }

    public FirebaseDb(Context c) {
        this.c = c;
    }

    public void uptodb() {
        mRef = FirebaseDatabase.getInstance().getReference();

        p = retrieveUserData();

        setNode(mRef, p);

        /*int i;
        for (i = 1; i < 10; i++) {
            String idr = hashId(randomString());
            mRef.child("users").child(idr).child("delta").setValue(randomNumber(-300,600));
            mRef.child("users").child(idr).child("monstep").setValue(randomNumber(30000,60000));
            mRef.child("users").child(idr).child("nome").setValue(randomString());
            mRef.child("users").child(idr).child("cognome").setValue(randomString());
            mRef.child("users").child(idr).child("eta").setValue(randomNumber(25, 115));
            mRef.child("users").child(idr).child("peso").setValue(randomNumber(50, 215));
            mRef.child("users").child(idr).child("altezza").setValue(randomNumber(135, 230));
            mRef.child("users").child(idr).child("sesso").setValue("si");
            mRef.child("users").child(idr).child("passi").setValue(randomNumber(0, 8000));
        }*/

        array = new ArrayList<>();

        Query q = mRef.child("users").orderByChild("passi");

        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot users : dataSnapshot.getChildren()) {
                    String name = users.child("nome").getValue().toString();
                    String passi = users.child("passi").getValue().toString();
                    s = "Utente: " + name + " - " + "Passi: " + passi;
                    array.add(s);
                }
                Collections.reverse(array);
                int tmp = array.indexOf("Utente: " + p.getName() + " - " + "Passi: " + p.getSteps());

                String tmp1 = "Your position: " + (tmp + 1) + "/" + array.size();
                textView.setText(tmp1);

                String[] list = new String[array.size()];
                for (int j = 0; j < array.size(); j++) {
                    int index = j + 1;
                    list[j] = "     " + index + " -> " + array.get(j);
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(c, android.R.layout.simple_list_item_1, list);
                listView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void dbGeneral(final String value) {
        final String temp = value;
        mRef = FirebaseDatabase.getInstance().getReference();

        p = retrieveUserData();

        setNode(mRef, p);

        array = new ArrayList<>();
        values = new ArrayList<>();

        Query q = mRef.child("users").orderByChild(temp);

        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                SharedPreferences user_id = c.getSharedPreferences("Id", MODE_PRIVATE);
                int tmp = 0;
                for (DataSnapshot users : dataSnapshot.getChildren()) {
                    String name = users.child("nome").getValue().toString();
                    String value1 = users.child(temp).getValue().toString();
                    array.add(name);
                    values.add(value1);
                    if (users.getKey().equalsIgnoreCase(user_id.getString("id", "null"))) {
                        tmp = array.size();
                    }
                }
                Collections.reverse(array);
                Collections.reverse(values);

                String tmp1 = "Your position: " + (array.size() - tmp + 1) + "/" + array.size();
                textView.setText(tmp1);

                String[] list = new String[array.size()];
                for (int j = 0; j < array.size(); j++) {
                    list[j] = array.get(j);
                }

                String[] pass_value = new String[values.size()];
                for (int j = 0; j < values.size(); j++) {
                    pass_value[j] = values.get(j);
                }

                MyArrayAdapter arrayAdapter = new MyArrayAdapter(c, list, pass_value, (array.size() - tmp));
                listView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void deltaStep (){
        mRef = FirebaseDatabase.getInstance().getReference();

        p = retrieveUserData();

        setNode(mRef, p);

        array = new ArrayList<>();
        values = new ArrayList<>();

        Query q = mRef.child("users").orderByChild("delta");

        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                SharedPreferences user_id = c.getSharedPreferences("Id", MODE_PRIVATE);
                int tmp = 0;

                for (DataSnapshot users : dataSnapshot.getChildren()) {
                    String name = users.child("nome").getValue().toString();
                    String value1 = users.child("delta").getValue().toString();
                    int temp = Integer.parseInt(value1);
                    if(temp > 0){
                        values.add("+" + value1 + "%");
                    }else{
                        values.add(value1 + "%");
                    }
                    array.add(name);
                    if (users.getKey().equalsIgnoreCase(user_id.getString("id", "null"))) {
                        tmp = array.size();
                    }
                }
                Collections.reverse(array);
                Collections.reverse(values);

                String tmp1 = "Your position: " + (array.size() - tmp + 1) + "/" + array.size();
                textView.setText(tmp1);

                String[] list = new String[array.size()];
                for (int j = 0; j < array.size(); j++) {
                    list[j] = array.get(j);
                }

                String[] pass_value = new String[values.size()];
                for (int j = 0; j < values.size(); j++) {
                    pass_value[j] = values.get(j);
                }

                MyArrayAdapter arrayAdapter = new MyArrayAdapter(c, list, pass_value, array.size() - tmp);
                listView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void setNode(DatabaseReference mRef, Person p) {
        String user = p.getName() + " " + p.getSurname() + " " + p.getBirth();
        String id = hashId(user);

        SharedPreferences identifier = c.getSharedPreferences("Id", MODE_PRIVATE);
        identifier.edit().putString("id", id).apply();

        new StepCounter(c).stepsThisMonth();
        SharedPreferences monstep = c.getSharedPreferences("MonStep", MODE_PRIVATE);
        String month = monstep.getString("month", "0");

        SharedPreferences delta = c.getSharedPreferences("DeltaStep", MODE_PRIVATE);
        String deltaStep = delta.getString("delta", "0");

        mRef.child("users").child(id).child("delta").setValue(Integer.parseInt(deltaStep));
        mRef.child("users").child(id).child("monstep").setValue(Integer.parseInt(month));
        mRef.child("users").child(id).child("nome").setValue(p.getName());
        mRef.child("users").child(id).child("cognome").setValue(p.getSurname());
        mRef.child("users").child(id).child("sesso").setValue(p.getSex());
        mRef.child("users").child(id).child("eta").setValue(Integer.parseInt(get_age(p.getBirth())));
        mRef.child("users").child(id).child("peso").setValue(Integer.parseInt(p.getWeight()));
        mRef.child("users").child(id).child("altezza").setValue(Integer.parseInt(p.getHeight()));
        mRef.child("users").child(id).child("passi").setValue(Integer.parseInt(p.getSteps()));
    }


    public Person retrieveUserData() {
        Gson gson = new Gson();
        SharedPreferences tmpPerson = c.getSharedPreferences("tmp", MODE_PRIVATE);
        String json = tmpPerson.getString("Person", "");
        if (json.equalsIgnoreCase("")) {
            Person p = new Person("", "", "", "", "", "", "");
            return p;
        } else {
            Person obj = gson.fromJson(json, Person.class);
            return obj;
        }
    }

    public static String hashId(String encTarget) {
        MessageDigest mdEnc = null;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while encrypting to md5");
            e.printStackTrace();
        } // Encryption algorithm
        mdEnc.update(encTarget.getBytes(), 0, encTarget.length());
        String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
        while (md5.length() < 32) {
            md5 = "0" + md5;
        }
        return md5;
    }

    public String get_age(String date) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        int day = Integer.parseInt(date.substring(0, 2));
        int month = Integer.parseInt(date.substring(3, 5));
        int year = Integer.parseInt(date.substring(6, 10));

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return String.valueOf(age);
    }

    public int randomNumber(int low, int high) {
        Random random = new Random();
        return random.nextInt(high - low) + low;
    }

    public String randomString() {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDE".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output;
    }

}
