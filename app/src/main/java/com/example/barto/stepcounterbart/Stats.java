package com.example.barto.stepcounterbart;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Stats extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    EditText editStartDate;
    EditText editEndDate;
    TextView startDate;
    TextView endDate;
    GraphView graph;
    Button button;
    int whatEdit = 0;
    ArrayList<String> array;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistic_layout);

        Spinner spinner = findViewById(R.id.spinnerId);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Last Week", "Last Month", "Custom"});

        spinner.setAdapter(arrayAdapter);

        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        editStartDate = findViewById(R.id.editTextStartDate);
        editEndDate = findViewById(R.id.editTextEndDate);
        button = findViewById(R.id.buttonstats);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if (position == 0) {
                    setInvisible();
                    setWeekStep();
                    graph.setVisibility(View.VISIBLE);
                }
                if (position == 1) {
                    setInvisible();
                    setMonthStep();
                    graph.setVisibility(View.VISIBLE);
                }
                if (position == 2) {
                    setVisible();
                    graph.setVisibility(View.INVISIBLE);

                    editStartDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            whatEdit = 1;
                            Calendar now = Calendar.getInstance();
                            DatePickerDialog dpd = DatePickerDialog.newInstance(
                                    Stats.this,
                                    now.get(Calendar.YEAR),
                                    now.get(Calendar.MONTH),
                                    now.get(Calendar.DAY_OF_MONTH)
                            );
                            dpd.show(getFragmentManager(), "Datepickerdialog");
                        }
                    });

                    editEndDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            whatEdit = 2;
                            Calendar now = Calendar.getInstance();
                            DatePickerDialog dpd = DatePickerDialog.newInstance(
                                    Stats.this,
                                    now.get(Calendar.YEAR),
                                    now.get(Calendar.MONTH),
                                    now.get(Calendar.DAY_OF_MONTH)
                            );
                            dpd.show(getFragmentManager(), "Datepickerdialog");
                        }
                    });

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setInvisible();
                            //graph.setVisibility(View.VISIBLE);
                            final Date startDate = get_date(editStartDate.getText().toString());
                            final Date endDate = get_date(editEndDate.getText().toString());

                            if (check()) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(startDate);
                                cal.add(Calendar.DAY_OF_YEAR, -1);
                                new StepCounter(Stats.this).customSteps(cal.getTime(), endDate);

                                Handler h = new Handler();
                                h.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        customSteps();
                                        graph.setVisibility(View.VISIBLE);
                                        SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");
                                        String formatDate1 = format1.format(startDate);
                                        String formatDate2 = format1.format(endDate);
                                        graph.setTitle("From: " + formatDate1 + "     To: " + formatDate2);
                                    }
                                }, 1000);

                            } else {
                                setVisible();
                                Toast.makeText(Stats.this, "Inserisci date valide!", Toast.LENGTH_SHORT).show();
                            }
                            editStartDate.getText().clear();
                            editEndDate.getText().clear();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void customSteps() {
        String s = "customSteps";

        ArrayList<String> array = retrieveArray(s);
        DataPoint[] dataPoints = new DataPoint[array.size()];
        int i;

        for (i = 0; i < array.size(); i++) {
            dataPoints[i] = new DataPoint(i + 1, Integer.parseInt(array.get(i)));
        }

        graph = findViewById(R.id.graph);
        graph.removeAllSeries();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        graph.addSeries(series);
        graph.getGridLabelRenderer().setNumVerticalLabels(6);
        if (array.size() > 15) {
            graph.getGridLabelRenderer().setNumHorizontalLabels((array.size()/2)+2);
            graph.getGridLabelRenderer().setHorizontalLabelsAngle(90);
        } else {
            graph.getGridLabelRenderer().setNumHorizontalLabels(array.size());
        }
        series.setDrawDataPoints(true);
        series.setAnimated(true);
        graph.addSeries(series);
        series.setDrawBackground(true);
        series.setDataPointsRadius(20);
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(Stats.this, "Passi: " + dataPoint.getY(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setMonthStep() {
        new StepCounter(this).stepsThisMonth();
        String s = "monthSteps";
        int total = 0;

        ArrayList<String> array = retrieveArray(s);
        DataPoint[] dataPoints = new DataPoint[array.size()];

        for (int i = 0; i < array.size(); i++) {
            total += Integer.parseInt(array.get(i));
            dataPoints[i] = new DataPoint(i + 1, Integer.parseInt(array.get(i)));
        }

        Log.e("TOTAL", String.valueOf(total));

        graph = findViewById(R.id.graph);
        graph.removeAllSeries();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0);
        Date date = cal.getTime();
        SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");
        String date1 = format1.format(date);
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_YEAR, -29);
        Date date3 = cal1.getTime();
        String date4 = format1.format(date3);


        graph.setTitle("From: " + date4 + "     To: " + date1);
        graph.addSeries(series);
        graph.getGridLabelRenderer().setNumVerticalLabels(6);
        if (array.size() > 15) {
            graph.getGridLabelRenderer().setNumHorizontalLabels(16);
        } else {
            graph.getGridLabelRenderer().setNumHorizontalLabels(array.size());
        }
        series.setDrawDataPoints(true);
        series.setAnimated(true);
        graph.addSeries(series);
        series.setDrawBackground(true);
        series.setDataPointsRadius(20);
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(Stats.this, "Passi: " + dataPoint.getY(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setWeekStep() {
        new StepCounter(this).stepsThisWeek();
        String s = "weekSteps";

        ArrayList<String> array = retrieveArray(s);
        DataPoint[] dataPoints = new DataPoint[array.size()];

        for (int i = 0; i < array.size(); i++) {
            dataPoints[i] = new DataPoint(i + 1, Integer.parseInt(array.get(i)));
        }

        graph = findViewById(R.id.graph);
        graph.removeAllSeries();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0);
        Date date = cal.getTime();
        SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");
        String date1 = format1.format(date);
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_YEAR, -6);
        Date date3 = cal1.getTime();
        String date4 = format1.format(date3);


        graph.setTitle("From: " + date4 + "     To: " + date1);
        series.setDrawDataPoints(true);
        series.setAnimated(true);
        graph.addSeries(series);
        graph.getGridLabelRenderer().setNumVerticalLabels(8);
        graph.getGridLabelRenderer().setNumHorizontalLabels(array.size());
        series.setDrawBackground(true);
        series.setDataPointsRadius(20);
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(Stats.this, "Passi: " + dataPoint.getY(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public ArrayList<String> retrieveArray(String s) {
        SharedPreferences tmpArray = getSharedPreferences(s, MODE_PRIVATE);
        ArrayList<String> array = new ArrayList<>();
        int i = 0;
        while (!tmpArray.getString("item" + i, "0").equalsIgnoreCase("0")) {
            array.add(tmpArray.getString("item" + i, "0"));
            i++;
        }
        tmpArray.edit().clear().apply();
        return array;
    }

    public void setVisible() {
        startDate.setVisibility(View.VISIBLE);
        endDate.setVisibility(View.VISIBLE);
        editStartDate.setVisibility(View.VISIBLE);
        editEndDate.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);

    }

    public void setInvisible() {
        startDate.setVisibility(View.INVISIBLE);
        endDate.setVisibility(View.INVISIBLE);
        editStartDate.setVisibility(View.INVISIBLE);
        editEndDate.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);
    }

    public Date get_date(String date) {
        Calendar dob = Calendar.getInstance();

        int day = Integer.parseInt(date.substring(0, 2));
        int month = Integer.parseInt(date.substring(3, 5));
        int year = Integer.parseInt(date.substring(6, 10));

        dob.set(year, month - 1, day);

        return dob.getTime();
    }

    public boolean check() {
        Date startDate = get_date(editStartDate.getText().toString());
        Date endDate = get_date(editEndDate.getText().toString());

        Date now = Calendar.getInstance().getTime();

        return !((startDate.getTime() > endDate.getTime()) || (startDate.getTime() > now.getTime())
                || (endDate.getTime() > now.getTime()));
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String day = String.format("%02d", dayOfMonth);
        String month = String.format("%02d", (monthOfYear + 1));
        String date = day + "/" + month + "/" + year;
        if (whatEdit == 1) {
            editStartDate.setText(date);
        }
        if (whatEdit == 2) {
            editEndDate.setText(date);
        }
    }
}
