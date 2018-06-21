package com.example.vmac.WatBot;

import java.io.Serializable;
import java.security.PublicKey;

/**
 * Created by gelmi on 15/02/2018.
 */

public class UserMessages implements Serializable {


    private String message;

    public UserMessages (String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString(){

        return message;
    }
}
