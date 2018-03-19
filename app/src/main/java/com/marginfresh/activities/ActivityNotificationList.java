package com.marginfresh.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.marginfresh.Model.ModelNotificationList;
import com.marginfresh.R;
import com.marginfresh.adapter.AdapterNotification;
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
 * Created by bitware on 5/8/17.
 */

public class ActivityNotificationList extends AppCompatActivity {

    ListView lv_notificationList;
    ArrayList<ModelNotificationList> arrNotificationList;
    ConnectionDetector cd;
    Boolean isInternetPresent;
    String user_id="";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    AdapterNotification adapterNotification;
    ImageView iv_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificationlist);
        init();
        user_id = sharedPreferences.getString("user_id","");
        if (isInternetPresent){
            new NotificationList().execute("{\"userId\":\"" + user_id + "\",\"deviceType\":\"" + "android"+  "\"}");
        }else {
            Toast.makeText(ActivityNotificationList.this,getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void init() {
        cd = new ConnectionDetector(ActivityNotificationList.this);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences =getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor =sharedPreferences.edit();
        lv_notificationList = (ListView) findViewById(R.id.lv_notificationList);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        editor.putString("notiCount","0");
        editor.commit();
    }

    class NotificationList extends AsyncTask<String, Void, String> {
        Dialog dialog= AppUtils.customLoader(ActivityNotificationList.this);
        @Override
        protected void onPreExecute() {
            dialog = AppUtils.customLoader(ActivityNotificationList.this);
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
                    .url(Config.BASE_URL_SHOPCART+"deviceid/deviceNotifyList.php?"+"userId="+user_id+"&deviceType="+"android")
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
            System.out.println(">>> Notification List result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                        JSONArray notifcationData = resObj.getJSONArray("notifcationData");
                        arrNotificationList = new ArrayList<>();
                        for (int i=0;i<notifcationData.length();i++){
                            JSONObject notiObj = notifcationData.getJSONObject(i);
                            ModelNotificationList modelNotificationList = new ModelNotificationList();
                            modelNotificationList.setNotification_id(notiObj.getString("id"));
                            modelNotificationList.setNotify_title(notiObj.getString("notify_title"));
                            modelNotificationList.setNotify_content(notiObj.getString("notify_content"));
                            modelNotificationList.setNotify_logo(notiObj.getString("notify_logo"));
                            arrNotificationList.add(modelNotificationList);
                        }
                        adapterNotification = new AdapterNotification(ActivityNotificationList.this,R.layout.row_notification,arrNotificationList);
                        lv_notificationList.setAdapter(adapterNotification);
                    }else{
                        Toast.makeText(ActivityNotificationList.this,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityNotificationList.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                dialog.dismiss();
                Toast.makeText(ActivityNotificationList.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
