package com.marginfresh.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.gson.GsonBuilder;
import com.marginfresh.Model.CustomerReview;
import com.marginfresh.Model.GetNearBy;
import com.marginfresh.R;
import com.marginfresh.adapter.CustomerReviewAdapter;
import com.marginfresh.adapter.StoreListingAdapter;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by bitware on 10/8/17.
 */

public class ActivityCustomerReview extends AppCompatActivity {

    ListView lv_customerReview;
    CustomerReviewAdapter customerReviewAdapter;
    ConnectionDetector cd;
    Boolean isInternetPresent;
    String productId,response_msg;
    ArrayList<CustomerReview> arrCustomerReview;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ImageView iv_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_review);

        init();
        productId = sharedPreferences.getString("product_id","");
        if (isInternetPresent){
            new CustomerReviewTask().execute("{\"productId\":\"" + productId +  "\"}");
        }else {
            Toast.makeText(ActivityCustomerReview.this,getResources().getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
        }

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void init() {
        cd = new ConnectionDetector(ActivityCustomerReview.this);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        lv_customerReview = (ListView) findViewById(R.id.lv_customerReview);
        iv_back = (ImageView) findViewById(R.id.iv_back);
    }

    private class CustomerReviewTask extends AsyncTask<String, Void, String> {

        Dialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(ActivityCustomerReview.this);
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
                    .url(Config.BASE_URL+"product_ratings.php?"+"productId="+ productId)
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
            System.out.println(">>>customer review result :" + s);
            dialog.dismiss();

            if(s != null){
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if(status.equals("1"))
                    {
                        Toast.makeText(ActivityCustomerReview.this, response_msg, Toast.LENGTH_SHORT).show();
                        JSONArray ratingArray = jsonObject.getJSONArray("ratingData");
                        arrCustomerReview = new ArrayList<>();
                        for (int i=0;i<ratingArray.length();i++){
                            JSONObject rateObj = ratingArray.getJSONObject(i);
                            CustomerReview customerReview = new CustomerReview();
                            customerReview.setRatingUser(rateObj.getString("ratingUser"));
                            customerReview.setRatingDetail(rateObj.getString("ratingDetail"));
                            customerReview.setRatingStars(rateObj.getString("ratingStars"));
                            customerReview.setRatingTitle(rateObj.getString("ratingTitle"));

                            arrCustomerReview.add(customerReview);
                        }
                        customerReviewAdapter = new CustomerReviewAdapter(ActivityCustomerReview.this,R.layout.review,arrCustomerReview);
                        lv_customerReview.setAdapter(customerReviewAdapter);
                    }
                    else {
                        dialog.dismiss();
                        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(ActivityCustomerReview.this);
                        alertDialogBuilder
                                .setMessage(response_msg)
                                .setCancelable(false)
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                startActivity(new Intent(ActivityCustomerReview.this,ActivityProductDetail.class));
                                                finish();
                                            }
                                        });

                        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityCustomerReview.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }
            else {
                dialog.dismiss();
                Toast.makeText(ActivityCustomerReview.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
