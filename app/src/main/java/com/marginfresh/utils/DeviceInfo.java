package com.marginfresh.utils;

import android.provider.Settings;

import com.marginfresh.app.MyApplication;


/**
 * Created by Techteam on 10/28/2015.
 */
public class DeviceInfo {

    public static String getDeviceId() {
        return  Settings.Secure.getString(MyApplication.getInstance().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
}
