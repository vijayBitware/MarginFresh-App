package com.marginfresh.domain;

import com.marginfresh.Model.ModelAllTypeArray;
import com.marginfresh.Model.ModelProductList;
import com.marginfresh.Model.ModelSubTypeArray;

import java.util.ArrayList;

/**
 * Created by bitwarepc on 09-Jun-17.
 */

public class Config {
//    public static String BASE_URL = "http://103.224.243.154/magento/marginfresh/soapapi/core/";
//    public static String BASE_URL = "https://marginfresh.com/staging/soapapi/core/";
    public static String BASE_URL = "https://www.marginfresh.com/soapapi/core/";
    public static String BASE_URL_SHOPCART = "https://www.marginfresh.com/soapapi/";
    public static String BASE_URL_WALLET = "https://www.marginfresh.com/soapapi/wallet/";

    public static String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)$";
    public static String google_map_key = "AIzaSyC-rAlEKUzlj6X_ECTJNtNAeMKuQWF6dlc";
    public static String isFiterApply = "no";
    public static ArrayList<ModelProductList> arrayListToFilter = new ArrayList<>();
    public static ArrayList<ModelAllTypeArray> arrAllTypeArrayList = new ArrayList<>();
    public static String status = "";
    public static String presentfragment = "";
    public static ArrayList<ModelSubTypeArray> arrSubCatArrayList= new ArrayList<>();
    public static int notificationCount = 0,isNeedToService=0;
    public static boolean isCheckGPS = false;
    public static String minPriceAfterFilter="",maxPriceAfterFilter="";

    public static  ArrayList<String> arrProductIds = new ArrayList<>() ;
    public static String cartCount = "";
    public static int cartCounter=0;
    public static Float productPrice = 0.0f;

}
