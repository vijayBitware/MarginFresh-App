package com.marginfresh.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
 * Created by bitware on 3/7/17.
 */

public class ActivityUpdateProfile extends AppCompatActivity {

    TextView btn_Register;
    String userName = "", userContact = "", userLocality = "", userBuilding = "", userEmail = "", userPassword = "",user_id="",userLastName="";
    ConnectionDetector cd;
    Boolean isInternetPresent;
    SharedPreferences sharedPreferences;
    private String response_msg = "";
    SharedPreferences.Editor editor;
    EditText edtRegName,edtRegContact,edtRegLocality,edtRegBuilding,edtRegEmail,edtRegPassword,edtLastName;
    ImageView iv_locationIcon,iv_back;
    String colored = "*";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateprofile);

        initView();

        edtRegName.setText(sharedPreferences.getString("customerFirstName",""));
        edtLastName.setText(sharedPreferences.getString("customerLastName",""));
        edtRegContact.setText(sharedPreferences.getString("customerContact",""));
        edtRegEmail.setText(sharedPreferences.getString("customerEmail",""));
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*editor.putString("NavigationPosition","7");
                editor.commit();
                Intent intent=new Intent(ActivityUpdateProfile.this,DrawerActivity.class);
                startActivity(intent);*/
                finish();
            }
        });
        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtRegEmail.getWindowToken(), 0);
                userName = edtRegName.getText().toString();
                userContact = edtRegContact.getText().toString();
                userEmail = edtRegEmail.getText().toString();
                user_id = sharedPreferences.getString("user_id","");
                userLastName = edtLastName.getText().toString();

                if(userName.isEmpty() && userLastName.isEmpty() && userContact.isEmpty()&& userEmail.isEmpty() ){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityUpdateProfile.this);
                    alertDialogBuilder
                            .setMessage("Please Fill All Fields")
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
                    if (!userName.isEmpty()){
                        if (!userLastName.isEmpty()) {
                            if (!userContact.isEmpty()) {
                                if (userContact.length() >= 10 && userContact.length() <=12 ) {
                                    if (!userEmail.isEmpty()) {
                                        if (userEmail.matches(Config.EMAIL_REGEX)) {
                                            if (isInternetPresent) {
                                                new UpdateProfile().execute("{\"firstname\":\"" + userName + "\",\"lastname\":\"" + userLastName + "\",\"userEmail\":\"" + userEmail + "\",\"mobileNumber\":\"" + userContact + "\",\"email_id\":\"" + userEmail + "\",\"customerId\":\"" + user_id + "\"}");
                                            } else {
                                                Toast.makeText(ActivityUpdateProfile.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            edtRegEmail.setError("Please Enter Valid Email Id");
                                            edtRegEmail.requestFocus();
                                        }
                                    } else {
                                        edtRegEmail.setError("Please Enter Email Id");
                                        edtRegEmail.requestFocus();
                                    }
                                } else {
                                    edtRegContact.setError("Please Enter Valid Contact Number");
                                    edtRegContact.requestFocus();
                                }
                            } else {
                                edtRegContact.setError("Please Enter Contact Number");
                                edtRegContact.requestFocus();
                            }
                        }else {
                            edtLastName.setError("Please Enter Last Name");
                            edtLastName.requestFocus();
                        }
                    }else {
                        edtRegName.setError("Please Enter First Name");
                        edtRegName.requestFocus();
                    }
                }
            }
        });

    }

    private void initView()
    {
        cd = new ConnectionDetector(ActivityUpdateProfile.this);
        isInternetPresent = cd.isConnectingToInternet();
        btn_Register = (TextView) findViewById(R.id.btn_Register);
        sharedPreferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        edtRegName = (EditText) findViewById(R.id.edit_name);
        edtRegContact = (EditText) findViewById(R.id.edit_phone1);
        edtRegEmail = (EditText) findViewById(R.id.edit_registeremail);
        btn_Register = (TextView) findViewById(R.id.btn_Register);
        iv_locationIcon = (ImageView) findViewById(R.id.iv_locationIcon);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        edtLastName = (EditText) findViewById(R.id.edit_lastname);

        String firstName = "First Name";
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(firstName);
        int start = builder.length();
        builder.append(colored);
        int end = builder.length();
        builder.setSpan(new ForegroundColorSpan(Color.RED), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edtRegName.setHint(builder);

        String lastName = "Last Name";
        SpannableStringBuilder builderL = new SpannableStringBuilder();
        builderL.append(lastName);
        int startL = builderL.length();
        builderL.append(colored);
        int endL = builderL.length();
        builderL.setSpan(new ForegroundColorSpan(Color.RED), startL, endL,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edtLastName.setHint(builderL);

        String contact = "Contact No";
        SpannableStringBuilder contactBulder = new SpannableStringBuilder();
        contactBulder.append(contact);
        int contactstart = contactBulder.length();
        contactBulder.append(colored);
        int contactEnd = contactBulder.length();
        contactBulder.setSpan(new ForegroundColorSpan(Color.RED), contactstart, contactEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edtRegContact.setHint(contactBulder);

        String email = "Email Id";
        SpannableStringBuilder emailbuilder = new SpannableStringBuilder();
        emailbuilder.append(email);
        int emailStart = emailbuilder.length();
        emailbuilder.append(colored);
        int emailEnd = emailbuilder.length();
        emailbuilder.setSpan(new ForegroundColorSpan(Color.RED), emailStart, emailEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edtRegEmail.setHint(emailbuilder);
    }

    class UpdateProfile extends AsyncTask<String, Void, String> {

        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(ActivityUpdateProfile.this);
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
                    .url(Config.BASE_URL+"customerupdateProfile.php?"+"firstname="+ userName +"&lastname="+userLastName+"&"+ "userEmail=" +userEmail+"&"+ "mobileNumber=" +userContact+"&"+ "customerId=" +user_id)
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
            System.out.println(">>> Signup result :" + s);
            dialog.dismiss();

            if(s != null){
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if(status.equals("1")){
                        Toast.makeText(ActivityUpdateProfile.this,response_msg,Toast.LENGTH_SHORT).show();
                        String name = edtRegName.getText().toString()+" "+edtLastName.getText().toString();
                        editor.putString("customerFirstName",edtRegName.getText().toString());
                        editor.putString("customerLastName",edtLastName.getText().toString());
                        editor.putString("NavigationPosition","8");
                        editor.putString("customerName",name);
                        editor.putString("customerContact",edtRegContact.getText().toString());
                        editor.putString("customerEmail",edtRegEmail.getText().toString());
                        editor.commit();
                        Intent intent=new Intent(ActivityUpdateProfile.this,DrawerActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(ActivityUpdateProfile.this,response_msg,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityUpdateProfile.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }
            else {
                dialog.dismiss();
                Toast.makeText(ActivityUpdateProfile.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        /*editor.putString("NavigationPosition","7");
        editor.commit();
        Intent intent=new Intent(ActivityUpdateProfile.this,DrawerActivity.class);
        startActivity(intent);*/
        finish();
    }
}
