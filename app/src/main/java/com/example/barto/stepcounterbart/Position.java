package com.example.barto.stepcounterbart;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by belia on 21/12/2017.
 */

public class Position {
    List<LatLng> list;

    public Position(){

    }

    public Position(List<LatLng> tmp){
        list = tmp;
    }

    public List<LatLng> getList() {
        return list;
    }

    public void setList(List<LatLng> list) {
        this.list = list;
    }
}
