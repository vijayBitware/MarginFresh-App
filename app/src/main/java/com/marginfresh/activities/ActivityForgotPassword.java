package com.marginfresh.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
 * Created by Admin on 6/21/2017.
 */

public class ActivityForgotPassword extends AppCompatActivity {

    EditText edit_Email;
    TextView btn_Send_password;
    String user_Email = " ";
    ImageView iv_back;
    private String response_msg="";
    Boolean isInternetPresent;
    ConnectionDetector cd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acivity_forgot_password);

        init();
        btn_Send_password.setOnClickListener(new SendMail());

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityForgotPassword.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void init()
    {
        cd = new ConnectionDetector(ActivityForgotPassword.this);
        isInternetPresent = cd.isConnectingToInternet();
        edit_Email = (EditText) findViewById(R.id.edit_email);
        btn_Send_password = (TextView) findViewById(R.id.send_password);
        iv_back = (ImageView) findViewById(R.id.iv_back);
    }

    class SendMailTask extends AsyncTask<String, Void, String> {
        Dialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(ActivityForgotPassword.this);
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

            Request request = new  Request.Builder()
                    .url(Config.BASE_URL + "forgot_pass.php?" + "userEmail=" +user_Email)
                    .post(body)
                    .build();

            try{
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            System.out.println(">>> Result :" +s);
            dialog.dismiss();

            if(s != null){
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if(status.equals("1")){
                        Toast.makeText(ActivityForgotPassword.this, response_msg, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ActivityForgotPassword.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(ActivityForgotPassword.this,response_msg,Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityForgotPassword.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else {
                dialog.dismiss();
                Toast.makeText(ActivityForgotPassword.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class SendMail implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            user_Email = edit_Email.getText().toString();

            if(user_Email.isEmpty()){

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityForgotPassword.this);
                alertDialogBuilder
                        .setMessage("Please Enter Email Id")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            else {
                if(isInternetPresent){
                    new SendMailTask().execute("{\"userEmail\":\"" + user_Email + "}");
                }
                else {
                    Toast.makeText(ActivityForgotPassword.this, "Please Check Internet Connection.!!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}



