package com.example.vmac.WatBot;


import java.util.ArrayList;

public class UserModel {


    public ArrayList<UserMessages> messageList;


    public UserModel(){

        messageList = new ArrayList<>();
    }

    public  UserMessages getUserMessage(int index){

        return messageList.get(index);
    }


    public void addMessages(UserMessages userMessages){
        messageList.add(userMessages);
    }


    public ArrayList<UserMessages> getMessageList(){

        return messageList;
    }

}
