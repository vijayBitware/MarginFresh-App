package com.marginfresh.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.marginfresh.R;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppUtils {

    public static int isOfferAvailable =0;
    static int []imageArray={R.drawable.loader_1,R.drawable.loader_2,R.drawable.loader_3,
            R.drawable.loader_4,R.drawable.loader_5,R.drawable.loader_6,
            R.drawable.loader_7,R.drawable.loader_8,R.drawable.loader_9,
            R.drawable.loader_10,R.drawable.loader_11,
            R.drawable.loader_1,R.drawable.loader_2,R.drawable.loader_3,
            R.drawable.loader_4,R.drawable.loader_5,R.drawable.loader_6,
            R.drawable.loader_7,R.drawable.loader_8,R.drawable.loader_9,
            R.drawable.loader_10,R.drawable.loader_11};
    static Animation bounce;

    public static String getFormattedPrice(double num){
        double num1 = (int)Math.round(num * 100)/(double)100;
        String formattedNumber = new DecimalFormat("00.00").format(num1);
        return formattedNumber;
    }


    public static String getFormattedDate(String time) {
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "MMM dd h:mm a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String toUppercase(String s){
        String upperCase = s.substring(0,1).toUpperCase() + s.substring(1);
        return upperCase;
    }

    public static Dialog customLoader(Context context){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_loader);
        final ImageView img = (ImageView) dialog.findViewById(R.id.iv_loaderImage);
        final TextView tv_title = (TextView) dialog.findViewById(R.id.tv_title);
        bounce = AnimationUtils.loadAnimation(context, R.anim.loader_animation);

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int i=0;
            public void run() {
                img.setImageResource(imageArray[i]);
                img.setAnimation(bounce);
                i++;
                if(i>imageArray.length-1)
                {
                    i=0;
                }
                handler.postDelayed(this, 2000);  //for interval...
            }
        };
            handler.postDelayed(runnable,000); //for initial delay..
       // dialog.show();
        return dialog;
    }

    public static boolean IsDatabaseExist(Context context, String dbName) {

        File dbFile = context.getDatabasePath(dbName);
        Log.e("PATH:",""+dbFile.exists());
        return dbFile.exists();
    }
}
