package com.example.barto.stepcounterbart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by belia on 05/12/2017.
 */

public class MyArrayAdapter extends ArrayAdapter<String>{
    private final Context context;
    private String[] values;
    private String[] pass_values;
    private int mypos=0;
    TextView textView, values1, value_position;

    public MyArrayAdapter(Context context, String[] values, String[] pass_values, int mypos) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        this.pass_values = pass_values;
        this.mypos = mypos;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_layout, parent, false);

        textView = rowView.findViewById(R.id.label);
        textView.setText(values[position]);
        values1 = rowView.findViewById(R.id.valuesId);
        values1.setText(pass_values[position]);
        value_position = rowView.findViewById(R.id.position);
        String tmp = String.valueOf(position + 1);
        value_position.setText(tmp);
        ImageView img = rowView.findViewById(R.id.medal);

        if(position==0){
            setPosition(Color.parseColor("#FFE083"), 24);
            textView.setShadowLayer(6,5,5, Color.parseColor("#FFB958"));
            values1.setShadowLayer(6,5,5, Color.parseColor("#FFB958"));
            img.setImageResource(R.drawable.gold);
            img.getLayoutParams().height = 200;
            img.getLayoutParams().width = 200;
            img.requestLayout();
        }else{
            if(position==1){
                setPosition(Color.parseColor("#C6C9E8"), 21);
                textView.setShadowLayer(6,5,5, Color.parseColor("#A0A7DD"));
                values1.setShadowLayer(6,5,5, Color.parseColor("#A0A7DD"));
                img.setImageResource(R.drawable.silver);
                img.getLayoutParams().height = 160;
                img.getLayoutParams().width = 160;
                img.requestLayout();
            }else {
                if (position == 2) {
                    setPosition(Color.parseColor("#CEB1A1"), 21);
                    textView.setShadowLayer(6,5,5, Color.parseColor("#B49080"));
                    values1.setShadowLayer(6,5,5, Color.parseColor("#B49080"));
                    img.setImageResource(R.drawable.bronze);
                    img.getLayoutParams().height = 130;
                    img.getLayoutParams().width = 130;
                    img.requestLayout();
                }
            }
        }

        if(position == mypos && (position != 0 && position != 1 && position != 2 )){
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextColor(Color.RED);
            values1.setTypeface(null, Typeface.BOLD);
            values1.setTextColor(Color.RED);
            value_position.setTypeface(null, Typeface.BOLD);
            value_position.setTextColor(Color.RED);
        }

        return rowView;
    }

    public void setPosition(int color, int size){
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextSize(size);
        textView.setTextColor(color);

        values1.setTypeface(null, Typeface.BOLD);
        values1.setTextColor(color);
        values1.setTextSize(size);

        value_position.setText("");
    }
}
