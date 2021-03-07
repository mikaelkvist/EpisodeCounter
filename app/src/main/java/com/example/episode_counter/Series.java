package com.example.episode_counter;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "series")
public class Series {


    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "title")
    private String mTitle;

    @ColumnInfo(name = "episode")
    private int mEpisode;

    @ColumnInfo(name = "season")
    private int mSeason;

    @ColumnInfo(name = "rating")
    private float mRating;

    @ColumnInfo(name = "date_changed")
    private String mDate;

    public Series(String title, float rating) {
        mTitle = title;
        mEpisode = 0;
        mSeason = 0;
        mRating = rating;
        mDate = DateCreator.getCurrentDate();
    }

    public String getTitle() {return mTitle;}
    public int getEpisode() {return mEpisode;}
    public int getSeason() {return mSeason;}
    public float getRating() {return mRating;}
    public String getDate() {return mDate;}

    public void setEpisode(int episode) {mEpisode = episode; setDate(DateCreator.getCurrentDate());}
    public void setSeason(int season) {mSeason = season; setDate(DateCreator.getCurrentDate());}
    public void setDate(String date) {mDate = date;}



}
