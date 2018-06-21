package com.example.vmac.WatBot;

import com.google.android.gms.awareness.state.Weather;

import java.util.ArrayList;

/**
 * Created by gelmi on 08/03/2018.
 */

public class WeatherModel {


    ArrayList<Weatherw> weatherList;

    public WeatherModel(){

        weatherList = new ArrayList<>();

    }

    public Weatherw getWeatherAt(int index){

        return weatherList.get(index);
    }

    public void addWeather(Weatherw weather){

        weatherList.add(weather);

    }

    public boolean isEmpty(){

        return weatherList.isEmpty();

    }

    public ArrayList<Weatherw> getWeatherList(){

        return weatherList;
    }
}


