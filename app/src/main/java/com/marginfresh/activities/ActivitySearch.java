package com.marginfresh.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.Model.ModelProductList;
import com.marginfresh.R;
import com.marginfresh.adapter.AdapterProductList;
import com.marginfresh.adapter.AdapterSearch;
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
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by bitware on 3/7/17.
 */

public class ActivitySearch extends AppCompatActivity {

    ImageView iv_back;
    RecyclerView rv_search;
    AdapterSearch adapterSearch;
    String store_id="",user_id="",searchString="";
    ArrayList<ModelProductList> arrProductList;
    ConnectionDetector cd;
    boolean isInternetPresent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView tv_storeName;
    Button btn_cart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        init();
        tv_storeName.setText(sharedPreferences.getString("storeName",""));
        store_id = sharedPreferences.getString("store_id","");
        user_id = sharedPreferences.getString("user_id","");
        searchString = sharedPreferences.getString("searchString","");
        if (isInternetPresent){
            new SearchTask().execute("{\"store_id\":\"" + store_id + "\",\"user_id\":\"" + user_id + "\",\"searchStringValue\":\"" + searchString +  "\"}");
        }else {
            Toast.makeText(ActivitySearch.this,R.string.no_internet,Toast.LENGTH_SHORT).show();
        }

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getString("searchFrom","").equals("home")){
                    editor.putString("NavigationPosition","0");
                    editor.commit();
                    startActivity(new Intent(ActivitySearch.this,DrawerActivity.class));
                    finish();
                }else if (sharedPreferences.getString("searchFrom","").equals("categoryList")){
                    startActivity(new Intent(ActivitySearch.this,ActivityCategoryList.class));
                    finish();
                } else if (sharedPreferences.getString("searchFrom","").equals("foodProductList")){
                    Intent intent = new Intent(ActivitySearch.this,ActivityFoodProductList.class);
                    startActivity(intent);
                    finish();
                }else if (sharedPreferences.getString("searchFrom","").equals("topOffer")){
                    startActivity(new Intent(ActivitySearch.this,ActivityTopOffers.class));
                    finish();
                }
            }
        });

    }

    private void init() {
        tv_storeName = (TextView) findViewById(R.id.tv_storeName);
        sharedPreferences=getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        cd = new ConnectionDetector(ActivitySearch.this);
        isInternetPresent = cd.isConnectingToInternet();
        iv_back= (ImageView) findViewById(R.id.iv_back);
        rv_search= (RecyclerView) findViewById(R.id.rv_search);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(this,2);
        rv_search.setLayoutManager(linearLayoutManager);
        btn_cart = (Button) findViewById(R.id.btn_cart);
    }

    class SearchTask extends AsyncTask<String, Void, String> {
        Dialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = AppUtils.customLoader(ActivitySearch.this);
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
                    .url(Config.BASE_URL+"searchproduct.php?"+"store_id="+store_id + "&user_id="+user_id+"&searchStringValue="+searchString)
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
            dialog.dismiss();
            System.out.println(">>> Add to wishlist result : "+s);
            if (s != null) {
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                        Toast.makeText(ActivitySearch.this,message,Toast.LENGTH_SHORT).show();
                        JSONArray productListArray = resObj.getJSONArray("product_array");
                        arrProductList = new ArrayList<>();
                        if (productListArray.length()!=0) {
                            for (int i = 0; i < productListArray.length(); i++) {
                                JSONObject productObj = productListArray.getJSONObject(i);

                                ModelProductList modelProductList = new ModelProductList();
                                modelProductList.setProductId(productObj.getString("productId"));
                                modelProductList.setProduct_name(productObj.getString("product_name"));
                                modelProductList.setProduct_image_url(productObj.getString("product_image_url"));
                                modelProductList.setPrice(productObj.getString("price"));
                                modelProductList.setNew_price(productObj.getString("new_price"));
                                modelProductList.setProduct_rating_count(productObj.getString("product_rating_count"));
                                modelProductList.setProduct_isInWislist(productObj.getString("product_isInWislist"));

                                arrProductList.add(modelProductList);
                            }
                        }else {
                            arrProductList = new ArrayList<>();
                            Toast.makeText(ActivitySearch.this,"Products Not Available",Toast.LENGTH_SHORT).show();
                        }
                        adapterSearch = new AdapterSearch(ActivitySearch.this,arrProductList);
                        rv_search.setAdapter(adapterSearch);
                    }else{
                        showNoProductDialog(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivitySearch.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                dialog.dismiss();
                Toast.makeText(ActivitySearch.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showNoProductDialog(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivitySearch.this)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (sharedPreferences.getString("searchFrom","").equals("home")){
                            editor.putString("NavigationPosition","0");
                            editor.commit();
                            startActivity(new Intent(ActivitySearch.this,DrawerActivity.class));
                            finish();
                        }else if (sharedPreferences.getString("searchFrom","").equals("categoryList")){
                            startActivity(new Intent(ActivitySearch.this,ActivityCategoryList.class));
                            finish();
                        }
                        else if (sharedPreferences.getString("searchFrom","").equals("foodProductList")){
                            Intent intent = new Intent(ActivitySearch.this,ActivityFoodProductList.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (sharedPreferences.getString("searchFrom","").equals("home")){
            editor.putString("NavigationPosition","0");
            editor.commit();
            startActivity(new Intent(ActivitySearch.this,DrawerActivity.class));
            finish();
        }else if (sharedPreferences.getString("searchFrom","").equals("categoryList")){
            startActivity(new Intent(ActivitySearch.this,ActivityCategoryList.class));
            finish();
        }
        else if (sharedPreferences.getString("searchFrom","").equals("foodProductList")){
            Intent intent = new Intent(ActivitySearch.this,ActivityFoodProductList.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPreferences.getString("cartItemCount","").equals("0") || sharedPreferences.getString("cartItemCount","").equals("")){
            btn_cart.setVisibility(View.GONE);
        }else {
            btn_cart.setVisibility(View.VISIBLE);
            btn_cart.setText(sharedPreferences.getString("cartItemCount",""));
        }
    }
}
