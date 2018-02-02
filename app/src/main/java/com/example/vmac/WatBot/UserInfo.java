package com.example.vmac.WatBot;

import android.annotation.SuppressLint;
import android.app.Application;
import java.io.Serializable;

@SuppressLint("Registered")
public class UserInfo extends Application implements Serializable{

    private static String username2 = "";

    public  UserInfo(){

    }

    public UserInfo (String username){
        this.username2 = username;
    }

    public static  String getUsername()  { return username2;    }

    public static  void setUsername(String username){ username2 = username; }

}
