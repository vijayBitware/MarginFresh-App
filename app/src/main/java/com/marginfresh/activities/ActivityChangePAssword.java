package com.marginfresh.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.Model.ModelMyWishlist;
import com.marginfresh.R;
import com.marginfresh.adapter.AdapterMyWishlist;
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

/**
 * Created by bitware on 30/6/17.
 */

public class ActivityChangePAssword extends AppCompatActivity{

    EditText edt_oldPassword,edt_newPassword,edt_confirmNewPassword;
    TextView tv_changePassword;
    private String response_msg="",user_id="",oldPassword="",confirmNewPassword="",new_password="";
    ConnectionDetector cd;
    boolean isInternetPresent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ImageView iv_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        init();
        tv_changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldPassword = edt_oldPassword.getText().toString();
                new_password = edt_newPassword.getText().toString();
                confirmNewPassword= edt_confirmNewPassword.getText().toString();
                user_id = sharedPreferences.getString("user_id","");

                if (!oldPassword.isEmpty()) {
                    if (oldPassword.length() > 6 || oldPassword.length() == 6 ) {
                        if (!oldPassword.contains(" ")) {
                            if (!new_password.isEmpty()) {
                                if (new_password.length() > 6 || new_password.length() == 6) {
                                    if (!new_password.contains(" ")) {
                                        if (!confirmNewPassword.isEmpty()) {
                                            if (confirmNewPassword.length() > 6 || confirmNewPassword.length() == 6) {
                                                if (!confirmNewPassword.contains(" ")) {
                                                    if (new_password.equals(confirmNewPassword)) {
                                                        if (isInternetPresent) {
                                                            new ChangePassword().execute("{\"user_id\":\"" + user_id + "\",\"oldPassword\":\"" + oldPassword + "\",\"newPassword\":\"" + new_password + "\"}");
                                                        } else {
                                                            Toast.makeText(ActivityChangePAssword.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(ActivityChangePAssword.this, "Password Dosen't Match", Toast.LENGTH_SHORT).show();
                                                    }
                                                }else {
                                                    Toast.makeText(ActivityChangePAssword.this,"Space Is Not Allowed In Password", Toast.LENGTH_SHORT).show();

                                                }
                                            } else {
                                                edt_confirmNewPassword.setError("Password should be min 6 chars");
                                                edt_confirmNewPassword.requestFocus();
                                            }
                                        } else {
                                            edt_confirmNewPassword.setError("Please Enter confirm Password");
                                            edt_confirmNewPassword.requestFocus();
                                        }
                                    }else {
                                        Toast.makeText(ActivityChangePAssword.this,"Space Is Not Allowed In Password", Toast.LENGTH_SHORT).show();

                                    }
                                } else {
                                    edt_newPassword.setError("Password should be min 6 chars");
                                    edt_newPassword.requestFocus();
                                }
                            } else {
                                edt_newPassword.setError("Please Enter New Passsword");
                                edt_newPassword.requestFocus();
                            }
                        }else {
                            Toast.makeText(ActivityChangePAssword.this,"Space Is Not Allowed In Password", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        edt_oldPassword.setError("Password should be min 6 chars");
                        edt_oldPassword.requestFocus();
                    }
                }else {
                    edt_oldPassword.setError("Please Enter Old Password");
                    edt_oldPassword.requestFocus();
                }

            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*editor.putString("NavigationPosition","8");
                editor.commit();
                startActivity(new Intent(ActivityChangePAssword.this,DrawerActivity.class));*/
                finish();
            }
        });
    }

    private void init() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor= sharedPreferences.edit();
        cd = new ConnectionDetector(ActivityChangePAssword.this);
        isInternetPresent = cd.isConnectingToInternet();
        edt_oldPassword = (EditText) findViewById(R.id.edt_oldPassword);
        edt_newPassword = (EditText) findViewById(R.id.edt_newPassword);
        edt_confirmNewPassword = (EditText) findViewById(R.id.edt_confirmNewPassword);
        tv_changePassword = (TextView) findViewById(R.id.tv_changePassword);
    }

    class ChangePassword extends AsyncTask<String, Void, String> {

       Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(ActivityChangePAssword.this);
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
                    .url(Config.BASE_URL + "changepassword.php?"+"user_id="+user_id+"&oldPassword="+oldPassword+"&newPassword="+confirmNewPassword)
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
            System.out.println(">>>Change Password result :" + s);
            dialog.dismiss();
            if (s != null) {
//                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if (status.equals("1")) {
                        Toast.makeText(ActivityChangePAssword.this, response_msg, Toast.LENGTH_SHORT).show();
                        editor.putString("NavigationPosition","8");
                        editor.commit();
                        startActivity(new Intent(ActivityChangePAssword.this,DrawerActivity.class));

                    } else {
                        dialog.dismiss();
                        Toast.makeText(ActivityChangePAssword.this, response_msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityChangePAssword.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            } else {
                dialog.dismiss();
                Toast.makeText(ActivityChangePAssword.this,"Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
