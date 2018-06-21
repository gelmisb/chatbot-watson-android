package com.example.vmac.WatBot;


import java.io.Serializable;

public class Article implements Serializable{

    private String title;
    private String content;
    private String author;
    private String URL;
    private String imageURL;
    private String date;

    public Article(String title, String content, String author, String URL, String imageURL, String date){

        this.title = title;
        this.content = content;
        this.author = author;
        this.URL = URL;
        this.imageURL = imageURL;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public  String toString(){

        return title;

    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
