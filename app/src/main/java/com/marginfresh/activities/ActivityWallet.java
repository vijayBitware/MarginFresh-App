package com.marginfresh.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.Fragments.FragmentWallet;
import com.marginfresh.R;
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
import java.util.concurrent.TimeUnit;

/**
 * Created by bitware on 23/9/17.
 */

public class ActivityWallet extends AppCompatActivity {

    String user_id="",response_msg="";
    TextView tv_wallet;
    ConnectionDetector cd;
    Boolean isInternetPresent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ImageView iv_back;
    TextView tv_storeName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        init();
        tv_storeName.setText(sharedPreferences.getString("storeName",""));
        user_id = sharedPreferences.getString("user_id","");
        if (isInternetPresent){
            new GetWallet().execute("{\"user_id\":\"" + user_id + "\"}");
        }

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityWallet.this,ActivityProductList.class));
                finish();
            }
        });
    }

    private void init() {
        tv_storeName= (TextView) findViewById(R.id.tv_storeName);
        cd = new ConnectionDetector(ActivityWallet.this);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        tv_wallet = (TextView)findViewById(R.id.tv_wallet);
        iv_back = (ImageView) findViewById(R.id.iv_back);
    }

    class GetWallet extends AsyncTask<String, Void, String> {

        Dialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(ActivityWallet.this);
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
                    .url(Config.BASE_URL_WALLET+"getWalletAmount.php?"+"user_id="+ user_id )
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

            System.out.println(">>> Add budget result :" + s);
            dialog.dismiss();

            if(s != null){
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if(status.equals("1")){
                        Toast.makeText(ActivityWallet.this,response_msg,Toast.LENGTH_SHORT).show();
                        tv_wallet.setText("AED " +jsonObject.getString("amountInWallet"));
                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(ActivityWallet.this,response_msg,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    Toast.makeText(ActivityWallet.this,getResources().getString(R.string.network_error),Toast.LENGTH_SHORT).show();
                }
            }
            else {
                dialog.dismiss();
                Toast.makeText(ActivityWallet.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ActivityWallet.this,ActivityNotificationList.class));
        finish();
    }
}
