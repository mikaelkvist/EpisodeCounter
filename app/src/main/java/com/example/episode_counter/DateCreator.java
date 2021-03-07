package com.example.episode_counter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DateCreator {


    public static String  getCurrentDate() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmssZ", Locale.getDefault());
        String strDate = formatter.format(date);
        System.out.println(strDate);
        return strDate;
    }

    public static Long getCurrentTime() {
        return System.currentTimeMillis();

    }

}