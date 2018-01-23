package com.example.vmac.WatBot;
import java.util.ArrayList;


public class NewsModel {

    ArrayList<Article> newsList;

    public NewsModel(){

        newsList = new ArrayList<>();

    }

    public  Article getArticleAt(int index){

        return newsList.get(index);
    }

    public  void addArticle(Article article){

        newsList.add(article);

    }

    public boolean isEmpty(){

        return newsList.isEmpty();

    }

    public ArrayList<Article> getNewsList(){

        return newsList;
    }
}
