package com.example.vmac.WatBot;

import java.io.Serializable;

/**
 * Created by gelmi on 08/03/2018.
 */

public class Weatherw implements Serializable {

    private String dow;
    private double temp;
    private double feelsLike;
    private int icon;
    private String phrase;
    private int pop;
    private String popType;
    private String clouds;
    private String rh;
    private String wspd;
    private String time;



    public Weatherw(String dow, double temp, double feelsLike, int icon, String phrase, int pop, String popType, String clouds, String rh, String wspd, String time){

        this.dow = dow;
        this.temp = temp;
        this.feelsLike = feelsLike;
        this.icon = icon;
        this.phrase = phrase;
        this.pop = pop;
        this.popType = popType;
        this.clouds = clouds;
        this.rh = wspd;
        this.time = time;
    }

    @Override
    public  String toString(){

        return dow;

    }


    public String getDow() {
        return dow;
    }

    public void setDow(String dow) {
        this.dow = dow;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public int getPop() {
        return pop;
    }

    public void setPop(int pop) {
        this.pop = pop;
    }

    public String getPopType() {
        return popType;
    }

    public void setPopType(String popType) {
        this.popType = popType;
    }

    public String getClouds() {
        return clouds;
    }

    public void setClouds(String clouds) {
        this.clouds = clouds;
    }

    public String getRh() {
        return rh;
    }

    public void setRh(String rh) {
        this.rh = rh;
    }

    public String getWspd() {
        return wspd;
    }

    public void setWspd(String wspd) {
        this.wspd = wspd;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
