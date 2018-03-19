package com.marginfresh.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.marginfresh.Fragments.FragmentBudget;
import com.marginfresh.Fragments.FragmentContactUs;
import com.marginfresh.Fragments.FragmentDrawer;
import com.marginfresh.Fragments.FragmentHome;
import com.marginfresh.Fragments.FragmentMyOrders;
import com.marginfresh.Fragments.FragmentMyWishlist;
import com.marginfresh.Fragments.FragmentSelectStore;
import com.marginfresh.Fragments.FragmentSetting;
import com.marginfresh.Fragments.FragmentWallet;
import com.marginfresh.R;
import com.marginfresh.ZendeskLiveChat.LiveChatActivity;
import com.marginfresh.db_utils.DatabaseHandler;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.zopim.android.sdk.api.ZopimChat;
import com.zopim.android.sdk.model.VisitorInfo;
import com.zopim.android.sdk.prechat.PreChatForm;
import com.zopim.android.sdk.prechat.ZopimChatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import static com.marginfresh.R.id.drawer_layout;
import static com.marginfresh.R.id.fragment_navigation_drawer;


/**
 * Created by bitware on 31/5/17.
 */

public class DrawerActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener, GoogleApiClient.OnConnectionFailedListener {

    Toolbar toolbar;
    private FragmentDrawer drawerFragment;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView tv_toolbarTitle;
    ImageView iv_location;
    private String strNavigation;
    boolean isSelected ;
    private String response_msg="",user_id="",lattitude="",longitude="",address="";
    ConnectionDetector cd;
    boolean isInternetPresent,isLocationClicked=true;
    RelativeLayout rlCart,rl_notification;
    private Button btn_cart,btn_notiCount;
    LinearLayout ll_locationBar;
    GPSTracker gpsTracker;
    BroadcastReceiver receiver;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;
    DatabaseHandler db;
    double lat,lng;
    ZopimChat.SessionConfig config;
  //  private static final AndroidHttpClient ANDROID_HTTP_CLIENT = AndroidHttpClient.newInstance(DrawerActivity.class.getName());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        init();
        ZopimChat.init(getResources().getString(R.string.zendesk_account_key));
        VisitorInfo visitorInfo = new VisitorInfo.Builder()
                .phoneNumber(sharedPreferences.getString("customerContact",""))
                .email(sharedPreferences.getString("customerEmail",""))
                .name(sharedPreferences.getString("customerName",""))
                .build();
        ZopimChat.setVisitorInfo(visitorInfo);
        // set pre chat fields as mandatory
        PreChatForm preChatForm = new PreChatForm.Builder()
                .name(PreChatForm.Field.REQUIRED_EDITABLE)
                .email(PreChatForm.Field.REQUIRED_EDITABLE)
                .phoneNumber(PreChatForm.Field.REQUIRED_EDITABLE)
                .department(PreChatForm.Field.REQUIRED_EDITABLE)
                .message(PreChatForm.Field.REQUIRED_EDITABLE)
                .build();

        // build chat config
        config = new ZopimChat.SessionConfig().preChatForm(preChatForm).department("My memory");

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        gpsTracker = new GPSTracker(DrawerActivity.this);
        if (gpsTracker.canGetLocation()){
            lattitude = String.valueOf(gpsTracker.getLatitude());
            longitude = String.valueOf(gpsTracker.getLongitude());
            System.out.println("Current Location >>> Lattitude-"+lattitude+"Longitude-"+longitude);
            editor.putString("lattitude", String.valueOf(lattitude));
            editor.putString("longitude", String.valueOf(longitude));
            editor.commit();
        }
        strNavigation  = sharedPreferences.getString("NavigationPosition","");
        displayView(Integer.parseInt(strNavigation));

        ll_locationBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isLocationClicked) {
                        isLocationClicked = false;
                        Intent intent = new PlaceAutocomplete
                                .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .build(DrawerActivity.this);
                        startActivityForResult(intent, 1);
                    }
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        rlCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("cartFrom","home");
                editor.putString("NavigationPosition",strNavigation);
                editor.commit();
                startActivity(new Intent(DrawerActivity.this,ActivityCart.class));
            }
        });

        rl_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DrawerActivity.this,ActivityNotificationList.class));
            }
        });
    }

    private void init() {
        cd =new  ConnectionDetector(DrawerActivity.this);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(null);

        drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(fragment_navigation_drawer);
        drawerFragment.setUp(fragment_navigation_drawer, (DrawerLayout) findViewById(drawer_layout), toolbar);
        drawerFragment.setDrawerListener(this);

        tv_toolbarTitle = (TextView) findViewById(R.id.tv_toolbarTitle);
        iv_location= (ImageView) findViewById(R.id.iv_location);
        rlCart = (RelativeLayout) findViewById(R.id.rlCart);
        rl_notification = (RelativeLayout) findViewById(R.id.rl_notification);
        ll_locationBar = (LinearLayout) findViewById(R.id.ll_locationBar);


        db = new DatabaseHandler(DrawerActivity.this);
        btn_cart = (Button) findViewById(R.id.btn_cartCount);
        String cartCount = db.getProductsInCartCount(sharedPreferences.getString("user_id",""));
        System.out.println("Cart Item Count > " +cartCount);
        if (cartCount.equals("0") || cartCount.equals("")){
            btn_cart.setVisibility(View.GONE);
        }else {
            btn_cart.setVisibility(View.VISIBLE);
            btn_cart.setText(cartCount);
        }

        btn_notiCount = (Button) findViewById(R.id.btn_notiCount);
        //code to update notification count
        if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
            btn_notiCount.setVisibility(View.INVISIBLE);
        }else {
            btn_notiCount.setVisibility(View.VISIBLE);
            btn_notiCount.setText(sharedPreferences.getString("notiCount",""));
        }

        IntentFilter intent = new IntentFilter();
        intent.addAction("notificationSend");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
                    btn_notiCount.setVisibility(View.INVISIBLE);
                }else {
                    System.out.println("noti count> " +sharedPreferences.getString("notiCount",""));
                    btn_notiCount.setVisibility(View.VISIBLE);
                    btn_notiCount.setText(sharedPreferences.getString("notiCount",""));
                }
            }
        };

        registerReceiver(receiver,intent);

    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);

    }

    @Override
    protected void onResume() {
        super.onResume();
        isLocationClicked=true;
        //Code For updating cart count
        if (sharedPreferences.getString("cartItemCount","").equals("0") || sharedPreferences.getString("cartItemCount","").equals("")){
            btn_cart.setVisibility(View.GONE);
        }else {
            btn_cart.setVisibility(View.VISIBLE);
            btn_cart.setText(sharedPreferences.getString("cartItemCount",""));
        }
        //code to update notification count
        if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
            btn_notiCount.setVisibility(View.INVISIBLE);
        }else if (!sharedPreferences.getString("notiCount","").equals("0") || !sharedPreferences.getString("notiCount","").equals("")){
            btn_notiCount.setVisibility(View.VISIBLE);
            btn_notiCount.setText(sharedPreferences.getString("notiCount",""));
        }
    }

    private void displayView(int position){

        switch (position){
            case 0:
                Config.presentfragment = "home";
                replaceFragment(new FragmentHome(),"");
                iv_location.setVisibility(View.VISIBLE);
                ll_locationBar.setEnabled(true);
                break;
            case 1:
                Config.presentfragment = "store";
                replaceFragment(new FragmentSelectStore(),"Stores");
                ll_locationBar.setEnabled(true);
                break;
            case 2:
                replaceFragment(new FragmentBudget(),"Budget");
                iv_location.setVisibility(View.GONE);
                ll_locationBar.setEnabled(false);
                break;
            case 3:
                replaceFragment(new FragmentWallet(),"Wallet");
                iv_location.setVisibility(View.GONE);
                ll_locationBar.setEnabled(false);
                break;
            case 4:
                replaceFragment(new FragmentMyOrders(),"My Orders");
                iv_location.setVisibility(View.GONE);
                ll_locationBar.setEnabled(false);
                break;
            case 5:
                replaceFragment(new FragmentMyWishlist(),"My Wishlist");
                iv_location.setVisibility(View.GONE);
                ll_locationBar.setEnabled(false);
                break;
            case 6:
                replaceFragment(new FragmentContactUs(), "Contact Us");
                iv_location.setVisibility(View.GONE);
                ll_locationBar.setEnabled(false);
                break;
            case 7:
                LiveChat();
                break;
            case 8:
                replaceFragment(new FragmentSetting(), "Settings");
                iv_location.setVisibility(View.GONE);
                ll_locationBar.setEnabled(false);
                break;
            case 9:
                showLogoutDialog();
                break;
            default:
                break;
        }
    }

    private void LiveChat() {
       // ZopimChat.trackEvent("Application created");
        // start chat activity with config
        ZopimChatActivity.startActivity(DrawerActivity.this,config);
        // Sample breadcrumb
     //   ZopimChat.trackEvent("Started chat with pre-set visitor information");
    }

    public void replaceFragment(Fragment fragment,String title) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container_body, fragment);
            transaction.commit();
            tv_toolbarTitle.setText(title);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber());
                address = String.valueOf(place.getAddress());
                getLocationFromAddress(address);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.e("Tag", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

    }

    public void getLocationFromAddress(String strAddress){

        new DataLongOperationAsynchTask().execute();
    }

    @Override
    public void onBackPressed() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(DrawerActivity.this);
        alertDialogBuilder.setTitle("Exit Application");
        alertDialogBuilder
                .setMessage("Are you sure you want to exit ?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                                homeIntent.addCategory( Intent.CATEGORY_HOME );
                                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(homeIntent);
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

    private void showLogoutDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DrawerActivity.this)
                .setTitle("Log Out?")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.clear();
                        editor.commit();
                        editor.putString("isUserRegister","yes");
                        editor.commit();
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                        startActivity(new Intent(DrawerActivity.this, LoginActivity.class));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    class HomeDataWhenSkipStore extends AsyncTask<String, Void, String> {

        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(DrawerActivity.this);
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
                    .url(Config.BASE_URL + "homedata.php?"+"user_id="+user_id+"&latitude="+lat+"&longitude="+lng)
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
            dialog.dismiss();
            System.out.println(">>>Home data result :" + s);
            if (s != null) {
//                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if (status.equals("1")) {
                        String store_name = jsonObject.getString("public_name");
                        String cartItemCount = jsonObject.getString("cartItemCount");
                        editor.putString("cartItemCount",cartItemCount);
                        editor.putString("storeName",store_name);
                        editor.putString("comeFrom","fromGooglePlace");
                        editor.putString("NavigationPosition","0");
                        editor.commit();
                        startActivity(new Intent(DrawerActivity.this,DrawerActivity.class));
                    } else {
//                        p.dismiss();
                        Toast.makeText(DrawerActivity.this, response_msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(DrawerActivity.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            } else {
                dialog.dismiss();
                Toast.makeText(DrawerActivity.this,"Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class DataLongOperationAsynchTask extends AsyncTask<String, Void, String[]> {
        Dialog dialog = AppUtils.customLoader(DrawerActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected String[] doInBackground(String... params) {
            String response;
            try {
                address = address.replaceAll(" ","%20");
                response = getLatLongByURL("https://maps.google.com/maps/api/geocode/json?key=AIzaSyCROPk1VN4nxMTroLdBfrU50ywnCs55_Rk&address="+address+"&sensor=false");
                Log.e("response",""+response);
                return new String[]{response};
            } catch (Exception e) {
                return new String[]{"error"};
            }
        }

        @Override
        protected void onPostExecute(String... result) {
            try {
                System.out.println("result > " +result);
                JSONObject jsonObject = new JSONObject(result[0]);

                lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                Log.e("latitude", "" + lat);
                Log.e("longitude", "" + lng);

                System.out.println("Status " +Config.presentfragment);
                if (Config.presentfragment.equals("home")){
                    editor.putString("selectedAreaLat",String.valueOf(lat));
                    editor.putString("selectedAreaLng", String.valueOf(lng));
                    editor.putString("comeFrom","fromGooglePlace");
                    editor.putString("NavigationPosition","0");
                    editor.commit();
                    startActivity(new Intent(DrawerActivity.this,DrawerActivity.class));
                    /*user_id = sharedPreferences.getString("user_id","");
                    if (isInternetPresent){
                        new HomeDataWhenSkipStore().execute("{\"user_id\":\"" + user_id + "\",\"latitude\":\"" + lat + "\",\"longitude\":\"" + lng +  "\"}");
                    }
                    else {
                        Toast.makeText(DrawerActivity.this,R.string.no_internet,Toast.LENGTH_SHORT).show();
                    }*/
                }else if (Config.presentfragment.equals("store")){
                    Config.status = "googlePlaces";
                    editor.putString("presentFragment","googlePlaces");
                    editor.putString("selectedAreaLat",String.valueOf(lat));
                    editor.putString("selectedAreaLng", String.valueOf(lng));
                    editor.putString("NavigationPosition","1");
                    editor.commit();
                    startActivity(new Intent(DrawerActivity.this,DrawerActivity.class));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }


    public String getLatLongByURL(String requestURL) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}

