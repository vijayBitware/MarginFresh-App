package com.marginfresh.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.Model.ModelProductList;
import com.marginfresh.Model.ModelSavedAddress;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityProductList;
import com.marginfresh.adapter.AdapterProductList;
import com.marginfresh.adapter.AdapterSavedAddress;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by bitware on 4/8/17.
 */

public class ActivitySavedAddress extends AppCompatActivity{

    ListView lv_savedAddress;
    TextView tv_addNewAddress;
    ArrayList<ModelSavedAddress> arrSavedAddress;
    AdapterSavedAddress adapterSavedAddress;
    String user_id;
    ConnectionDetector cd;
    Boolean isInternetPresent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ImageView iv_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_myaddress);

        init();
        user_id =sharedPreferences.getString("user_id","");
        if (isInternetPresent){
            new AddressListTask().execute("{\"customerId\":\"" + user_id +  "\"}");
        }else {
            Toast.makeText(ActivitySavedAddress.this,getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }

        tv_addNewAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("myAddress","saveAddress");
                editor.commit();
                startActivity(new Intent(ActivitySavedAddress.this,ActivityMyAddress.class));
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*editor.putString("NavigationPosition","7");
                editor.commit();
                startActivity(new Intent(ActivitySavedAddress.this,DrawerActivity.class));*/
                finish();
            }
        });
    }

    private void init() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        cd=new ConnectionDetector(ActivitySavedAddress.this);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        lv_savedAddress = (ListView) findViewById(R.id.lv_savedAddress);
        tv_addNewAddress = (TextView)findViewById(R.id.tv_addNewAddress);
    }

    class AddressListTask extends AsyncTask<String, Void, String> {
        Dialog dialog;
        @Override
        protected void onPreExecute() {
           dialog = AppUtils.customLoader(ActivitySavedAddress.this);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            com.squareup.okhttp.Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"customer_address_list.php?"+"customerId="+user_id)
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
            System.out.println(">>> Address list result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){

                        JSONArray savedAddressArray = resObj.getJSONArray("addressList");
                        arrSavedAddress = new ArrayList<>();
                            for (int i = 0; i < savedAddressArray.length(); i++) {
                                JSONObject addressObj = savedAddressArray.getJSONObject(i);
                                ModelSavedAddress modelSavedAddress = new ModelSavedAddress();
                                modelSavedAddress.setAddressFirstName(addressObj.getString("addressFirstName"));
                                modelSavedAddress.setAddressLastName(addressObj.getString("addressLastName"));
                                modelSavedAddress.setAddressBuildingnumber(addressObj.getString("addressBuildingnumber"));
                                modelSavedAddress.setAddressCity(addressObj.getString("addressCity"));
                                modelSavedAddress.setAddressCountry(addressObj.getString("addressCountry"));
                                modelSavedAddress.setAddressFlatnumber(addressObj.getString("addressFlatnumber"));
                                modelSavedAddress.setAddressMobileNumber(addressObj.getString("addressMobileNumber"));
                                modelSavedAddress.setAddressId(addressObj.getString("addressId"));
                                modelSavedAddress.setAddressPostcode(addressObj.getString("addressPostcode"));
                                modelSavedAddress.setAddressRegion(addressObj.getString("addressRegion"));
                                modelSavedAddress.setAddressStreet(addressObj.getString("addressStreet"));

                                arrSavedAddress.add(modelSavedAddress);

                            }
                        adapterSavedAddress=new AdapterSavedAddress(ActivitySavedAddress.this,R.layout.row_savedaddress,arrSavedAddress);
                        lv_savedAddress.setAdapter(adapterSavedAddress);

                    }else{
                        editor.putString("addressAvailable","0");
                        editor.commit();
                        Toast.makeText(ActivitySavedAddress.this,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivitySavedAddress.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                Toast.makeText(ActivitySavedAddress.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        /*editor.putString("NavigationPosition","7");
        editor.commit();
        startActivity(new Intent(ActivitySavedAddress.this,DrawerActivity.class));*/
        finish();
    }
}
