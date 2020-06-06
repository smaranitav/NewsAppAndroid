package com.example.newsapp;

import android.content.Context;

public class Item {
    private String image, title, times, section, articleId, article_time ;
    private String cityName, stateName, temp, descr, weatherImage;


    //constructor used for article cards/ news cards
    public Item(String image, String title,String times,String section, String articleId, String article_time){
        this.image=image;
        this.title=title;
        this.times=times;
        this.section=section;
        this.articleId=articleId;
        this.article_time=article_time;
    }

    //constructor used for location card
    public Item(String cityName, String stateName, String temp, String descr, String weatherImage){
        this.cityName=cityName;
        this.stateName=stateName;
        this.temp=temp;
        this.descr=descr;
        this.weatherImage=weatherImage;

    }
    public String getImage(){
        return image;
    }

    public void setImage(String image){
        this.image=image;

    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title=title;

    }
    public String getTimes(){
        return times;
    }

    public void setTimes(String times){
        this.times=times;

    }
    public String getSection(){
        return section;
    }

    public void setSection(String section){
        this.section=section;

    }
    public String getArticleId(){
        return articleId;
    }

    public void setArticleId(String articleId){
        this.articleId=articleId;

    }
    public String getCity(){
        return cityName;
    }
    public void setCity(String cityName){
        this.cityName=cityName;
    }
    public String getState(){
        return stateName;
    }
    public void setState(String stateName){
        this.stateName=stateName;
    }

    public String getTemp(){
        return temp;
    }
    public void setTemp(String temp){
        this.temp=temp;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }
    public String getWeatherImage(){
        return weatherImage;
    }
    public void setWeatherImage(String weatherImage){
        this.weatherImage=weatherImage;
    }
    public String getArticleTime(){
        return article_time;
    }
    public void setArticleTime(String article_time){
        this.article_time=article_time;
    }


}
