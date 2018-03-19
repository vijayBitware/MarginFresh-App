package com.marginfresh.activities;

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
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.marginfresh.Model.GetNearBy;
import com.marginfresh.Model.SelectStore;
import com.marginfresh.R;
import com.marginfresh.adapter.StoreListingAdapter;
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

public class SelectStoreActivity extends AppCompatActivity {

    RecyclerView recycler_storeList;
    StoreListingAdapter storeListingAdapter;
    ArrayList<SelectStore> arrSelectStore;
    LinearLayout ll_moreStore;
    TextView tv_selectStores;
    String userid = "";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean isInternetPresent;
    ConnectionDetector cd;
    private String response_msg = "";
    GetNearBy getNearBy;
    GPSTracker gpsTracker;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    double lattitude, longitude;
    private String user_id = "";
    boolean allgranted = false, isPermissionGranted, isLocationEnabled = false;
    boolean gps_enabled = false;
    boolean network_enabled = false;

    LocationManager locManager;
    LocationListener locListener;
    Location CurrentLocation;
    LocationManager locationManager;
    boolean locationUpdated = false;
    ProgressDialog p ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_store_new);

        initializeView();
        locationManager = (LocationManager) SelectStoreActivity.this.getSystemService(Context.LOCATION_SERVICE);

        tv_selectStores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userid = sharedPreferences.getString("user_id", "");
                editor.putString("status", "afterStore");
                editor.putString("homePage", "fromSelectStore");
                editor.putString("NavigationPosition", "0");
                editor.putString("comeFrom", "skipStore");
                editor.commit();
                startActivity(new Intent(SelectStoreActivity.this, DrawerActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentLocation();
    }

    private void showLocationDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SelectStoreActivity.this);
        dialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
//                    Config.isCheckGPS = true;
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
                //get gps
            }
        });

        dialog.show();
    }

    private void initializeView() {
        cd = new ConnectionDetector(SelectStoreActivity.this);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gpsTracker = new GPSTracker(SelectStoreActivity.this);
        recycler_storeList = (RecyclerView) findViewById(R.id.rv_selectStore);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(this, 3);
        recycler_storeList.setLayoutManager(linearLayoutManager);
        tv_selectStores = (TextView) findViewById(R.id.tv_selectStores);
        ll_moreStore = (LinearLayout) findViewById(R.id.ll_moreStore);
        editor.putString("status", "isSelectStore");
        editor.putString("currentlyOn", "selectStoreActivity");
        editor.commit();


        p= new ProgressDialog(SelectStoreActivity.this);
        p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        p.setMessage("Updating User location");


    }

    private class SelectStoreTask extends AsyncTask<String, Void, String> {

        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(SelectStoreActivity.this);
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
                    .url(Config.BASE_URL + "nearby.php?" + "user_id=" + userid + "&" + "latitude=" + lattitude + "&" + "longitude=" + longitude)
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
            System.out.println(">>>result :" + s);
            dialog.dismiss();

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if (status.equals("1")) {
                        getNearBy = new GsonBuilder().create().fromJson(s, GetNearBy.class);
                        storeListingAdapter = new StoreListingAdapter(getApplicationContext(), getNearBy.getNearby_store_array());
                        recycler_storeList.setAdapter(storeListingAdapter);
                    } else {
                        dialog.dismiss();
                        Toast.makeText(SelectStoreActivity.this, "No Stores Available", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SelectStoreActivity.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            } else {
                dialog.dismiss();
                Toast.makeText(SelectStoreActivity.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(SelectStoreActivity.this);
        alertDialogBuilder.setTitle("Exit Application");
        alertDialogBuilder
                .setMessage("Are you sure you want to exit ?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveTaskToBack(true);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public final class GPSTracker implements LocationListener {

        private final Context mContext;

        // flag for GPS status
        public boolean isGPSEnabled = false;

        // flag for network status
        boolean isNetworkEnabled = false;

        // flag for GPS status
        boolean canGetLocation = false;

        Location location; // location
        // The minimum distance to change Updates in meters
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

        // The minimum time between updates in milliseconds
        private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

        // Declaring a Location Manager
        protected LocationManager locationManager;

        public GPSTracker(Context context) {
            this.mContext = context;
            getLocation();

        }

        /**
         * Function to get the user's current location
         * @return
         */
        public Location getLocation() {
            try {
                System.out.println("In getlocation.............");
                locationManager = (LocationManager) mContext
                        .getSystemService(Context.LOCATION_SERVICE);

                // getting GPS status
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                Log.v("isGPSEnabled", "=" + isGPSEnabled);

                // getting network status
                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

                if (isGPSEnabled == false && isNetworkEnabled == false) {
                    // no network provider is enabled
                } else {
                    this.canGetLocation = true;
                    if (isNetworkEnabled) {
                        if (ActivityCompat.checkSelfPermission(SelectStoreActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SelectStoreActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                lattitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);


                                if (location != null) {
                                    lattitude = location.getLatitude();
                                    longitude = location.getLongitude();

                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return location;
        }

        /**
         * Stop using GPS listener Calling this function will stop using GPS in your
         * app
         * */
        public void stopUsingGPS() {
            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(SelectStoreActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SelectStoreActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.removeUpdates(GPSTracker.this);
            }
        }

        /**
         * Function to get latitude
         * */
        public double getLatitude() {
            if (location != null) {

                lattitude = location.getLatitude();
            }

            // return latitude
            return lattitude;
        }

        /**
         * Function to get longitude
         * */
        public double getLongitude() {
            if (location != null) {

                longitude = location.getLongitude();
            }

            // return longitude
            return longitude;
        }

        /**
         * Function to check GPS/wifi enabled
         *
         * @return boolean
         * */
        public boolean canGetLocation() {
            return this.canGetLocation;
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                lattitude = location.getLatitude();
                longitude = location.getLongitude();
               /* System.out.println("lat>>>>>++++++" + latitude);
                System.out.println("lang>>>>>>+++++++" + longitude);*/
                if (lattitude == 0.0 && longitude == 0.0) {
                    System.out.println("lat>>>>>++++++" + lattitude);
                    System.out.println("lang>>>>>>+++++++" + longitude);
                    p.show();
                    //locationUpdated = false;
                } else{
                    System.out.println("lat>>>>>++++++" + lattitude);
                    System.out.println("lang>>>>>>+++++++" + longitude);
                    if(!locationUpdated)
                    {
                        System.out.println("***************got location*******************");
                        locationUpdated = true;
                        if (isInternetPresent) {
                            new SelectStoreTask().execute("{\"user_id\":\"" + userid + "\",\"latitude\":\"" + lattitude + "\",\"longitude\":\"" + longitude + "\"}");
                        } else {
                            Toast.makeText(SelectStoreActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                        }


                    }

                    p.dismiss();
                }

            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }
    private void getCurrentLocation() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            showLocationDialog();
        }
        else{
            GPSTracker mGPS = new GPSTracker(SelectStoreActivity.this);
            if(mGPS.canGetLocation ){
                mGPS = new GPSTracker(SelectStoreActivity.this);
                lattitude = mGPS.getLatitude();
                longitude = mGPS.getLongitude();
                userid = sharedPreferences.getString("user_id", "");
                if (isInternetPresent) {
                    new SelectStoreTask().execute("{\"user_id\":\"" + userid + "\",\"latitude\":\"" + lattitude + "\",\"longitude\":\"" + longitude + "\"}");
                } else {
                    Toast.makeText(SelectStoreActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(SelectStoreActivity.this, "unable to get location", Toast.LENGTH_LONG).show();
            }

        }



    }

}
