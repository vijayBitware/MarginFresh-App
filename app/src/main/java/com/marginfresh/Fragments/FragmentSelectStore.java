package com.marginfresh.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.marginfresh.Model.GetNearBy;
import com.marginfresh.Model.SelectStore;
import com.marginfresh.R;
import com.marginfresh.activities.DrawerActivity;
import com.marginfresh.activities.GPSTracker;
import com.marginfresh.activities.SelectStoreActivity;
import com.marginfresh.adapter.AdaperStoreList;
import com.marginfresh.adapter.StoreListingAdapter;
import com.marginfresh.db_utils.DatabaseHandler;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bitware on 6/6/17.
 */

public class FragmentSelectStore extends Fragment {

    View view;
    RecyclerView recycler_storeList;
    ArrayList<GetNearBy.Nearby_store_array> arrSelectStore;
    LinearLayout ll_moreStore;
    TextView tv_selectStores;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    GPSTracker gpsTracker;
    double longitude,lattitude;
    private String userid="",response_msg,searchString;
    GetNearBy getNearBy;
    StoreListingAdapter storeListingAdapter;
    Boolean isInternetPresent;
    ConnectionDetector cd;
    EditText edt_search;
    ImageView iv_search;
    boolean isCheckGPS = false;
    Dialog dialog;
    SwipeRefreshLayout sw_store;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view =inflater.inflate(R.layout.fragment_stores,container,false);

        initializeView();
        gpsTracker = new GPSTracker(getContext());
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

        if (Config.presentfragment.equals("store") && Config.status.equals("googlePlaces")){
            Log.e("TAG","In 1st case");
            lattitude = Double.parseDouble(sharedPreferences.getString("selectedAreaLat",""));
            longitude = Double.parseDouble(sharedPreferences.getString("selectedAreaLng",""));
            System.out.println("Adress Location >>> Lattitude-"+lattitude+"Longitude-"+longitude);
            userid =  sharedPreferences.getString("user_id","");
            if (!(lattitude ==0.0) && !(longitude ==0.0)){
                if(isInternetPresent){
                    new SelectStoreTask().execute("{\"user_id\":\"" + userid + "\",\"latitude\":\"" + lattitude + "\",\"longitude\":\"" + longitude + "\"}");
                }
                else {
                    Toast.makeText(getContext(),"Check Internet Connection",Toast.LENGTH_SHORT).show();
                }
            }
        }else {
            Log.e("TAG","In 2nd case");
            userid = sharedPreferences.getString("user_id", "");
            if (!(lattitude ==0.0) && !(longitude ==0.0)) {
                if (isInternetPresent) {
                    new SelectStoreTask().execute("{\"user_id\":\"" + userid + "\",\"latitude\":\"" + lattitude + "\",\"longitude\":\"" + longitude + "\"}");
                } else {
                    Toast.makeText(getContext(), "Check Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        }

        edt_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId== EditorInfo.IME_ACTION_DONE){
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edt_search.getWindowToken(), 0);
                    searchString = edt_search.getText().toString();
                    if (!searchString.isEmpty()) {
                        if (isInternetPresent) {
                            new SearchStore().execute("{\"user_id\":\"" + userid + "\",\"latitude\":\"" + lattitude + "\",\"longitude\":\"" + longitude + "\",\"searchstring\":\"" + searchString + "\"}");
                        } else {
                            Toast.makeText(getContext(), getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getContext(), "Please Enter Store To Search", Toast.LENGTH_SHORT).show();
                    }

                }

                return true;
            }
        });

        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edt_search.getWindowToken(), 0);

                searchString = edt_search.getText().toString();
                if (!searchString.isEmpty()) {
                    if (isInternetPresent) {
                        new SearchStore().execute("{\"user_id\":\"" + userid + "\",\"latitude\":\"" + lattitude + "\",\"longitude\":\"" + longitude + "\",\"searchstring\":\"" + searchString + "\"}");
                    } else {
                        Toast.makeText(getContext(),getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getContext(), "Please Enter Store To Search", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sw_store.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sw_store.setRefreshing(false);
                if (!(lattitude ==0.0) && !(longitude ==0.0)) {
                    if (isInternetPresent) {
                        new SelectStoreTask().execute("{\"user_id\":\"" + userid + "\",\"latitude\":\"" + lattitude + "\",\"longitude\":\"" + longitude + "\"}");
                    } else {
                        Toast.makeText(getContext(), "Check Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return view;
    }

    private void initializeView() {
        cd =new ConnectionDetector(getContext());
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor= sharedPreferences.edit();
        recycler_storeList = (RecyclerView)view.findViewById(R.id.rv_selectStore);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(),3);
        recycler_storeList.setLayoutManager(linearLayoutManager);
        tv_selectStores = (TextView) view.findViewById(R.id.tv_selectStores);
        edt_search = (EditText) view.findViewById(R.id.edt_search);
        iv_search = (ImageView) view.findViewById(R.id.iv_search);
        sw_store = (SwipeRefreshLayout) view.findViewById(R.id.sw_store);
        sw_store.setColorSchemeResources(R.color.colorPrimary);
        Log.e("TAG","inSelectStoreFragment");
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
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getContext().startActivity(myIntent);
                    //get gps
                }
            });
            dialog.show();
        }
    }

    private class SelectStoreTask extends AsyncTask<String, Void, String> {

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
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"nearby.php?"+"user_id="+ userid +"&"+ "latitude=" +lattitude +"&"+ "longitude=" +longitude)
                    .post(body)
                    .build();

            try
            {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            System.out.println(">>>result :" + s);
            if(s != null){
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if(status.equals("1"))
                    {
                        getNearBy = new GsonBuilder().create().fromJson(s,GetNearBy.class);
                        storeListingAdapter = new StoreListingAdapter(getContext(),getNearBy.getNearby_store_array());
                        recycler_storeList.setAdapter(storeListingAdapter);
                        Config.status = "";
                     //   Config.presentfragment="";
                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(getContext(),response_msg,Toast.LENGTH_SHORT).show();
                        Config.status = "";
                        getNearBy = new GsonBuilder().create().fromJson(s,GetNearBy.class);
                        getNearBy.setNearby_store_array(new ArrayList<GetNearBy.Nearby_store_array>());
                        storeListingAdapter = new StoreListingAdapter(getContext(),getNearBy.getNearby_store_array());
                        recycler_storeList.setAdapter(storeListingAdapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }
            }
            else {
                dialog.dismiss();
                Toast.makeText(getActivity(),"Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SearchStore extends AsyncTask<String, Void, String> {

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
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"searchspecificstorenearby.php?"+"user_id="+ userid +"&"+ "latitude=" +lattitude +"&"+ "longitude=" +longitude+"&searchstring=" +searchString)
                    .post(body)
                    .build();

            try
            {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // clearEditTextData();
            dialog.dismiss();
            System.out.println(">>>result :" + s);

            if(s != null){

                try{
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if(status.equals("1"))
                    {
                        getNearBy = new GsonBuilder().create().fromJson(s,GetNearBy.class);
                        storeListingAdapter = new StoreListingAdapter(getContext(),getNearBy.getNearby_store_array());
                        recycler_storeList.setAdapter(storeListingAdapter);
                    }
                    else {
                        Toast.makeText(getContext(),response_msg,Toast.LENGTH_SHORT).show();
                        getNearBy.setNearby_store_array(new ArrayList<GetNearBy.Nearby_store_array>());
                        storeListingAdapter = new StoreListingAdapter(getContext(),getNearBy.getNearby_store_array());
                        recycler_storeList.setAdapter(storeListingAdapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    Toast.makeText(getContext(),getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }
            }
            else {
                dialog.dismiss();
                Toast.makeText(getContext(), "Network error.try again later...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCheckGPS) {
            editor.putString("NavigationPosition", "1");
            editor.commit();
            startActivity(new Intent(getContext(), DrawerActivity.class));
        }

        Button noticount = (Button) getActivity().findViewById(R.id.btn_notiCount);
        if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
            noticount.setVisibility(View.INVISIBLE);
        }else {
            noticount.setVisibility(View.VISIBLE);
            noticount.setText(sharedPreferences.getString("notiCount",""));
        }

        DatabaseHandler db = new DatabaseHandler(getContext());
        Button btn_cartCount = (Button) getActivity().findViewById(R.id.btn_cartCount);
        String cartCount = db.getProductsInCartCount(sharedPreferences.getString("user_id",""));
        System.out.println("Activity Cart > cart count is " +cartCount);
        if (cartCount.equals("0") || cartCount.equals("")){
            btn_cartCount.setVisibility(View.GONE);
        }else {
            btn_cartCount.setVisibility(View.VISIBLE);
            btn_cartCount.setText(cartCount);
        }
    }
}
