package com.marginfresh.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.marginfresh.Model.ModelAllTypeArray;
import com.marginfresh.Model.ModelSubTypeArray;
import com.marginfresh.Model.ModelTopOffers;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityCart;
import com.marginfresh.activities.ActivityCategoryList;
import com.marginfresh.activities.ActivityMyAddress;
import com.marginfresh.activities.ActivitySearch;
import com.marginfresh.activities.ActivityTopOffers;
import com.marginfresh.activities.DrawerActivity;
import com.marginfresh.activities.GPSTracker;
import com.marginfresh.activities.SelectStoreActivity;
import com.marginfresh.adapter.AdapterStoreProductHome;
import com.marginfresh.adapter.AdapterTopOffers;
import com.marginfresh.db_utils.DatabaseHandler;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bitware on 1/6/17.
 */

public class FragmentHome extends Fragment {

    View view;
    RecyclerView rv_topOffers,rv_storeProduct;
    AdapterTopOffers adapterTopOffers;
    AdapterStoreProductHome adapterStoreProductHome;
    TextView tv_viewMore,tv_storeNameBannar,tv_storeName,tv_toolbarTitle,txt_noRecord;
    EditText edt_search;
    private String response_msg="",user_id="",store_id="",store_name="",storeImageUrl="",storeBannerImage="",all_type_image_url="",cartItemCount="";
    double lattitude,longitude;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    boolean isInternetPresent;
    public static ArrayList<ModelTopOffers> arrTopOfferList;
    ArrayList<ModelSubTypeArray> arrSubTypeArrayList;
    ImageView iv_storeBannerImage,iv_storeLogo,iv_search;
    LinearLayout ll_topoffersTitleBar,ll_topBar,ll_loacationBar;
    SwipeRefreshLayout swipeRefreshLayout;
    GPSTracker gpsTracker;
    boolean isCheckGPS = false;
    ProgressDialog p ;
    boolean locationUpdated = false,isLocationClicked=true;
    SwipeRefreshLayout sw_home;
    LocationManager locationManager;
    Dialog dialog;
    Button btn_cart;
    DatabaseHandler db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_new,container,false);
        init();
        locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);

        if (gpsTracker.canGetLocation()){
            lattitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            System.out.println("Current Location >>> Lattitude-"+lattitude+"Longitude-"+longitude);
            editor.putString("lattitude", String.valueOf(lattitude));
            editor.putString("longitude", String.valueOf(longitude));
            editor.commit();
        }else {
            showLocationDialog();
        }
        if (!(lattitude ==0.0) && !(longitude ==0.0)){
            serviceCall();
        }

        tv_viewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ActivityTopOffers.class);
                startActivity(intent);
            }
        });

        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edt_search.getWindowToken(), 0);
                String searchString = edt_search.getText().toString();
                if (!searchString.isEmpty()){
                    edt_search.setText("");
                    editor.putString("searchString",searchString);
                    editor.putString("searchFrom","home");
                    editor.commit();
                    startActivity(new Intent(getContext(), ActivitySearch.class));
                }else {
                    Toast.makeText(getContext(),getResources().getString(R.string.empty_string),Toast.LENGTH_SHORT).show();
                }
            }
        });

        edt_search.setOnEditorActionListener(new TextView.OnEditorActionListener(){

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edt_search.getWindowToken(), 0);
                    String searchString = edt_search.getText().toString();
                    if (!searchString.isEmpty()){
                        editor.putString("searchString",searchString);
                        editor.putString("searchFrom","home");
                        editor.commit();
                        startActivity(new Intent(getContext(), ActivitySearch.class));
                    }else {
                        Toast.makeText(getContext(),getResources().getString(R.string.empty_string),Toast.LENGTH_SHORT).show();
                    }

                }

                return false;
            }
        });

        sw_home.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sw_home.setRefreshing(false);
                if (gpsTracker.canGetLocation()){
                    lattitude = gpsTracker.getLatitude();
                    longitude = gpsTracker.getLongitude();
                    System.out.println("Current Location >>> Lattitude-"+lattitude+"Longitude-"+longitude);
                    editor.putString("lattitude", String.valueOf(lattitude));
                    editor.putString("longitude", String.valueOf(longitude));
                    editor.commit();
                }else {
                    showLocationDialog();
                }
                serviceCall();
            }
        });

        return view;
    }

    private void init() {
        cd = new ConnectionDetector(getContext());
        isInternetPresent =cd.isConnectingToInternet();
        sharedPreferences = getContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor= sharedPreferences.edit();
        gpsTracker = new GPSTracker(getContext());
        rv_topOffers = (RecyclerView) view.findViewById(R.id.rv_topOffers);
        LinearLayoutManager horizontalLayoutManagerCigars = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rv_topOffers.setLayoutManager(horizontalLayoutManagerCigars);
        rv_storeProduct = (RecyclerView) view.findViewById(R.id.rv_storeProduct);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(),3);
        rv_storeProduct.setLayoutManager(linearLayoutManager);
        tv_viewMore = (TextView) view.findViewById(R.id.tv_viewMore);
        edt_search = (EditText) view.findViewById(R.id.edt_search);
        iv_storeBannerImage = (ImageView) view.findViewById(R.id.iv_bannerImage);
        iv_storeLogo= (ImageView) view.findViewById(R.id.iv_storeLogo);
        tv_storeNameBannar = (TextView) view.findViewById(R.id.tv_storeNameBanner);
        tv_storeName = (TextView) view.findViewById(R.id.tv_storeName);
        ll_topoffersTitleBar= (LinearLayout) view.findViewById(R.id.ll_topOffersTitleBar);
        arrTopOfferList = new ArrayList<>();
        ll_topBar = (LinearLayout) view.findViewById(R.id.ll_topBar);
        iv_search = (ImageView) view.findViewById(R.id.iv_search);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        Config.arrAllTypeArrayList=new ArrayList<>();
        gpsTracker = new GPSTracker(getContext());
        tv_toolbarTitle= (TextView) getActivity().findViewById(R.id.tv_toolbarTitle);
        sw_home = (SwipeRefreshLayout) view.findViewById(R.id.sw_home);
        sw_home.setColorSchemeResources(R.color.colorPrimary);
        btn_cart = (Button) getActivity().findViewById(R.id.btn_cartCount);
        ll_loacationBar = (LinearLayout) getActivity().findViewById(R.id.ll_locationBar);
        //txt_noRecord = (TextView) view.findViewById(R.id.txt_noRecord);

        p= new ProgressDialog(getContext());
        p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        p.setMessage("Updating User location");

        db = new DatabaseHandler(getContext());
    }

    public void serviceCall(){
            if (sharedPreferences.getString("comeFrom", "").equals("skipStore")) {
                System.out.println(">> Store is Skipped");
                Log.e("TAG","In 1st Case");
                user_id = sharedPreferences.getString("user_id", "");
                lattitude = Double.parseDouble(sharedPreferences.getString("lattitude", ""));
                longitude = Double.parseDouble(sharedPreferences.getString("longitude", ""));
                    if (isInternetPresent) {
                        new HomeDataWhenSkipStore().execute("{\"user_id\":\"" + user_id + "\",\"latitude\":\"" + lattitude + "\",\"longitude\":\"" + longitude + "\"}");
                    } else {
                        Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
            } else if (sharedPreferences.getString("comeFrom", "").equals("selectingStore")) {
                System.out.println(">> Store is selected");
                Log.e("TAG","In 2st Case");
                store_id = sharedPreferences.getString("store_id", "");
                user_id = sharedPreferences.getString("user_id", "");
                if (isInternetPresent) {
                    new HomeDataWhenSelectStore().execute("{\"store_id\":\"" + store_id + "\",\"user_id\":\"" + user_id + "\"}");
                } else {
                    Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            } else if (sharedPreferences.getString("comeFrom", "").equals("fromGooglePlace")) {
                System.out.println(">> From Google Place");
                Log.e("TAG","In 3st Case");
                user_id = sharedPreferences.getString("user_id", "");
                lattitude = Double.parseDouble(sharedPreferences.getString("selectedAreaLat", ""));
                longitude = Double.parseDouble(sharedPreferences.getString("selectedAreaLng", ""));

                    if (isInternetPresent) {
                        new HomeDataWhenSkipStore().execute("{\"user_id\":\"" + user_id + "\",\"latitude\":\"" + lattitude + "\",\"longitude\":\"" + longitude + "\"}");
                    } else {
                        Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
            }else {
                Log.e("TAG","In 4th Case");
                user_id = sharedPreferences.getString("user_id", "");
                if (gpsTracker.canGetLocation()){
                    lattitude = Double.parseDouble(String.valueOf(gpsTracker.getLatitude()));
                    longitude = Double.parseDouble(String.valueOf(gpsTracker.getLongitude()));
                    System.out.println("Current Location >>> Lattitude-"+lattitude+"Longitude-"+longitude);
                    editor.putString("lattitude", String.valueOf(lattitude));
                    editor.putString("longitude", String.valueOf(longitude));
                    editor.commit();
                }
                if (isInternetPresent) {
                    new HomeDataWhenSkipStore().execute("{\"user_id\":\"" + user_id + "\",\"latitude\":\"" + lattitude + "\",\"longitude\":\"" + longitude + "\"}");
                } else {
                    Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
    }

    class HomeDataWhenSkipStore extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(getContext());
            dialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(180, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(180, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "homedata.php?"+"user_id="+user_id+"&latitude="+lattitude+"&longitude="+longitude)
                    .post(body)
                    .build();
            try {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(">>>Home data result :" + s);
            dialog.dismiss();
            if (s != null) {
//                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if (status.equals("1")) {
                        cartItemCount = jsonObject.getString("cartItemCount");
                        System.out.println("Cart Item Count home data when skip store > " +cartItemCount);

                      //  db.insertCartCount(cartItemCount);

                        store_id = jsonObject.getString("store_id");
                        store_name = jsonObject.getString("public_name");
                        storeImageUrl=jsonObject.getString("store_image_url");
                        storeBannerImage = jsonObject.getString("store_banner_url");

                        String grandtotal = jsonObject.getString("grandtotal");
                        editor.putString("cartTotal",grandtotal);
                       // editor.putString("cartItemCount",cartItemCount);
                        editor.putString("storeName",AppUtils.toUppercase(store_name));
                        editor.putString("storeLogoImage",storeImageUrl);
                        editor.putString("storeBanerImage",storeBannerImage);
                        editor.putString("store_id",store_id);
                        editor.commit();
                        setData();
                        ll_topBar.setVisibility(View.GONE);
                        rv_topOffers.setVisibility(View.GONE);
                        //Top Offer Array
                        JSONArray topOfferArray = jsonObject.getJSONArray("top_offer_array");
                        arrTopOfferList = new ArrayList<>();
                        for (int i=0;i<topOfferArray.length();i++){
                            JSONObject topOfferObj = topOfferArray.getJSONObject(i);
                            ModelTopOffers modelTopOffers = new ModelTopOffers();

                            modelTopOffers.setProduct_id(topOfferObj.getString("product_id"));
                            modelTopOffers.setProduct_name(topOfferObj.getString("product_name"));
                            modelTopOffers.setOffer(topOfferObj.getString("offer"));
                            modelTopOffers.setStore_image_url(topOfferObj.getString("store_image_url"));

                            arrTopOfferList.add(modelTopOffers);
                        }
                        if (arrTopOfferList.size()==0){
                            AppUtils.isOfferAvailable =1;
                            Log.e("In FragmentHome", String.valueOf(AppUtils.isOfferAvailable));
                           ll_topBar.setVisibility(View.GONE);
                            rv_topOffers.setVisibility(View.GONE);
                        }else {
                            AppUtils.isOfferAvailable =0;
                            Log.e("In FragmentHome",String.valueOf(AppUtils.isOfferAvailable));
                            rv_topOffers.setVisibility(View.VISIBLE);
                            ll_topBar.setVisibility(View.VISIBLE);
                            adapterTopOffers = new AdapterTopOffers(getContext(),arrTopOfferList);
                            rv_topOffers.setAdapter(adapterTopOffers);

                        }
                        //All type Array
                        JSONArray allTypeArray = jsonObject.getJSONArray("all_type_array");
                        Config.arrAllTypeArrayList = new ArrayList<>();
                        ModelAllTypeArray modelAllTypeArray1 = new ModelAllTypeArray();
                        modelAllTypeArray1.setType_name("All Items");
                        modelAllTypeArray1.setType_image_url(jsonObject.getString("all_type_image_url"));
                        Config.arrAllTypeArrayList.add(modelAllTypeArray1);

                        for (int i=0;i<allTypeArray.length();i++){
                            JSONObject allTypeObj = allTypeArray.getJSONObject(i);
                            ModelAllTypeArray modelAllTypeArray = new ModelAllTypeArray();
                            modelAllTypeArray.setType_id(allTypeObj.getString("type_id"));
                            modelAllTypeArray.setType_name(allTypeObj.getString("type_name"));
                            modelAllTypeArray.setType_image_url(allTypeObj.getString("type_image_url"));
                            JSONArray subTypeArray = allTypeObj.getJSONArray("sub_type_array");
                            arrSubTypeArrayList = new ArrayList<>();
                            for (int j=0;j<subTypeArray.length();j++){
                                JSONObject subTypeObj = subTypeArray.getJSONObject(j);
                                ModelSubTypeArray modelSubTypeArray = new ModelSubTypeArray();
                                modelSubTypeArray.setType_id(subTypeObj.getString("type_id"));
                                modelSubTypeArray.setType_name(subTypeObj.getString("type_name"));
                                modelSubTypeArray.setSub_type_image(subTypeObj.getString("sub_type_image"));

                                arrSubTypeArrayList.add(modelSubTypeArray);
                            }
                            modelAllTypeArray.setArrSubTypeArrayList(arrSubTypeArrayList);
                            Config.arrAllTypeArrayList.add(modelAllTypeArray);
                        }
                        adapterStoreProductHome = new AdapterStoreProductHome(getContext(),Config.arrAllTypeArrayList);
                        rv_storeProduct.setAdapter(adapterStoreProductHome);
                    } else {
                        dialog.dismiss();
                        showNoProductsDialog(response_msg);
                        tv_storeName.setText("No stores available at your current location.");
                        editor.putString("storeName","");
                        editor.commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    Toast.makeText(getContext(),getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                   /* editor.putString("storeName",store_name);
                    editor.commit();*/
                }
            } else {
                dialog.dismiss();
                Toast.makeText(getContext(),"Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setData() {
        System.out.println("Banner Image >>> " +storeBannerImage);
        Glide.with(getContext()).load(storeBannerImage).into(iv_storeBannerImage);
        tv_storeName.setText(AppUtils.toUppercase(store_name));
        tv_toolbarTitle.setText(AppUtils.toUppercase(store_name));
    }

    class HomeDataWhenSelectStore extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(getContext());
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(180, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(180, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "homedatagetusingstore.php?"+"store_id="+store_id + "&user_id="+user_id)
                    .post(body)
                    .build();
            try {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // clearEditTextData();
            System.out.println(">>>Home data when select storeresult :" + s);
            dialog.dismiss();
            if (s != null) {
//                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if (status.equals("1")) {
                      //  Toast.makeText(getContext(), response_msg, Toast.LENGTH_SHORT).show();
                        store_id = jsonObject.getString("store_id");
                        store_name = jsonObject.getString("public_name");
                        storeImageUrl=jsonObject.getString("store_image_url");
                        storeBannerImage = jsonObject.getString("store_banner_url");

                        cartItemCount = jsonObject.getString("cartItemCount");
                        System.out.println("Cart Item Count when select store > " +cartItemCount);
                        String grandtotal = jsonObject.getString("grandtotal");
                        editor.putString("cartTotal",grandtotal);
                        editor.putString("cartItemCount",cartItemCount);
                        editor.putString("storeName", AppUtils.toUppercase(store_name));
                        editor.putString("storeLogoImage",storeImageUrl);
                        editor.putString("storeBanerImage",storeBannerImage);
                        editor.putString("store_id",store_id);
                        editor.commit();
                        setData();
                        ll_topBar.setVisibility(View.GONE);
                        rv_topOffers.setVisibility(View.GONE);
                      //  txt_noRecord.setVisibility(View.GONE);
                        //Top Offer Array
                        JSONArray topOfferArray = jsonObject.getJSONArray("top_offer_array");
                        arrTopOfferList = new ArrayList<>();
                        for (int i=0;i<topOfferArray.length();i++){
                            JSONObject topOfferObj = topOfferArray.getJSONObject(i);
                            ModelTopOffers modelTopOffers = new ModelTopOffers();

                            modelTopOffers.setProduct_id(topOfferObj.getString("product_id"));
                            modelTopOffers.setProduct_name(topOfferObj.getString("product_name"));
                            modelTopOffers.setOffer(topOfferObj.getString("offer"));
                            modelTopOffers.setStore_image_url(topOfferObj.getString("store_image_url"));

                            arrTopOfferList.add(modelTopOffers);
                        }

                        if (arrTopOfferList.size()==0){
                            ll_topBar.setVisibility(View.GONE);
                            rv_topOffers.setVisibility(View.GONE);
                        }else {
                            ll_topBar.setVisibility(View.VISIBLE);
                            rv_topOffers.setVisibility(View.VISIBLE);
                            adapterTopOffers = new AdapterTopOffers(getContext(),arrTopOfferList);
                            rv_topOffers.setAdapter(adapterTopOffers);

                        }

                        adapterTopOffers = new AdapterTopOffers(getContext(),arrTopOfferList);
                        rv_topOffers.setAdapter(adapterTopOffers);

                        //All type Array
                        JSONArray allTypeArray = jsonObject.getJSONArray("all_type_array");
                        ModelAllTypeArray modelAllTypeArray1 = new ModelAllTypeArray();
                        modelAllTypeArray1.setType_name("All Items");
                        modelAllTypeArray1.setType_image_url(jsonObject.getString("all_type_image_url"));
                        Config.arrAllTypeArrayList = new ArrayList<>();
                        Config.arrAllTypeArrayList.add(modelAllTypeArray1);

                        for (int i=0;i<allTypeArray.length();i++){

                            JSONObject allTypeObj = allTypeArray.getJSONObject(i);
                            ModelAllTypeArray modelAllTypeArray = new ModelAllTypeArray();
                            modelAllTypeArray.setType_id(allTypeObj.getString("type_id"));
                            modelAllTypeArray.setType_name(allTypeObj.getString("type_name"));
                            modelAllTypeArray.setType_image_url(allTypeObj.getString("type_image_url"));

                            JSONArray subTypeArray = allTypeObj.getJSONArray("sub_type_array");
                            arrSubTypeArrayList = new ArrayList<>();
                            for (int j=0;j<subTypeArray.length();j++){
                                JSONObject subTypeObj = subTypeArray.getJSONObject(j);
                                ModelSubTypeArray modelSubTypeArray = new ModelSubTypeArray();

                                modelSubTypeArray.setType_id(subTypeObj.getString("type_id"));
                                modelSubTypeArray.setType_name(subTypeObj.getString("type_name"));
                                modelSubTypeArray.setSub_type_image(subTypeObj.getString("sub_type_image"));

                                arrSubTypeArrayList.add(modelSubTypeArray);
                            }
                            modelAllTypeArray.setArrSubTypeArrayList(arrSubTypeArrayList);
                            Config.arrAllTypeArrayList.add(modelAllTypeArray);
                        }

                        adapterStoreProductHome = new AdapterStoreProductHome(getContext(),Config.arrAllTypeArrayList);
                        rv_storeProduct.setAdapter(adapterStoreProductHome);
                    } else {
                        dialog.dismiss();
                        showNoProductsDialog(response_msg);
                        tv_storeName.setText("No stores available at your current location.");
                        editor.putString("storeName","");
                        editor.commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    Toast.makeText(getContext(),getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                   /* editor.putString("storeName", AppUtils.toUppercase(store_name));
                    editor.commit();*/
                }
            } else {
                dialog.dismiss();
                Toast.makeText(getContext(),"Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showNoProductsDialog(String responceMsg){
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getContext());
        alertDialogBuilder
                .setMessage("No stores available at your current location.")
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void showLocationDialog() {
        LocationManager lm = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
            dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    isCheckGPS = true;
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getContext().startActivity(myIntent);
                    //get gps
                }
            });
            dialog.show();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (isCheckGPS){
            editor.putString("NavigationPosition", "0");
            editor.commit();
            startActivity(new Intent(getContext(), DrawerActivity.class));
        }

        String cartCount = db.getProductsInCartCount(sharedPreferences.getString("user_id",""));
        System.out.println("Activity Cart > cart count is " +cartCount);
        if (cartCount.equals("0") || cartCount.equals("")){
            btn_cart.setVisibility(View.GONE);
        }else {
            btn_cart.setVisibility(View.VISIBLE);
            btn_cart.setText(cartCount);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("Tag","in activity result");
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getContext(), data);
                Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber());
                String address = String.valueOf(place.getAddress());
              //  getLocationFromAddress(address);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getContext(), data);
                // TODO: Handle the error.
                Log.e("Tag", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
