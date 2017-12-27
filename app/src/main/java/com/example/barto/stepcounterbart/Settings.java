package com.example.barto.stepcounterbart;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

public class Settings extends AppCompatActivity {
    EditText edit_name;
    EditText edit_surname;
    RadioButton male_button;
    RadioButton female_button;
    EditText edit_weight;
    EditText edit_height;
    EditText edit_birth;
    FirebaseDb fdb = new FirebaseDb(this);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.settings);
        super.onCreate(savedInstanceState);

        edit_birth = findViewById(R.id.birthText);
        new DateInputMask(edit_birth);

        edit_name = findViewById(R.id.nameText);
        edit_surname = findViewById(R.id.surnameText);
        male_button = findViewById(R.id.radioMale);
        female_button = findViewById(R.id.radioFemale);
        edit_weight = findViewById(R.id.weightText);
        edit_height = findViewById(R.id.heightText);

        /*final Button confirmButton = findViewById(R.id.confirmButton);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check_fields()) {
                    edit_name.setEnabled(false);
                    edit_surname.setEnabled(false);
                    edit_birth.setEnabled(false);
                    RadioGroup group = findViewById(R.id.radioGroup);
                    group.getChildAt(0).setEnabled(false);
                    group.getChildAt(1).setEnabled(false);

                    fdb.setNode(FirebaseDatabase.getInstance().getReference(), retrieveUserData());
                }
            }
        });*/

        final ProgressGenerator progressGenerator = new ProgressGenerator(new ProgressGenerator.OnCompleteListener() {
            @Override
            public void onComplete() {

            }
        });
        final ActionProcessButton btnSignIn = findViewById(R.id.btnSignIn);
        Bundle extras = getIntent().getExtras();
        if(extras != null && extras.getBoolean("EXTRAS_ENDLESS_MODE")) {
            btnSignIn.setMode(ActionProcessButton.Mode.ENDLESS);
        } else {
            btnSignIn.setMode(ActionProcessButton.Mode.ENDLESS);
        }
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check_fields()) {
                    progressGenerator.start(btnSignIn);
                    btnSignIn.setEnabled(false);
                    edit_name.setEnabled(false);
                    edit_surname.setEnabled(false);
                    edit_birth.setEnabled(false);
                    RadioGroup group = findViewById(R.id.radioGroup);
                    group.getChildAt(0).setEnabled(false);
                    group.getChildAt(1).setEnabled(false);
                    fdb.setNode(FirebaseDatabase.getInstance().getReference(), retrieveUserData());
                }
            }
        });

        SharedPreferences firstStart = getSharedPreferences("FirstStart", MODE_PRIVATE);
        if (firstStart.getString("boolean", "false").equalsIgnoreCase("true")) {
            Person p = retrieveUserData();
            edit_name.setText(p.getName());
            edit_name.setEnabled(false);
            edit_surname.setText(p.getSurname());
            edit_surname.setEnabled(false);
            edit_birth.setText(p.getBirth());
            edit_birth.setEnabled(false);
            RadioGroup group = findViewById(R.id.radioGroup);
            if (p.getSex().equalsIgnoreCase("Uomo")) {
                male_button.setChecked(true);
            } else {
                female_button.setChecked(true);
            }
            group.getChildAt(0).setEnabled(false);
            group.getChildAt(1).setEnabled(false);
        }
    }

    public boolean check_fields() {
        ArrayList<String> list = new ArrayList<>();
        int i;
        int count = 0;

        list.add(edit_name.getText().toString());
        list.add(edit_surname.getText().toString());
        list.add(edit_birth.getText().toString());
        list.add(edit_weight.getText().toString());
        list.add(edit_height.getText().toString());


        String name = edit_name.getText().toString();
        String surname = edit_surname.getText().toString();
        String birth = edit_birth.getText().toString();
        String weight = edit_weight.getText().toString();
        String height = edit_height.getText().toString();

        for (i = 0; i < list.size(); i++) {
            if (list.get(i).isEmpty()) {
                count++;
            }
        }

        if (count == 0 && (male_button.isChecked() || female_button.isChecked())) {

            if (male_button.isChecked()) {
                saveUserData(new Person(name, surname, "Uomo", birth, weight, height, "0"));
                //Log.i("Person", p.toString());
            } else if (female_button.isChecked()) {
                saveUserData(new Person(name, surname, "Donna", birth, weight, height, "0"));
                //Log.i("Person", p.toString());
            }
            SharedPreferences firstStart = getSharedPreferences("FirstStart", MODE_PRIVATE);
            firstStart.edit().putString("boolean", "true").apply();
            //Toast.makeText(this, "Inserimento avvenuto correttamente!", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, "Completa tutti i campi", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void saveUserData(Person p) {
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
            return obj;
        }
    }
}
