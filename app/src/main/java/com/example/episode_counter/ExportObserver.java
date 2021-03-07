package com.example.episode_counter;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;

public class ExportObserver implements Observer<Object> {

    private OutputStream out;
    private OutputStreamWriter output;
    private Context mContext;

    public ExportObserver(Context context, Uri uri) {
        mContext = context;
        try {
             out = context.getContentResolver().openOutputStream(uri);
             output = new OutputStreamWriter(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onSubscribe(Disposable d) {
        if (output != null) {
            try {
                output.write("Title, Rating, Season, Episode, Date, Image\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNext(Object value) {
        if (output != null) {
            Series s = (Series) value;
            try {
                String imageStr = BitmapConversions.getImageAsString(mContext, s.getTitle());
                String str = s.getTitle() + ","
                        + s.getRating() + ","
                        + s.getSeason() + ","
                        + s.getEpisode() + ","
                        + s.getDate() + ","
                        + imageStr + "\n";
                output.write(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
