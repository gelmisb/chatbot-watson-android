package com.example.vmac.WatBot;

import android.annotation.SuppressLint;
import android.app.Application;
import java.io.Serializable;

@SuppressLint("Registered")
public class UserInfo extends Application implements Serializable{

    private static String username2 = "";
    private static String[]  userFeed2;

    public UserInfo(){
    }

    public UserInfo (String username, String[] userFeed){
        this.username2 = username;
        this.userFeed2 = userFeed;
    }

    public static  String getUsername()  { return username2;    }

    public static void setUsername(String username){ username2 = username; }


    public static String[] getUserfeed()  { return userFeed2;    }

    public static void setUserFeed(String[] userFeed){ userFeed2 = userFeed; }

}
