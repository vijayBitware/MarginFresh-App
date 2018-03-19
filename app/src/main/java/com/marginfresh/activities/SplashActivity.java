package com.marginfresh.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.marginfresh.R;
import com.marginfresh.utils.DeviceInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 1500;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getKeyHAsh();
        System.out.println("Firebase Token > " + FirebaseInstanceId.getInstance().getToken());

        sharedPreferences= getSharedPreferences("MyPref",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String status = sharedPreferences.getString("status","");
                switch (status){
                    case "isLogin":
                        editor.putString("NavigationPosition","0");
                        editor.putString("comeFrom","skipStore");
                        editor.commit();
                        Intent intent = new Intent(SplashActivity.this,DrawerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        break;
                    case "howItWorks":
                        Intent intent1 = new Intent(SplashActivity.this,HowItWorksActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                        finish();
                        break;
                    case "isSelectStore":
                        Intent intent2 = new Intent(SplashActivity.this,SelectStoreActivity.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        finish();
                        break;
                    case "afterStore":
                        editor.putString("NavigationPosition","0");
                        editor.putString("comeFrom","skipStore");
                        editor.commit();
                        Intent intent3 = new Intent(SplashActivity.this,DrawerActivity.class);
                        intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent3);
                        finish();
                        break;
                    default:
                        Intent intent4 = new Intent(SplashActivity.this,LoginActivity.class);
                        intent4.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent4);
                        finish();
                }
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    public void getKeyHAsh(){
        try {
            PackageInfo info =this.getPackageManager().getPackageInfo(
                    "com.marginfresh", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash", "KeyHash:" + Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
}
