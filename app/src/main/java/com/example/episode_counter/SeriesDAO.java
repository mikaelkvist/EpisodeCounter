package com.example.episode_counter;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;

@Dao
public interface SeriesDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insert(Series series);

    @Update
    Completable update(Series series);

    @Delete
    Completable delete(Series series);

    @Query("SELECT * FROM series ORDER BY title ASC")
    LiveData<List<Series>> getAllTitleASC();

    @Query("SELECT * FROM series ORDER BY title DESC")
    LiveData<List<Series>> getAllTitleDESC();

    @Query("SELECT * FROM series ORDER BY rating DESC")
    LiveData<List<Series>> getAllRatingHigh();

    @Query("SELECT * FROM series ORDER BY rating ASC")
    LiveData<List<Series>> getAllRatingLow();

    @Query("SELECT * FROM series ORDER BY date_changed DESC")
    LiveData<List<Series>> getAllDateNew();

    @Query("SELECT * FROM series ORDER BY date_changed ASC")
    LiveData<List<Series>> getAllDateOld();




}
