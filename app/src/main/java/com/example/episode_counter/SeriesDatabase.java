package com.example.episode_counter;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Series.class}, version = 1, exportSchema = false)
public abstract class SeriesDatabase extends RoomDatabase {

    private static final String DB_NAME = "series_database";
    private static SeriesDatabase instance;     // we only want to have one database for the app


    // synchronized is used to ensure only one instance of the database is created
    public static synchronized SeriesDatabase getInstance(Context context) {

        if (instance == null) {
            // Create new database
            instance = Room.databaseBuilder(context.getApplicationContext(),
                                            SeriesDatabase.class,
                                            DB_NAME)
                                            .build();
        }
        return instance;

    }


    public abstract SeriesDAO seriesDAO();


}
