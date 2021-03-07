package com.example.episode_counter;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.bumptech.glide.signature.ObjectKey;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/*
SeriesRepository is responsible for loading and saving data regarding the series. It is also
responsible for handling the threading to ensure the MainThread/UI-thread remains responsive.
 */
public class SeriesRepository {

    private SeriesDAO mDao;
    private LiveData<List<Series>> mSeries;
    private MutableLiveData<String> sortOrder;
    private final Context mContext;
    private final SharedPreferences mSharedPreferences;

    private final HashMap<String, Long> mImageDateChanged;

    public SeriesRepository(Application application) {
        SeriesDatabase db = SeriesDatabase.getInstance(application);
        mContext = application.getApplicationContext();
        mSharedPreferences = mContext.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        mImageDateChanged = new HashMap<>();

        mDao = db.seriesDAO();
        sortOrder = new MutableLiveData<>();
        sortOrder.setValue(mSharedPreferences.getString("sort_order", "title_ASC"));
        mSeries = Transformations.switchMap(sortOrder, input -> {
            // updating the saved sorting order
            mSharedPreferences.edit().putString("sort_order", input).apply();

            // updating mSeries to the new sorting order
            if (input.equals("title_ASC"))          return mDao.getAllTitleASC();
            else if (input.equals("title_DESC"))    return mDao.getAllTitleDESC();
            else if (input.equals("rating_high"))   return mDao.getAllRatingHigh();
            else if (input.equals("rating_low"))    return mDao.getAllRatingLow();
            else if (input.equals("date_new"))      return mDao.getAllDateNew();
            else if (input.equals("date_old"))      return mDao.getAllDateOld();
            else                                    return mDao.getAllTitleASC();
        });

    }

    /*
    Exporting the series to a file specified by the Uri.
     */
    public void exportData(Uri uri) {
        
        Observable<Object> observable = Observable.create(emitter -> {
            if (mSeries.getValue() != null) {
                for (Series s : mSeries.getValue()) {
                    emitter.onNext(s);
                }
            }
            emitter.onComplete();
            
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.computation());
        observable.subscribe(new ExportObserver(mContext, uri));
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Object>() {
             @Override
             public void onSubscribe(Disposable d) { }
             @Override
             public void onNext(Object value) { }
             @Override
             public void onError(Throwable e) { }
             @Override
             public void onComplete() {
                 Toast.makeText(mContext, "Export complete.", Toast.LENGTH_SHORT).show();
             }
         });
    }

    /*
    Importing previously exported series.
     */
    public void importData(Uri uri) {
        // Observable that sends the uri

        // Observable for emitting each line in the file
        Observable<Object> observable = Observable.create(emitter -> {
            try (InputStream in = mContext.getContentResolver().openInputStream(uri);
                 BufferedReader input = new BufferedReader(new InputStreamReader(in))) {

                // Keep sending the lines in the file
                String currentLine;
                while ((currentLine = input.readLine()) != null) {
                    emitter.onNext(currentLine);
                }
                emitter.onComplete();

            } catch (IOException e) {
                e.printStackTrace();
            }
        // skip first line, it contains just information about how the data is stored
        // Observer for importing each series
        }).skip(1).subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
        observable.subscribe(s -> {
            String[] values = s.toString().split(",");
            if (values.length == 6) {
                Series importedSeries = new Series(values[0], Float.parseFloat(values[1]));
                importedSeries.setSeason(Integer.parseInt(values[2]));
                importedSeries.setEpisode(Integer.parseInt(values[3]));
                importedSeries.setDate(values[4]);
                mDao.insert(importedSeries)
                        .subscribeOn(Schedulers.io())
                        .subscribe();

                Bitmap image = BitmapConversions.convertStringToBitMap(values[5]);
                if (image != null) {
                    saveImage(image, values[0]);
                }
            }
        });
        // Observer for displaying a message when the import is complete
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) { }
            @Override
            public void onNext(Object value) { }
            @Override
            public void onError(Throwable e) { }
            @Override
            public void onComplete() {
                Toast.makeText(mContext, "Import complete.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Boolean existsSeriesTitle(String title) {
        if (mSeries.getValue() == null) {
            return Boolean.FALSE;
        } else {
            for (Series s : mSeries.getValue()) {
                if (s.getTitle().equals(title)) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }
    }

    private Boolean isImageSaved(String name) {
        File file = new File(mContext.getFilesDir(), name);
        return file.exists();
    }

    public LiveData<List<Series>> getSeries() {return mSeries;}

    public String getSortOrder() {
        return mSharedPreferences.getString("sort_order", "title_ASC");
    }

    public void changeSortOrder(String order) { sortOrder.setValue(order); }

    /*
    Method used when creating new series
     */
    public void insert(Series series, Bitmap image) {

        if (image != null) {
            saveImage(image, series.getTitle());
        }

        // Insert series in database, it will ignore if series already exists
        mDao.insert(series)
                .subscribeOn(Schedulers.io())
                .subscribe();

        // Check if series exist and display Toast according to result
        Single<Boolean> observable = Single.just(existsSeriesTitle(series.getTitle()));
        observable.subscribeOn(Schedulers.computation());
        observable.subscribe(new SingleObserver<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) { }

            @Override
            public void onSuccess(Boolean value) {

                if (value == Boolean.TRUE) {
                    Toast.makeText(mContext, "Series already exist.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "Series created.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) { }
        });
    }

    /*
    Simple update: same title and no image change
     */
    public void update(Series series) {
        mDao.update(series)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void update(Series newSeries, Series oldSeries, boolean deleteOldImage) {
        if (deleteOldImage) {
            deleteImage(oldSeries.getTitle());
        }
        update(newSeries, oldSeries, null);
    }

    /*
    Method used when existing existing series
     */
    public void update(Series newSeries, Series oldSeries, Bitmap newImage) {
        if (newSeries.getTitle().equals(oldSeries.getTitle())) {

            if (newImage != null) {
                // Overwrites the old image with the same name
                saveImage(newImage, newSeries.getTitle());
            }

            mDao.update(newSeries)
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        } else {
            // Need to delete and insert (since title is the primary key which identifies the series)

            if (newImage != null) {
                // Delete image with old name
                deleteImage(oldSeries.getTitle());
                // Save image with new name
                saveImage(newImage, newSeries.getTitle());


            } else {
                // Didn't change the image
                // Rename old image to fit new title
                renameImage(newSeries.getTitle(), oldSeries.getTitle());

            }

            mDao.delete(oldSeries)
                    .subscribeOn(Schedulers.io())
                    .subscribe();

            mDao.insert(newSeries)
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    public void showViewIfImageExist(View view, String imageName) {
        Single<Boolean> observable = Single.just(isImageSaved(imageName));
        observable.subscribeOn(Schedulers.computation());
        observable.subscribe(new SingleObserver<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) { }

            @Override
            public void onSuccess(Boolean value) {

                if (value == Boolean.TRUE) {
                    /*Toast.makeText(mContext, "Image exist.",
                            Toast.LENGTH_SHORT).show();*/
                    view.setVisibility(View.VISIBLE);
                } else {
                    /*Toast.makeText(mContext, "No such image.",
                            Toast.LENGTH_SHORT).show();*/
                    view.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Throwable e) { }
        });
    }

    public void loadImage(String seriesTitle, ImageView view) {
        Completable completable = Completable.fromAction(() -> {
            // Do this off UI thread just in case there are many item in the map and it takes
            // a while to check 'containsKey'
            if (!mImageDateChanged.containsKey(seriesTitle)) {
                mImageDateChanged.put(seriesTitle, DateCreator.getCurrentTime());
            }
            Glide.with(mContext)
                    .load(new File(mContext.getFilesDir(), seriesTitle))
                    .signature(new MediaStoreSignature("", mImageDateChanged.get(seriesTitle), 0))
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_24)
                    .into(view);
            });
        completable.subscribeOn(Schedulers.io());
        completable.subscribe();
    }

    public void saveImage(final Bitmap image, final String seriesTitle) {
        Completable completable = Completable.fromAction(() -> {
            try (FileOutputStream out = mContext.openFileOutput(seriesTitle, Context.MODE_PRIVATE)) {

                image.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored JPEG
                mImageDateChanged.put(seriesTitle, DateCreator.getCurrentTime());
            } catch (IOException e) {
                Toast.makeText(mContext, "Failed to save image", Toast.LENGTH_SHORT).show();
            }

        });
        completable.subscribeOn(Schedulers.io());
        completable.subscribe();
    }


    public void renameImage(final String newTitle, final String oldTitle) {
        Completable completable_rename = Completable.fromAction(() -> {
            File oldImage = new File(mContext.getFilesDir(), oldTitle);
            boolean success = oldImage.renameTo(new File(mContext.getFilesDir(), newTitle));
            mImageDateChanged.remove(oldTitle);
        });
        completable_rename.subscribeOn(Schedulers.io());
        completable_rename.subscribe();
    }

    public void delete(Series series) {
        mDao.delete(series)
            .subscribeOn(Schedulers.io())
            .subscribe();

        deleteImage(series.getTitle());
    }

    public void delete(List<Series> series) {
        Observable<Series> observable = Observable.fromIterable(series);
        observable.subscribeOn(Schedulers.io());
        observable.subscribe(new Observer<Series>() {
            @Override
            public void onSubscribe(Disposable d) { }

            @Override
            public void onNext(Series value) { delete(value); }

            @Override
            public void onError(Throwable e) { }

            @Override
            public void onComplete() { }
        });
    }

    public void deleteImage(final String seriesTitle) {
        Completable completable_delete = Completable.fromAction(() -> {
            // Delete old image
            File oldImage = new File(mContext.getFilesDir(), seriesTitle);
            boolean success = oldImage.delete();
            mImageDateChanged.remove(seriesTitle);
        });
        completable_delete.subscribeOn(Schedulers.io());
        completable_delete.subscribe();
    }

}
